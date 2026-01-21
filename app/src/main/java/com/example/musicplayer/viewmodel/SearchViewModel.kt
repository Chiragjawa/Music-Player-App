package com.example.musicplayer.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicplayer.data.model.Album
import com.example.musicplayer.data.model.Song
import com.example.musicplayer.data.repository.MusicRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: MusicRepository
) : ViewModel() {

    private val _songs = MutableStateFlow<List<Song>>(emptyList())
    val songs = _songs.asStateFlow()

    private val _randomSongs = MutableStateFlow<List<Song>>(emptyList())
    val randomSongs = _randomSongs.asStateFlow()

    private val _artistResults = MutableStateFlow<List<String>>(emptyList())
    val artistResults = _artistResults.asStateFlow()

    private val _albumResults = MutableStateFlow<List<Album>>(emptyList())
    val albumResults = _albumResults.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    init {
        loadRandomSongs()
    }

    fun clearResults() {
        _songs.value = emptyList()
        _artistResults.value = emptyList()
        _albumResults.value = emptyList()
        _errorMessage.value = null
    }

    fun clearError() {
        _errorMessage.value = null
    }

    private fun loadRandomSongs() {
        viewModelScope.launch {
            try {
                _randomSongs.value = repository
                    .searchSongs("popular")
                    .shuffled()
                    .take(10)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load suggestions: ${e.message}"
            }
        }
    }

    fun searchSongs(query: String) {
        viewModelScope.launch {
            if (query.isBlank()) {
                _songs.value = emptyList()
                return@launch
            }

            try {
                _isLoading.value = true
                _errorMessage.value = null
                _songs.value = repository.searchSongs(query)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to search songs: ${e.message}"
                _songs.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun searchArtists(query: String) {
        viewModelScope.launch {
            if (query.isBlank()) {
                _artistResults.value = emptyList()
                return@launch
            }

            try {
                _isLoading.value = true
                _errorMessage.value = null
                val songs = repository.searchSongs(query)
                _artistResults.value =
                    songs.flatMap { it.artists.split(",") }
                        .map { it.trim() }
                        .distinct()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to search artists: ${e.message}"
                _artistResults.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun searchAlbums(query: String) {
        viewModelScope.launch {
            if (query.isBlank()) {
                _albumResults.value = emptyList()
                return@launch
            }

            try {
                _isLoading.value = true
                _errorMessage.value = null
                _albumResults.value = repository.searchAlbums(query)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to search albums: ${e.message}"
                _albumResults.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }
}
