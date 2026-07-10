package com.example.game

import android.app.Application
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.AppRepository
import com.example.data.GameStats
import com.example.data.PlayerProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class GameViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application, viewModelScope)
    private val repository = AppRepository(database.appDao())

    val playerProfile: StateFlow<PlayerProfile?> = repository.playerProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PlayerProfile())

    val gameStats: StateFlow<GameStats?> = repository.gameStats
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), GameStats())

    val achievements = repository.achievements
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Bot Integration API Keys
    private val _botApiKeys = MutableStateFlow<List<BotApiKey>>(emptyList())
    val botApiKeys: StateFlow<List<BotApiKey>> = _botApiKeys.asStateFlow()

    // UI Navigation State
    private val _currentScreen = MutableStateFlow(GameScreen.MainMenu)
    val currentScreen: StateFlow<GameScreen> = _currentScreen.asStateFlow()

    // Sound Options & Settings
    private val _isSoundEnabled = MutableStateFlow(true)
    val isSoundEnabled = _isSoundEnabled.asStateFlow()

    // Configurable Game Settings
    val playerSpeedMultiplier = MutableStateFlow(1.2f)
    val killCooldownSetting = MutableStateFlow(25f)
    val crewmateVisionSetting = MutableStateFlow(120f) // radius in pixels
    val impostorVisionSetting = MutableStateFlow(250f)
    val anonymousVoting = MutableStateFlow(false)
    val confirmEjects = MutableStateFlow(true)
    val emergencyMeetingsLimit = MutableStateFlow(2)

    // Matchmaking / Lobbies List
    private val _lobbies = MutableStateFlow<List<LobbyInfo>>(emptyList())
    val lobbies = _lobbies.asStateFlow()

    private val _currentLobby = MutableStateFlow<LobbyInfo?>(null)
    val currentLobby = _currentLobby.asStateFlow()

    // Game Running State
    private val _characters = MutableStateFlow<List<GameCharacter>>(emptyList())
    val characters = _characters.asStateFlow()

    private val _deadBodies = MutableStateFlow<List<DeadBody>>(emptyList())
    val deadBodies = _deadBodies.asStateFlow()

    private val _activeSabotage = MutableStateFlow<ActiveSabotage>(ActiveSabotage(ActiveSabotageType.None, 0))
    val activeSabotage = _activeSabotage.asStateFlow()

    // Chat log inside lobby/game
    val chatMessages = mutableStateListOf<ChatMessage>()

    // Local user references
    var myCharacterId: String = "player_1"
    private var gameLoopJob: Job? = null

    // Meeting Discussion State
    private val _discussionLogs = MutableStateFlow<List<ChatMessage>>(emptyList())
    val discussionLogs = _discussionLogs.asStateFlow()

    private val _meetingTimeLeft = MutableStateFlow(15)
    val meetingTimeLeft = _meetingTimeLeft.asStateFlow()

    private val _isMeetingActive = MutableStateFlow(false)
    val isMeetingActive = _isMeetingActive.asStateFlow()

    // Ejection display
    private val _whoWasEjected = MutableStateFlow<GameCharacter?>(null)
    val whoWasEjected = _whoWasEjected.asStateFlow()
    private val _ejectionText = MutableStateFlow("")
    val ejectionText = _ejectionText.asStateFlow()

    // End Game details
    private val _didCrewmatesWin = MutableStateFlow(true)
    val didCrewmatesWin = _didCrewmatesWin.asStateFlow()
    private val _gameOverReason = MutableStateFlow("")
    val gameOverReason = _gameOverReason.asStateFlow()

    // Local tasks for current player
    private val _assignedTasks = MutableStateFlow<List<GameTask>>(emptyList())
    val assignedTasks = _assignedTasks.asStateFlow()

    // Interactivity details
    private val _activeMinigameTask = MutableStateFlow<TaskNode?>(null)
    val activeMinigameTask = _activeMinigameTask.asStateFlow()

    private val _isVenting = MutableStateFlow(false)
    val isVenting = _isVenting.asStateFlow()

    private val _currentVentId = MutableStateFlow<String?>(null)
    val currentVentId = _currentVentId.asStateFlow()

    // Cooldown variables (timers in seconds)
    private val _myKillCooldown = MutableStateFlow(10f)
    val myKillCooldown = _myKillCooldown.asStateFlow()

    private val _mySabotageCooldown = MutableStateFlow(15f)
    val mySabotageCooldown = _mySabotageCooldown.asStateFlow()

    private val _myMeetingsCalled = MutableStateFlow(0)
    val myMeetingsCalled = _myMeetingsCalled.asStateFlow()

    // Task scanning progress
    private val _isScanning = MutableStateFlow(false)
    val isScanning = _isScanning.asStateFlow()
    private val _scanProgress = MutableStateFlow(0)
    val scanProgress = _scanProgress.asStateFlow()

    init {
        generateSimulatedLobbies()
    }

    fun setScreen(screen: GameScreen) {
        _currentScreen.value = screen
    }

    fun toggleSound() {
        _isSoundEnabled.value = !_isSoundEnabled.value
    }

    private fun generateSimulatedLobbies() {
        _lobbies.value = listOf(
            LobbyInfo(name = "Casual Astronauts", host = "RedAlpha", playersCount = 6, mapName = "The Skeld", impostorCount = 1),
            LobbyInfo(name = "Polus Tryhards 24/7", host = "IceCold", playersCount = 8, mapName = "Polus", impostorCount = 2),
            LobbyInfo(name = "MIRA HQ Fun", host = "FlyHigh", playersCount = 4, mapName = "MIRA HQ", impostorCount = 1),
            LobbyInfo(name = "No Venting Allowed", host = "SafeScan", playersCount = 3, mapName = "The Skeld", impostorCount = 1),
            LobbyInfo(name = "Amogus Impostor Club", host = "SusLord", playersCount = 9, mapName = "The Skeld", impostorCount = 2)
        )
    }

    // Refresh Lobbies
    fun refreshLobbies() {
        generateSimulatedLobbies()
    }

    // Save profile customization
    fun updateProfile(name: String, colorName: String, hatId: String, skinId: String, petId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val updated = PlayerProfile(
                playerName = name.ifBlank { "Crewmate" },
                colorName = colorName,
                hatId = hatId,
                skinId = skinId,
                petId = petId
            )
            repository.savePlayerProfile(updated)
        }
    }

    // Generate a secure API Key for Bot Integration
    fun generateBotApiKey(name: String) {
        val finalBotName = name.trim().ifBlank { "Automation Bot" }
        val randomStr = (1..24).map {
            "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".random()
        }.joinToString("")
        val token = "au_live_$randomStr"
        val formatter = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
        val dateStr = formatter.format(java.util.Date())
        
        val newKey = BotApiKey(
            name = finalBotName,
            token = token,
            createdAt = dateStr
        )
        _botApiKeys.value = _botApiKeys.value + newKey
    }

    // Revoke an API Key
    fun revokeBotApiKey(id: String) {
        _botApiKeys.value = _botApiKeys.value.map { key ->
            if (key.id == id) key.copy(isRevoked = true) else key
        }
    }

    // Completely delete an API Key
    fun deleteBotApiKey(id: String) {
        _botApiKeys.value = _botApiKeys.value.filterNot { it.id == id }
    }

    // Join a lobby
    fun joinLobby(lobby: LobbyInfo) {
        _currentLobby.value = lobby
        chatMessages.clear()
        chatMessages.add(ChatMessage("System", "Joined lobby ${lobby.name}. Code: ${lobby.code}", isSystem = true))
        setScreen(GameScreen.LobbyRoom)
        simulateLobbyChat()
    }

    // Join lobby by searching for custom room code
    fun joinLobbyByCode(code: String): Boolean {
        val uppercaseCode = code.uppercase().trim()
        if (uppercaseCode.length != 6) return false
        val matched = _lobbies.value.firstOrNull { it.code == uppercaseCode }
        if (matched != null) {
            joinLobby(matched)
            return true
        } else {
            // If lobby does not exist, create a private lobby on the fly with this exact code
            val newLobby = LobbyInfo(
                name = "Private Ship",
                host = "PlayerHost",
                playersCount = 1,
                mapName = "The Skeld",
                impostorCount = 1,
                code = uppercaseCode
            )
            _lobbies.value = _lobbies.value + newLobby
            joinLobby(newLobby)
            return true
        }
    }

    // Create custom private lobby
    fun createLobby(name: String, mapName: String, impostors: Int) {
        val finalName = name.ifBlank { "My Space Ship" }
        val newLobby = LobbyInfo(
            name = finalName,
            host = playerProfile.value?.playerName ?: "Crewmate",
            playersCount = 1,
            mapName = mapName,
            impostorCount = impostors
        )
        _currentLobby.value = newLobby
        chatMessages.clear()
        chatMessages.add(ChatMessage("System", "Created Lobby: $finalName. Code: ${newLobby.code}", isSystem = true))
        setScreen(GameScreen.LobbyRoom)
        simulateLobbyChat()
    }

    // Leave current lobby
    fun leaveLobby() {
        _currentLobby.value = null
        setScreen(GameScreen.MainMenu)
    }

    // Send a chat message in lobby
    fun sendChatMessage(text: String) {
        if (text.isBlank()) return
        val author = playerProfile.value?.playerName ?: "Crewmate"
        chatMessages.add(ChatMessage(author, text))

        // Trigger bot responses sometimes
        viewModelScope.launch {
            delay(1000)
            val botNames = listOf("CyanPanda", "LimeJuice", "Pinky", "OrangeJuice", "BlackMamba", "WhiteSnow")
            val botResponses = listOf(
                "Hi there! Let's start the game!",
                "Start?",
                "I want to scan in MedBay",
                "Who is the impostor going to be?",
                "gg",
                "Add speed multiplier please"
            )
            chatMessages.add(ChatMessage(botNames.random(), botResponses.random()))
        }
    }

    private fun simulateLobbyChat() {
        viewModelScope.launch {
            var count = 0
            while (_currentScreen.value == GameScreen.LobbyRoom && count < 5) {
                delay(3000 + (1000..3000).random().toLong())
                if (_currentScreen.value != GameScreen.LobbyRoom) break
                val botNames = listOf("CyanPanda", "LimeJuice", "Pinky", "OrangeJuice", "BlackMamba", "WhiteSnow", "YellowSub", "PurpleRain")
                val botMessages = listOf(
                    "Yellow is looking sus already",
                    "Please start, I'm ready",
                    "I love this map",
                    "Let's play!",
                    "Who's host?"
                )
                chatMessages.add(ChatMessage(botNames.random(), botMessages.random()))
                count++
            }
        }
    }

    // Start Active Game
    fun startGame() {
        val lobby = _currentLobby.value ?: return
        val profile = playerProfile.value ?: PlayerProfile()

        // Create player & 9 bot characters
        val list = mutableListOf<GameCharacter>()
        val botColors = AmongUsColors.keys.toMutableList()
        botColors.remove(profile.colorName)

        val botNames = mutableListOf(
            "CyanPanda", "LimeJuice", "Pinky", "OrangeJuice", "BlackMamba", "WhiteSnow", "YellowSub", "PurpleRain", "RedStorm", "GreenField"
        ).shuffled().toMutableList()

        myCharacterId = "player_user"

        // Randomly select Impostor indices
        val totalPlayers = 10
        val impostorsCount = lobby.impostorCount.coerceIn(1, 3)
        val impostorIndices = (0 until totalPlayers).shuffled().take(impostorsCount).toSet()

        // Add User
        val userIsImpostor = impostorIndices.contains(0)
        list.add(
            GameCharacter(
                id = myCharacterId,
                name = profile.playerName,
                colorName = profile.colorName,
                isImpostor = userIsImpostor,
                isBot = false,
                x = 500f,
                y = 200f, // starts in Cafeteria
                hatId = profile.hatId,
                skinId = profile.skinId,
                petId = profile.petId
            )
        )

        // Add 9 Bots
        for (i in 1 until totalPlayers) {
            val botName = botNames.getOrNull(i - 1) ?: "Bot $i"
            val botColor = botColors.getOrNull(i - 1) ?: "White"
            val botIsImpostor = impostorIndices.contains(i)

            // Random hat/skin for bots
            val hats = listOf("none", "sprout", "toilet_paper", "viking", "chef", "cowboy", "astronaut")
            val skins = listOf("none", "suit", "astronaut", "doctor", "police")
            val pets = listOf("none", "mini", "dog", "alien", "robot")

            list.add(
                GameCharacter(
                    id = "bot_$i",
                    name = botName,
                    colorName = botColor,
                    isImpostor = botIsImpostor,
                    isBot = true,
                    x = 500f + (-80..80).random(),
                    y = 200f + (-80..80).random(),
                    hatId = hats.random(),
                    skinId = skins.random(),
                    petId = pets.random()
                )
            )
        }

        _characters.value = list
        _deadBodies.value = emptyList()
        _activeSabotage.value = ActiveSabotage(ActiveSabotageType.None, 0)
        _isVenting.value = false
        _currentVentId.value = null
        _myKillCooldown.value = killCooldownSetting.value
        _mySabotageCooldown.value = 15f
        _myMeetingsCalled.value = 0
        _activeMinigameTask.value = null

        // Assign tasks
        val generatedTasks = mutableListOf<GameTask>()
        val selectedNodes = GameMapData.taskNodes.shuffled().take(4)
        selectedNodes.forEach { node ->
            generatedTasks.add(
                GameTask(
                    id = node.id,
                    roomName = node.roomName,
                    taskType = node.taskType
                )
            )
        }
        _assignedTasks.value = generatedTasks

        // Track game statistics
        viewModelScope.launch(Dispatchers.IO) {
            repository.incrementStats(gamesPlayed = 1)
        }

        setScreen(GameScreen.ActiveGame)
        startGameLoop()
    }

    // Toggle scanning action
    fun startScanning() {
        if (_isScanning.value) return
        _isScanning.value = true
        _scanProgress.value = 0
        viewModelScope.launch {
            while (_isScanning.value && _scanProgress.value < 100) {
                delay(80)
                _scanProgress.value += 2
                if (_scanProgress.value >= 100) {
                    _isScanning.value = false
                    completeTask("task_med_scan")
                    viewModelScope.launch(Dispatchers.IO) {
                        repository.unlockAchievement("first_scan")
                    }
                }
            }
        }
    }

    fun stopScanning() {
        _isScanning.value = false
        _scanProgress.value = 0
    }

    // Completing local user task
    fun completeTask(taskId: String) {
        val currentTasks = _assignedTasks.value.toMutableList()
        val index = currentTasks.indexOfFirst { it.id == taskId }
        if (index != -1 && !currentTasks[index].isCompleted) {
            currentTasks[index] = currentTasks[index].copy(isCompleted = true)
            _assignedTasks.value = currentTasks
            GameSoundSynth.playSound(SoundType.TaskComplete, _isSoundEnabled.value)

            // Increment stats
            viewModelScope.launch(Dispatchers.IO) {
                repository.incrementStats(tasksCompleted = 1)
            }

            checkWinConditions()
        }
    }

    private fun checkWinConditions() {
        val aliveChars = _characters.value.filter { !it.isDead }
        val crewmates = aliveChars.filter { !it.isImpostor }
        val impostors = aliveChars.filter { it.isImpostor }

        // 1. All tasks completed
        val allTasksCompleted = _assignedTasks.value.all { it.isCompleted } && _assignedTasks.value.isNotEmpty()
        if (allTasksCompleted) {
            endGame(crewmatesWon = true, reason = "All tasks completed!")
            return
        }

        // 2. Impostors equal or outnumber crewmates
        if (impostors.size >= crewmates.size) {
            endGame(crewmatesWon = false, reason = "Impostors outnumber crewmates!")
            return
        }

        // 3. No impostors left
        if (impostors.isEmpty()) {
            endGame(crewmatesWon = true, reason = "All Impostors voted out!")
            return
        }
    }

    private fun endGame(crewmatesWon: Boolean, reason: String) {
        _isMeetingActive.value = false
        _isVenting.value = false
        _currentVentId.value = null
        _activeMinigameTask.value = null
        stopScanning()
        gameLoopJob?.cancel()

        _didCrewmatesWin.value = crewmatesWon
        _gameOverReason.value = reason

        viewModelScope.launch(Dispatchers.IO) {
            if (crewmatesWon) {
                repository.incrementStats(crewmateWins = 1)
                GameSoundSynth.playSound(SoundType.GameWin, _isSoundEnabled.value)
            } else {
                repository.incrementStats(impostorWins = 1)
                GameSoundSynth.playSound(SoundType.GameLose, _isSoundEnabled.value)
            }
        }

        setScreen(GameScreen.GameOverScreen)
    }

    // Core Game Loop Ticker
    private fun startGameLoop() {
        gameLoopJob?.cancel()
        gameLoopJob = viewModelScope.launch {
            while (_currentScreen.value == GameScreen.ActiveGame) {
                // ~60 FPS update
                delay(16)
                tickGame()
            }
        }
    }

    private fun tickGame() {
        val currentList = _characters.value.toMutableList()

        // 1. Update user cooldowns if user is Impostor
        val myUser = currentList.firstOrNull { it.id == myCharacterId }
        if (myUser != null && myUser.isImpostor) {
            if (_myKillCooldown.value > 0f) {
                _myKillCooldown.value = (_myKillCooldown.value - 0.016f).coerceAtLeast(0f)
            }
            if (_mySabotageCooldown.value > 0f) {
                _mySabotageCooldown.value = (_mySabotageCooldown.value - 0.016f).coerceAtLeast(0f)
            }
        }

        // Sabotage Countdown
        val activeSab = _activeSabotage.value
        if (activeSab.type != ActiveSabotageType.None) {
            val updatedTimer = (activeSab.timer - 0.016f)
            if (updatedTimer <= 0f) {
                // Sabotage Game Over!
                endGame(crewmatesWon = false, reason = "${activeSab.type.name} Meltdown triggered!")
                return
            } else {
                _activeSabotage.value = activeSab.copy(timer = updatedTimer.toInt())
            }
        }

        // 2. Update all characters position & bots decision AI
        for (i in currentList.indices) {
            val char = currentList[i]
            if (char.id == myCharacterId) {
                // Player movement already handled via drag/joystick in UI, but we can clamp
                continue
            }

            // Bot behavior updates
            if (char.isBot) {
                currentList[i] = updateBotAI(char, currentList)
            }
        }

        _characters.value = currentList
        checkWinConditions()
    }

    // Bot AI state machine
    private fun updateBotAI(bot: GameCharacter, allPlayers: List<GameCharacter>): GameCharacter {
        if (bot.isDead) {
            // Ghosts can wander randomly through walls
            val currentRoom = GameMapData.getRoomAt(bot.x, bot.y)
            val tx = bot.targetX ?: 500f
            val ty = bot.targetY ?: 200f
            val dist = sqrt((tx - bot.x) * (tx - bot.x) + (ty - bot.y) * (ty - bot.y))

            if (dist < 10f) {
                // Pick next random room center
                val randomRoom = GameMapData.rooms.random()
                return bot.copy(
                    targetX = randomRoom.x,
                    targetY = randomRoom.y,
                    currentRoom = currentRoom,
                    vx = 0f,
                    vy = 0f
                )
            } else {
                // Move directly towards center
                val angle = atan2(ty - bot.y, tx - bot.x)
                val nx = bot.x + cos(angle) * bot.speed * playerSpeedMultiplier.value
                val ny = bot.y + sin(angle) * bot.speed * playerSpeedMultiplier.value
                return bot.copy(
                    x = nx,
                    y = ny,
                    vx = nx - bot.x,
                    vy = ny - bot.y,
                    currentRoom = currentRoom
                )
            }
        }

        // Active Living Bot Behavior
        val currentRoom = GameMapData.getRoomAt(bot.x, bot.y)

        // Sabotage state resolver
        val activeSab = _activeSabotage.value
        if (activeSab.type != ActiveSabotageType.None && !bot.isImpostor && bot.aiState != "fixing_sabotage") {
            // Crewmate bots have 70% chance to run to resolve sabotage
            if ((1..100).random() < 70) {
                val destX: Float
                val destY: Float
                if (activeSab.type == ActiveSabotageType.ReactorMeltdown) {
                    destX = 80f
                    destY = 450f
                } else if (activeSab.type == ActiveSabotageType.O2Depletion) {
                    destX = 760f
                    destY = 360f
                } else {
                    destX = 340f
                    destY = 560f // Electrical
                }
                return bot.copy(
                    aiState = "fixing_sabotage",
                    targetX = destX,
                    targetY = destY
                )
            }
        }

        // Check if bot can report a body
        _deadBodies.value.forEach { body ->
            val dist = sqrt((body.x - bot.x) * (body.x - bot.x) + (body.y - bot.y) * (body.y - bot.y))
            if (dist < 80f && !bot.isDead) {
                // Trigger body report immediately!
                triggerEmergencyMeeting(reportedBodyPlayerId = body.playerId)
                return bot
            }
        }

        // Update Bot Cooldowns
        val updatedKillCooldown = if (bot.killCooldown > 0) (bot.killCooldown - 0.016f).coerceAtLeast(0f) else 0f
        val updatedSabotageCooldown = if (bot.sabotageCooldown > 0) (bot.sabotageCooldown - 0.016f).coerceAtLeast(0f) else 0f

        // State Machine Decision
        when (bot.aiState) {
            "idle", "wandering" -> {
                if (bot.isImpostor) {
                    // Impostor bot hunts for victims
                    val target = findHuntTarget(bot, allPlayers)
                    if (target != null && updatedKillCooldown == 0f) {
                        return bot.copy(
                            aiState = "hunting",
                            targetX = target.x,
                            targetY = target.y,
                            currentRoom = currentRoom,
                            killCooldown = updatedKillCooldown,
                            sabotageCooldown = updatedSabotageCooldown
                        )
                    } else {
                        // Pretend to do task (wander to a task node)
                        val node = GameMapData.taskNodes.random()
                        return bot.copy(
                            aiState = "moving_to_task",
                            targetX = node.x,
                            targetY = node.y,
                            currentTaskTargetId = node.id,
                            currentRoom = currentRoom,
                            killCooldown = updatedKillCooldown,
                            sabotageCooldown = updatedSabotageCooldown
                        )
                    }
                } else {
                    // Crewmate bot goes to random task node
                    val node = GameMapData.taskNodes.random()
                    return bot.copy(
                        aiState = "moving_to_task",
                        targetX = node.x,
                        targetY = node.y,
                        currentTaskTargetId = node.id,
                        currentRoom = currentRoom,
                        killCooldown = updatedKillCooldown,
                        sabotageCooldown = updatedSabotageCooldown
                    )
                }
            }

            "moving_to_task", "fixing_sabotage" -> {
                val tx = bot.targetX ?: 500f
                val ty = bot.targetY ?: 200f
                val dist = sqrt((tx - bot.x) * (tx - bot.x) + (ty - bot.y) * (ty - bot.y))

                if (dist < 15f) {
                    if (bot.aiState == "fixing_sabotage") {
                        // Fix sabotage
                        resolveSabotage()
                        return bot.copy(
                            aiState = "idle",
                            currentRoom = currentRoom,
                            killCooldown = updatedKillCooldown,
                            sabotageCooldown = updatedSabotageCooldown
                        )
                    } else {
                        // Doing task state
                        return bot.copy(
                            aiState = "doing_task",
                            aiTaskTimer = (3..5).random().toFloat(),
                            currentRoom = currentRoom,
                            killCooldown = updatedKillCooldown,
                            sabotageCooldown = updatedSabotageCooldown
                        )
                    }
                } else {
                    // Navigate towards target avoiding walls
                    val angle = atan2(ty - bot.y, tx - bot.x)
                    val speed = bot.speed * playerSpeedMultiplier.value
                    var dx = cos(angle) * speed
                    var dy = sin(angle) * speed

                    // Simple obstacle avoidance: check if next step is walkable
                    var nx = bot.x + dx
                    var ny = bot.y + dy

                    if (!GameMapData.isPositionWalkable(nx, ny, isGhost = false)) {
                        // Try slide along axes
                        if (GameMapData.isPositionWalkable(bot.x + dx, bot.y, isGhost = false)) {
                            nx = bot.x + dx
                            ny = bot.y
                        } else if (GameMapData.isPositionWalkable(bot.x, bot.y + dy, isGhost = false)) {
                            nx = bot.x
                            ny = bot.y + dy
                        } else {
                            // Turn 90 degrees
                            val altAngle = angle + 1.57f
                            nx = bot.x + cos(altAngle) * speed
                            ny = bot.y + sin(altAngle) * speed
                        }
                    }

                    // Final clamp
                    if (!GameMapData.isPositionWalkable(nx, ny, isGhost = false)) {
                        nx = bot.x
                        ny = bot.y
                    }

                    return bot.copy(
                        x = nx,
                        y = ny,
                        vx = nx - bot.x,
                        vy = ny - bot.y,
                        currentRoom = currentRoom,
                        killCooldown = updatedKillCooldown,
                        sabotageCooldown = updatedSabotageCooldown
                    )
                }
            }

            "doing_task" -> {
                val left = bot.aiTaskTimer - 0.016f
                if (left <= 0f) {
                    // Finished doing task
                    if (bot.isImpostor) {
                        // Triggers a random sabotage sometimes!
                        if (updatedSabotageCooldown == 0f && (1..100).random() < 40) {
                            triggerSabotage(ActiveSabotageType.values().filter { it != ActiveSabotageType.None }.random())
                            return bot.copy(
                                aiState = "idle",
                                currentRoom = currentRoom,
                                killCooldown = updatedKillCooldown,
                                sabotageCooldown = killCooldownSetting.value // reset sabotage cooldown
                            )
                        }
                    }
                    return bot.copy(
                        aiState = "idle",
                        currentRoom = currentRoom,
                        killCooldown = updatedKillCooldown,
                        sabotageCooldown = updatedSabotageCooldown
                    )
                } else {
                    return bot.copy(
                        aiTaskTimer = left,
                        currentRoom = currentRoom,
                        killCooldown = updatedKillCooldown,
                        sabotageCooldown = updatedSabotageCooldown
                    )
                }
            }

            "hunting" -> {
                val tx = bot.targetX ?: 500f
                val ty = bot.targetY ?: 200f
                val dist = sqrt((tx - bot.x) * (tx - bot.x) + (ty - bot.y) * (ty - bot.y))

                // Find target character to make sure they are still alive
                val victim = allPlayers.firstOrNull { (it.x == tx && it.y == ty) || (sqrt((it.x - bot.x)*(it.x - bot.x) + (it.y - bot.y)*(it.y - bot.y)) < 150f && !it.isDead && !it.isImpostor) }

                if (victim == null || victim.isDead) {
                    return bot.copy(
                        aiState = "idle",
                        currentRoom = currentRoom,
                        killCooldown = updatedKillCooldown,
                        sabotageCooldown = updatedSabotageCooldown
                    )
                }

                if (dist < 40f && updatedKillCooldown == 0f) {
                    // KILL TRIGGER
                    performBotKill(bot, victim)
                    return bot.copy(
                        aiState = "idle",
                        killCooldown = killCooldownSetting.value,
                        currentRoom = currentRoom,
                        sabotageCooldown = updatedSabotageCooldown
                    )
                } else {
                    // Chase target
                    val angle = atan2(victim.y - bot.y, victim.x - bot.x)
                    val speed = bot.speed * playerSpeedMultiplier.value * 1.1f
                    val nx = bot.x + cos(angle) * speed
                    val ny = bot.y + sin(angle) * speed

                    val walkedX = if (GameMapData.isPositionWalkable(nx, bot.y, isGhost = false)) nx else bot.x
                    val walkedY = if (GameMapData.isPositionWalkable(walkedX, ny, isGhost = false)) ny else bot.y

                    return bot.copy(
                        x = walkedX,
                        y = walkedY,
                        vx = walkedX - bot.x,
                        vy = walkedY - bot.y,
                        targetX = victim.x,
                        targetY = victim.y,
                        currentRoom = currentRoom,
                        killCooldown = updatedKillCooldown,
                        sabotageCooldown = updatedSabotageCooldown
                    )
                }
            }
        }
        return bot
    }

    private fun findHuntTarget(bot: GameCharacter, allPlayers: List<GameCharacter>): GameCharacter? {
        val candidates = allPlayers.filter { !it.isDead && !it.isImpostor }
        if (candidates.isEmpty()) return null

        // Find closest candidate
        return candidates.minByOrNull {
            sqrt((it.x - bot.x) * (it.x - bot.x) + (it.y - bot.y) * (it.y - bot.y))
        }
    }

    private fun performBotKill(impostor: GameCharacter, victim: GameCharacter) {
        val currentList = _characters.value.toMutableList()
        val victimIndex = currentList.indexOfFirst { it.id == victim.id }
        if (victimIndex != -1) {
            val killed = currentList[victimIndex].copy(isDead = true, isGhost = true)
            currentList[victimIndex] = killed

            // Spawn Dead Body
            val bodies = _deadBodies.value.toMutableList()
            bodies.add(
                DeadBody(
                    playerId = victim.id,
                    playerName = victim.name,
                    colorName = victim.colorName,
                    x = victim.x,
                    y = victim.y,
                    roomName = GameMapData.getRoomAt(victim.x, victim.y)
                )
            )
            _deadBodies.value = bodies
            _characters.value = currentList

            GameSoundSynth.playSound(SoundType.Kill, _isSoundEnabled.value)

            // If user is killed
            if (victim.id == myCharacterId) {
                chatMessages.add(ChatMessage("System", "You were killed by ${impostor.name}!", isSystem = true))
            }
        }
    }

    // Trigger Emergency Meeting or Body Report
    fun triggerEmergencyMeeting(reportedBodyPlayerId: String? = null) {
        // Cancel active mini games or scanning
        _activeMinigameTask.value = null
        stopScanning()

        _isMeetingActive.value = true
        _discussionLogs.value = emptyList()

        viewModelScope.launch {
            if (reportedBodyPlayerId != null) {
                val reported = _characters.value.firstOrNull { it.id == reportedBodyPlayerId }
                GameSoundSynth.playSound(SoundType.Report, _isSoundEnabled.value)
                delay(1000)
                setScreen(GameScreen.EmergencyMeeting)
                startMeetingDiscussion(reporterName = "Someone", bodyName = reported?.name ?: "Crewmate")
            } else {
                GameSoundSynth.playSound(SoundType.Emergency, _isSoundEnabled.value)
                _myMeetingsCalled.value += 1
                viewModelScope.launch(Dispatchers.IO) {
                    repository.incrementStats(meetingsCalled = 1)
                }
                delay(1000)
                setScreen(GameScreen.EmergencyMeeting)
                startMeetingDiscussion(reporterName = playerProfile.value?.playerName ?: "Crewmate", bodyName = null)
            }
        }
    }

    // Meeting Discussion simulation
    private fun startMeetingDiscussion(reporterName: String, bodyName: String?) {
        val logs = mutableListOf<ChatMessage>()
        _discussionLogs.value = logs
        _meetingTimeLeft.value = 15

        viewModelScope.launch {
            if (bodyName != null) {
                logs.add(ChatMessage("System", "Dead body of $bodyName was reported!", isSystem = true))
            } else {
                logs.add(ChatMessage("System", "$reporterName called an Emergency Meeting!", isSystem = true))
            }

            val aliveBots = _characters.value.filter { !it.isDead && it.id != myCharacterId }
            val totalDiscussionSeconds = 15

            // Background discussion task ticking
            launch {
                for (sec in totalDiscussionSeconds downTo 1) {
                    _meetingTimeLeft.value = sec
                    delay(1000)
                }
                // When discussion timer ends, trigger voting ejection
                executeMeetingVoting()
            }

            // Bots chatting
            delay(1000)
            if (bodyName != null) {
                logs.add(ChatMessage(aliveBots.random().name, "Where?"))
                delay(1200)
                logs.add(ChatMessage(aliveBots.random().name, "Who was nearby?"))
                delay(1500)

                // High suspicion bot response
                val deadBodyRoom = _deadBodies.value.lastOrNull()?.roomName ?: "Corridors"
                val suspect = findMeetingSuspect(bodyName)
                if (suspect != null) {
                    logs.add(ChatMessage(aliveBots.random().name, "I saw $suspect near $deadBodyRoom, super sus!"))
                } else {
                    logs.add(ChatMessage(aliveBots.random().name, "I think we should skip, no info."))
                }
            } else {
                logs.add(ChatMessage(aliveBots.random().name, "Why called?"))
                delay(1500)
                logs.add(ChatMessage(aliveBots.random().name, "Any info? Or troll?"))
            }
        }
    }

    private fun findMeetingSuspect(bodyName: String?): String? {
        val imp = _characters.value.firstOrNull { it.isImpostor && !it.isDead }
        return imp?.name
    }

    // Cast vote from user
    fun castUserVote(votedId: String?) {
        // User casts their vote
        val logs = _discussionLogs.value.toMutableList()
        val username = playerProfile.value?.playerName ?: "Crewmate"
        if (votedId == "skip" || votedId == null) {
            logs.add(ChatMessage(username, "Skipped vote", isSystem = true))
        } else {
            val votedPlayer = _characters.value.firstOrNull { it.id == votedId }
            logs.add(ChatMessage(username, "Voted for ${votedPlayer?.name}", isSystem = true))
        }
        _discussionLogs.value = logs
        GameSoundSynth.playSound(SoundType.MeetingVote, _isSoundEnabled.value)
    }

    private fun executeMeetingVoting() {
        val aliveChars = _characters.value.filter { !it.isDead }
        val voteTally = mutableMapOf<String, Int>() // playerId to voteCount
        var skipVotes = 0

        // Bots voting logic
        aliveChars.forEach { char ->
            if (char.id == myCharacterId) {
                // Already cast or skip
                return@forEach
            }
            // Bot logic: Vote for the most suspicious (Impostor) with 60% probability, or skip
            val suspectedImpostor = _characters.value.firstOrNull { it.isImpostor && !it.isDead }
            if (suspectedImpostor != null && (1..100).random() < 65) {
                voteTally[suspectedImpostor.id] = (voteTally[suspectedImpostor.id] ?: 0) + 1
            } else {
                if ((1..100).random() < 50) {
                    skipVotes++
                } else {
                    val randomAlive = aliveChars.random().id
                    voteTally[randomAlive] = (voteTally[randomAlive] ?: 0) + 1
                }
            }
        }

        // Determine who gets ejected
        val maxVotesEntry = voteTally.maxByOrNull { it.value }
        val highestVoteId = maxVotesEntry?.key
        val highestVoteCount = maxVotesEntry?.value ?: 0

        val ejectedPlayer: GameCharacter?
        if (highestVoteId != null && highestVoteCount > skipVotes && highestVoteCount > 1) {
            ejectedPlayer = _characters.value.firstOrNull { it.id == highestVoteId }
        } else {
            ejectedPlayer = null // skipped
        }

        // Apply ejection and transition screen
        _isMeetingActive.value = false
        _deadBodies.value = emptyList() // clear dead bodies on report

        if (ejectedPlayer != null) {
            // Kill the ejected player
            val list = _characters.value.toMutableList()
            val index = list.indexOfFirst { it.id == ejectedPlayer.id }
            if (index != -1) {
                list[index] = list[index].copy(isDead = true, isGhost = true)
                _characters.value = list
            }

            val isImp = ejectedPlayer.isImpostor
            val impRemaining = _characters.value.count { it.isImpostor && !it.isDead }
            _whoWasEjected.value = ejectedPlayer
            _ejectionText.value = if (isImp) {
                "${ejectedPlayer.name} was An Impostor. ($impRemaining Impostors remain.)"
            } else {
                "${ejectedPlayer.name} was Not An Impostor. ($impRemaining Impostors remain.)"
            }

            // Unlock achievements
            if (isImp && ejectedPlayer.id != myCharacterId) {
                viewModelScope.launch(Dispatchers.IO) {
                    repository.unlockAchievement("deceiver")
                }
            }
        } else {
            val impRemaining = _characters.value.count { it.isImpostor && !it.isDead }
            _whoWasEjected.value = null
            _ejectionText.value = "No one was ejected. (Skipped) ($impRemaining Impostors remain.)"
        }

        setScreen(GameScreen.EjectionScreen)
        viewModelScope.launch {
            delay(4000)
            // Go back to active game
            setScreen(GameScreen.ActiveGame)
            startGameLoop()
        }
    }

    // Trigger local player Kill action
    fun performKillAction() {
        val user = _characters.value.firstOrNull { it.id == myCharacterId } ?: return
        if (!user.isImpostor || user.isDead) return
        if (_myKillCooldown.value > 0f) return

        // Find closest target crewmate within kill range
        val targets = _characters.value.filter { !it.isDead && !it.isImpostor }
        val closest = targets.minByOrNull {
            sqrt((it.x - user.x) * (it.x - user.x) + (it.y - user.y) * (it.y - user.y))
        }

        if (closest != null) {
            val dist = sqrt((closest.x - user.x) * (closest.x - user.x) + (closest.y - user.y) * (closest.y - user.y))
            if (dist < 80f) {
                // Kill closest
                performBotKill(user, closest)
                _myKillCooldown.value = killCooldownSetting.value

                // Increment total kills stat
                viewModelScope.launch(Dispatchers.IO) {
                    repository.incrementStats(totalKills = 1)
                }
            }
        }
    }

    // Vent actions
    fun performVentAction() {
        val user = _characters.value.firstOrNull { it.id == myCharacterId } ?: return
        if (!user.isImpostor || user.isDead) return

        if (_isVenting.value) {
            // Unvent (exit vent)
            val ventId = _currentVentId.value ?: return
            val vent = GameMapData.vents.firstOrNull { it.id == ventId }
            if (vent != null) {
                val list = _characters.value.toMutableList()
                val index = list.indexOfFirst { it.id == myCharacterId }
                if (index != -1) {
                    list[index] = list[index].copy(x = vent.x, y = vent.y)
                    _characters.value = list
                }
                _isVenting.value = false
                _currentVentId.value = null
                GameSoundSynth.playSound(SoundType.Vent, _isSoundEnabled.value)
            }
        } else {
            // Find closest vent in range
            val closestVent = GameMapData.vents.minByOrNull {
                sqrt((it.x - user.x) * (it.x - user.x) + (it.y - user.y) * (it.y - user.y))
            }
            if (closestVent != null) {
                val dist = sqrt((closestVent.x - user.x) * (closestVent.x - user.x) + (closestVent.y - user.y) * (closestVent.y - user.y))
                if (dist < 60f) {
                    // Vent in!
                    _isVenting.value = true
                    _currentVentId.value = closestVent.id
                    GameSoundSynth.playSound(SoundType.Vent, _isSoundEnabled.value)

                    viewModelScope.launch(Dispatchers.IO) {
                        repository.unlockAchievement("vent_traveler")
                    }
                }
            }
        }
    }

    // Traverse connected vents
    fun navigateVent(nextVentId: String) {
        if (!_isVenting.value) return
        _currentVentId.value = nextVentId
        GameSoundSynth.playSound(SoundType.Vent, _isSoundEnabled.value)
    }

    // Trigger Sabotages
    fun triggerSabotage(type: ActiveSabotageType) {
        if (_mySabotageCooldown.value > 0f) return
        _activeSabotage.value = ActiveSabotage(type, 45) // 45 seconds to resolve
        _mySabotageCooldown.value = 25f
        GameSoundSynth.playSound(SoundType.Alarm, _isSoundEnabled.value)
    }

    // Resolve active sabotage
    fun resolveSabotage() {
        val current = _activeSabotage.value
        if (current.type != ActiveSabotageType.None) {
            _activeSabotage.value = ActiveSabotage(ActiveSabotageType.None, 0)
            GameSoundSynth.playSound(SoundType.TaskComplete, _isSoundEnabled.value)

            viewModelScope.launch(Dispatchers.IO) {
                repository.incrementStats(sabotagesFixed = 1)
                repository.unlockAchievement("saviour")
            }
        }
    }

    fun openMinigame(taskNode: TaskNode) {
        _activeMinigameTask.value = taskNode
    }

    fun closeMinigame() {
        _activeMinigameTask.value = null
    }

    fun updatePlayerPosition(newX: Float, newY: Float, vx: Float = 0f, vy: Float = 0f) {
        val list = _characters.value.toMutableList()
        val index = list.indexOfFirst { it.id == myCharacterId }
        if (index != -1) {
            list[index] = list[index].copy(
                x = newX,
                y = newY,
                vx = vx,
                vy = vy,
                currentRoom = GameMapData.getRoomAt(newX, newY)
            )
            _characters.value = list
        }
    }
}

data class ChatMessage(
    val author: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isSystem: Boolean = false
)
