package com.example.musicplayer.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.musicplayer.data.model.Song
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "queue_prefs")

@Singleton
class QueueDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val QUEUE_KEY = stringPreferencesKey("queue")
    private val CURRENT_INDEX_KEY = intPreferencesKey("current_index")

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    suspend fun saveQueue(queue: List<Song>, currentIndex: Int) {
        context.dataStore.edit { preferences ->
            val queueJson = json.encodeToString(queue)
            preferences[QUEUE_KEY] = queueJson
            preferences[CURRENT_INDEX_KEY] = currentIndex
        }
    }

    fun getQueue(): Flow<Pair<List<Song>, Int>> {
        return context.dataStore.data.map { preferences ->
            val queueJson = preferences[QUEUE_KEY] ?: ""
            val queue = if (queueJson.isNotEmpty()) {
                try {
                    json.decodeFromString<List<Song>>(queueJson)
                } catch (e: Exception) {
                    emptyList()
                }
            } else {
                emptyList()
            }
            val index = preferences[CURRENT_INDEX_KEY] ?: -1
            queue to index
        }
    }

    suspend fun clearQueue() {
        context.dataStore.edit { preferences ->
            preferences.remove(QUEUE_KEY)
            preferences.remove(CURRENT_INDEX_KEY)
        }
    }
}
