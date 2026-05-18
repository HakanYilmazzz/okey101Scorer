package com.example.okey101scorer

import android.app.Application
import android.content.Context
import androidx.core.content.edit
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
import java.util.UUID
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ChildEventListener

data class Round(
    val id: String = UUID.randomUUID().toString(),
    val score1: Int = 0,
    val score2: Int = 0,
    val isScore1Entered: Boolean = false,
    val isScore2Entered: Boolean = false
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

class ScoreViewModel(application: Application) : AndroidViewModel(application) {

    private val sharedPrefs = application.getSharedPreferences("okey_101_scorer_prefs", Context.MODE_PRIVATE)

    private val _teamNames = MutableStateFlow(listOf("BİZ", "ONLAR"))
    val teamNames: StateFlow<List<String>> = _teamNames.asStateFlow()

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

    private fun serializeRounds(roundsList: List<Round>): String {
        return roundsList.joinToString(";") { 
            "${it.id},${it.score1},${it.score2},${it.isScore1Entered},${it.isScore2Entered}" 
        }
    }

    private fun deserializeRounds(serialized: String): List<Round> {
        if (serialized.isBlank()) return listOf(Round())
        return try {
            serialized.split(";").map { row ->
                val parts = row.split(",")
                Round(
                    id = parts[0],
                    score1 = parts[1].toIntOrNull() ?: 0,
                    score2 = parts[2].toIntOrNull() ?: 0,
                    isScore1Entered = parts[3].toBooleanStrictOrNull() ?: false,
                    isScore2Entered = parts[4].toBooleanStrictOrNull() ?: false
                )
            }
        } catch (_: Exception) {
            listOf(Round())
        }
    }

    private fun saveData() {
        val serializedRounds = serializeRounds(_rounds.value)
        val serializedTeamNames = _teamNames.value.joinToString(",")
        sharedPrefs.edit {
            putString("rounds", serializedRounds)
            putString("teamNames", serializedTeamNames)
        }

        // Trigger immediate live broadcast if active
        if (_isSpectatorActive.value) {
            viewModelScope.launch(Dispatchers.IO) {
                publishState()
            }
        }
    }

    private fun loadData() {
        val serializedRounds = sharedPrefs.getString("rounds", "") ?: ""
        val serializedTeamNames = sharedPrefs.getString("teamNames", "") ?: ""

        if (serializedTeamNames.isNotEmpty()) {
            val names = serializedTeamNames.split(",")
            if (names.size == 2) {
                _teamNames.value = names
            }
        }

        if (serializedRounds.isNotEmpty()) {
            _rounds.value = deserializeRounds(serializedRounds)
        } else {
            _rounds.value = listOf(Round())
        }
        calculateSums()
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
        val newList = _rounds.value.toMutableList()
        newList.add(Round())
        _rounds.value = newList
        saveData()
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
            newList[index] = if (colIndex == 0) {
                round.copy(score1 = newValue, isScore1Entered = true)
            } else {
                round.copy(score2 = newValue, isScore2Entered = true)
            }
            _rounds.value = newList
            calculateSums()
            saveData()

            val updatedRound = newList[index]
            if (index == newList.size - 1 && updatedRound.isScore1Entered && updatedRound.isScore2Entered) {
                addRound()
            }
        }
    }

    private fun calculateSums() {
        val sum1 = _rounds.value.sumOf { it.score1 }
        val sum2 = _rounds.value.sumOf { it.score2 }
        _columnSums.value = listOf(sum1, sum2)
    }

    fun resetGame() {
        _rounds.value = listOf(Round())
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
