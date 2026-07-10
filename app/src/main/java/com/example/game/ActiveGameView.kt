package com.example.game

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.roundToInt

@Composable
fun ActiveGameView(viewModel: GameViewModel) {
    val characters by viewModel.characters.collectAsState()
    val deadBodies by viewModel.deadBodies.collectAsState()
    val activeSabotage by viewModel.activeSabotage.collectAsState()
    val assignedTasks by viewModel.assignedTasks.collectAsState()
    val activeMinigame by viewModel.activeMinigameTask.collectAsState()

    val isVenting by viewModel.isVenting.collectAsState()
    val currentVentId by viewModel.currentVentId.collectAsState()

    val myKillCooldown by viewModel.myKillCooldown.collectAsState()
    val mySabotageCooldown by viewModel.mySabotageCooldown.collectAsState()

    val isScanning by viewModel.isScanning.collectAsState()
    val scanProgress by viewModel.scanProgress.collectAsState()

    val myChar = characters.firstOrNull { it.id == viewModel.myCharacterId } ?: return

    var screenWidth by remember { mutableStateOf(1000f) }
    var screenHeight by remember { mutableStateOf(1000f) }

    // Toggleable full minimap overlay
    var showMinimap by remember { mutableStateOf(false) }

    // Toggleable sabotage menu overlay
    var showSabotageMenu by remember { mutableStateOf(false) }

    // Joystick Position States
    var joystickDeltaX by remember { mutableStateOf(0f) }
    var joystickDeltaY by remember { mutableStateOf(0f) }
    val joystickRadius = 120f

    val coroutineScope = rememberCoroutineScope()

    // Joystick movement updater
    LaunchedEffect(joystickDeltaX, joystickDeltaY) {
        while (joystickDeltaX != 0f || joystickDeltaY != 0f) {
            delay(16)
            if (isVenting) continue // cannot move while in vents

            val length = sqrt(joystickDeltaX * joystickDeltaX + joystickDeltaY * joystickDeltaY)
            if (length > 0) {
                val speed = myChar.speed * viewModel.playerSpeedMultiplier.value
                val dx = (joystickDeltaX / length) * speed
                val dy = (joystickDeltaY / length) * speed

                val nx = (myChar.x + dx).coerceIn(50f, 950f)
                val ny = (myChar.y + dy).coerceIn(50f, 950f)

                // Check collision
                if (GameMapData.isPositionWalkable(nx, ny, isGhost = myChar.isGhost)) {
                    viewModel.updatePlayerPosition(nx, ny)
                } else if (GameMapData.isPositionWalkable(nx, myChar.y, isGhost = myChar.isGhost)) {
                    viewModel.updatePlayerPosition(nx, myChar.y)
                } else if (GameMapData.isPositionWalkable(myChar.x, ny, isGhost = myChar.isGhost)) {
                    viewModel.updatePlayerPosition(myChar.x, ny)
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B0F19))
            .onGloballyPositioned { layoutCoordinates ->
                screenWidth = layoutCoordinates.size.width.toFloat()
                screenHeight = layoutCoordinates.size.height.toFloat()
            }
    ) {
        // Space Background visual detail (stars)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    // Draw some static background stars
                    val randomStars = listOf(
                        Offset(100f, 200f), Offset(400f, 150f), Offset(800f, 300f),
                        Offset(250f, 600f), Offset(650f, 500f), Offset(900f, 800f),
                        Offset(150f, 850f), Offset(500f, 900f), Offset(700f, 120f)
                    )
                    randomStars.forEach { star ->
                        drawCircle(Color(0x88FFFFFF), radius = 2f, center = star)
                    }
                }
        )

        // Game Board Offset Math
        val mapOffsetX = screenWidth / 2f - myChar.x
        val mapOffsetY = screenHeight / 2f - myChar.y

        // Render MAP layer (Rooms, Corridors, Tasks, Vents, Dead Bodies, Players)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    // Touch to move (alternative fallback or decoration)
                }
        ) {
            // 1. Draw Map Rooms
            GameMapData.rooms.forEach { room ->
                val rx = room.x + mapOffsetX
                val ry = room.y + mapOffsetY
                val rw = room.width
                val rh = room.height

                Box(
                    modifier = Modifier
                        .offset { IntOffset((rx - rw / 2f).toInt(), (ry - rh / 2f).toInt()) }
                        .size(rw.dp, rh.dp)
                        .background(room.color.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                        .border(1.5.dp, Color(0xFF34495E), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = room.name,
                        color = Color(0x99FFFFFF),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // 2. Draw Obstacles/Colliders
            GameMapData.colliders.forEach { col ->
                val cx = col.x + mapOffsetX
                val cy = col.y + mapOffsetY

                Box(
                    modifier = Modifier
                        .offset { IntOffset(cx.toInt(), cy.toInt()) }
                        .size(col.width.dp, col.height.dp)
                        .background(Color(0xFF2C3E50))
                        .border(1.dp, Color(0xFF1F2D3D))
                )
            }

            // 3. Draw Vents
            GameMapData.vents.forEach { vent ->
                val vx = vent.x + mapOffsetX
                val vy = vent.y + mapOffsetY

                Box(
                    modifier = Modifier
                        .offset { IntOffset((vx - 18).toInt(), (vy - 18).toInt()) }
                        .size(36.dp)
                        .background(Color(0xFF3F474E), CircleShape)
                        .border(2.dp, Color.Gray, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("⚙️", fontSize = 14.sp)
                }
            }

            // 4. Draw Task Nodes (Yellow exclamation mark if incomplete, glowing green outline)
            GameMapData.taskNodes.forEach { taskNode ->
                // Check if user is assigned this task
                val isAssigned = assignedTasks.any { it.id == taskNode.id }
                if (isAssigned) {
                    val isDone = assignedTasks.firstOrNull { it.id == taskNode.id }?.isCompleted == true
                    val tx = taskNode.x + mapOffsetX
                    val ty = taskNode.y + mapOffsetY

                    Box(
                        modifier = Modifier
                            .offset { IntOffset((tx - 20).toInt(), (ty - 20).toInt()) }
                            .size(40.dp)
                            .background(
                                if (isDone) Color(0x332ECC71) else Color(0x33F1C40F),
                                CircleShape
                            )
                            .border(
                                2.dp,
                                if (isDone) Color.Green else Color.Yellow,
                                CircleShape
                            )
                            .clickable {
                                if (!isDone && !myChar.isImpostor && !myChar.isDead) {
                                    viewModel.openMinigame(taskNode)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isDone) "✔" else "❗",
                            color = if (isDone) Color.Green else Color.Yellow,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                }
            }

            // 5. Draw Dead Bodies
            deadBodies.forEach { body ->
                val bx = body.x + mapOffsetX
                val by = body.y + mapOffsetY

                CrewmateSprite(
                    colorName = body.colorName,
                    modifier = Modifier
                        .offset { IntOffset((bx - 24).toInt(), (by - 24).toInt()) },
                    isDeadBody = true
                )
            }

            // 6. Draw Characters (Bots + User)
            characters.forEach { char ->
                // If venting, hide from map unless it's yourself showing inside vent
                if (char.id != myChar.id && characters.firstOrNull { it.id == char.id }?.currentVentId != null) {
                    return@forEach
                }

                val cx = char.x + mapOffsetX
                val cy = char.y + mapOffsetY

                val walkProgress = if (char.vx != 0f || char.vy != 0f) {
                    (System.currentTimeMillis() % 1000f) / 1000f * 6.28f
                } else 0f

                Box(
                    modifier = Modifier
                        .offset { IntOffset((cx - 24).toInt(), (cy - 36).toInt()) }
                        .size(48.dp, 60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        // Display Character Tag (Red text if yourself is Impostor and bot is Impostor!)
                        val isNameRed = myChar.isImpostor && char.isImpostor
                        Text(
                            text = char.name,
                            color = if (isNameRed) Color(0xFFC61A1A) else Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .background(Color(0x66000000), RoundedCornerShape(2.dp))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))

                        // Venting or Normal visual
                        val isCurrentlyVenting = char.currentVentId != null
                        if (isCurrentlyVenting) {
                            Text("⬇", color = Color.Red, fontSize = 24.sp)
                        } else {
                            CrewmateSprite(
                                colorName = char.colorName,
                                isFacingLeft = char.vx < 0,
                                walkProgress = walkProgress,
                                hatId = char.hatId,
                                skinId = char.skinId,
                                petId = char.petId,
                                isGhost = char.isGhost,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                }
            }
        }

        // 7. Dynamic Vision Masking (Crewmate vignette circle, shrunk if lights are out)
        if (!myChar.isImpostor && !myChar.isGhost) {
            val isLightsOut = activeSabotage.type == ActiveSabotageType.LightsOut
            val baseVision = viewModel.crewmateVisionSetting.collectAsState().value
            val visionRadius = if (isLightsOut) 50f else baseVision

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .drawBehind {
                        // Draw ambient black everywhere except the player's vision circle
                        val canvasWidth = size.width
                        val canvasHeight = size.height

                        // Draw dark overlay with transparency circle
                        drawRect(Color(0x33000000))
                    }
            )
        }

        // TOP BAR UI (Task Bar, Current Room, Alerts)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp, start = 16.dp, end = 16.dp)
        ) {
            // Task completed bar
            val totalTasksCount = assignedTasks.size
            val completedCount = assignedTasks.count { it.isCompleted }
            val taskRatio = if (totalTasksCount > 0) completedCount.toFloat() / totalTasksCount else 0f

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xAA111C24), RoundedCornerShape(6.dp))
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total Tasks Completed: ",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(14.dp)
                        .background(Color(0xFF2C3E50), RoundedCornerShape(4.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(taskRatio)
                            .background(Color(0xFF2ECC71), RoundedCornerShape(4.dp))
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "$completedCount/$totalTasksCount",
                    color = Color(0xFF2ECC71),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Alarm indicator if sabotage is active
            if (activeSabotage.type != ActiveSabotageType.None) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFC61A1A), RoundedCornerShape(6.dp))
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "⚠️ CRITICAL ALERT: ${activeSabotage.type.name} IN ${activeSabotage.timer}s!",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Room Title Banner
            Box(
                modifier = Modifier
                    .background(Color(0x99111C24), RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Current Room: ${myChar.currentRoom}",
                    color = Color(0xFF38E5E5),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // FLOATING ACTION TRIGGERS (Right side overlay)
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 110.dp, end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Minimap Button
            FloatingActionButton(
                onClick = { showMinimap = !showMinimap },
                containerColor = Color(0xAA1E272C),
                contentColor = Color.White,
                modifier = Modifier.size(45.dp)
            ) {
                Icon(Icons.Default.Map, contentDescription = "Minimap")
            }

            // Sabotage Menu Button (Impostor only)
            if (myChar.isImpostor && !myChar.isDead) {
                FloatingActionButton(
                    onClick = { showSabotageMenu = !showSabotageMenu },
                    containerColor = Color(0xAAC61A1A),
                    contentColor = Color.White,
                    modifier = Modifier.size(45.dp)
                ) {
                    Icon(Icons.Default.FlashOn, contentDescription = "Sabotage")
                }
            }
        }

        // INTERACTION CONTROLS BAR (Joystick & Actions at bottom)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp, start = 24.dp, end = 24.dp)
        ) {
            // 1. Joystick Control (Left side)
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .align(Alignment.BottomStart)
                    .background(Color(0x33FFFFFF), CircleShape)
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDrag = { change, dragAmount ->
                                change.consume()
                                joystickDeltaX = (joystickDeltaX + dragAmount.x).coerceIn(-joystickRadius, joystickRadius)
                                joystickDeltaY = (joystickDeltaY + dragAmount.y).coerceIn(-joystickRadius, joystickRadius)
                            },
                            onDragEnd = {
                                joystickDeltaX = 0f
                                joystickDeltaY = 0f
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                // Outer ring
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .border(3.dp, Color(0x77FFFFFF), CircleShape)
                )

                // Joystick Thumb Knob
                Box(
                    modifier = Modifier
                        .offset { IntOffset(joystickDeltaX.roundToInt(), joystickDeltaY.roundToInt()) }
                        .size(60.dp)
                        .background(Color(0xAAFFFFFF), CircleShape)
                        .border(1.5.dp, Color.White, CircleShape)
                )
            }

            // 2. Action buttons cluster (Right side)
            Row(
                modifier = Modifier.align(Alignment.BottomEnd),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Vent Navigation arrows if inside vent
                if (isVenting && currentVentId != null) {
                    val activeVent = GameMapData.vents.firstOrNull { it.id == currentVentId }
                    if (activeVent != null) {
                        activeVent.connectedVentIds.forEach { targetId ->
                            val targetVent = GameMapData.vents.firstOrNull { it.id == targetId }
                            if (targetVent != null) {
                                Button(
                                    onClick = { viewModel.navigateVent(targetId) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3F474E)),
                                    modifier = Modifier.height(48.dp)
                                ) {
                                    Text("➡ ${targetVent.roomName}", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // "REPORT" button (grows when near dead body)
                val canReport = deadBodies.any { body ->
                    val dist = sqrt((body.x - myChar.x) * (body.x - myChar.x) + (body.y - myChar.y) * (body.y - myChar.y))
                    dist < 85f
                } && !myChar.isDead

                FloatingActionButton(
                    onClick = {
                        if (canReport) {
                            val targetBody = deadBodies.minByOrNull {
                                sqrt((it.x - myChar.x) * (it.x - myChar.x) + (it.y - myChar.y) * (it.y - myChar.y))
                            }
                            viewModel.triggerEmergencyMeeting(reportedBodyPlayerId = targetBody?.playerId)
                        }
                    },
                    containerColor = if (canReport) Color(0xFFE67E22) else Color(0x331E272C),
                    contentColor = if (canReport) Color.White else Color.Gray,
                    modifier = Modifier.size(65.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Campaign, contentDescription = "Report")
                        Text("REPORT", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // USE/TASK button (grows near task)
                val nearbyTask = GameMapData.taskNodes.firstOrNull { node ->
                    val isAssigned = assignedTasks.any { it.id == node.id && !it.isCompleted }
                    val dist = sqrt((node.x - myChar.x) * (node.x - myChar.x) + (node.y - myChar.y) * (node.y - myChar.y))
                    isAssigned && dist < 85f
                }
                val canUse = nearbyTask != null && !myChar.isDead && !myChar.isImpostor

                FloatingActionButton(
                    onClick = {
                        if (nearbyTask != null) {
                            viewModel.openMinigame(nearbyTask)
                        }
                    },
                    containerColor = if (canUse) Color(0xFF2ECC71) else Color(0x331E272C),
                    contentColor = if (canUse) Color.White else Color.Gray,
                    modifier = Modifier.size(65.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.TouchApp, contentDescription = "Use")
                        Text("USE", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Vent button (Impostor only)
                val nearbyVent = GameMapData.vents.firstOrNull { vent ->
                    val dist = sqrt((vent.x - myChar.x) * (vent.x - myChar.x) + (vent.y - myChar.y) * (vent.y - myChar.y))
                    dist < 60f
                }
                val canVent = (nearbyVent != null || isVenting) && myChar.isImpostor && !myChar.isDead

                if (myChar.isImpostor) {
                    FloatingActionButton(
                        onClick = {
                            if (canVent) {
                                viewModel.performVentAction()
                            }
                        },
                        containerColor = if (canVent) Color(0xFF3F474E) else Color(0x331E272C),
                        contentColor = if (canVent) Color.White else Color.Gray,
                        modifier = Modifier.size(65.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.ImportExport, contentDescription = "Vent")
                            Text(if (isVenting) "EXIT" else "VENT", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // KILL button (Impostor only)
                    val nearbyKillTarget = characters.firstOrNull {
                        !it.isDead && !it.isImpostor && sqrt((it.x - myChar.x) * (it.x - myChar.x) + (it.y - myChar.y) * (it.y - myChar.y)) < 80f
                    }
                    val canKill = nearbyKillTarget != null && myKillCooldown == 0f && !myChar.isDead

                    FloatingActionButton(
                        onClick = {
                            if (canKill) {
                                viewModel.performKillAction()
                            }
                        },
                        containerColor = if (canKill) Color(0xFFC61A1A) else Color(0x331E272C),
                        contentColor = if (canKill) Color.White else Color.Gray,
                        modifier = Modifier.size(65.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Dangerous, contentDescription = "Kill")
                            Text(
                                text = if (myKillCooldown > 0) "${myKillCooldown.toInt()}s" else "KILL",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Active Task Mini-game Popup Dialog overlay
        if (activeMinigame != null) {
            TaskMinigameOverlay(
                taskNode = activeMinigame!!,
                onComplete = {
                    viewModel.completeTask(activeMinigame!!.id)
                    viewModel.closeMinigame()
                },
                onClose = { viewModel.closeMinigame() }
            )
        }

        // MINIMAP Full-Screen Overlay
        AnimatedVisibility(
            visible = showMinimap,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            MinimapOverlay(assignedTasks = assignedTasks, myPosition = Offset(myChar.x, myChar.y)) {
                showMinimap = false
            }
        }

        // SABOTAGE Grid Map Popup overlay
        AnimatedVisibility(
            visible = showSabotageMenu,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            SabotageMenuOverlay(mySabotageCooldown = mySabotageCooldown, onTrigger = { type ->
                viewModel.triggerSabotage(type)
                showSabotageMenu = false
            }, onClose = { showSabotageMenu = false })
        }
    }
}

// Map HUD Overlay Composable
@Composable
fun MinimapOverlay(assignedTasks: List<GameTask>, myPosition: Offset, onClose: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xDD000000))
            .clickable { onClose() },
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .fillMaxHeight(0.8f),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E272C))
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("SPACESHIP MINIMAP", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Interactive 2D schematic projection map of rooms
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(Color.Black, RoundedCornerShape(8.dp))
                        .border(1.dp, Color(0xFF34495E), RoundedCornerShape(8.dp))
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        // Scale game map coordinates (0..1000) to current canvas aspect sizes
                        val scaleX = this.size.width / 1000f
                        val scaleY = this.size.height / 1000f

                        // Draw room silhouettes
                        GameMapData.rooms.forEach { room ->
                            drawRect(
                                color = Color(0x332980B9),
                                topLeft = Offset((room.x - room.width / 2f) * scaleX, (room.y - room.height / 2f) * scaleY),
                                size = Size(room.width * scaleX, room.height * scaleY)
                            )
                            drawRect(
                                color = Color(0xFF2980B9),
                                topLeft = Offset((room.x - room.width / 2f) * scaleX, (room.y - room.height / 2f) * scaleY),
                                size = Size(room.width * scaleX, room.height * scaleY),
                                style = Stroke(1.5f)
                            )
                        }

                        // Draw tasks nodes (Flashing yellow circles on nodes)
                        GameMapData.taskNodes.forEach { taskNode ->
                            val isAssigned = assignedTasks.any { it.id == taskNode.id }
                            if (isAssigned) {
                                val isDone = assignedTasks.firstOrNull { it.id == taskNode.id }?.isCompleted == true
                                drawCircle(
                                    color = if (isDone) Color.Green else Color.Yellow,
                                    radius = 12f,
                                    center = Offset(taskNode.x * scaleX, taskNode.y * scaleY)
                                )
                            }
                        }

                        // Draw User Position Dot
                        drawCircle(
                            color = Color(0xFFE74C3C),
                            radius = 15f,
                            center = Offset(myPosition.x * scaleX, myPosition.y * scaleY)
                        )
                    }
                }
            }
        }
    }
}

// Sabotage Menu
@Composable
fun SabotageMenuOverlay(
    mySabotageCooldown: Float,
    onTrigger: (ActiveSabotageType) -> Unit,
    onClose: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xDD000000))
            .clickable { onClose() },
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.width(320.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E272C))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("TRIGGER SABOTAGE", color = Color.Red, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, tint = Color.White, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                val isReady = mySabotageCooldown == 0f

                Text(
                    text = if (isReady) "SELECT SYSTEM TO DE-STABILIZE" else "SABOTAGE RECHARGING: ${mySabotageCooldown.toInt()}s",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Sabotage options
                Button(
                    onClick = { onTrigger(ActiveSabotageType.ReactorMeltdown) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC61A1A)),
                    enabled = isReady,
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text("Reactor Meltdown (45s Timer)", color = Color.White)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { onTrigger(ActiveSabotageType.O2Depletion) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE67E22)),
                    enabled = isReady,
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text("O2 Filter Depletion (45s Timer)", color = Color.White)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { onTrigger(ActiveSabotageType.LightsOut) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C3E50)),
                    enabled = isReady,
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text("Lights Out (Limits Vision)", color = Color.White)
                }
            }
        }
    }
}
