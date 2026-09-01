package com.atomykcoder.atomykplay.state

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.atomykcoder.atomykplay.models.Music
import com.atomykcoder.atomykplay.utils.StorageUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Singleton class that manages the favorite songs state using StateFlows.
 * This serves as the single source of truth for favorite-related data.
 */
class FavoriteStateManager(private val storageUtil: StorageUtil) : ViewModel() {

    // StateFlows for favorite state
    private val _favoriteIds = MutableStateFlow<Set<String>>(loadFavoriteIds())
    val favoriteIds: StateFlow<Set<String>> = _favoriteIds.asStateFlow()

    /**
     * Loads favorite IDs from StorageUtil
     */
    private fun loadFavoriteIds(): Set<String> {
        return storageUtil.favouriteList.map { it.id }.toSet()
    }

    /**
     * Adds a music to favorites
     * @param music The music to add to favorites
     */
    fun addFavorite(music: Music?) {
        if (music == null) return
        viewModelScope.launch {
            val currentFavorites = _favoriteIds.value
            val updatedFavorites = currentFavorites + music.id
            _favoriteIds.value = updatedFavorites
            // Persist to storage
            storageUtil.saveFavorite(music)
        }
    }

    /**
     * Removes a music from favorites
     * @param music The music to remove from favorites
     */
    fun removeFavorite(music: Music?) {
        if (music == null) return
        viewModelScope.launch {
            val currentFavorites = _favoriteIds.value
            val updatedFavorites = currentFavorites - music.id
            _favoriteIds.value = updatedFavorites
            // Persist to storage
            storageUtil.removeFavorite(music)
        }
    }

    /**
     * Checks if a music is in favorites
     * @param music The music to check
     * @return true if the music is in favorites
     */
    fun isFavorite(music: Music?): Boolean {
        if (music == null) return false
        return music.id in _favoriteIds.value
    }
}