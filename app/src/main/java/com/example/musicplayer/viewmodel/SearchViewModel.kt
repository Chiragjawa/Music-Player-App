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

    private val _hasMoreSongs = MutableStateFlow(true)
    val hasMoreSongs = _hasMoreSongs.asStateFlow()

    private val _hasMoreArtists = MutableStateFlow(true)
    val hasMoreArtists = _hasMoreArtists.asStateFlow()

    private val _hasMoreAlbums = MutableStateFlow(true)
    val hasMoreAlbums = _hasMoreAlbums.asStateFlow()

    private var currentSongPage = 0
    private var currentArtistPage = 0
    private var currentAlbumPage = 0
    private var lastSongQuery = ""
    private var lastArtistQuery = ""
    private var lastAlbumQuery = ""

    companion object {
        const val PAGE_SIZE = 20
    }

    init {
        loadRandomSongs()
    }

    fun clearResults() {
        _songs.value = emptyList()
        _artistResults.value = emptyList()
        _albumResults.value = emptyList()
        _errorMessage.value = null
        currentSongPage = 0
        currentArtistPage = 0
        currentAlbumPage = 0
        lastSongQuery = ""
        lastArtistQuery = ""
        lastAlbumQuery = ""
        _hasMoreSongs.value = true
        _hasMoreArtists.value = true
        _hasMoreAlbums.value = true
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

    fun searchSongs(query: String, loadMore: Boolean = false) {
        viewModelScope.launch {
            if (query.isBlank()) {
                _songs.value = emptyList()
                return@launch
            }

            try {
                _isLoading.value = true
                _errorMessage.value = null

                if (!loadMore || query != lastSongQuery) {
                    currentSongPage = 0
                    lastSongQuery = query
                    _hasMoreSongs.value = true
                }

                val newSongs = repository.searchSongs(query, page = currentSongPage, limit = PAGE_SIZE)

                if (loadMore && currentSongPage > 0) {
                    _songs.value = _songs.value + newSongs
                } else {
                    _songs.value = newSongs
                }

                _hasMoreSongs.value = newSongs.size >= PAGE_SIZE
                if (newSongs.isNotEmpty()) {
                    currentSongPage++
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to search songs: ${e.message}"
                if (!loadMore) _songs.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun searchArtists(query: String, loadMore: Boolean = false) {
        viewModelScope.launch {
            if (query.isBlank()) {
                _artistResults.value = emptyList()
                return@launch
            }

            try {
                _isLoading.value = true
                _errorMessage.value = null

                if (!loadMore || query != lastArtistQuery) {
                    currentArtistPage = 0
                    lastArtistQuery = query
                    _hasMoreArtists.value = true
                }

                val songs = repository.searchSongs(query, page = currentArtistPage, limit = PAGE_SIZE * 2)
                val newArtists = songs.flatMap { it.artists.split(",") }
                    .map { it.trim() }
                    .distinct()
                    .take(PAGE_SIZE)

                if (loadMore && currentArtistPage > 0) {
                    val existingArtists = _artistResults.value.toSet()
                    val uniqueNewArtists = newArtists.filterNot { it in existingArtists }
                    _artistResults.value = _artistResults.value + uniqueNewArtists
                } else {
                    _artistResults.value = newArtists
                }

                _hasMoreArtists.value = newArtists.size >= PAGE_SIZE
                if (newArtists.isNotEmpty()) {
                    currentArtistPage++
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to search artists: ${e.message}"
                if (!loadMore) _artistResults.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun searchAlbums(query: String, loadMore: Boolean = false) {
        viewModelScope.launch {
            if (query.isBlank()) {
                _albumResults.value = emptyList()
                return@launch
            }

            try {
                _isLoading.value = true
                _errorMessage.value = null

                if (!loadMore || query != lastAlbumQuery) {
                    currentAlbumPage = 0
                    lastAlbumQuery = query
                    _hasMoreAlbums.value = true
                }

                val newAlbums = repository.searchAlbums(query, page = currentAlbumPage, limit = PAGE_SIZE)

                if (loadMore && currentAlbumPage > 0) {
                    _albumResults.value = _albumResults.value + newAlbums
                } else {
                    _albumResults.value = newAlbums
                }

                _hasMoreAlbums.value = newAlbums.size >= PAGE_SIZE
                if (newAlbums.isNotEmpty()) {
                    currentAlbumPage++
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to search albums: ${e.message}"
                if (!loadMore) _albumResults.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }
}
