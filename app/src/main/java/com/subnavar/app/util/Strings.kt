package com.subnavar.app.util

/**
 * Bilingual string provider for English and Hebrew.
 * Used for in-app text that needs to change dynamically based on user language selection.
 */
object Strings {

    data class BilingualString(val en: String, val he: String)

    // App general
    val appName = BilingualString("Lot25 Map", "Lot25 Map")
    val ok = BilingualString("OK", "אישור")
    val cancel = BilingualString("Cancel", "ביטול")
    val confirm = BilingualString("Confirm", "אישור")
    val delete = BilingualString("Delete", "מחק")
    val save = BilingualString("Save", "שמור")
    val back = BilingualString("Back", "חזרה")
    val close = BilingualString("Close", "סגור")
    val add = BilingualString("Add", "הוסף")
    val edit = BilingualString("Edit", "ערוך")
    val search = BilingualString("Search", "חיפוש")

    // Tabs
    val tabMap = BilingualString("Map", "מפה")
    val tabNavigate = BilingualString("Navigate", "ניווט")
    val tabSettings = BilingualString("Settings", "הגדרות")

    // Buildings
    val buildings = BilingualString("Buildings", "מבנים")
    val addBuilding = BilingualString("Add Building", "הוסף מבנה")
    val buildingName = BilingualString("Building Name", "שם המבנה")
    val description = BilingualString("Description (optional)", "תיאור (אופציונלי)")
    val noBuildings = BilingualString(
        "No buildings yet.\nTap + to add one.",
        "אין מבנים עדיין.\nלחץ על + להוספה."
    )
    val deleteBuilding = BilingualString(
        "Are you sure you want to delete this building?",
        "האם אתה בטוח שברצונך למחוק מבנה זה?"
    )

    // Floors
    val floors = BilingualString("Floors", "קומות")
    val addFloor = BilingualString("Add Floor", "הוסף קומה")
    val floorName = BilingualString("Floor Name (e.g., B1, B2)", "שם הקומה (למשל, B1, B2)")
    val floorLevel = BilingualString("Level Number (e.g., -1, -2)", "מספר קומה (למשל, 1-, 2-)")
    val noFloors = BilingualString(
        "No floors yet.\nAdd a floor to get started.",
        "אין קומות עדיין.\nהוסף קומה כדי להתחיל."
    )
    val floorPlanImported = BilingualString("Floor plan imported", "תוכנית קומה מיובאת")
    val noFloorPlan = BilingualString("No floor plan", "אין תוכנית קומה")
    val importPlan = BilingualString("Import Plan", "ייבוא תוכנית")

    // Mapping
    val mapping = BilingualString("Mapping", "מיפוי")
    val startMapping = BilingualString("Start", "התחל")
    val pauseMapping = BilingualString("Pause", "השהה")
    val placeWaypoint = BilingualString("Waypoint", "נקודת ציון")
    val transition = BilingualString("Transition", "מעבר")
    val tracking = BilingualString("Tracking", "מעקב")
    val notTracking = BilingualString("Not Tracking", "אין מעקב")
    val waypointsPlaced = BilingualString("waypoints placed", "נקודות ציון הוצבו")
    val mappingActive = BilingualString(
        "Mapping active - walk and place waypoints",
        "מיפוי פעיל - הלך והצב נקודות ציון"
    )
    val mappingPaused = BilingualString("Mapping paused", "מיפוי מושהה")
    val waypointPlaced = BilingualString("Waypoint placed", "נקודת ציון הוצבה")
    val label = BilingualString("Label (optional)", "תווית (אופציונלי)")
    val type = BilingualString("Type", "סוג")
    val place = BilingualString("Place", "הצב")
    val position = BilingualString("Position", "מיקום")
    val cameraPermRequired = BilingualString(
        "Camera permission is required for AR mapping",
        "נדרשת הרשאת מצלמה למיפוי AR"
    )

    // Waypoint types
    val hallway = BilingualString("Hallway", "מסדרון")
    val room = BilingualString("Room", "חדר")
    val stairwell = BilingualString("Stairwell", "חדר מדרגות")
    val elevator = BilingualString("Elevator", "מעלית")
    val entrance = BilingualString("Entrance", "כניסה")
    val poi = BilingualString("Point of Interest", "נקודת עניין")

    // Floor transition
    val floorTransition = BilingualString("Floor Transition", "מעבר בין קומות")
    val stairs = BilingualString("Stairs", "מדרגות")
    val connectedFloorId = BilingualString("Connected Floor ID", "מזהה קומה מחוברת")

    // Street View
    val streetView = BilingualString("Street View", "תצוגת רחוב")
    val navigateTo = BilingualString("Navigate to:", "נווט אל:")
    val noPhotos = BilingualString(
        "No photos captured for this waypoint",
        "לא צולמו תמונות לנקודת ציון זו"
    )

    // Navigation
    val navigate = BilingualString("Navigate", "נווט")
    val selectStart = BilingualString("Select start point", "בחר נקודת התחלה")
    val selectDestination = BilingualString("Select destination", "בחר יעד")
    val selectStartPoint = BilingualString("Select start point:", "בחר נקודת התחלה:")
    val selectDestinationPoint = BilingualString("Select destination:", "בחר יעד:")
    val distance = BilingualString("Distance", "מרחק")
    val waypoints = BilingualString("Waypoints", "נקודות ציון")
    val floorChanges = BilingualString("Floor changes", "שינויי קומה")
    val noPathFound = BilingualString(
        "No path found between these waypoints",
        "לא נמצא מסלול בין נקודות הציון"
    )
    val noWaypointsMapped = BilingualString(
        "No waypoints mapped yet.\nMap a building first to navigate.",
        "אין נקודות ציון ממופות.\nמפה מבנה קודם כדי לנווט."
    )

    // Settings
    val settings = BilingualString("Settings", "הגדרות")
    val language = BilingualString("Language", "שפה")
    val selectLanguage = BilingualString("Select Language", "בחר שפה")
    val about = BilingualString("About", "אודות")
    val version = BilingualString("Version", "גרסה")
    val exportData = BilingualString("Export Building Data", "ייצוא נתוני מבנה")
    val importData = BilingualString("Import Building Data", "ייבוא נתוני מבנה")
    val dataManagement = BilingualString("Data Management", "ניהול נתונים")
    val general = BilingualString("General", "כללי")
    val appInfo = BilingualString(
        "Lot25 Map - Indoor AR Navigation System\nFor underground multi-floor buildings",
        "Lot25 Map - מערכת ניווט AR פנים-מבנית\nלמבנים תת-קרקעיים רב-קומתיים"
    )

    // Data sharing
    val exportSuccess = BilingualString("Data exported successfully", "הנתונים יוצאו בהצלחה")
    val importSuccess = BilingualString("Data imported successfully", "הנתונים יובאו בהצלחה")
    val exportFailed = BilingualString("Export failed", "הייצוא נכשל")
    val importFailed = BilingualString("Import failed", "הייבוא נכשל")

    // 3D View
    val view3D = BilingualString("3D View", "תצוגת תלת-מימד")
    val tilt = BilingualString("Tilt", "הטיה")
    val pinchToZoomDragToPan = BilingualString(
        "Pinch to zoom • Drag to pan",
        "צביטה לזום • גרירה להזזה"
    )
    val floorPlan3D = BilingualString("Floor Plan 3D", "תוכנית קומה תלת-מימד")
    val markedLocation = BilingualString("Marked location", "מיקום מסומן")
    val noFloorPlanAvailable = BilingualString(
        "No floor plan available\nImport one in the Map tab",
        "אין תוכנית קומה זמינה\nייבא אחת בלשונית המפה"
    )

    // Mapping screen
    val mappingTitle = BilingualString("Mapping", "מיפוי")
    val waypointsPlacedCount = BilingualString("waypoints placed", "נקודות ציון הוצבו")
    val floorPlanMode = BilingualString("Floor Plan", "תוכנית קומה")
    val cameraMode = BilingualString("Camera", "מצלמה")
    val splitMode = BilingualString("Split", "מפוצל")
    val pause = BilingualString("Pause", "השהה")
    val start = BilingualString("Start", "התחל")
    val placeHere = BilingualString("Place Here", "הצב כאן")
    val waypoint = BilingualString("Waypoint", "נקודת ציון")
    val record = BilingualString("Record", "הקלט")
    val stop = BilingualString("Stop", "עצור")
    val locationMarkedTapPlace = BilingualString(
        "Location marked! Tap 'Place Here' to add waypoint",
        "מיקום סומן! לחץ על 'הצב כאן' להוספת נקודת ציון"
    )
    val tapOnFloorPlanToMark = BilingualString(
        "Tap on the floor plan to mark your location",
        "לחץ על תוכנית הקומה לסימון מיקומך"
    )
    val clearMark = BilingualString("Clear mark", "נקה סימון")
    val noFloorPlanImportedMapping = BilingualString(
        "No floor plan imported yet.\nImport a floor plan in the Map tab\nto use blueprint-based mapping.",
        "טרם יובאה תוכנית קומה.\nייבא תוכנית קומה בלשונית המפה\nלשימוש במיפוי מבוסס שרטוט."
    )
    val cameraPermissionRequired = BilingualString(
        "Camera permission is required for AR mapping",
        "נדרשת הרשאת מצלמה למיפוי AR"
    )
    val placeWaypointTitle = BilingualString("Place Waypoint", "הצב נקודת ציון")
    val placeWaypointOnPlan = BilingualString("Place Waypoint on Plan", "הצב נקודת ציון על התוכנית")
    val locationCoords = BilingualString("Location", "מיקום")
    val labelOptional = BilingualString("Label (optional)", "תווית (אופציונלי)")
    val typeLabel = BilingualString("Type", "סוג")
    val placeButton = BilingualString("Place", "הצב")
    val floorTransitionTitle = BilingualString("Floor Transition", "מעבר בין קומות")
    val labelStairwell = BilingualString("Label (e.g., Stairwell A)", "תווית (למשל, חדר מדרגות א)")
    val stairsButton = BilingualString("Stairs", "מדרגות")
    val elevatorButton = BilingualString("Elevator", "מעלית")
    val connectedFloorIdLabel = BilingualString("Connected Floor ID", "מזהה קומה מחוברת")
    val transitionButton = BilingualString("Transition", "מעבר")
    val photo = BilingualString("Photo", "צילום")
    val dragToRelocate = BilingualString(
        "Drag waypoints to relocate them",
        "גרור נקודות ציון כדי להזיז אותן"
    )

    // Navigation screen
    val navigateTitle = BilingualString("Navigate", "ניווט")
    val reset = BilingualString("Reset", "איפוס")
    val chooseStartPoint = BilingualString(
        "Choose how to set your starting point:",
        "בחר כיצד להגדיר את נקודת ההתחלה:"
    )
    val chooseDestination = BilingualString(
        "Choose how to set your destination:",
        "בחר כיצד להגדיר את היעד:"
    )
    val searchTab = BilingualString("Search", "חיפוש")
    val cameraTab = BilingualString("Camera", "מצלמה")
    val roomTab = BilingualString("Room #", "חדר #")
    val searchWaypointsPlaceholder = BilingualString(
        "Search waypoints by name or type...",
        "חפש נקודות ציון לפי שם או סוג..."
    )
    val clear = BilingualString("Clear", "נקה")
    val noWaypointsMatch = BilingualString(
        "No waypoints match your search.",
        "אין נקודות ציון התואמות לחיפוש."
    )
    val noWaypointsMappedNav = BilingualString(
        "No waypoints mapped yet.\nMap a building first to navigate.",
        "אין נקודות ציון ממופות.\nמפה מבנה קודם כדי לנווט."
    )
    val arLocationDetection = BilingualString("AR Location Detection", "זיהוי מיקום AR")
    val arLocationDesc = BilingualString(
        "Use your camera to detect your current location by matching visual features against mapped waypoints.",
        "השתמש במצלמה לזיהוי מיקומך הנוכחי על ידי התאמת מאפיינים חזותיים לנקודות ציון ממופות."
    )
    val startCameraScan = BilingualString("Start Camera Scan", "התחל סריקת מצלמה")
    val requiresMappedWaypoints = BilingualString(
        "Requires mapped waypoints with photos",
        "דורש נקודות ציון ממופות עם תמונות"
    )
    val enterRoomNumber = BilingualString(
        "Enter room number (e.g., 101, B2-05)...",
        "הזן מספר חדר (למשל, 101, B2-05)..."
    )
    val roomsFound = BilingualString("rooms found", "חדרים נמצאו")
    val noRoomsMatch = BilingualString(
        "No rooms match your search.",
        "אין חדרים התואמים לחיפוש."
    )
    val noRoomsMapped = BilingualString(
        "No rooms mapped yet.\nMap rooms first, then search by number.",
        "אין חדרים ממופים.\nמפה חדרים קודם ואז חפש לפי מספר."
    )
    val selectStartPointLabel = BilingualString("Select start point", "בחר נקודת התחלה")
    val selectDestinationLabel = BilingualString("Select destination", "בחר יעד")
    val swap = BilingualString("Swap", "החלף")
    val distanceLabel = BilingualString("Distance", "מרחק")
    val waypointsLabel = BilingualString("Waypoints", "נקודות ציון")
    val floorChangesLabel = BilingualString("Floor changes", "שינויי קומה")

    // FloorPlan screen
    val lot25Map = BilingualString("Lot25 Map", "Lot25 Map")
    val deleteFloor = BilingualString("Delete Floor", "מחק קומה")
    val deleteBuildingAction = BilingualString("Delete Building", "מחק מבנה")
    val importPlanButton = BilingualString("Import Plan", "ייבוא תוכנית")
    val mapButton = BilingualString("Map", "מיפוי")
    val addFloorButton = BilingualString("Add Floor", "הוסף קומה")
    val addBuildingButton = BilingualString("Add Building", "הוסף מבנה")
    val noBuildingsYet = BilingualString(
        "No buildings yet.\nTap + to add one.",
        "אין מבנים עדיין.\nלחץ על + להוספה."
    )
    val noFloorsYet = BilingualString(
        "No floors yet.\nTap + to add a floor.",
        "אין קומות עדיין.\nהוסף קומה כדי להתחיל."
    )
    val floorPlanImportedStatus = BilingualString("Floor plan imported", "תוכנית קומה מיובאת")
    val noFloorPlanStatus = BilingualString("No floor plan", "אין תוכנית קומה")
    val noFloorPlanImported = BilingualString(
        "No floor plan imported yet.\nTap the upload button to import one.",
        "טרם יובאה תוכנית קומה.\nלחץ על כפתור ההעלאה לייבוא."
    )
    val addBuildingTitle = BilingualString("Add Building", "הוסף מבנה")
    val buildingNameLabel = BilingualString("Building Name", "שם המבנה")
    val descriptionOptional = BilingualString("Description (optional)", "תיאור (אופציונלי)")
    val addButton = BilingualString("Add", "הוסף")
    val cancelButton = BilingualString("Cancel", "ביטול")
    val addFloorTitle = BilingualString("Add Floor", "הוסף קומה")
    val floorNameLabel = BilingualString("Floor Name (e.g., B1, B2)", "שם הקומה (למשל, B1, B2)")
    val floorLevelLabel = BilingualString("Level Number (e.g., -1, -2)", "מספר קומה (למשל, 1-, 2-)")

    // Street View
    val streetViewTitle = BilingualString("Street View", "תצוגת רחוב")
    val waypointPhoto = BilingualString("Waypoint Photo", "תמונת נקודת ציון")
    val previous = BilingualString("Previous", "הקודם")
    val next = BilingualString("Next", "הבא")
    val noPhotosCaptured = BilingualString(
        "No photos captured for this waypoint",
        "לא צולמו תמונות לנקודת ציון זו"
    )
    val navigateToLabel = BilingualString("Navigate to:", "נווט אל:")

    // Common
    val confirmAction = BilingualString("Confirm", "אישור")
    val cancelAction = BilingualString("Cancel", "ביטול")
    val scanning = BilingualString("Scanning...", "סורק...")
    val analyzingFrame = BilingualString("Analyzing frame...", "מנתח מסגרת...")
    val startingCamera = BilingualString(
        "Starting camera... Point at a mapped area.",
        "מפעיל מצלמה... כוון לאזור ממופה."
    )
    val scanningPointCamera = BilingualString(
        "Scanning... Point your camera at your surroundings.",
        "סורק... כוון את המצלמה לסביבתך."
    )

    fun BilingualString.get(language: LocaleManager.AppLanguage): String {
        return when (language) {
            LocaleManager.AppLanguage.ENGLISH -> en
            LocaleManager.AppLanguage.HEBREW -> he
        }
    }
}
