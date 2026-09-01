package com.atomykcoder.atomykplay.state

import com.atomykcoder.atomykplay.utils.StorageUtil

/**
 * Simple singleton holder for state managers to enable access from Service and ViewModel
 * This is a temporary solution for migration - in a full DI setup, this would be provided through proper dependency injection
 */
object StateHolder {
    val playbackStateManager = PlaybackStateManager()
    lateinit var favoriteStateManager: FavoriteStateManager

    /**
     * Initialize favorite state manager with storage util
     * Should be called from Application or MainActivity
     */
    fun initFavoriteStateManager(storageUtil: StorageUtil) {
        favoriteStateManager = FavoriteStateManager(storageUtil)
    }
}