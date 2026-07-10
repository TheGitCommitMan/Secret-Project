package com.example.game

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun TaskMinigameOverlay(
    taskNode: TaskNode,
    onComplete: () -> Unit,
    onClose: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xAA000000)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.75f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E272C))
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF2C3E50))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "TASK: ${taskNode.label}",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "LOCATION: ${taskNode.roomName}",
                            color = Color(0xFFBDC3C7),
                            fontSize = 12.sp
                        )
                    }
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                // Minigame Body
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    when (taskNode.taskType) {
                        TaskType.SwipeCard -> SwipeCardMinigame(onComplete = onComplete)
                        TaskType.FixWiring -> FixWiringMinigame(onComplete = onComplete)
                        TaskType.PrimeShields -> PrimeShieldsMinigame(onComplete = onComplete)
                        TaskType.SubmitScan -> SubmitScanMinigame(onComplete = onComplete)
                        TaskType.DownloadData -> DownloadDataMinigame(onComplete = onComplete)
                        TaskType.ClearAsteroids -> ClearAsteroidsMinigame(onComplete = onComplete)
                    }
                }
            }
        }
    }
}

// 1. Swipe Card Minigame
@Composable
fun SwipeCardMinigame(onComplete: () -> Unit) {
    var cardExtracted by remember { mutableStateOf(false) }
    var swipeStatus by remember { mutableStateOf("INSERT CARD") }
    var swipeColor by remember { mutableStateOf(Color(0xFFBDC3C7)) }
    var isSuccess by remember { mutableStateOf(false) }

    val swipeWidthPx = 500f
    var dragOffsetX by remember { mutableStateOf(0f) }
    var lastTime by remember { mutableStateOf(0L) }

    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Status Reader Screen
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .background(Color.Black, RoundedCornerShape(8.dp))
                .border(2.dp, Color(0xFF34495E), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = swipeStatus,
                color = swipeColor,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                textAlign = TextAlign.Center
            )
        }

        // Wallet with card inside
        if (!cardExtracted) {
            Box(
                modifier = Modifier
                    .size(240.dp, 130.dp)
                    .background(Color(0xFF8B5A2B), RoundedCornerShape(12.dp))
                    .border(3.dp, Color(0xFF5C3A21), RoundedCornerShape(12.dp))
                    .clickable {
                        cardExtracted = true
                        swipeStatus = "DRAG CARD ACROSS SLOT"
                        swipeColor = Color.Yellow
                    },
                contentAlignment = Alignment.Center
            ) {
                // Credit Card showing out of wallet
                Box(
                    modifier = Modifier
                        .offset(y = (-20).dp)
                        .size(160.dp, 90.dp)
                        .background(Color(0xFF2980B9), RoundedCornerShape(8.dp))
                        .border(2.dp, Color.White, RoundedCornerShape(8.dp))
                ) {
                    Box(modifier = Modifier.offset(10.dp, 10.dp).size(30.dp, 20.dp).background(Color(0xFFF1C40F)))
                    Text(
                        text = "CREW ID",
                        modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
                Text("TAP WALLET TO DRAW CARD", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        } else {
            // Card reader rail
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Swipe Reader Slot
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .background(Color(0xFF2C3E50), RoundedCornerShape(6.dp))
                        .border(2.dp, Color(0xFF34495E), RoundedCornerShape(6.dp))
                ) {
                    // Left to right indicator line
                    Divider(color = Color(0xFF7F8C8D), thickness = 2.dp, modifier = Modifier.align(Alignment.Center))

                    // Drag card box
                    Box(
                        modifier = Modifier
                            .offset { IntOffset(dragOffsetX.roundToInt(), 0) }
                            .size(110.dp, 60.dp)
                            .background(if (isSuccess) Color(0xFF27AE60) else Color(0xFF2980B9), RoundedCornerShape(6.dp))
                            .border(2.dp, Color.White, RoundedCornerShape(6.dp))
                            .pointerInput(isSuccess) {
                                if (isSuccess) return@pointerInput
                                detectDragGestures(
                                    onDragStart = {
                                        lastTime = System.currentTimeMillis()
                                    },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        dragOffsetX = (dragOffsetX + dragAmount.x).coerceIn(0f, size.width.toFloat() - 250f)
                                    },
                                    onDragEnd = {
                                        val totalTime = System.currentTimeMillis() - lastTime
                                        if (dragOffsetX > (size.width - 320f)) {
                                            // Evaluated read speed
                                            if (totalTime < 250) {
                                                swipeStatus = "TOO FAST. TRY AGAIN."
                                                swipeColor = Color.Red
                                                dragOffsetX = 0f
                                            } else if (totalTime > 800) {
                                                swipeStatus = "TOO SLOW. TRY AGAIN."
                                                swipeColor = Color.Red
                                                dragOffsetX = 0f
                                            } else {
                                                swipeStatus = "CARD ACCEPTED!"
                                                swipeColor = Color.Green
                                                isSuccess = true
                                                coroutineScope.launch {
                                                    delay(1000)
                                                    onComplete()
                                                }
                                            }
                                        } else {
                                            swipeStatus = "BAD READ. SWIPE FULLY."
                                            swipeColor = Color.Red
                                            dragOffsetX = 0f
                                        }
                                    }
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("KEY CARD", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        cardExtracted = false
                        dragOffsetX = 0f
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                ) {
                    Text("Return Card")
                }
            }
        }
    }
}

// 2. Fix Wiring Minigame
@Composable
fun FixWiringMinigame(onComplete: () -> Unit) {
    val wireColors = listOf(Color.Red, Color.Blue, Color(0xFFF1C40F), Color(0xFFD35400))
    val leftNodeColors = remember { wireColors.shuffled() }
    val rightNodeColors = remember { wireColors.shuffled() }

    // Maps color to target matched node index
    var matches by remember { mutableStateOf(emptyMap<Color, Int>()) }
    var activeDraggingColor by remember { mutableStateOf<Color?>(null) }
    var dragPosition by remember { mutableStateOf(Offset.Zero) }

    val coroutineScope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Draw already connected static wires
            matches.forEach { (color, rightIdx) ->
                val leftIdx = leftNodeColors.indexOf(color)
                val startX = 60f
                val startY = 100f + leftIdx * 120f
                val endX = size.width - 60f
                val endY = 100f + rightIdx * 120f

                drawLine(
                    color = color,
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = 14f
                )
            }

            // Draw current dragged wire
            val activeColor = activeDraggingColor
            if (activeColor != null) {
                val leftIdx = leftNodeColors.indexOf(activeColor)
                val startX = 60f
                val startY = 100f + leftIdx * 120f
                drawLine(
                    color = activeColor,
                    start = Offset(startX, startY),
                    end = dragPosition,
                    strokeWidth = 14f
                )
            }
        }

        // Left Colored Connectors
        Column(
            modifier = Modifier.align(Alignment.CenterStart).fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceAround
        ) {
            leftNodeColors.forEachIndexed { index, color ->
                Box(
                    modifier = Modifier
                        .size(45.dp)
                        .background(color, CircleShape)
                        .border(2.dp, Color.White, CircleShape)
                        .pointerInput(matches.containsKey(color)) {
                            if (matches.containsKey(color)) return@pointerInput
                            detectDragGestures(
                                onDragStart = {
                                    activeDraggingColor = color
                                    dragPosition = Offset(60f, 100f + index * 120f)
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    dragPosition += dragAmount
                                },
                                onDragEnd = {
                                    // Check if drag position is close to any right node with same color
                                    val rWidth = size.width
                                    val rightNodeX = rWidth - 120f
                                    if (dragPosition.x > rightNodeX) {
                                        // Calculate closest right index
                                        val closestRightIdx = (dragPosition.y / 180f).toInt().coerceIn(0, 3)
                                        if (rightNodeColors[closestRightIdx] == color) {
                                            // MATCH!
                                            matches = matches + (color to closestRightIdx)
                                            if (matches.size == 4) {
                                                coroutineScope.launch {
                                                    delay(800)
                                                    onComplete()
                                                }
                                            }
                                        }
                                    }
                                    activeDraggingColor = null
                                }
                            )
                        }
                )
            }
        }

        // Right Colored Connectors
        Column(
            modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceAround
        ) {
            rightNodeColors.forEach { color ->
                Box(
                    modifier = Modifier
                        .size(45.dp)
                        .background(color, RoundedCornerShape(4.dp))
                        .border(2.dp, Color.White, RoundedCornerShape(4.dp))
                )
            }
        }
    }
}

// 3. Prime Shields Minigame
@Composable
fun PrimeShieldsMinigame(onComplete: () -> Unit) {
    // 7 Shield Hexagons. True = red/offline, False = white/glowing
    var shieldsState by remember { mutableStateOf(List(7) { (1..100).random() < 60 }) }
    val coroutineScope = rememberCoroutineScope()

    // Ensure at least one shield is offline on start
    LaunchedEffect(Unit) {
        if (shieldsState.all { !it }) {
            shieldsState = listOf(true, false, true, false, true, false, true)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceAround
    ) {
        Text("TAP RED PANELS TO PRIME SHIELDS", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)

        Box(modifier = Modifier.size(260.dp)) {
            val offsets = listOf(
                Offset(130f, 60f),  // Top
                Offset(50f, 110f),  // Top Left
                Offset(210f, 110f), // Top Right
                Offset(130f, 130f), // Center
                Offset(50f, 200f),  // Bot Left
                Offset(210f, 200f), // Bot Right
                Offset(130f, 250f)  // Bottom
            )

            offsets.forEachIndexed { index, offset ->
                val isRed = shieldsState[index]
                Box(
                    modifier = Modifier
                        .offset(offset.x.dp, offset.y.dp)
                        .size(60.dp)
                        .background(if (isRed) Color(0xFFC61A1A) else Color(0xFFEAEDED), RoundedCornerShape(12.dp))
                        .border(3.dp, if (isRed) Color(0xFF6B1A1A) else Color(0xFF38E5E5), RoundedCornerShape(12.dp))
                        .clickable {
                            if (isRed) {
                                val updated = shieldsState.toMutableList()
                                updated[index] = false
                                shieldsState = updated

                                if (updated.all { !it }) {
                                    coroutineScope.launch {
                                        delay(800)
                                        onComplete()
                                    }
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(if (isRed) "⚠️" else "⚡", color = if (isRed) Color.White else Color.Black)
                }
            }
        }
    }
}

// 4. Submit Scan Minigame
@Composable
fun SubmitScanMinigame(onComplete: () -> Unit) {
    var isScanning by remember { mutableStateOf(false) }
    var scanProgress by remember { mutableStateOf(0) }
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceAround
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .background(Color.Black, RoundedCornerShape(8.dp))
                .border(2.dp, Color(0xFF1ABC9C), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (isScanning) {
                    Text("ANALYZING BIO-DATA...", color = Color(0xFF1ABC9C), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("PROGRESS: $scanProgress%", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = scanProgress / 100f,
                        color = Color(0xFF1ABC9C),
                        trackColor = Color(0xFF2C3E50),
                        modifier = Modifier.fillMaxWidth(0.8f).height(10.dp)
                    )
                } else {
                    Text("STAND ON THE PLATFORM", color = Color.White, fontWeight = FontWeight.Bold)
                    Text("AND INITIALIZE MEDBAY SCANNER", color = Color(0xFFBDC3C7), fontSize = 12.sp)
                }
            }
        }

        Button(
            onClick = {
                if (!isScanning) {
                    isScanning = true
                    scanProgress = 0
                    coroutineScope.launch {
                        while (scanProgress < 100) {
                            delay(100)
                            scanProgress += 5
                        }
                        delay(500)
                        onComplete()
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1ABC9C)),
            enabled = !isScanning,
            modifier = Modifier.fillMaxWidth(0.6f).height(50.dp)
        ) {
            Text("START SCAN", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

// 5. Download Data Minigame
@Composable
fun DownloadDataMinigame(onComplete: () -> Unit) {
    var isDownloading by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(0) }
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceAround
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(Color(0xFF111111), RoundedCornerShape(8.dp))
                .border(2.dp, Color.Gray, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(
                    modifier = Modifier.fillMaxWidth(0.8f),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("📁 Folder A", color = Color.Cyan)
                    Text(if (progress >= 100) "📁 Saved" else "➡", color = Color.White)
                    Text("💻 Admin PC", color = Color.Green)
                }
                Spacer(modifier = Modifier.height(16.dp))
                LinearProgressIndicator(
                    progress = progress / 100f,
                    color = Color.Green,
                    trackColor = Color(0xFF222222),
                    modifier = Modifier.fillMaxWidth(0.8f).height(12.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (progress >= 100) "DOWNLOAD COMPLETE" else if (isDownloading) "DOWNLOADING... $progress%" else "READY TO DOWNLOAD",
                    color = if (progress >= 100) Color.Green else Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Button(
            onClick = {
                if (!isDownloading) {
                    isDownloading = true
                    coroutineScope.launch {
                        while (progress < 100) {
                            delay(120)
                            progress += 4
                        }
                        delay(600)
                        onComplete()
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Green),
            enabled = !isDownloading && progress < 100
        ) {
            Text("DOWNLOAD", color = Color.Black, fontWeight = FontWeight.Bold)
        }
    }
}

// 6. Clear Asteroids Minigame
@Composable
fun ClearAsteroidsMinigame(onComplete: () -> Unit) {
    var destroyedCount by remember { mutableStateOf(0) }
    val requiredCount = 10
    val coroutineScope = rememberCoroutineScope()

    // Simulated flying asteroids
    var asteroidPos1 by remember { mutableStateOf(Offset(50f, 30f)) }
    var asteroidPos2 by remember { mutableStateOf(Offset(180f, 100f)) }

    LaunchedEffect(destroyedCount) {
        if (destroyedCount >= requiredCount) {
            onComplete()
        }
    }

    // Move asteroids on ticks
    LaunchedEffect(Unit) {
        while (destroyedCount < requiredCount) {
            delay(50)
            asteroidPos1 = asteroidPos1.copy(
                x = if (asteroidPos1.x > 320f) 0f else asteroidPos1.x + 8f,
                y = if (asteroidPos1.x > 320f) (20..150).random().toFloat() else asteroidPos1.y
            )
            asteroidPos2 = asteroidPos2.copy(
                x = if (asteroidPos2.x > 320f) 0f else asteroidPos2.x + 12f,
                y = if (asteroidPos2.x > 320f) (20..150).random().toFloat() else asteroidPos2.y
            )
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceAround
    ) {
        Text("SHOOT THE FLYING ASTEROIDS: $destroyedCount / $requiredCount", color = Color.White, fontWeight = FontWeight.Bold)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(Color.Black, RoundedCornerShape(8.dp))
                .border(2.dp, Color(0xFFC61A1A), RoundedCornerShape(8.dp))
        ) {
            // Target Crosshair
            Divider(color = Color(0x33FF0000), thickness = 1.dp, modifier = Modifier.fillMaxHeight().width(1.dp).align(Alignment.Center))
            Divider(color = Color(0x33FF0000), thickness = 1.dp, modifier = Modifier.fillMaxWidth().height(1.dp).align(Alignment.Center))

            // Asteroid 1
            Box(
                modifier = Modifier
                    .offset(asteroidPos1.x.dp, asteroidPos1.y.dp)
                    .size(28.dp)
                    .background(Color(0xFF7F8C8D), CircleShape)
                    .border(1.5.dp, Color(0xFF34495E), CircleShape)
                    .clickable {
                        destroyedCount++
                        asteroidPos1 = Offset(0f, (20..150).random().toFloat())
                    }
            )

            // Asteroid 2
            Box(
                modifier = Modifier
                    .offset(asteroidPos2.x.dp, asteroidPos2.y.dp)
                    .size(22.dp)
                    .background(Color(0xFF95A5A6), CircleShape)
                    .border(1.5.dp, Color(0xFF7F8C8D), CircleShape)
                    .clickable {
                        destroyedCount++
                        asteroidPos2 = Offset(0f, (20..150).random().toFloat())
                    }
            )
        }
    }
}
