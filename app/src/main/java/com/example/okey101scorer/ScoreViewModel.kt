package com.example.okey101scorer

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.firstOrNull
import java.util.UUID
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import com.example.okey101scorer.engine.TableEvent
import com.example.okey101scorer.engine.EventEngine
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ChildEventListener

val Context.dataStore by preferencesDataStore(name = "okey_101_prefs")

@Serializable
data class Round(
    val id: String = UUID.randomUUID().toString(),
    val score1: Int = 0,
    val score2: Int = 0,
    val isScore1Entered: Boolean = false,
    val isScore2Entered: Boolean = false,
    val event: TableEvent = TableEvent.NONE
)

data class SpectatorReaction(
    val senderName: String,
    val senderAvatar: String,
    val emoji: String
)

data class SpectatorChat(
    val id: Long = System.nanoTime(),
    val senderName: String,
    val senderAvatar: String,
    val message: String
)

sealed class ActiveEventDialog {
    object None : ActiveEventDialog()
    data class MysteryBox(val teamIndex: Int, val amount: Int, val teamName: String) : ActiveEventDialog()
    data class YanciIhtilali(val targetTeamIndex: Int, val teamName: String) : ActiveEventDialog()
    object GreatSwap : ActiveEventDialog()
    data class CifteKumar(val teamIndex: Int, val teamName: String, val currentPenalty: Int, val roundId: String) : ActiveEventDialog()
    data class CifteKumarResult(val win: Boolean, val finalScore: Int) : ActiveEventDialog()
}

class ScoreViewModel(application: Application) : AndroidViewModel(application) {

    private val dataStore = application.dataStore

    private val _teamNames = MutableStateFlow(listOf("BİZ", "ONLAR"))
    val teamNames: StateFlow<List<String>> = _teamNames.asStateFlow()

    private val _activeEvent = MutableStateFlow(TableEvent.NONE)
    val activeEvent: StateFlow<TableEvent> = _activeEvent.asStateFlow()

    private val _activeEventDialog = MutableStateFlow<ActiveEventDialog>(ActiveEventDialog.None)
    val activeEventDialog: StateFlow<ActiveEventDialog> = _activeEventDialog.asStateFlow()

    private val _rounds = MutableStateFlow(listOf(Round()))
    val rounds: StateFlow<List<Round>> = _rounds.asStateFlow()

    private val _columnSums = MutableStateFlow(listOf(0, 0))
    val columnSums: StateFlow<List<Int>> = _columnSums.asStateFlow()

    // Spectator Mode State
    private val _isSpectatorActive = MutableStateFlow(false)
    val isSpectatorActive: StateFlow<Boolean> = _isSpectatorActive.asStateFlow()

    private val _roomId = MutableStateFlow<String?>(null)
    val roomId: StateFlow<String?> = _roomId.asStateFlow()

    private val _incomingReactions = MutableSharedFlow<SpectatorReaction>(extraBufferCapacity = 64)
    val incomingReactions: SharedFlow<SpectatorReaction> = _incomingReactions.asSharedFlow()

    private val _incomingChats = MutableSharedFlow<SpectatorChat>(extraBufferCapacity = 64)
    val incomingChats: SharedFlow<SpectatorChat> = _incomingChats.asSharedFlow()

    private var broadcastJob: Job? = null
    private var reactionListenerJob: Job? = null

    // Undo state tracking
    private var lastDeletedRound: Round? = null
    private var lastDeletedIndex: Int = -1

    init {
        loadData()
    }

    private val ROUNDS_KEY = stringPreferencesKey("rounds")
    private val TEAM_NAMES_KEY = stringPreferencesKey("teamNames")

    private fun serializeRounds(roundsList: List<Round>): String {
        return Json.encodeToString(roundsList)
    }

    private fun deserializeRounds(serialized: String): List<Round> {
        if (serialized.isBlank()) return listOf(Round())
        return try {
            Json.decodeFromString(serialized)
        } catch (_: Exception) {
            listOf(Round())
        }
    }

    private fun saveData() {
        val currentRounds = _rounds.value.toList()
        val currentTeamNames = _teamNames.value.toList()
        
        viewModelScope.launch(Dispatchers.IO) {
            val serializedRounds = serializeRounds(currentRounds)
            val serializedTeamNames = Json.encodeToString(currentTeamNames)
            
            dataStore.edit { prefs ->
                prefs[ROUNDS_KEY] = serializedRounds
                prefs[TEAM_NAMES_KEY] = serializedTeamNames
            }
            
            // Trigger immediate live broadcast if active
            if (_isSpectatorActive.value) {
                publishState()
            }
        }
    }

    private fun loadData() {
        viewModelScope.launch(Dispatchers.IO) {
            val prefs = dataStore.data.firstOrNull()
            val serializedRounds = prefs?.get(ROUNDS_KEY) ?: ""
            val serializedTeamNames = prefs?.get(TEAM_NAMES_KEY) ?: ""

            if (serializedTeamNames.isNotEmpty()) {
                try {
                    val names = Json.decodeFromString<List<String>>(serializedTeamNames)
                    if (names.size == 2) {
                        _teamNames.value = names
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            if (serializedRounds.isNotEmpty()) {
                val loadedRounds = deserializeRounds(serializedRounds)
                _rounds.value = loadedRounds
                _activeEvent.value = loadedRounds.lastOrNull()?.event ?: TableEvent.NONE
            } else {
                val initialEvent = EventEngine.rollForNextRoundEvent(0)
                _activeEvent.value = initialEvent
                _rounds.value = listOf(Round(event = initialEvent))
            }
            calculateSums()
        }
    }

    // Broadcast logic
    fun startBroadcast() {
        if (_isSpectatorActive.value) return
        val newRoomId = (100000..999999).random().toString() // Generates unique 6-digit room code
        _roomId.value = newRoomId
        _isSpectatorActive.value = true

        val database = FirebaseDatabase.getInstance()
        val roomRef = database.getReference("rooms").child(newRoomId)
        val messagesRef = roomRef.child("messages")

        broadcastJob?.cancel()
        broadcastJob = viewModelScope.launch(Dispatchers.IO) {
            publishState()
        }

        reactionListenerJob?.cancel()
        reactionListenerJob = viewModelScope.launch(Dispatchers.IO) {
            messagesRef.addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val type = snapshot.child("type").getValue(String::class.java)
                    val name = snapshot.child("name").getValue(String::class.java) ?: "Yancı"
                    val avatar = snapshot.child("avatar").getValue(String::class.java) ?: "👤"

                    if (type == "reaction") {
                        val emoji = snapshot.child("emoji").getValue(String::class.java) ?: "👏"
                        viewModelScope.launch { _incomingReactions.emit(SpectatorReaction(name, avatar, emoji)) }
                    } else if (type == "chat") {
                        val message = snapshot.child("message").getValue(String::class.java) ?: ""
                        if (message.isNotBlank()) {
                            viewModelScope.launch { _incomingChats.emit(SpectatorChat(senderName = name, senderAvatar = avatar, message = message)) }
                        }
                    }
                    
                    // Cleanup message immediately after processing to save space
                    snapshot.ref.removeValue()
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onChildRemoved(snapshot: DataSnapshot) {}
                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onCancelled(error: DatabaseError) {
                    error.toException().printStackTrace()
                }
            })
        }
    }

    fun stopBroadcast() {
        val currentRoomId = _roomId.value
        broadcastJob?.cancel()
        broadcastJob = null
        reactionListenerJob?.cancel()
        reactionListenerJob = null
        _roomId.value = null
        _isSpectatorActive.value = false

        if (currentRoomId != null) {
            val database = FirebaseDatabase.getInstance()
            val roomRef = database.getReference("rooms").child(currentRoomId)
            roomRef.child("state").child("status").setValue("terminated")
        }
    }

    private suspend fun publishState() {
        val currentRoomId = _roomId.value ?: return
        
        val stateMap = mapOf(
            "teamNames" to _teamNames.value,
            "sums" to _columnSums.value,
            "rounds" to _rounds.value.map { round ->
                mapOf(
                    "score1" to round.score1,
                    "score2" to round.score2,
                    "isScore1Entered" to round.isScore1Entered,
                    "isScore2Entered" to round.isScore2Entered
                )
            },
            "activeEvent" to _activeEvent.value.name,
            "activeEventDialog" to when (val dialog = _activeEventDialog.value) {
                is ActiveEventDialog.None -> null
                is ActiveEventDialog.MysteryBox -> mapOf("type" to "MysteryBox", "teamName" to dialog.teamName, "amount" to dialog.amount)
                is ActiveEventDialog.YanciIhtilali -> mapOf("type" to "YanciIhtilali", "teamName" to dialog.teamName)
                is ActiveEventDialog.GreatSwap -> mapOf("type" to "GreatSwap")
                is ActiveEventDialog.CifteKumar -> mapOf("type" to "CifteKumar", "teamName" to dialog.teamName, "currentPenalty" to dialog.currentPenalty)
                is ActiveEventDialog.CifteKumarResult -> mapOf("type" to "CifteKumarResult", "win" to dialog.win, "finalScore" to dialog.finalScore)
            },
            "winningMessage" to getWinningMessage(),
            "status" to "active"
        )

        withContext(Dispatchers.IO) {
            try {
                val database = FirebaseDatabase.getInstance()
                val stateRef = database.getReference("rooms").child(currentRoomId).child("state")
                stateRef.setValue(stateMap)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun updateTeamName(index: Int, newName: String) {
        if (index in 0..1 && newName.isNotBlank()) {
            val newList = _teamNames.value.toMutableList()
            newList[index] = newName.uppercase()
            _teamNames.value = newList
            saveData()
        }
    }

    fun addRound() {
        val sums = _columnSums.value
        val sum1 = sums.getOrNull(0) ?: 0
        val sum2 = sums.getOrNull(1) ?: 0
        val diff = kotlin.math.abs(sum1 - sum2)
        
        val newEvent = EventEngine.rollForNextRoundEvent(diff)
        _activeEvent.value = newEvent

        val newList = _rounds.value.toMutableList()
        var newRound = Round(event = newEvent)

        // Handle instant events
        when (newEvent) {
            TableEvent.MYSTERY_BOX -> {
                val teamIndex = listOf(0, 1).random()
                val amount = listOf(-202, +202).random()
                if (teamIndex == 0) {
                    newRound = newRound.copy(score1 = amount, isScore1Entered = true)
                } else {
                    newRound = newRound.copy(score2 = amount, isScore2Entered = true)
                }
                _activeEventDialog.value = ActiveEventDialog.MysteryBox(teamIndex, amount, _teamNames.value[teamIndex])
            }
            TableEvent.YANCI_IHTILALI -> {
                val teamIndex = listOf(0, 1).random()
                if (teamIndex == 0) {
                    newRound = newRound.copy(score1 = 101, isScore1Entered = true)
                } else {
                    newRound = newRound.copy(score2 = 101, isScore2Entered = true)
                }
                _activeEventDialog.value = ActiveEventDialog.YanciIhtilali(teamIndex, _teamNames.value[teamIndex])
            }
            TableEvent.GREAT_SWAP -> {
                _activeEventDialog.value = ActiveEventDialog.GreatSwap
            }
            else -> {}
        }

        newList.add(newRound)
        _rounds.value = newList
        saveData()
    }

    fun dismissEventDialog() {
        val currentDialog = _activeEventDialog.value
        _activeEventDialog.value = ActiveEventDialog.None
        saveData()
        
        if (currentDialog is ActiveEventDialog.CifteKumarResult) {
            // After seeing the result, we proceed to add a new round
            addRound()
        }
    }

    fun resolveCifteKumar(acceptRisk: Boolean, roundId: String, teamIndex: Int, currentPenalty: Int) {
        if (!acceptRisk) {
            dismissEventDialog()
            addRound()
            return
        }

        val win = kotlin.random.Random.nextBoolean()
        val finalScore = if (win) currentPenalty * 2 else 0
        
        val newList = _rounds.value.toMutableList()
        val index = newList.indexOfFirst { it.id == roundId }
        if (index != -1) {
            val r = newList[index]
            newList[index] = if (teamIndex == 0) r.copy(score1 = finalScore) else r.copy(score2 = finalScore)
            _rounds.value = newList
            calculateSums()
            saveData()
        }
        
        _activeEventDialog.value = ActiveEventDialog.CifteKumarResult(win, finalScore)
    }

    fun deleteRound(id: String) {
        val newList = _rounds.value.toMutableList()
        val index = newList.indexOfFirst { it.id == id }
        if (index != -1) {
            lastDeletedRound = newList[index]
            lastDeletedIndex = index
            newList.removeAt(index)
            _rounds.value = newList
            calculateSums()
            saveData()
        }
    }

    fun undoDelete() {
        if (lastDeletedRound != null && lastDeletedIndex != -1) {
            val newList = _rounds.value.toMutableList()
            // Ensure index is within bounds
            val safeIndex = lastDeletedIndex.coerceAtMost(newList.size)
            newList.add(safeIndex, lastDeletedRound!!)
            _rounds.value = newList
            calculateSums()
            saveData()
            
            // Clear memory
            lastDeletedRound = null
            lastDeletedIndex = -1
        }
    }

    fun updateCell(roundId: String, colIndex: Int, newValue: Int) {
        val newList = _rounds.value.toMutableList()
        val index = newList.indexOfFirst { it.id == roundId }
        if (index != -1) {
            val round = newList[index]
            val actualValue = if (round.event == TableEvent.KAOS_ELI) newValue * 2 else newValue
            newList[index] = if (colIndex == 0) {
                round.copy(score1 = actualValue, isScore1Entered = true)
            } else {
                round.copy(score2 = actualValue, isScore2Entered = true)
            }
            _rounds.value = newList
            calculateSums()
            saveData()

            val updatedRound = newList[index]
            if (index == newList.size - 1 && updatedRound.isScore1Entered && updatedRound.isScore2Entered) {
                if (updatedRound.event == TableEvent.CIFTE_KUMAR) {
                    val isTeam1Winner = updatedRound.score1 < 0
                    val isTeam2Winner = updatedRound.score2 < 0
                    
                    if (isTeam1Winner && !isTeam2Winner) {
                        _activeEventDialog.value = ActiveEventDialog.CifteKumar(0, _teamNames.value[0], updatedRound.score1, updatedRound.id)
                    } else if (isTeam2Winner && !isTeam1Winner) {
                        _activeEventDialog.value = ActiveEventDialog.CifteKumar(1, _teamNames.value[1], updatedRound.score2, updatedRound.id)
                    } else {
                        addRound()
                    }
                } else {
                    addRound()
                }
            }
        }
    }

    private fun calculateSums() {
        val sum1 = _rounds.value.sumOf { it.score1 }
        val sum2 = _rounds.value.sumOf { it.score2 }
        _columnSums.value = listOf(sum1, sum2)
    }

    fun resetGame() {
        val initialEvent = EventEngine.rollForNextRoundEvent(0)
        _activeEvent.value = initialEvent
        _rounds.value = listOf(Round(event = initialEvent))
        lastDeletedRound = null
        lastDeletedIndex = -1
        calculateSums()
        saveData()
    }

    fun getWinningMessage(): String {
        val sums = _columnSums.value
        val names = _teamNames.value
        val sum1 = sums.getOrNull(0) ?: 0
        val sum2 = sums.getOrNull(1) ?: 0
        val diff = kotlin.math.abs(sum1 - sum2)

        return when {
            sum1 < sum2 -> "${names[0]} ÖNDE! 😎\nFARK: $diff"
            sum2 < sum1 -> "${names[1]} ÖNDE! 🥶\nFARK: $diff"
            else -> "BERABERE! ⚔️\nFARK: 0"
        }
    }
}
