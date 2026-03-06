package com.subnavar.app.ar.relocalization

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages ORB feature extraction and matching for visual re-localization.
 * Uses a lightweight Java-based ORB implementation for offline operation.
 * 
 * Feature extraction pipeline:
 * 1. Convert camera frame to grayscale
 * 2. Detect FAST corners (ORB keypoints)
 * 3. Compute BRIEF descriptors (oriented)
 * 4. Store descriptors as binary feature files
 * 
 * Re-localization pipeline:
 * 1. Extract features from current camera frame
 * 2. Match against stored waypoint descriptors using Hamming distance
 * 3. Return best-matching waypoint with confidence score
 */
@Singleton
class ORBFeatureManager @Inject constructor() {

    data class FeaturePoint(
        val x: Int,
        val y: Int,
        val response: Float,
        val angle: Float,
        val descriptor: ByteArray
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is FeaturePoint) return false
            return x == other.x && y == other.y
        }

        override fun hashCode(): Int = 31 * x + y
    }

    data class MatchResult(
        val waypointId: Long,
        val matchCount: Int,
        val confidence: Float,
        val averageDistance: Float
    )

    companion object {
        private const val MAX_KEYPOINTS = 500
        private const val DESCRIPTOR_SIZE = 32 // 256 bits = 32 bytes
        private const val FAST_THRESHOLD = 20
        private const val MATCH_DISTANCE_THRESHOLD = 64 // Hamming distance threshold
        private const val MIN_MATCH_COUNT = 10
        private const val CONFIDENCE_THRESHOLD = 0.3f
    }

    /**
     * Extract ORB features from a bitmap image.
     */
    suspend fun extractFeatures(bitmap: Bitmap): List<FeaturePoint> = withContext(Dispatchers.Default) {
        val gray = toGrayscale(bitmap)
        val width = bitmap.width
        val height = bitmap.height

        // Step 1: Detect FAST corners
        val keypoints = detectFASTCorners(gray, width, height)

        // Step 2: Sort by response and take top N
        val topKeypoints = keypoints
            .sortedByDescending { it.response }
            .take(MAX_KEYPOINTS)

        // Step 3: Compute oriented BRIEF descriptors
        topKeypoints.map { kp ->
            val angle = computeOrientation(gray, width, height, kp.x, kp.y)
            val descriptor = computeBRIEFDescriptor(gray, width, height, kp.x, kp.y, angle)
            FeaturePoint(kp.x, kp.y, kp.response, angle, descriptor)
        }
    }

    /**
     * Save extracted features to a binary file.
     */
    suspend fun saveFeatures(features: List<FeaturePoint>, outputPath: String) = withContext(Dispatchers.IO) {
        val file = File(outputPath)
        file.parentFile?.mkdirs()
        FileOutputStream(file).use { fos ->
            // Header: feature count (4 bytes)
            val count = features.size
            fos.write(count shr 24 and 0xFF)
            fos.write(count shr 16 and 0xFF)
            fos.write(count shr 8 and 0xFF)
            fos.write(count and 0xFF)

            // Each feature: x(4) + y(4) + response(4) + angle(4) + descriptor(32) = 48 bytes
            for (fp in features) {
                writeInt(fos, fp.x)
                writeInt(fos, fp.y)
                writeFloat(fos, fp.response)
                writeFloat(fos, fp.angle)
                fos.write(fp.descriptor)
            }
        }
    }

    /**
     * Load features from a binary file.
     */
    suspend fun loadFeatures(filePath: String): List<FeaturePoint> = withContext(Dispatchers.IO) {
        val file = File(filePath)
        if (!file.exists()) return@withContext emptyList()

        val bytes = file.readBytes()
        if (bytes.size < 4) return@withContext emptyList()

        val count = readInt(bytes, 0)
        val features = mutableListOf<FeaturePoint>()
        var offset = 4

        for (i in 0 until count) {
            if (offset + 48 > bytes.size) break
            val x = readInt(bytes, offset)
            val y = readInt(bytes, offset + 4)
            val response = readFloat(bytes, offset + 8)
            val angle = readFloat(bytes, offset + 12)
            val descriptor = bytes.copyOfRange(offset + 16, offset + 16 + DESCRIPTOR_SIZE)
            features.add(FeaturePoint(x, y, response, angle, descriptor))
            offset += 48
        }
        features
    }

    /**
     * Match current frame features against stored waypoint features.
     * Returns the best matching waypoint.
     */
    suspend fun findBestMatch(
        currentFeatures: List<FeaturePoint>,
        waypointFeatures: Map<Long, List<FeaturePoint>>
    ): MatchResult? = withContext(Dispatchers.Default) {
        if (currentFeatures.isEmpty()) return@withContext null

        val results = waypointFeatures.mapNotNull { (waypointId, storedFeatures) ->
            val matches = matchFeatures(currentFeatures, storedFeatures)
            if (matches.size >= MIN_MATCH_COUNT) {
                val avgDistance = matches.map { it.second }.average().toFloat()
                val confidence = matches.size.toFloat() / currentFeatures.size.coerceAtLeast(1)
                MatchResult(waypointId, matches.size, confidence, avgDistance)
            } else null
        }

        results
            .filter { it.confidence >= CONFIDENCE_THRESHOLD }
            .minByOrNull { it.averageDistance }
    }

    /**
     * Match features between two sets using brute-force Hamming distance.
     * Returns pairs of (matchIndex, distance).
     */
    private fun matchFeatures(
        queryFeatures: List<FeaturePoint>,
        trainFeatures: List<FeaturePoint>
    ): List<Pair<Int, Int>> {
        val matches = mutableListOf<Pair<Int, Int>>()

        for ((qi, qf) in queryFeatures.withIndex()) {
            var bestDist = Int.MAX_VALUE
            var secondBestDist = Int.MAX_VALUE

            for (tf in trainFeatures) {
                val dist = hammingDistance(qf.descriptor, tf.descriptor)
                if (dist < bestDist) {
                    secondBestDist = bestDist
                    bestDist = dist
                } else if (dist < secondBestDist) {
                    secondBestDist = dist
                }
            }

            // Lowe's ratio test
            if (bestDist < MATCH_DISTANCE_THRESHOLD &&
                bestDist.toFloat() / secondBestDist.coerceAtLeast(1) < 0.75f
            ) {
                matches.add(qi to bestDist)
            }
        }
        return matches
    }

    // --- Low-level image processing ---

    private fun toGrayscale(bitmap: Bitmap): IntArray {
        val w = bitmap.width
        val h = bitmap.height
        val pixels = IntArray(w * h)
        bitmap.getPixels(pixels, 0, w, 0, 0, w, h)
        for (i in pixels.indices) {
            val p = pixels[i]
            val r = (p shr 16) and 0xFF
            val g = (p shr 8) and 0xFF
            val b = p and 0xFF
            pixels[i] = (0.299 * r + 0.587 * g + 0.114 * b).toInt()
        }
        return pixels
    }

    private data class Corner(val x: Int, val y: Int, val response: Float)

    private fun detectFASTCorners(gray: IntArray, width: Int, height: Int): List<Corner> {
        val corners = mutableListOf<Corner>()
        val border = 3

        // Circle offsets for FAST-9 (16-point circle)
        val circleX = intArrayOf(0, 1, 2, 3, 3, 3, 2, 1, 0, -1, -2, -3, -3, -3, -2, -1)
        val circleY = intArrayOf(-3, -3, -2, -1, 0, 1, 2, 3, 3, 3, 2, 1, 0, -1, -2, -3)

        for (y in border until height - border) {
            for (x in border until width - border) {
                val center = gray[y * width + x]
                val tHigh = center + FAST_THRESHOLD
                val tLow = center - FAST_THRESHOLD

                // Quick reject: check pixels at 0, 4, 8, 12 (cardinal points)
                var brightCount = 0
                var darkCount = 0
                for (idx in intArrayOf(0, 4, 8, 12)) {
                    val px = gray[(y + circleY[idx]) * width + (x + circleX[idx])]
                    if (px > tHigh) brightCount++
                    if (px < tLow) darkCount++
                }
                if (brightCount < 3 && darkCount < 3) continue

                // Full 16-point check — need 9 contiguous
                var consecutiveBright = 0
                var consecutiveDark = 0
                var maxBright = 0
                var maxDark = 0
                var totalResponse = 0f

                for (round in 0 until 32) {
                    val idx = round % 16
                    val px = gray[(y + circleY[idx]) * width + (x + circleX[idx])]
                    if (px > tHigh) {
                        consecutiveBright++
                        consecutiveDark = 0
                        totalResponse += (px - center).toFloat()
                    } else {
                        if (consecutiveBright > maxBright) maxBright = consecutiveBright
                        consecutiveBright = 0
                    }
                    if (px < tLow) {
                        consecutiveDark++
                        consecutiveBright = 0
                        totalResponse += (center - px).toFloat()
                    } else {
                        if (consecutiveDark > maxDark) maxDark = consecutiveDark
                        consecutiveDark = 0
                    }
                }
                if (consecutiveBright > maxBright) maxBright = consecutiveBright
                if (consecutiveDark > maxDark) maxDark = consecutiveDark

                if (maxBright >= 9 || maxDark >= 9) {
                    corners.add(Corner(x, y, totalResponse))
                }
            }
        }

        // Non-maximum suppression
        return nonMaxSuppression(corners, width, height)
    }

    private fun nonMaxSuppression(corners: List<Corner>, width: Int, height: Int): List<Corner> {
        if (corners.isEmpty()) return emptyList()

        val gridSize = 8
        val gridW = (width + gridSize - 1) / gridSize
        val gridH = (height + gridSize - 1) / gridSize
        val grid = arrayOfNulls<Corner>(gridW * gridH)

        for (c in corners) {
            val gx = c.x / gridSize
            val gy = c.y / gridSize
            val idx = gy * gridW + gx
            val existing = grid[idx]
            if (existing == null || c.response > existing.response) {
                grid[idx] = c
            }
        }

        return grid.filterNotNull()
    }

    private fun computeOrientation(gray: IntArray, width: Int, height: Int, cx: Int, cy: Int): Float {
        val patchSize = 15
        val half = patchSize / 2
        var m01 = 0L
        var m10 = 0L

        for (dy in -half..half) {
            for (dx in -half..half) {
                val px = cx + dx
                val py = cy + dy
                if (px in 0 until width && py in 0 until height) {
                    val intensity = gray[py * width + px]
                    m10 += dx.toLong() * intensity
                    m01 += dy.toLong() * intensity
                }
            }
        }

        return Math.atan2(m01.toDouble(), m10.toDouble()).toFloat()
    }

    private fun computeBRIEFDescriptor(
        gray: IntArray, width: Int, height: Int,
        cx: Int, cy: Int, angle: Float
    ): ByteArray {
        val descriptor = ByteArray(DESCRIPTOR_SIZE)
        val cosA = Math.cos(angle.toDouble()).toFloat()
        val sinA = Math.sin(angle.toDouble()).toFloat()

        // Pre-defined sampling pattern (simplified — normally from training set)
        val rng = java.util.Random(42) // Deterministic pattern
        for (i in 0 until DESCRIPTOR_SIZE * 8) {
            val byteIdx = i / 8
            val bitIdx = i % 8

            // Random point pair in patch
            val ax = rng.nextInt(31) - 15
            val ay = rng.nextInt(31) - 15
            val bx = rng.nextInt(31) - 15
            val by = rng.nextInt(31) - 15

            // Rotate by orientation
            val rax = (cosA * ax - sinA * ay).toInt() + cx
            val ray = (sinA * ax + cosA * ay).toInt() + cy
            val rbx = (cosA * bx - sinA * by).toInt() + cx
            val rby = (sinA * bx + cosA * by).toInt() + cy

            val pa = getPixelSafe(gray, width, height, rax, ray)
            val pb = getPixelSafe(gray, width, height, rbx, rby)

            if (pa < pb) {
                descriptor[byteIdx] = (descriptor[byteIdx].toInt() or (1 shl bitIdx)).toByte()
            }
        }
        return descriptor
    }

    private fun getPixelSafe(gray: IntArray, width: Int, height: Int, x: Int, y: Int): Int {
        val cx = x.coerceIn(0, width - 1)
        val cy = y.coerceIn(0, height - 1)
        return gray[cy * width + cx]
    }

    private fun hammingDistance(a: ByteArray, b: ByteArray): Int {
        var distance = 0
        for (i in a.indices) {
            distance += Integer.bitCount((a[i].toInt() xor b[i].toInt()) and 0xFF)
        }
        return distance
    }

    // --- Binary I/O helpers ---

    private fun writeInt(fos: FileOutputStream, value: Int) {
        fos.write(value shr 24 and 0xFF)
        fos.write(value shr 16 and 0xFF)
        fos.write(value shr 8 and 0xFF)
        fos.write(value and 0xFF)
    }

    private fun writeFloat(fos: FileOutputStream, value: Float) {
        writeInt(fos, java.lang.Float.floatToIntBits(value))
    }

    private fun readInt(bytes: ByteArray, offset: Int): Int {
        return (bytes[offset].toInt() and 0xFF shl 24) or
                (bytes[offset + 1].toInt() and 0xFF shl 16) or
                (bytes[offset + 2].toInt() and 0xFF shl 8) or
                (bytes[offset + 3].toInt() and 0xFF)
    }

    private fun readFloat(bytes: ByteArray, offset: Int): Float {
        return java.lang.Float.intBitsToFloat(readInt(bytes, offset))
    }
}
