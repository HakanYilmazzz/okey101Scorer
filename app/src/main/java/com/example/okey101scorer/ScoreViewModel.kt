package com.example.okey101scorer

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

data class Round(
    val id: String = UUID.randomUUID().toString(),
    val score1: Int = 0,
    val score2: Int = 0,
    val isScore1Entered: Boolean = false,
    val isScore2Entered: Boolean = false
)

class ScoreViewModel(application: Application) : AndroidViewModel(application) {

    private val sharedPrefs = application.getSharedPreferences("okey_101_scorer_prefs", Context.MODE_PRIVATE)

    private val _teamNames = MutableStateFlow(listOf("BİZ", "ONLAR"))
    val teamNames: StateFlow<List<String>> = _teamNames.asStateFlow()

    private val _rounds = MutableStateFlow<List<Round>>(listOf(Round()))
    val rounds: StateFlow<List<Round>> = _rounds.asStateFlow()

    private val _columnSums = MutableStateFlow<List<Int>>(listOf(0, 0))
    val columnSums: StateFlow<List<Int>> = _columnSums.asStateFlow()

    // Spectator Mode State
    private val _isSpectatorActive = MutableStateFlow(false)
    val isSpectatorActive: StateFlow<Boolean> = _isSpectatorActive.asStateFlow()

    private val _roomId = MutableStateFlow<String?>(null)
    val roomId: StateFlow<String?> = _roomId.asStateFlow()

    private var broadcastJob: Job? = null

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
        } catch (e: Exception) {
            listOf(Round())
        }
    }

    private fun saveData() {
        val serializedRounds = serializeRounds(_rounds.value)
        val serializedTeamNames = _teamNames.value.joinToString(",")
        sharedPrefs.edit()
            .putString("rounds", serializedRounds)
            .putString("teamNames", serializedTeamNames)
            .apply()

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

        broadcastJob?.cancel()
        broadcastJob = viewModelScope.launch(Dispatchers.IO) {
            while (isActive) {
                publishState()
                delay(4000) // Periodic update every 4 seconds
            }
        }
    }

    fun stopBroadcast() {
        broadcastJob?.cancel()
        broadcastJob = null
        _roomId.value = null
        _isSpectatorActive.value = false
    }

    private suspend fun publishState() {
        val currentRoomId = _roomId.value ?: return
        val root = JSONObject().apply {
            put("teamNames", JSONArray(_teamNames.value))
            put("sums", JSONArray(_columnSums.value))
            
            val roundsArray = JSONArray()
            _rounds.value.forEach { round ->
                val roundObj = JSONObject().apply {
                    put("score1", round.score1)
                    put("score2", round.score2)
                    put("isScore1Entered", round.isScore1Entered)
                    put("isScore2Entered", round.isScore2Entered)
                }
                roundsArray.put(roundObj)
            }
            put("rounds", roundsArray)
            put("winningMessage", getWinningMessage())
        }
        val jsonPayload = root.toString()

        withContext(Dispatchers.IO) {
            try {
                val url = URL("https://ntfy.sh/okey101_room_$currentRoomId")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "application/json")
                
                val writer = OutputStreamWriter(connection.outputStream, "UTF-8")
                writer.write(jsonPayload)
                writer.flush()
                writer.close()

                val responseCode = connection.responseCode
                connection.disconnect()
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
        val diff = Math.abs(sum1 - sum2)

        return when {
            sum1 < sum2 -> "${names[0]} ÖNDE! 😎\nFARK: $diff"
            sum2 < sum1 -> "${names[1]} ÖNDE! 🥶\nFARK: $diff"
            else -> "BERABERE! ⚔️\nFARK: 0"
        }
    }
}
