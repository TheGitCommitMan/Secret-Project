package com.example.game

import androidx.compose.ui.graphics.Color

data class WallCollider(
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val label: String = "Wall"
) {
    fun intersects(px: Float, py: Float, radius: Float): Boolean {
        // Find the closest point on the rectangle to the circle's center
        val closestX = px.coerceIn(x, x + width)
        val closestY = py.coerceIn(y, y + height)

        // Calculate distance between closest point and circle center
        val dx = px - closestX
        val dy = py - closestY

        // If the distance is less than the circle's radius, they intersect
        return (dx * dx + dy * dy) < (radius * radius)
    }
}

object GameMapData {
    val rooms = listOf(
        GameRoom("Cafeteria", 500f, 200f, 240f, 160f, Color(0xFF2C3E50)),
        GameRoom("Weapons", 760f, 160f, 140f, 110f, Color(0xFF34495E)),
        GameRoom("O2", 760f, 360f, 100f, 100f, Color(0xFF27AE60)),
        GameRoom("Navigation", 920f, 450f, 120f, 150f, Color(0xFF2980B9)),
        GameRoom("Shields", 780f, 660f, 150f, 140f, Color(0xFF8E44AD)),
        GameRoom("Communications", 650f, 820f, 120f, 100f, Color(0xFFD35400)),
        GameRoom("Storage", 500f, 680f, 180f, 160f, Color(0xFF7F8C8D)),
        GameRoom("Admin", 680f, 480f, 130f, 110f, Color(0xFFC0392B)),
        GameRoom("Electrical", 340f, 560f, 130f, 140f, Color(0xFFF1C40F)),
        GameRoom("Security", 220f, 440f, 110f, 110f, Color(0xFF16A085)),
        GameRoom("MedBay", 340f, 340f, 110f, 130f, Color(0xFF1ABC9C)),
        GameRoom("Reactor", 80f, 450f, 120f, 170f, Color(0xFFE74C3C)),
        GameRoom("Upper Engine", 160f, 240f, 140f, 140f, Color(0xFF95A5A6)),
        GameRoom("Lower Engine", 160f, 660f, 140f, 140f, Color(0xFF7F8C8D))
    )

    val vents = listOf(
        GameVent("vent_medbay", "MedBay", 340f, 320f, listOf("vent_security", "vent_electrical")),
        GameVent("vent_security", "Security", 220f, 440f, listOf("vent_medbay", "vent_electrical")),
        GameVent("vent_electrical", "Electrical", 340f, 540f, listOf("vent_medbay", "vent_security")),
        GameVent("vent_cafeteria", "Cafeteria", 530f, 210f, listOf("vent_admin")),
        GameVent("vent_admin", "Admin", 680f, 480f, listOf("vent_cafeteria")),
        GameVent("vent_nav_top", "Navigation", 920f, 420f, listOf("vent_weapons")),
        GameVent("vent_nav_bot", "Navigation", 920f, 480f, listOf("vent_shields")),
        GameVent("vent_weapons", "Weapons", 760f, 150f, listOf("vent_nav_top")),
        GameVent("vent_shields", "Shields", 780f, 660f, listOf("vent_nav_bot"))
    )

    val taskNodes = listOf(
        // Map of static task spots
        TaskNode("task_cafe_garbage", "Cafeteria", TaskType.SwipeCard, 500f, 160f, "Empty Trash"),
        TaskNode("task_admin_swipe", "Admin", TaskType.SwipeCard, 680f, 460f, "Swipe Card"),
        TaskNode("task_elec_wire", "Electrical", TaskType.FixWiring, 340f, 560f, "Fix Wiring"),
        TaskNode("task_med_scan", "MedBay", TaskType.SubmitScan, 340f, 360f, "Submit Scan"),
        TaskNode("task_shields_prime", "Shields", TaskType.PrimeShields, 780f, 680f, "Prime Shields"),
        TaskNode("task_nav_data", "Navigation", TaskType.DownloadData, 920f, 440f, "Download Data"),
        TaskNode("task_weapons_asteroids", "Weapons", TaskType.ClearAsteroids, 760f, 140f, "Clear Asteroids"),
        TaskNode("task_reactor_start", "Reactor", TaskType.PrimeShields, 80f, 450f, "Unlock Manifolds"),
        TaskNode("task_o2_clean", "O2", TaskType.FixWiring, 760f, 350f, "Clean O2 Filter")
    )

    // Axis-Aligned bounding boxes of solid walls
    val colliders = listOf(
        // Boundary outer walls
        WallCollider(0f, 0f, 1000f, 50f, "Outer North"),
        WallCollider(0f, 950f, 1000f, 50f, "Outer South"),
        WallCollider(0f, 0f, 50f, 1000f, "Outer West"),
        WallCollider(950f, 0f, 50f, 1000f, "Outer East"),

        // Cafeteria center table
        WallCollider(460f, 160f, 80f, 80f, "Cafe Table"),

        // Reactor Core
        WallCollider(50f, 410f, 60f, 80f, "Reactor Core"),

        // Security monitors
        WallCollider(180f, 410f, 40f, 40f, "Security Monitors"),

        // Medbay Scanner base
        WallCollider(320f, 330f, 40f, 40f, "Medbay Scan Pad"),

        // Electrical generator
        WallCollider(310f, 530f, 60f, 60f, "Electrical Generator"),

        // Storage boxes
        WallCollider(470f, 650f, 60f, 60f, "Storage Crates"),

        // Mid-map block divider walls (blocking navigation between specific rooms)
        WallCollider(280f, 50f, 20f, 300f, "West Partition Top"),
        WallCollider(280f, 450f, 20f, 300f, "West Partition Bottom"),
        WallCollider(600f, 50f, 20f, 300f, "East Partition Top"),
        WallCollider(600f, 550f, 20f, 300f, "East Partition Bottom"),
        WallCollider(350f, 450f, 250f, 20f, "Center Corridor Divider")
    )

    fun getRoomAt(x: Float, y: Float): String {
        for (room in rooms) {
            val hWidth = room.width / 2f
            val hHeight = room.height / 2f
            if (x in (room.x - hWidth)..(room.x + hWidth) &&
                y in (room.y - hHeight)..(room.y + hHeight)) {
                return room.name
            }
        }
        return "Corridors"
    }

    fun isPositionWalkable(x: Float, y: Float, radius: Float = 15f, isGhost: Boolean = false): Boolean {
        // Ghosts can walk through any inner colliders, but stay in outer bounds (50 to 950)
        if (x < 50f || x > 950f || y < 50f || y > 950f) return false
        if (isGhost) return true

        // Check intersection with any collider
        for (collider in colliders) {
            if (collider.intersects(x, y, radius)) {
                return false
            }
        }
        return true
    }
}

data class TaskNode(
    val id: String,
    val roomName: String,
    val taskType: TaskType,
    val x: Float,
    val y: Float,
    val label: String
)
