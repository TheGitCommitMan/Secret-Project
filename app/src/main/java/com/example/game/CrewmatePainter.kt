package com.example.game

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.unit.dp
import kotlin.math.sin

@Composable
fun CrewmateSprite(
    colorName: String,
    modifier: Modifier = Modifier,
    isFacingLeft: Boolean = false,
    walkProgress: Float = 0f,
    hatId: String = "none",
    skinId: String = "none",
    petId: String = "none",
    isGhost: Boolean = false,
    isDeadBody: Boolean = false
) {
    val crewColor = AmongUsColors[colorName] ?: Color.Red

    Canvas(modifier = modifier.size(48.dp)) {
        val scaleX = if (isFacingLeft) -1f else 1f
        withTransform({
            scale(scaleX, 1f)
        }) {
            if (isDeadBody) {
                drawDeadBody(crewColor)
            } else {
                drawCrewmate(crewColor, walkProgress, hatId, skinId, petId, isGhost)
            }
        }
    }
}

private fun DrawScope.drawCrewmate(
    bodyColor: Color,
    walkProgress: Float,
    hatId: String,
    skinId: String,
    petId: String,
    isGhost: Boolean
) {
    val alpha = if (isGhost) 0.5f else 1f
    val strokeColor = Color(0xFF0F0F0F)
    val strokeWidth = 3f

    // 1. Draw Backpack (Oxygen tank)
    drawRoundRect(
        color = bodyColor,
        topLeft = Offset(2f, 15f),
        size = Size(10f, 22f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f),
        alpha = alpha
    )
    drawRoundRect(
        color = strokeColor,
        topLeft = Offset(2f, 15f),
        size = Size(10f, 22f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f),
        style = Stroke(strokeWidth),
        alpha = alpha
    )

    // 2. Legs walking animation
    val legOffset1 = if (isGhost) 0f else sin(walkProgress) * 5f
    val legOffset2 = if (isGhost) 0f else sin(walkProgress + Math.PI.toFloat()) * 5f

    // Back leg
    drawRoundRect(
        color = bodyColor,
        topLeft = Offset(14f, 32f + legOffset1),
        size = Size(8f, 10f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f, 3f),
        alpha = alpha
    )
    drawRoundRect(
        color = strokeColor,
        topLeft = Offset(14f, 32f + legOffset1),
        size = Size(8f, 10f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f, 3f),
        style = Stroke(strokeWidth),
        alpha = alpha
    )

    // Front leg
    drawRoundRect(
        color = bodyColor,
        topLeft = Offset(24f, 32f + legOffset2),
        size = Size(8f, 10f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f, 3f),
        alpha = alpha
    )
    drawRoundRect(
        color = strokeColor,
        topLeft = Offset(24f, 32f + legOffset2),
        size = Size(8f, 10f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f, 3f),
        style = Stroke(strokeWidth),
        alpha = alpha
    )

    // 3. Main Body
    drawRoundRect(
        color = bodyColor,
        topLeft = Offset(10f, 10f),
        size = Size(24f, 26f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f),
        alpha = alpha
    )
    drawRoundRect(
        color = strokeColor,
        topLeft = Offset(10f, 10f),
        size = Size(24f, 26f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f),
        style = Stroke(strokeWidth),
        alpha = alpha
    )

    // 4. Draw Skin Overlay
    when (skinId) {
        "suit" -> {
            // Draw a black suit torso with white collar and red tie
            drawRect(Color(0xFF222222), Offset(11f, 22f), Size(22f, 12f), alpha = alpha)
            val tiePath = Path().apply {
                moveTo(22f, 22f)
                lineTo(20f, 28f)
                lineTo(22f, 31f)
                lineTo(24f, 28f)
                close()
            }
            drawPath(tiePath, Color.Red, alpha = alpha)
        }
        "astronaut" -> {
            // White suit lines and chest controls
            drawRect(Color(0xFFE5E9F0), Offset(11f, 22f), Size(22f, 12f), alpha = alpha)
            drawCircle(Color.Red, radius = 2f, center = Offset(16f, 26f), alpha = alpha)
            drawCircle(Color.Blue, radius = 2f, center = Offset(22f, 26f), alpha = alpha)
        }
        "doctor" -> {
            // White coat with stethoscope
            drawRect(Color(0xFFE5E9F0), Offset(11f, 22f), Size(22f, 12f), alpha = alpha)
            drawLine(Color(0xFF888888), Offset(13f, 22f), Offset(13f, 28f), strokeWidth = 2f, alpha = alpha)
            drawLine(Color(0xFF888888), Offset(31f, 22f), Offset(31f, 28f), strokeWidth = 2f, alpha = alpha)
        }
        "police" -> {
            // Police dark blue suit and yellow badge
            drawRect(Color(0xFF1E272C), Offset(11f, 22f), Size(22f, 12f), alpha = alpha)
            val badgePath = Path().apply {
                moveTo(16f, 24f)
                lineTo(18f, 23f)
                lineTo(20f, 24f)
                lineTo(18f, 27f)
                close()
            }
            drawPath(badgePath, Color(0xFFF1C40F), alpha = alpha)
        }
    }

    // 5. Draw Visor (Reflective glass faceplate)
    val visorBrush = Brush.linearGradient(
        colors = listOf(Color(0xFF8EDDF2), Color(0xFF4C98AF)),
        start = Offset(20f, 14f),
        end = Offset(32f, 24f)
    )
    drawRoundRect(
        brush = visorBrush,
        topLeft = Offset(20f, 14f),
        size = Size(14f, 10f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(5f, 5f),
        alpha = alpha
    )
    drawRoundRect(
        color = strokeColor,
        topLeft = Offset(20f, 14f),
        size = Size(14f, 10f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(5f, 5f),
        style = Stroke(strokeWidth),
        alpha = alpha
    )

    // Visor highlight glow
    drawOval(
        color = Color.White,
        topLeft = Offset(22f, 15f),
        size = Size(5f, 2.5f),
        alpha = alpha * 0.7f
    )

    // 6. Draw Hat Cosmetics
    when (hatId) {
        "sprout" -> {
            // Tiny green sprout leaf
            val leafPath = Path().apply {
                moveTo(22f, 10f)
                quadraticTo(16f, 5f, 18f, 2f)
                quadraticTo(22f, 2f, 22f, 10f)
            }
            drawPath(leafPath, Color(0xFF2ECC71), alpha = alpha)
            drawLine(Color(0xFF27AE60), Offset(22f, 10f), Offset(22f, 6f), strokeWidth = 2f, alpha = alpha)
        }
        "toilet_paper" -> {
            // Toilet paper roll on head
            drawRoundRect(Color.White, Offset(16f, 3f), Size(12f, 7f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f), alpha = alpha)
            drawRoundRect(strokeColor, Offset(16f, 3f), Size(12f, 7f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f), style = Stroke(2f), alpha = alpha)
            // Paper tail hanging off
            drawLine(Color.White, Offset(16f, 8f), Offset(12f, 12f), strokeWidth = 3f, alpha = alpha)
            drawLine(strokeColor, Offset(16f, 8f), Offset(12f, 12f), strokeWidth = 1f, alpha = alpha)
        }
        "viking" -> {
            // Horned helmet
            drawArc(Color(0xFF95A5A6), 180f, 180f, false, Offset(14f, 4f), Size(16f, 12f), alpha = alpha)
            drawArc(strokeColor, 180f, 180f, false, Offset(14f, 4f), Size(16f, 12f), style = Stroke(strokeWidth), alpha = alpha)
            // Left horn
            val leftHorn = Path().apply {
                moveTo(14f, 8f)
                quadraticTo(8f, 6f, 8f, 1f)
                quadraticTo(11f, 6f, 15f, 8f)
            }
            drawPath(leftHorn, Color.White, alpha = alpha)
            drawPath(leftHorn, strokeColor, style = Stroke(1.5f), alpha = alpha)
            // Right horn
            val rightHorn = Path().apply {
                moveTo(30f, 8f)
                quadraticTo(36f, 6f, 36f, 1f)
                quadraticTo(33f, 6f, 29f, 8f)
            }
            drawPath(rightHorn, Color.White, alpha = alpha)
            drawPath(rightHorn, strokeColor, style = Stroke(1.5f), alpha = alpha)
        }
        "chef" -> {
            // Puffy tall chef hat
            val chefPath = Path().apply {
                moveTo(16f, 10f)
                lineTo(16f, 6f)
                quadraticTo(14f, 2f, 18f, 2f)
                quadraticTo(22f, 0f, 24f, 2f)
                quadraticTo(28f, 2f, 26f, 6f)
                lineTo(26f, 10f)
                close()
            }
            drawPath(chefPath, Color.White, alpha = alpha)
            drawPath(chefPath, strokeColor, style = Stroke(2f), alpha = alpha)
        }
        "cowboy" -> {
            // Brown wide-brim cowboy hat
            drawRoundRect(Color(0xFF8B5A2B), Offset(8f, 8f), Size(28f, 3f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(1f, 1f), alpha = alpha)
            drawRoundRect(strokeColor, Offset(8f, 8f), Size(28f, 3f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(1f, 1f), style = Stroke(1.5f), alpha = alpha)
            // Dome
            drawRoundRect(Color(0xFF8B5A2B), Offset(15f, 2f), Size(14f, 6f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f, 3f), alpha = alpha)
            drawRoundRect(strokeColor, Offset(15f, 2f), Size(14f, 6f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f, 3f), style = Stroke(1.5f), alpha = alpha)
        }
        "astronaut" -> {
            // Complete glass dome enclosing
            drawCircle(Color(0x3387CEEB), radius = 17f, center = Offset(22f, 20f), alpha = alpha)
            drawCircle(strokeColor, radius = 17f, center = Offset(22f, 20f), style = Stroke(1.5f), alpha = alpha)
        }
    }
}

private fun DrawScope.drawDeadBody(bodyColor: Color) {
    val strokeColor = Color(0xFF0F0F0F)
    val strokeWidth = 3f

    // 1. Slumped bottom crewmate pants on the side
    // Leg 1
    drawRoundRect(
        color = bodyColor,
        topLeft = Offset(10f, 22f),
        size = Size(10f, 12f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f, 3f)
    )
    drawRoundRect(
        color = strokeColor,
        topLeft = Offset(10f, 22f),
        size = Size(10f, 12f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f, 3f),
        style = Stroke(strokeWidth)
    )

    // Leg 2
    drawRoundRect(
        color = bodyColor,
        topLeft = Offset(24f, 22f),
        size = Size(10f, 12f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f, 3f)
    )
    drawRoundRect(
        color = strokeColor,
        topLeft = Offset(24f, 22f),
        size = Size(10f, 12f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f, 3f),
        style = Stroke(strokeWidth)
    )

    // 2. Cut-off bone tube body
    drawRoundRect(
        color = bodyColor,
        topLeft = Offset(10f, 14f),
        size = Size(24f, 12f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
    )
    drawRoundRect(
        color = strokeColor,
        topLeft = Offset(10f, 14f),
        size = Size(24f, 12f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f),
        style = Stroke(strokeWidth)
    )

    // 3. Central bone extending upwards from flesh cut
    drawRoundRect(
        color = Color(0xFFECF0F1),
        topLeft = Offset(20f, 4f),
        size = Size(4f, 10f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(1f, 1f)
    )
    drawRoundRect(
        color = strokeColor,
        topLeft = Offset(20f, 4f),
        size = Size(4f, 10f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(1f, 1f),
        style = Stroke(2f)
    )
    // Bone head bulb
    drawCircle(Color(0xFFECF0F1), radius = 3.5f, center = Offset(19f, 4f))
    drawCircle(strokeColor, radius = 3.5f, center = Offset(19f, 4f), style = Stroke(1.5f))
    drawCircle(Color(0xFFECF0F1), radius = 3.5f, center = Offset(25f, 4f))
    drawCircle(strokeColor, radius = 3.5f, center = Offset(25f, 4f), style = Stroke(1.5f))

    // Red flesh center
    drawOval(Color(0xFFE74C3C), topLeft = Offset(12f, 12f), size = Size(20f, 4f))
}
