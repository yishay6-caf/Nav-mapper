package com.subnavar.app.util

/**
 * Bilingual string provider for English and Hebrew.
 * Used for in-app text that needs to change dynamically based on user language selection.
 */
object Strings {

    data class BilingualString(val en: String, val he: String)

    // App general
    val appName = BilingualString("SubNav AR", "SubNav AR")
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
        "SubNav AR - Indoor AR Navigation System\nFor underground multi-floor buildings",
        "SubNav AR - מערכת ניווט AR פנים-מבנית\nלמבנים תת-קרקעיים רב-קומתיים"
    )

    // Data sharing
    val exportSuccess = BilingualString("Data exported successfully", "הנתונים יוצאו בהצלחה")
    val importSuccess = BilingualString("Data imported successfully", "הנתונים יובאו בהצלחה")
    val exportFailed = BilingualString("Export failed", "הייצוא נכשל")
    val importFailed = BilingualString("Import failed", "הייבוא נכשל")

    fun BilingualString.get(language: LocaleManager.AppLanguage): String {
        return when (language) {
            LocaleManager.AppLanguage.ENGLISH -> en
            LocaleManager.AppLanguage.HEBREW -> he
        }
    }
}
