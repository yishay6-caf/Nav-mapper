package com.subnavar.app.nav

import com.subnavar.app.domain.model.Edge
import com.subnavar.app.domain.model.Waypoint
import java.util.PriorityQueue
import kotlin.math.sqrt

/**
 * A* pathfinding algorithm on the waypoint graph.
 * Works across floors using floor transition waypoints.
 */
class PathFinder {

    data class PathResult(
        val waypoints: List<Waypoint>,
        val totalDistance: Float,
        val floorTransitions: Int
    )

    fun findPath(
        start: Waypoint,
        end: Waypoint,
        allWaypoints: List<Waypoint>,
        allEdges: List<Edge>
    ): PathResult? {
        val waypointMap = allWaypoints.associateBy { it.id }
        val adjacencyMap = buildAdjacencyMap(allEdges)

        val openSet = PriorityQueue<Node>(compareBy { it.fScore })
        val cameFrom = mutableMapOf<Long, Long>()
        val gScore = mutableMapOf<Long, Float>().withDefault { Float.MAX_VALUE }
        val fScore = mutableMapOf<Long, Float>().withDefault { Float.MAX_VALUE }
        val closedSet = mutableSetOf<Long>()

        gScore[start.id] = 0f
        fScore[start.id] = heuristic(start, end)
        openSet.add(Node(start.id, fScore[start.id]!!))

        while (openSet.isNotEmpty()) {
            val current = openSet.poll() ?: break

            if (current.waypointId == end.id) {
                return reconstructPath(cameFrom, current.waypointId, waypointMap, gScore)
            }

            if (current.waypointId in closedSet) continue
            closedSet.add(current.waypointId)

            val neighbors = adjacencyMap[current.waypointId] ?: continue
            for ((neighborId, edgeDistance) in neighbors) {
                if (neighborId in closedSet) continue

                val neighbor = waypointMap[neighborId] ?: continue
                val tentativeG = gScore.getValue(current.waypointId) + edgeDistance

                if (tentativeG < gScore.getValue(neighborId)) {
                    cameFrom[neighborId] = current.waypointId
                    gScore[neighborId] = tentativeG
                    fScore[neighborId] = tentativeG + heuristic(neighbor, end)
                    openSet.add(Node(neighborId, fScore[neighborId]!!))
                }
            }
        }

        return null // No path found
    }

    private fun buildAdjacencyMap(edges: List<Edge>): Map<Long, List<Pair<Long, Float>>> {
        val map = mutableMapOf<Long, MutableList<Pair<Long, Float>>>()
        for (edge in edges) {
            if (!edge.isAccessible) continue
            map.getOrPut(edge.fromWaypointId) { mutableListOf() }.add(edge.toWaypointId to edge.distance)
            map.getOrPut(edge.toWaypointId) { mutableListOf() }.add(edge.fromWaypointId to edge.distance)
        }
        return map
    }

    private fun heuristic(a: Waypoint, b: Waypoint): Float {
        val dx = a.posX - b.posX
        val dy = a.posY - b.posY
        val dz = a.posZ - b.posZ
        return sqrt(dx * dx + dy * dy + dz * dz)
    }

    private fun reconstructPath(
        cameFrom: Map<Long, Long>,
        endId: Long,
        waypointMap: Map<Long, Waypoint>,
        gScore: Map<Long, Float>
    ): PathResult {
        val path = mutableListOf<Waypoint>()
        var currentId = endId
        var floorTransitions = 0

        while (true) {
            val waypoint = waypointMap[currentId] ?: break
            path.add(0, waypoint)
            if (waypoint.isFloorTransition && path.size > 1) {
                floorTransitions++
            }
            currentId = cameFrom[currentId] ?: break
        }

        return PathResult(
            waypoints = path,
            totalDistance = gScore[endId] ?: 0f,
            floorTransitions = floorTransitions / 2 // Each transition involves 2 waypoints
        )
    }

    private data class Node(val waypointId: Long, val fScore: Float)
}
