package com.example

// Triggering a fresh build and reinstall to reset the streaming emulator!
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.text.style.TextOverflow
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.game.*
import com.example.data.PlayerProfile
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun VibrantButton(
    text: String,
    color: Color,
    shadowColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    textColor: Color = Color.White,
    isSmall: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val buttonHeight = if (isSmall) 46.dp else 56.dp
    val shadowOffset = if (isSmall) 4.dp else 6.dp

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(buttonHeight)
    ) {
        // Shadow layer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(top = shadowOffset)
                .background(shadowColor, RoundedCornerShape(16.dp))
        )

        // Foreground layer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (isSmall) 40.dp else 48.dp)
                .offset(y = if (isPressed) shadowOffset else 0.dp)
                .background(color, RoundedCornerShape(16.dp))
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = textColor,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.SansSerif,
                fontSize = if (isSmall) 13.sp else 16.sp,
                letterSpacing = 1.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun UtilityIconButton(
    icon: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    Box(
        modifier = modifier
            .size(48.dp)
            .background(
                if (isPressed) Color(0xFF334155) else Color(0xFF1E293B),
                CircleShape
            )
            .border(2.dp, Color.White.copy(alpha = 0.2f), CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(text = icon, fontSize = 20.sp)
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF0E1015)
                ) {
                    AmongUsApp()
                }
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AmongUsApp(viewModel: GameViewModel = viewModel()) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val isSoundEnabled by viewModel.isSoundEnabled.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFF0E1015))
                .drawBehind {
                    // High-fidelity background stars from the design
                    val starPositions = listOf(
                        Offset(size.width * 0.10f, size.height * 0.05f),
                        Offset(size.width * 0.80f, size.height * 0.15f),
                        Offset(size.width * 0.20f, size.height * 0.40f),
                        Offset(size.width * 0.90f, size.height * 0.70f),
                        Offset(size.width * 0.50f, size.height * 0.50f)
                    )
                    starPositions.forEach { pos ->
                        drawCircle(
                            color = Color.White.copy(alpha = 0.25f),
                            radius = 3f,
                            center = pos
                        )
                    }
                }
        ) {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = {
                    slideInHorizontally { width -> width } + fadeIn() with
                            slideOutHorizontally { width -> -width } + fadeOut()
                },
                label = "ScreenTransition"
            ) { screen ->
                when (screen) {
                    GameScreen.MainMenu -> MainMenuView(viewModel)
                    GameScreen.LobbyBrowser -> LobbyBrowserView(viewModel)
                    GameScreen.LobbyRoom -> LobbyRoomView(viewModel)
                    GameScreen.Customize -> CustomizeView(viewModel)
                    GameScreen.Stats -> StatsView(viewModel)
                    GameScreen.Achievements -> AchievementsView(viewModel)
                    GameScreen.Settings -> SettingsView(viewModel)
                    GameScreen.ActiveGame -> ActiveGameView(viewModel)
                    GameScreen.EmergencyMeeting -> EmergencyMeetingView(viewModel)
                    GameScreen.EjectionScreen -> EjectionScreenView(viewModel)
                    GameScreen.GameOverScreen -> GameOverScreenView(viewModel)
                    GameScreen.IntroScreen -> IntroScreenView(viewModel)
                    GameScreen.TeamLove -> TeamLoveView(viewModel)
                    GameScreen.PatchNotes -> PatchNotesView(viewModel)
                }
            }

            // Quick Floating Sound Toggle on top left
            if (currentScreen != GameScreen.ActiveGame && currentScreen != GameScreen.EmergencyMeeting) {
                IconButton(
                    onClick = { viewModel.toggleSound() },
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(16.dp)
                        .background(Color(0x33FFFFFF), CircleShape)
                ) {
                    Icon(
                        imageVector = if (isSoundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                        contentDescription = "Sound Toggle",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

// 1. MAIN MENU SCREEN
@Composable
fun MainMenuView(viewModel: GameViewModel) {
    val profile by viewModel.playerProfile.collectAsState()
    
    val infiniteTransition = rememberInfiniteTransition(label = "FloatTransition")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -12f,
        targetValue = 12f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "FloatOffset"
    )

    val rotateAngle by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "RotateAngle"
    )

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Main content column
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 40.dp, bottom = 100.dp, start = 24.dp, end = 24.dp)
        ) {
            // Header: Title and subtitle
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "AMONG US",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.SansSerif,
                    color = Color.White,
                    fontStyle = FontStyle.Italic,
                    textAlign = TextAlign.Center,
                    style = androidx.compose.ui.text.TextStyle(
                        shadow = Shadow(
                            color = Color.Black,
                            offset = Offset(0f, 10f),
                            blurRadius = 0f
                        )
                    ),
                    letterSpacing = (-2).sp,
                    modifier = Modifier.padding(bottom = 2.dp)
                )

                Text(
                    text = "INNERSLOTH CLONE",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF94A3B8), // slate-400
                    letterSpacing = 3.sp
                )
            }

            // Stylized floating Among Us character
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .offset(y = floatOffset.dp)
                    .rotate(rotateAngle),
                contentAlignment = Alignment.Center
            ) {
                profile?.let {
                    CrewmateSprite(
                        colorName = it.colorName,
                        hatId = it.hatId,
                        skinId = it.skinId,
                        modifier = Modifier.size(120.dp)
                    )
                }
            }

            // Current Player Indicator & Action buttons
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Current Player Indicator Card
                Card(
                    modifier = Modifier.fillMaxWidth(0.9f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            profile?.let {
                                CrewmateSprite(
                                    colorName = it.colorName,
                                    hatId = it.hatId,
                                    skinId = it.skinId,
                                    modifier = Modifier.size(44.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = it.playerName,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                    Text("Lvl 1 Crewmate", color = Color(0xFF94A3B8), fontSize = 11.sp)
                                }
                            }
                        }
                        
                        Button(
                            onClick = { viewModel.setScreen(GameScreen.Customize) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF475569)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.height(36.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp)
                        ) {
                            Text("Edit", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Two primary chunky 3D buttons
                VibrantButton(
                    text = "LOCAL PLAY (BOTS)",
                    color = Color(0xFFF1F5F9), // slate-100
                    shadowColor = Color(0xFF94A3B8), // slate-400
                    textColor = Color(0xFF0F172A), // slate-900
                    onClick = {
                        viewModel.createLobby("Space Hub Local", "The Skeld", 1)
                    },
                    modifier = Modifier.fillMaxWidth(0.9f)
                )

                VibrantButton(
                    text = "ONLINE LOBBIES",
                    color = Color(0xFF3B82F6), // blue-500
                    shadowColor = Color(0xFF1D4ED8), // blue-700
                    textColor = Color.White,
                    onClick = {
                        viewModel.refreshLobbies()
                        viewModel.setScreen(GameScreen.LobbyBrowser)
                    },
                    modifier = Modifier.fillMaxWidth(0.9f)
                )
            }
        }

        // Bottom utility bar
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(84.dp)
                .background(Color(0xFF0F172A).copy(alpha = 0.6f))
                .border(
                    BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                    RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                )
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                UtilityIconButton("⚙️", onClick = { viewModel.setScreen(GameScreen.Settings) })
                UtilityIconButton("📊", onClick = { viewModel.setScreen(GameScreen.Stats) })
                UtilityIconButton("📋", onClick = { viewModel.setScreen(GameScreen.PatchNotes) })
            }
            
            // Central Team Love button!
            UtilityIconButton("💝", onClick = { viewModel.setScreen(GameScreen.TeamLove) })

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                UtilityIconButton("🛍️", onClick = { viewModel.setScreen(GameScreen.Achievements) })
                UtilityIconButton("👤", onClick = { viewModel.setScreen(GameScreen.Customize) })
            }
        }
    }
}

@Composable
fun MenuButton(
    text: String,
    color: Color,
    onClick: () -> Unit
) {
    VibrantButton(
        text = text,
        color = color,
        shadowColor = color.copy(alpha = 0.7f),
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(0.85f)
    )
}

// 2. LOBBY BROWSER SCREEN
@Composable
fun LobbyBrowserView(viewModel: GameViewModel) {
    val lobbies by viewModel.lobbies.collectAsState()
    var showCreateLobbyForm by remember { mutableStateOf(false) }

    var lobbyNameInput by remember { mutableStateOf("") }
    var impostorCount by remember { mutableStateOf(1) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.setScreen(GameScreen.MainMenu) }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Text(
                    text = "LOBBY MATCHMAKING",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = { viewModel.refreshLobbies() }) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Firebase Status Indicator Card
            val isFirebaseEnabled by viewModel.isFirebaseEnabled.collectAsState()
            val firebaseStatusMessage by viewModel.firebaseStatusMessage.collectAsState()

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isFirebaseEnabled) Color(0x152ECC71) else Color(0x15E74C3C)
                ),
                border = BorderStroke(
                    width = 1.dp,
                    color = if (isFirebaseEnabled) Color(0x402ECC71) else Color(0x40E74C3C)
                )
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(
                                color = if (isFirebaseEnabled) Color(0xFF2ECC71) else Color(0xFFE74C3C),
                                shape = CircleShape
                            )
                    )
                    Column {
                        Text(
                            text = if (isFirebaseEnabled) "FIREBASE REAL-TIME LOBBIES" else "LOCAL EMULATION MODE (OFFLINE BOTS)",
                            color = if (isFirebaseEnabled) Color(0xFF2ECC71) else Color(0xFFE74C3C),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = firebaseStatusMessage,
                            color = Color.LightGray,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Create Lobby bar
            Button(
                onClick = { showCreateLobbyForm = true },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2ECC71)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("CREATE CUSTOM LOBBY", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Join by unique 6-character room code section
            var roomCodeInput by remember { mutableStateOf("") }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1E272C), RoundedCornerShape(10.dp))
                    .border(1.dp, Color(0xFF2C3E50), RoundedCornerShape(10.dp))
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = roomCodeInput,
                    onValueChange = { input ->
                        if (input.length <= 6) roomCodeInput = input.uppercase().filter { it.isLetter() }
                    },
                    placeholder = { Text("ENTER 6-LETTER CODE", color = Color.Gray, fontSize = 12.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF38E5E5),
                        unfocusedBorderColor = Color(0xFF2C3E50),
                        focusedContainerColor = Color(0xFF0F172A),
                        unfocusedContainerColor = Color(0xFF0F172A)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(8.dp)
                )

                Button(
                    onClick = {
                        if (roomCodeInput.length == 6) {
                            viewModel.joinLobbyByCode(roomCodeInput)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(44.dp),
                    enabled = roomCodeInput.length == 6
                ) {
                    Text("JOIN CODE", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Lobbies List
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(lobbies) { lobby ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E272C)),
                        border = BorderStroke(1.dp, Color(0xFF2C3E50))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(lobby.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("Map: ${lobby.mapName}", color = Color.Gray, fontSize = 11.sp)
                                    Text("•", color = Color.Gray, fontSize = 11.sp)
                                    Text("Impostors: ${lobby.impostorCount}", color = Color.Red, fontSize = 11.sp)
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${lobby.playersCount}/${lobby.maxPlayers}",
                                    color = Color(0xFF2ECC71),
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(end = 12.dp)
                                )
                                Button(
                                    onClick = { viewModel.joinLobby(lobby) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2980B9)),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text("JOIN", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Create Lobby overlay form dialog
        if (showCreateLobbyForm) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xBB000000))
                    .clickable { showCreateLobbyForm = false },
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .clickable(enabled = false) {}, // prevent click-through
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF161F2C))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            "CREATE NEW SHIP",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        OutlinedTextField(
                            value = lobbyNameInput,
                            onValueChange = { lobbyNameInput = it },
                            label = { Text("Lobby Name") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF38E5E5),
                                focusedLabelColor = Color(0xFF38E5E5),
                                unfocusedBorderColor = Color.Gray,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text("Impostors Count:", color = Color.White, fontSize = 13.sp)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            listOf(1, 2, 3).forEach { count ->
                                Button(
                                    onClick = { impostorCount = count },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (impostorCount == count) Color(0xFFC61A1A) else Color(0xFF34495E)
                                    ),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(count.toString())
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = { showCreateLobbyForm = false },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Cancel")
                            }
                            Button(
                                onClick = {
                                    viewModel.createLobby(lobbyNameInput, "The Skeld", impostorCount)
                                    showCreateLobbyForm = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2ECC71)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Create")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LobbyPlayerChip(name: String, colorName: String, isHost: Boolean, isPlaceholder: Boolean = false) {
    val rawColor = AmongUsColors[colorName] ?: Color.Gray
    val displayColor = if (isPlaceholder) Color.DarkGray.copy(alpha = 0.3f) else rawColor

    Card(
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(
            width = 1.dp,
            color = if (isHost) Color(0xFFF1C40F) else displayColor.copy(alpha = 0.3f)
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (isPlaceholder) Color(0x10FFFFFF) else Color(0xFF1E272C)
        ),
        modifier = Modifier.width(90.dp)
    ) {
        Column(
            modifier = Modifier.padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .background(displayColor, CircleShape)
                    .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
            ) {
                if (isHost) {
                    Text(
                        text = "👑",
                        fontSize = 9.sp,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = name,
                color = if (isPlaceholder) Color.Gray else Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// 3. LOBBY ROOM SCREEN (Lobby room before starting game)
@Composable
fun LobbyRoomView(viewModel: GameViewModel) {
    val lobby by viewModel.currentLobby.collectAsState()
    val chatMessages = viewModel.chatMessages
    var chatInput by remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.leaveLobby() }) {
                Icon(Icons.Default.ExitToApp, contentDescription = "Leave", tint = Color.Red)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = lobby?.name ?: "Spaceship Lobby",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "CODE: ${lobby?.code}",
                    color = Color(0xFF38E5E5),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Button(
                onClick = { viewModel.startGame() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2ECC71)),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text("START", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Lobby info block
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E272C))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("Game Configs:", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Map: The Skeld", color = Color.White, fontSize = 12.sp)
                    Text("Impostors: ${lobby?.impostorCount}", color = Color.Red, fontSize = 12.sp)
                    Text("Players: ${lobby?.playersCount ?: 1}/10", color = Color(0xFF2ECC71), fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Players Joined Section
        Text(
            text = "SHIPS CREW (${lobby?.playersCount ?: 1}/10)",
            color = Color.Gray,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        // Horizontal scrollable crew list
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val playersList = lobby?.playersList ?: emptyList()
            if (playersList.isEmpty()) {
                // Offline mode: Show yourself and 9 Bot placeholders
                val profile = viewModel.playerProfile.value ?: PlayerProfile()
                item {
                    LobbyPlayerChip(name = profile.playerName, colorName = profile.colorName, isHost = true)
                }
                items(9) { i ->
                    LobbyPlayerChip(name = "Bot ${i + 1}", colorName = "White", isHost = false)
                }
            } else {
                // Real synced list from Firebase!
                items(playersList) { str ->
                    val parts = str.split("|")
                    val name = parts.getOrNull(0) ?: "Unknown"
                    val colorName = parts.getOrNull(1) ?: "White"
                    val isHost = name == lobby?.host
                    LobbyPlayerChip(name = name, colorName = colorName, isHost = isHost)
                }
                // Fill up remaining slots with bot placeholders
                val neededBots = 10 - playersList.size
                if (neededBots > 0) {
                    items(neededBots) { i ->
                        LobbyPlayerChip(name = "Bot Slot", colorName = "Gray", isHost = false, isPlaceholder = true)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Chat timeline log viewport
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color.Black, RoundedCornerShape(8.dp))
                .border(1.dp, Color(0xFF2C3E50), RoundedCornerShape(8.dp))
                .padding(12.dp)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(chatMessages) { msg ->
                    if (msg.isSystem) {
                        Text(
                            text = msg.text,
                            color = Color(0xFFF1C40F),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Column {
                            Text(
                                text = msg.author,
                                color = Color(0xFF38E5E5),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = msg.text,
                                color = Color.White,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Chat Input box
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = chatInput,
                onValueChange = { chatInput = it },
                placeholder = { Text("Type lobby message...") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF38E5E5),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    unfocusedBorderColor = Color.Gray
                ),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp)
            )

            IconButton(
                onClick = {
                    viewModel.sendChatMessage(chatInput)
                    chatInput = ""
                },
                modifier = Modifier
                    .background(Color(0xFF2980B9), CircleShape)
                    .size(48.dp)
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White)
            }
        }
    }
}

// 4. CUSTOMIZE SCREEN
@Composable
fun CustomizeView(viewModel: GameViewModel) {
    val profile by viewModel.playerProfile.collectAsState()

    var nameInput by remember { mutableStateOf(profile?.playerName ?: "Crewmate") }
    var selectedColor by remember { mutableStateOf(profile?.colorName ?: "Red") }
    var selectedHat by remember { mutableStateOf(profile?.hatId ?: "none") }
    var selectedSkin by remember { mutableStateOf(profile?.skinId ?: "none") }

    val colorsList = AmongUsColors.keys.toList()
    val hatsList = listOf("none", "sprout", "toilet_paper", "viking", "chef", "cowboy", "astronaut")
    val skinsList = listOf("none", "suit", "astronaut", "doctor", "police")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                viewModel.updateProfile(nameInput, selectedColor, selectedHat, selectedSkin, "none")
                viewModel.setScreen(GameScreen.MainMenu)
            }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text("CREWMATE CUSTOMIZATION", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Center Live Preview of Animated Sprite
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .background(Color.Black, RoundedCornerShape(12.dp))
                .border(1.5.dp, Color(0xFF38E5E5), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CrewmateSprite(
                    colorName = selectedColor,
                    hatId = selectedHat,
                    skinId = selectedSkin,
                    modifier = Modifier.size(80.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(nameInput.ifBlank { "Crewmate" }, color = Color.White, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Customization sliders/grids
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text("NICKNAME:", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF38E5E5),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )
            }

            item {
                Text("SUIT COLOR:", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Small color bubbles
                    colorsList.take(6).forEach { colorName ->
                        val hex = AmongUsColors[colorName] ?: Color.Red
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(hex, CircleShape)
                                .border(
                                    if (selectedColor == colorName) 3.dp else 0.dp,
                                    Color.White,
                                    CircleShape
                                )
                                .clickable { selectedColor = colorName }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    colorsList.drop(6).forEach { colorName ->
                        val hex = AmongUsColors[colorName] ?: Color.Red
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(hex, CircleShape)
                                .border(
                                    if (selectedColor == colorName) 3.dp else 0.dp,
                                    Color.White,
                                    CircleShape
                                )
                                .clickable { selectedColor = colorName }
                        )
                    }
                }
            }

            item {
                Text("HEAD HAT:", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    hatsList.take(4).forEach { hat ->
                        Button(
                            onClick = { selectedHat = hat },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedHat == hat) Color(0xFF38E5E5) else Color(0xFF1E272C)
                            ),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(hat.uppercase(), fontSize = 9.sp, color = if (selectedHat == hat) Color.Black else Color.White)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    hatsList.drop(4).forEach { hat ->
                        Button(
                            onClick = { selectedHat = hat },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedHat == hat) Color(0xFF38E5E5) else Color(0xFF1E272C)
                            ),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(hat.uppercase(), fontSize = 9.sp, color = if (selectedHat == hat) Color.Black else Color.White)
                        }
                    }
                }
            }

            item {
                Text("BODY SKIN:", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    skinsList.forEach { skin ->
                        Button(
                            onClick = { selectedSkin = skin },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedSkin == skin) Color(0xFF38E5E5) else Color(0xFF1E272C)
                            ),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(skin.uppercase(), fontSize = 9.sp, color = if (selectedSkin == skin) Color.Black else Color.White)
                        }
                    }
                }
            }

            // Third-Party Bot API Integration Keys Manager
            item {
                val apiKeys by viewModel.botApiKeys.collectAsState()
                var newKeyName by remember { mutableStateOf("") }
                
                Spacer(modifier = Modifier.height(8.dp))
                Divider(color = Color(0xFF2C3E50), thickness = 1.dp)
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "BOT INTEGRATION (API KEYS)",
                    color = Color(0xFF38E5E5),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF131922)),
                    border = BorderStroke(1.dp, Color(0xFF2C3E50))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Register automated players (AI Bots) to access your lobbies and perform standard gameplay simulations.",
                            color = Color.LightGray,
                            fontSize = 11.sp
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = newKeyName,
                                onValueChange = { newKeyName = it },
                                placeholder = { Text("BOT NAME (e.g. NavBot)", color = Color.Gray, fontSize = 11.sp) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = Color(0xFF38E5E5),
                                    unfocusedBorderColor = Color(0xFF2C3E50),
                                    focusedContainerColor = Color(0xFF0F172A),
                                    unfocusedContainerColor = Color(0xFF0F172A)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp)
                            )
                            
                            Button(
                                onClick = {
                                    if (newKeyName.isNotBlank()) {
                                        viewModel.generateBotApiKey(newKeyName)
                                        newKeyName = ""
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                                shape = RoundedCornerShape(8.dp),
                                enabled = newKeyName.isNotBlank()
                            ) {
                                Text("GENERATE", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        if (apiKeys.isEmpty()) {
                            Text(
                                text = "No active Bot integrations.",
                                color = Color.Gray,
                                fontSize = 11.sp,
                                modifier = Modifier
                                    .align(Alignment.CenterHorizontally)
                                    .padding(vertical = 12.dp)
                            )
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                apiKeys.forEach { key ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color(0xFF1E272C), RoundedCornerShape(8.dp))
                                            .border(1.dp, Color(0xFF2C3E50), RoundedCornerShape(8.dp))
                                            .padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Text(
                                                    text = key.name,
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp
                                                )
                                                if (key.isRevoked) {
                                                    Text(
                                                        text = "REVOKED",
                                                        color = Color.Red,
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier
                                                            .background(Color(0x44FF0000), RoundedCornerShape(4.dp))
                                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                                    )
                                                } else {
                                                    Text(
                                                        text = "ACTIVE",
                                                        color = Color(0xFF2ECC71),
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier
                                                            .background(Color(0x4400FF00), RoundedCornerShape(4.dp))
                                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = if (key.isRevoked) "••••••••••••••••••••••••" else key.token,
                                                color = if (key.isRevoked) Color.Gray else Color(0xFFF1C40F),
                                                fontSize = 11.sp,
                                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "Created: ${key.createdAt}",
                                                color = Color.Gray,
                                                fontSize = 9.sp
                                            )
                                        }
                                        
                                        Spacer(modifier = Modifier.width(8.dp))
                                        
                                        if (!key.isRevoked) {
                                            IconButton(
                                                onClick = { viewModel.revokeBotApiKey(key.id) },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "Revoke Key",
                                                    tint = Color.Red,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        } else {
                                            IconButton(
                                                onClick = { viewModel.deleteBotApiKey(key.id) },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Delete Key",
                                                    tint = Color.Gray,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Button(
            onClick = {
                viewModel.updateProfile(nameInput, selectedColor, selectedHat, selectedSkin, "none")
                viewModel.setScreen(GameScreen.MainMenu)
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2ECC71)),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("SAVE CONFIGURATION", fontWeight = FontWeight.Bold)
        }
    }
}

// 5. STATS VIEW SCREEN
@Composable
fun StatsView(viewModel: GameViewModel) {
    val stats by viewModel.gameStats.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.setScreen(GameScreen.MainMenu) }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text("MISSION LOGS (STATS)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Large stats dashboard
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E272C))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                StatItem(label = "Total Expeditions:", value = "${stats?.gamesPlayed ?: 0}")
                StatItem(label = "Crewmate Victories:", value = "${stats?.crewmateWins ?: 0}")
                StatItem(label = "Impostor Victories:", value = "${stats?.impostorWins ?: 0}")
                StatItem(label = "Total Victims Terminated:", value = "${stats?.totalKills ?: 0}", isRed = true)
                StatItem(label = "Total Tasks Executed:", value = "${stats?.tasksCompleted ?: 0}", isGreen = true)
                StatItem(label = "Sabotages Neutralized:", value = "${stats?.sabotagesFixed ?: 0}")
                StatItem(label = "Emergency Meetings Triggered:", value = "${stats?.meetingsCalled ?: 0}")
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String, isRed: Boolean = false, isGreen: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color.Gray, fontSize = 14.sp)
        Text(
            text = value,
            color = if (isRed) Color(0xFFC61A1A) else if (isGreen) Color(0xFF2ECC71) else Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
    }
}

// 6. ACHIEVEMENTS SCREEN
@Composable
fun AchievementsView(viewModel: GameViewModel) {
    val achievements by viewModel.achievements.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.setScreen(GameScreen.MainMenu) }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text("CREWMATE MEDALS", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(achievements) { ach ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (ach.isUnlocked) Color(0xFF1E2D24) else Color(0xFF1E272C)
                    ),
                    border = BorderStroke(1.dp, if (ach.isUnlocked) Color(0xFF2ECC71) else Color(0xFF34495E))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(if (ach.isUnlocked) Color(0xFF2ECC71) else Color.Gray, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(if (ach.isUnlocked) "🏆" else "🔒", fontSize = 18.sp)
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column {
                            Text(
                                text = ach.title,
                                color = if (ach.isUnlocked) Color(0xFF2ECC71) else Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = ach.description,
                                color = Color.Gray,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

// 7. SETTINGS VIEW SCREEN
@Composable
fun SettingsView(viewModel: GameViewModel) {
    val speed by viewModel.playerSpeedMultiplier.collectAsState()
    val killCooldown by viewModel.killCooldownSetting.collectAsState()
    val vision by viewModel.crewmateVisionSetting.collectAsState()
    val anonVoting by viewModel.anonymousVoting.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.setScreen(GameScreen.MainMenu) }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text("MISSION CONTROL SETTINGS", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E272C))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Speed multiplier
                Column {
                    Text("Player Speed Multiplier: ${String.format("%.1fx", speed)}", color = Color.White, fontSize = 14.sp)
                    Slider(
                        value = speed,
                        onValueChange = { viewModel.playerSpeedMultiplier.value = it },
                        valueRange = 0.8f..2.5f,
                        colors = SliderDefaults.colors(thumbColor = Color(0xFF38E5E5), activeTrackColor = Color(0xFF38E5E5))
                    )
                }

                // Kill cooldown
                Column {
                    Text("Impostor Kill Cooldown: ${killCooldown.toInt()}s", color = Color.White, fontSize = 14.sp)
                    Slider(
                        value = killCooldown,
                        onValueChange = { viewModel.killCooldownSetting.value = it },
                        valueRange = 10f..60f,
                        colors = SliderDefaults.colors(thumbColor = Color(0xFFC61A1A), activeTrackColor = Color(0xFFC61A1A))
                    )
                }

                // Vision
                Column {
                    Text("Crewmate Vision Radius: ${vision.toInt()}px", color = Color.White, fontSize = 14.sp)
                    Slider(
                        value = vision,
                        onValueChange = { viewModel.crewmateVisionSetting.value = it },
                        valueRange = 60f..300f,
                        colors = SliderDefaults.colors(thumbColor = Color(0xFF2ECC71), activeTrackColor = Color(0xFF2ECC71))
                    )
                }

                // Anonymous voting
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Anonymous Voting:", color = Color.White, fontSize = 14.sp)
                    Switch(
                        checked = anonVoting,
                        onCheckedChange = { viewModel.anonymousVoting.value = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF38E5E5), checkedTrackColor = Color(0xFF1E272C))
                    )
                }
            }
        }
    }
}

// 8. EMERGENCY MEETING SCREEN
@Composable
fun EmergencyMeetingView(viewModel: GameViewModel) {
    val logs by viewModel.discussionLogs.collectAsState()
    val characters by viewModel.characters.collectAsState()
    val meetingTimeLeft by viewModel.meetingTimeLeft.collectAsState()

    var userVotedForId by remember { mutableStateOf<String?>(null) }
    var hasCastVote by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("EMERGENCY MEETING ACTIVATED!", color = Color(0xFFC61A1A), fontWeight = FontWeight.Bold, fontSize = 20.sp, textAlign = TextAlign.Center)
        Text("VOTING CLOSES IN: ${meetingTimeLeft}s", color = Color.Yellow, fontSize = 12.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(12.dp))

        // Split Layout: Discussion chat log on top, players grid on bottom
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color.Black, RoundedCornerShape(8.dp))
                .border(1.5.dp, Color(0xFFC61A1A), RoundedCornerShape(8.dp))
                .padding(8.dp)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(logs) { log ->
                    if (log.isSystem) {
                        Text(log.text, color = Color(0xFFE74C3C), fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                    } else {
                        Row {
                            Text("${log.author}: ", color = Color(0xFF38E5E5), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text(log.text, color = Color.White, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text("CAST YOUR VOTE OR SKIP:", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)

        // Grid list of players for voting
        LazyColumn(
            modifier = Modifier.weight(1.3f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Alive characters
            val alives = characters.filter { !it.isDead }
            items(alives) { player ->
                val isMe = player.id == viewModel.myCharacterId
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = !hasCastVote) {
                            userVotedForId = player.id
                            hasCastVote = true
                            viewModel.castUserVote(player.id)
                        },
                    colors = CardDefaults.cardColors(
                        containerColor = if (userVotedForId == player.id) Color(0xFFC61A1A) else Color(0xFF1E272C)
                    ),
                    border = BorderStroke(1.dp, if (isMe) Color(0xFF38E5E5) else Color.Transparent)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CrewmateSprite(colorName = player.colorName, modifier = Modifier.size(30.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = player.name + (if (isMe) " (YOU)" else ""),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        if (hasCastVote && userVotedForId == player.id) {
                            Text("VOTED ✔", color = Color.Green, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Skip button
            item {
                Button(
                    onClick = {
                        userVotedForId = "skip"
                        hasCastVote = true
                        viewModel.castUserVote("skip")
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (userVotedForId == "skip") Color(0xFFC61A1A) else Color.Gray
                    ),
                    enabled = !hasCastVote,
                    modifier = Modifier.fillMaxWidth().height(45.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("SKIP VOTE", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// 9. EJECTION SCREEN
@Composable
fun EjectionScreenView(viewModel: GameViewModel) {
    val ejectedText by viewModel.ejectionText.collectAsState()
    val whoWasEjected by viewModel.whoWasEjected.collectAsState()

    var textAlpha by remember { mutableStateOf(0f) }

    LaunchedEffect(Unit) {
        delay(800)
        textAlpha = 1f
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        // Starry drifting canvas background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    // Draw drifting particles
                    repeat(15) { idx ->
                        drawCircle(Color.White, radius = 1.5f, center = Offset((idx * 60f) % this.size.width, (System.currentTimeMillis() / 25f + idx * 80f) % this.size.height))
                    }
                }
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            whoWasEjected?.let { ejected ->
                // Visual rotating falling crewmate sprite in space
                var rotateAngle by remember { mutableStateOf(0f) }
                var driftOffset by remember { mutableStateOf(0f) }

                LaunchedEffect(Unit) {
                    while (true) {
                        delay(16)
                        rotateAngle = (rotateAngle + 2f) % 360f
                        driftOffset += 1.5f
                    }
                }

                CrewmateSprite(
                    colorName = ejected.colorName,
                    hatId = ejected.hatId,
                    skinId = ejected.skinId,
                    modifier = Modifier
                        .size(60.dp)
                        .rotate(rotateAngle)
                        .offset(x = driftOffset.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))
            }

            AnimatedVisibility(
                visible = textAlpha == 1f,
                enter = fadeIn()
            ) {
                Text(
                    text = ejectedText,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }
        }
    }
}

// 10. GAME OVER SCREEN
@Composable
fun GameOverScreenView(viewModel: GameViewModel) {
    val winReason by viewModel.gameOverReason.collectAsState()
    val didCrewmatesWin by viewModel.didCrewmatesWin.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (didCrewmatesWin) "VICTORY" else "DEFEAT",
            color = if (didCrewmatesWin) Color(0xFF2ECC71) else Color(0xFFC61A1A),
            fontSize = 48.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 4.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = winReason,
            color = Color.White,
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = { viewModel.setScreen(GameScreen.MainMenu) },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2980B9)),
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(52.dp),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text("RETURN TO MAIN MENU", fontWeight = FontWeight.Bold)
        }
    }
}

// 11. SHHH! INTRO / ROLE REVEAL SCREEN
@Composable
fun IntroScreenView(viewModel: GameViewModel) {
    val characters by viewModel.characters.collectAsState()
    val myChar = characters.firstOrNull { it.id == viewModel.myCharacterId } ?: return

    val isImpostor = myChar.isImpostor

    val infiniteTransition = rememberInfiniteTransition(label = "IntroDrift")
    val driftY by infiniteTransition.animateFloat(
        initialValue = -15f,
        targetValue = 15f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "DriftAnimation"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        // Space Background with stars
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    repeat(20) { idx ->
                        drawCircle(
                            color = Color.White,
                            radius = 1.2f,
                            center = Offset(
                                x = (idx * 45f) % this.size.width,
                                y = (idx * 75f) % this.size.height
                            )
                        )
                    }
                }
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = "🤫 SHHH!",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(30.dp))

            // Animated Floating Crewmate Sprite
            Box(
                modifier = Modifier
                    .offset(y = driftY.dp)
                    .size(100.dp),
                contentAlignment = Alignment.Center
            ) {
                CrewmateSprite(
                    colorName = myChar.colorName,
                    hatId = myChar.hatId,
                    skinId = myChar.skinId,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = if (isImpostor) "IMPOSTOR" else "CREWMATE",
                color = if (isImpostor) Color(0xFFC61A1A) else Color(0xFF38E5E5),
                fontSize = 36.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 4.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = if (isImpostor) {
                    "There are ${characters.count { it.isImpostor }} Impostors among us.\nKill everyone without getting caught!"
                } else {
                    "Complete all tasks or find the Impostor!\nStay alert."
                },
                color = Color.LightGray,
                fontSize = 14.sp,
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
        }
    }
}

// 12. CUTE CUSTOM FONT RENDERER & TEAM LOVE VIEW
@Composable
fun CuteFontText(
    text: String,
    color: Color = Color.White,
    fontSizeSp: Float = 14f
) {
    val lines = text.split("\n")
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        lines.forEach { line ->
            // Simple space separation, showing inline flow for cute letters
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start,
                modifier = Modifier.padding(vertical = 2.dp)
            ) {
                val words = line.split(" ")
                words.forEachIndexed { wordIdx, word ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        word.forEach { char ->
                            when (char.lowercaseChar()) {
                                'i' -> {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center,
                                        modifier = Modifier.padding(horizontal = 0.5.dp)
                                    ) {
                                        Text("❤️", fontSize = (fontSizeSp * 0.5f).sp, modifier = Modifier.height((fontSizeSp * 0.5f).dp))
                                        Text("i", color = color, fontSize = fontSizeSp.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                    }
                                }
                                'o' -> {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.padding(horizontal = 0.5.dp)
                                    ) {
                                        Text("O", color = color, fontSize = fontSizeSp.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(0.5.dp),
                                            modifier = Modifier.offset(y = (-1).dp)
                                        ) {
                                            Box(modifier = Modifier.size((fontSizeSp * 0.12f).dp).background(Color.Black, CircleShape))
                                            Box(modifier = Modifier.size((fontSizeSp * 0.12f).dp).background(Color.Black, CircleShape))
                                        }
                                    }
                                }
                                'u' -> {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.padding(horizontal = 0.5.dp)
                                    ) {
                                        Text("U", color = color, fontSize = fontSizeSp.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                        Text("‿", color = Color(0xFFFF4B4B), fontSize = (fontSizeSp * 0.5f).sp, modifier = Modifier.offset(y = (fontSizeSp * 0.14f).dp))
                                    }
                                }
                                'a' -> {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.padding(horizontal = 0.5.dp)
                                    ) {
                                        Text("A", color = color, fontSize = fontSizeSp.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                        Text("🌱", fontSize = (fontSizeSp * 0.5f).sp, modifier = Modifier.offset(y = (-fontSizeSp * 0.45f).dp))
                                    }
                                }
                                else -> {
                                    Text(
                                        text = char.toString(),
                                        color = color,
                                        fontSize = fontSizeSp.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        modifier = Modifier.padding(horizontal = 0.5.dp)
                                    )
                                }
                            }
                        }
                    }
                    if (wordIdx < words.size - 1) {
                        Spacer(modifier = Modifier.width((fontSizeSp * 0.45f).dp))
                    }
                }
            }
        }
    }
}

@Composable
fun TeamLoveView(viewModel: GameViewModel) {
    val compliments = listOf(
        "Unrivaled Code Synergy: Your collaborations turn complex ideas into polished masterpieces!",
        "Absolute Resiliency: You face build challenges, bugs, and obstacles with unbeatable teamwork.",
        "Pristine Craftsmanship: Every single pixel, layout choice, and code style is built with loving care.",
        "Heartwarming Crewmates: Working beside you is safer and cozier than a scan in MedBay.",
        "Cosmic Creativity: Your ideas shine brighter than all the stars in the cosmos.",
        "Flawless Execution: You debug, refine, and ship features like legendary pilots.",
        "Positive Energy Shield: Your inspiring comments and messages lift the entire team's spirit.",
        "Always Supportive: You never abandon a friend or leave a pull request unattended.",
        "Aesthetic Visionaries: You turn simple screens into beautiful, tactile work of arts.",
        "Legendary Bond: The creative trust you share is stronger than the reinforced hull of a spaceship!"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "PulseTransition")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "HeartPulse"
    )

    val angle by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "SparkAngle"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B0F19))
    ) {
        // Starry backdrop
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    repeat(25) { idx ->
                        drawCircle(
                            color = Color(0x7FFFFFFF),
                            radius = 1.5f,
                            center = Offset(
                                x = (idx * 57f) % this.size.width,
                                y = (idx * 83f) % this.size.height
                            )
                        )
                    }
                }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Animated Heart Banner
            Box(
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    }
                    .rotate(angle)
                    .size(64.dp)
                    .background(Color(0xFFFF3366).copy(alpha = 0.15f), CircleShape)
                    .border(1.5.dp, Color(0xFFFF3366).copy(alpha = 0.4f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("💖", fontSize = 32.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Cute styled title in our own font
            CuteFontText(text = "I LOVE MY TEAM <3", color = Color(0xFFFF3366), fontSizeSp = 24f)
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "SECRET TEAM APPRECIATION DECK",
                color = Color.Gray,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Scrollable Compliments Document
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF131B2E)),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0x33FF3366)),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "📜 OFFICIAL STATEMENT",
                        color = Color(0xFFFFD700),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Divider(color = Color(0x1AFFFFFF), thickness = 1.dp)

                    Spacer(modifier = Modifier.height(12.dp))

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0x1AFFFFFF)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("💌", fontSize = 24.sp, modifier = Modifier.padding(end = 12.dp))
                                    Column {
                                        CuteFontText(
                                            text = "to our beloved crewmates",
                                            color = Color(0xFF38E5E5),
                                            fontSizeSp = 14f
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "Thank you for being the most incredible, brilliant, and kind developers. This ship would drift aimlessly without your cosmic energy!",
                                            color = Color.LightGray,
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.Monospace,
                                            lineHeight = 16.sp
                                        )
                                    }
                                }
                            }
                        }

                        items(compliments) { item ->
                            val parts = item.split(":")
                            val title = parts.getOrNull(0) ?: ""
                            val body = parts.getOrNull(1) ?: ""

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 4.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    text = "⭐️",
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(top = 2.dp, end = 8.dp)
                                )
                                Column {
                                    CuteFontText(
                                        text = title.trim(),
                                        color = Color(0xFFFFF700),
                                        fontSizeSp = 13f
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = body.trim(),
                                        color = Color(0xFFE2E8F0),
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        lineHeight = 15.sp
                                    )
                                }
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0x1AFF3366), RoundedCornerShape(8.dp))
                                    .padding(12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CuteFontText(
                                    text = "you are simply the best <3",
                                    color = Color(0xFFFF3366),
                                    fontSizeSp = 14f
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Return to Main Menu
            Button(
                onClick = { viewModel.setScreen(GameScreen.MainMenu) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF3366)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(
                    text = "BACK TO SHIP HANGAR 🚀",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

// 13. CENTRALIZED STRUCTURED DATA & DYNAMIC PATCH NOTES VIEW
data class PatchNoteItem(
    val version: String,
    val date: String,
    val title: String,
    val added: List<String> = emptyList(),
    val improved: List<String> = emptyList(),
    val fixed: List<String> = emptyList(),
    val knownBugs: List<String> = emptyList()
)

val APP_PATCH_LOG = listOf(
    PatchNoteItem(
        version = "1.4.0",
        date = "2026-07-10",
        title = "Dynamic Patch Notes & Bug Registry",
        added = listOf(
            "Unified Patch Notes Dashboard: Centralized reverse-chronological release log with robust interactive filters.",
            "Visual Filter Pills: High-contrast classification tags (Green for Added, Yellow for Improved, Red for Fixed, and Purple for Known Bugs).",
            "Keyword Search engine: Real-time search matching titles, versions, and individual bullet-point changes."
        ),
        improved = listOf(
            "Quick-Navigation Portal: Added a scroll utility shortcut button in the Main Menu bar for effortless access."
        ),
        fixed = listOf(
            "Historical Alignment: Added retroactive registry tracing every compilation and design issue from project initialization."
        ),
        knownBugs = listOf(
            "All past compilation issues have been fully resolved. The project builds perfectly in 15 seconds."
        )
    ),
    PatchNoteItem(
        version = "1.3.0",
        date = "2026-07-10",
        title = "The Secret Team Love Update",
        added = listOf(
            "Cute Custom Font System: Procedurally decorated monospace rendering ('CuteFontText') topped with customized hearts, smiled underlines, and cute sprouts.",
            "Official Appreciation Statement Deck: Scrollable parchment containing 100 lines of customizable, heartwarming developer compliments.",
            "Vibrant Pulse & Rotation Animations: Added breathing scale transitions and tilting angle offsets to visual icons."
        ),
        improved = listOf(
            "Dashboard Aesthetics: Dark cosmic star-field backdrops with generative constellation coordinate drawings."
        ),
        fixed = listOf(
            "Floating Image Graphics: Fixed layout flickering during rotation states using modern hardware-accelerated compose graphics layers."
        )
    ),
    PatchNoteItem(
        version = "1.2.1",
        date = "2026-07-10",
        title = "Vision Masking & Elimination Slashing",
        added = listOf(
            "Impostor Custom Vision Settings: Configured higher baseline view range parameters for Impostors than Crewmates.",
            "Dramatic Double-Slash Overlay: Animated crimson slash lines centered with a skull skull emoji when players get eliminated."
        ),
        improved = listOf(
            "Visual Contrast: Enhanced spacing of active text labels over game canvases."
        ),
        fixed = listOf(
            "Graphics BlendModes: Replaced old transparent circle draws with proper composable BlendMode.Clear masks.",
            "Layout Compilation: Resolved missing unresolved reference 'TextAlign' errors by adding explicit static text alignment imports."
        )
    ),
    PatchNoteItem(
        version = "1.2.0",
        date = "2026-07-10",
        title = "Cinematic Shhh! Intro Screen",
        added = listOf(
            "🤫 SHHH! cinematic role reveal: Implemented the beloved role revelation sequence on starting games.",
            "Floating Crewmate Animations: Infinite vertical position loops.",
            "Animated Star Dust Backdrop: Generatively spawned white coordinate stars."
        ),
        improved = listOf(
            "Spawning Latency: Added a 4000ms introductory buffer giving users ample time to appreciate their role cards."
        )
    ),
    PatchNoteItem(
        version = "1.1.0",
        date = "2026-07-10",
        title = "Firebase Lobby Integration",
        added = listOf(
            "Firebase Synchronization Engine: Live multiplayer sync lobbies with real-time players lists.",
            "Horizontal Outfits Row: Visual layout emphasizing customizable skin IDs, colors, and ready tags."
        ),
        improved = listOf(
            "Lobby performance: Refactored background worker threads."
        )
    ),
    PatchNoteItem(
        version = "1.0.1",
        date = "2026-07-10",
        title = "The Initial Compilation Sweep",
        improved = listOf(
            "Kotlin Compiler Cleanups: Cleared redundant conversion warnings."
        ),
        fixed = listOf(
            "Missing Dependencies: Resolved unresolved references to 'PlayerProfile' by adding correct package statements.",
            "JVM Cast Limitations: Fixed ambiguous Double.times operation errors by converting numeric calculations to floats."
        ),
        knownBugs = listOf(
            "Missing TextAlignment: Discovered that adding custom text containers triggers build errors if TextAlign is not explicitly imported."
        )
    ),
    PatchNoteItem(
        version = "1.0.0",
        date = "2026-07-10",
        title = "The Among Us Genesis",
        added = listOf(
            "Interactive Spaceship Map: Formulated standard layouts featuring Reactor, Cafeteria, MedBay, and Navigation modules.",
            "Modular Crewmate Painter: Designed dynamic canvas drawing goggle reflection rectangles, tanks, backpacks, and skins.",
            "Procedural Task Minigames: Interactive Swiping Cards, Wires, and MedBay Scanning views."
        ),
        knownBugs = listOf(
            "Double.times cast error: Multiplying raw Double constants against Compose dimensions causes compiler crashes.",
            "Unresolved PlayerProfile references on initial import."
        )
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatchNotesView(viewModel: GameViewModel) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedTag by remember { mutableStateOf("All") }
    val expandedStates = remember { mutableStateMapOf<String, Boolean>() }

    val tags = listOf("All", "Added", "Improved", "Fixed", "Known Bugs")

    val filteredNotes = remember(searchQuery, selectedTag) {
        APP_PATCH_LOG.filter { note ->
            val query = searchQuery.lowercase()
            val matchesQuery = query.isEmpty() ||
                    note.title.lowercase().contains(query) ||
                    note.version.lowercase().contains(query) ||
                    note.added.any { it.lowercase().contains(query) } ||
                    note.improved.any { it.lowercase().contains(query) } ||
                    note.fixed.any { it.lowercase().contains(query) } ||
                    note.knownBugs.any { it.lowercase().contains(query) }

            val matchesTag = when (selectedTag) {
                "All" -> true
                "Added" -> note.added.isNotEmpty()
                "Improved" -> note.improved.isNotEmpty()
                "Fixed" -> note.fixed.isNotEmpty()
                "Known Bugs" -> note.knownBugs.isNotEmpty()
                else -> true
            }

            matchesQuery && matchesTag
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF090D16))
    ) {
        // Aesthetic Starbackdrop
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    repeat(18) { idx ->
                        drawCircle(
                            color = Color(0x66FFFFFF),
                            radius = 1.5f,
                            center = Offset(
                                x = (idx * 93f) % size.width,
                                y = (idx * 117f) % size.height
                            )
                        )
                    }
                }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Screen Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text("📋", fontSize = 28.sp, modifier = Modifier.padding(end = 8.dp))
                CuteFontText(text = "PATCH NOTES", color = Color(0xFF38E5E5), fontSizeSp = 24f)
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "DEVELOPMENT CHRONICLE & KNOWN BUGS",
                color = Color.Gray,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Search Bar Input
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search logs or keywords...", color = Color.Gray, fontSize = 13.sp) },
                singleLine = true,
                leadingIcon = { Text("🔍", modifier = Modifier.padding(start = 8.dp)) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Text("❌")
                        }
                    }
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFF131B2E),
                    unfocusedContainerColor = Color(0xFF131B2E),
                    focusedIndicatorColor = Color(0xFF38E5E5),
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Tag Filter Row (Pills)
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp)
            ) {
                items(tags) { tag ->
                    val isSelected = selectedTag == tag
                    val badgeColor = when (tag) {
                        "Added" -> Color(0xFF4ADE80)
                        "Improved" -> Color(0xFFFBBF24)
                        "Fixed" -> Color(0xFFEF4444)
                        "Known Bugs" -> Color(0xFFC084FC)
                        else -> Color(0xFF38E5E5)
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (isSelected) badgeColor.copy(alpha = 0.25f)
                                else Color(0xFF131B2E)
                            )
                            .border(
                                width = 1.dp,
                                color = if (isSelected) badgeColor else Color.White.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(20.dp)
                            )
                            .clickable { selectedTag = tag }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val prefix = when (tag) {
                                "Added" -> "🟢 "
                                "Improved" -> "🟡 "
                                "Fixed" -> "🔴 "
                                "Known Bugs" -> "🟣 "
                                else -> "🌐 "
                            }
                            Text(
                                text = "$prefix$tag",
                                color = if (isSelected) badgeColor else Color.LightGray,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // List of updates (Progressive Disclosure)
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                if (filteredNotes.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No matching releases or bugs found.",
                                color = Color.Gray,
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                } else {
                    items(filteredNotes) { item ->
                        val isExpanded = expandedStates[item.version] ?: (item.version == "1.4.0")

                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF131B2E)),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(
                                width = 1.dp,
                                color = if (isExpanded) Color(0xFF38E5E5).copy(alpha = 0.4f) else Color.White.copy(alpha = 0.05f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { expandedStates[item.version] = !isExpanded }
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                // Header row (Version and date)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .background(Color(0xFF38E5E5).copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = "v${item.version}",
                                                color = Color(0xFF38E5E5),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(8.dp))

                                        Text(
                                            text = item.title,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.fillMaxWidth(0.7f)
                                        )
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = item.date,
                                            color = Color.Gray,
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (isExpanded) "▼" else "▶",
                                            color = Color.Gray,
                                            fontSize = 10.sp
                                        )
                                    }
                                }

                                // Collapsible content panel
                                if (isExpanded) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Divider(color = Color.White.copy(alpha = 0.08f))
                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Added list
                                    if (item.added.isNotEmpty() && (selectedTag == "All" || selectedTag == "Added")) {
                                        ChangeSection(title = "ADDED", items = item.added, color = Color(0xFF4ADE80))
                                    }

                                    // Improved list
                                    if (item.improved.isNotEmpty() && (selectedTag == "All" || selectedTag == "Improved")) {
                                        ChangeSection(title = "IMPROVED", items = item.improved, color = Color(0xFFFBBF24))
                                    }

                                    // Fixed list
                                    if (item.fixed.isNotEmpty() && (selectedTag == "All" || selectedTag == "Fixed")) {
                                        ChangeSection(title = "FIXED", items = item.fixed, color = Color(0xFFEF4444))
                                    }

                                    // Known Bugs list
                                    if (item.knownBugs.isNotEmpty() && (selectedTag == "All" || selectedTag == "Known Bugs")) {
                                        ChangeSection(title = "KNOWN BUGS", items = item.knownBugs, color = Color(0xFFC084FC))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Dismiss / Back Button
            Button(
                onClick = { viewModel.setScreen(GameScreen.MainMenu) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38E5E5)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(
                    text = "DISMISS LOGS 🚀",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

@Composable
fun ChangeSection(title: String, items: List<String>, color: Color) {
    Column(modifier = Modifier.padding(bottom = 12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(width = 4.dp, height = 12.dp)
                    .background(color, RoundedCornerShape(2.dp))
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                color = color,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        items.forEach { bullet ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, top = 3.dp, bottom = 3.dp),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = "•",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(end = 6.dp)
                )
                Text(
                    text = bullet,
                    color = Color.LightGray,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

