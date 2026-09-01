package com.atomykcoder.atomykplay.service.audio_source

import com.atomykcoder.atomykplay.models.Music

/**
 * Interface for resolving audio sources from Music objects
 * Abstracts where audio comes from (local, streaming, cached)
 */
interface AudioSourceResolver {
    /**
     * Resolves a playable audio source from a Music object
     * @param music The music object to resolve source for
     * @return A Source object that can be played by MediaPlayer
     */
    fun resolveSource(music: Music): Source

    /**
     * Represents a playable audio source
     */
    sealed class Source {
        data class LocalFile(val filePath: String) : Source()
        data class UriSource(val uriString: String) : Source()
        // Add more source types as needed (e.g., for streaming)
    }
}