package com.example.musicplayer.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicplayer.data.model.Song
import com.example.musicplayer.data.repository.MusicRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ArtistViewModel @Inject constructor(
    private val repository: MusicRepository
) : ViewModel() {

    private val _songs = MutableStateFlow<List<Song>>(emptyList())
    val songs: StateFlow<List<Song>> = _songs.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _artistName = MutableStateFlow("")
    val artistName: StateFlow<String> = _artistName.asStateFlow()

    /**
     * Load artist songs using the dedicated artist API
     */
    fun loadArtistSongs(artistQuery: String) {
        if (artistQuery.isBlank()) return

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val result = repository.searchArtistWithSongs(artistQuery)

                result.onSuccess { (name, songList) ->
                    _artistName.value = name
                    _songs.value = songList

                    if (songList.isEmpty()) {
                        _error.value = "No songs found for this artist"
                    }
                }.onFailure { exception ->
                    _error.value = exception.message ?: "Failed to load artist"
                    _songs.value = emptyList()
                }

            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error occurred"
                _songs.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Clear data when leaving artist screen
     */
    fun clearData() {
        _songs.value = emptyList()
        _artistName.value = ""
        _error.value = null
    }
}