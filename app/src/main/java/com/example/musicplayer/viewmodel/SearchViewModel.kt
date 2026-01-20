package com.example.musicplayer.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicplayer.data.model.Song
import com.example.musicplayer.data.repository.MusicRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: MusicRepository
) : ViewModel() {

    private val _songs = MutableStateFlow<List<Song>>(emptyList())
    val songs: StateFlow<List<Song>> = _songs

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _noResults = MutableStateFlow(false)
    val noResults: StateFlow<Boolean> = _noResults

    fun search(query: String) {
        if (query.isBlank()) {
            _error.value = "Please enter a search term"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val result = repository.searchSongs(query)

                result.onSuccess { list ->
                    _songs.value = list
                    _noResults.value = list.isEmpty()
                }.onFailure {
                    _error.value = it.message ?: "Unknown error occurred"
                    _noResults.value = false
                }

            } finally {
                _isLoading.value = false
            }

        }
    }
}
