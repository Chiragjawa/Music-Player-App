package com.example.musicplayer.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

    private val _albumResults = MutableStateFlow<List<String>>(emptyList())
    val albumResults = _albumResults.asStateFlow()

    init {
        loadRandomSongs()
    }

    fun clearResults() {
        _songs.value = emptyList()
        _artistResults.value = emptyList()
        _albumResults.value = emptyList()
    }

    private fun loadRandomSongs() {
        viewModelScope.launch {
            _randomSongs.value = repository
                .searchSongs("popular")
                .shuffled()
                .take(10)
        }
    }

    fun searchSongs(query: String) {
        viewModelScope.launch {
            _songs.value =
                if (query.isBlank()) emptyList()
                else repository.searchSongs(query)
        }
    }

    fun searchArtists(query: String) {
        viewModelScope.launch {
            if (query.isBlank()) {
                _artistResults.value = emptyList()
                return@launch
            }

            val songs = repository.searchSongs(query)
            _artistResults.value =
                songs.flatMap { it.artists.split(",") }
                    .map { it.trim() }
                    .distinct()
        }
    }

    fun searchAlbums(query: String) {
        viewModelScope.launch {
            _albumResults.value =
                if (query.isBlank()) emptyList()
                else listOf(query) // placeholder until album API wired
        }
    }
}
