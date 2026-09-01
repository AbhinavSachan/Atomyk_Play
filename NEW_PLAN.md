# Atomyk_Play Architecture Refactoring Plan

## Overview
This document outlines the architectural improvements for the Atomyk_Play Android music application, transitioning from a monolithic architecture to a clean, feature-oriented architecture with clear separation of concerns.

## Current Issues Identified
1. God Service Anti-pattern (MediaPlayerService has too many responsibilities)
2. Implicit State Management (scattered SharedPreferences, static variables)
3. Tight Coupling Between Layers (UI directly accessing service properties)
4. Inefficient Progress Updates (Handler polling every 300ms)
5. Broadcast Overuse (multiple broadcast actions for simple state changes)
6. Lack of Separation Between Concerns
7. Memory Leak Risks (Handler references, EventBus registrations)
8. Scalability Issues (difficult to add new features)

## Proposed Architecture

### High-Level Structure
```
Presentation Layer (UI)
        ↓ (Unidirectional data flow)
ViewModels / Presentation Layer
        ↓ (Use cases)
Domain Layer (Use Cases, Interfaces)
        ↓ (Repositories)
Data Layer (Repositories, Sources)
        ↓
Implementation Details (MediaPlayer, Network, Database, File System)
```

### Core Services
1. **PlayerService**: Handles only playback control (state machine)
2. **AudioSourceResolver**: Abstracts where audio comes from (local, streaming, cached)
3. **LyricsService**: Handles lyrics fetching, parsing, caching
4. **MusicLibraryService**: Handles music metadata, playlists, favorites
5. **PlaybackStateManager**: Single source of truth for playback state (StateFlow)
6. **NotificationHandler**: Manages media notifications and MediaSession
7. **AudioFocusManager**: Handles audio focus requests

### Service Responsibility Matrix
| Responsibility | Owner | Why |
| -------------- | ----- | --- |
| Play/pause/seek/skip | PlayerService | Single responsibility for playback transport controls |
| Audio focus management | AudioFocusManager | Separates audio policy from playback mechanics |
| MediaSession & notifications | NotificationHandler | Isolates Android-specific media APIs |
| Current track information | PlaybackStateManager | Single source of truth for playback state |
| Queue management | PlayerService (with MusicLibraryService) | Queue is inherently tied to playback |
| Lyrics fetching/parsing | LyricsService | Separates concern from playback |
| Music metadata/storage | MusicLibraryService | Centralizes library operations |
| Audio source resolution (local/stream) | AudioSourceResolver | Abstracts playback from source |
| Equalizer/audio effects | PlayerService (delegated) | Related to audio output |
| Timer/sleep functionality | PlayerService (delegated) | Playback lifecycle concern |
| UI state observation | ViewModels (collecting from StateFlows) | Reactive UI updates |
| Persistent storage | Repository implementations | Clean persistence abstraction |

### State Architecture
**Global Music State Lives In:**
- **PlaybackStateManager** (Singleton with StateFlow):
  - Current track: `StateFlow<Music?>`
  - Playback state: `StateFlow<PlaybackState>` (playing/paused/buffering/stopped)
  - Position: `StateFlow<Pair<Int, Int>>` (currentPosition, duration)
  - Queue: `StateFlow<List<Music>>`
  - Playback mode: `StateFlow<RepeatMode>`, `StateFlow<ShuffleMode>`
  - Loading/error states: `StateFlow<LoadState>`

### Package/Module Structure
```
com.atomykcoder.atomykplay
├── data
│   ├── local (Room database, SharedPreferences wrappers)
│   ├── mock (for testing)
│   └── model (data classes: Music, Playlist, LRCMap, etc.)
├── domain
│   ├── model (business logic models if different from data)
│   ├── repository (interfaces)
│   └── usecase
├── presentation
│   ├── ui (Compose/XML UI, Fragments, Activities)
│   │   ├── components (reusable UI pieces)
│   │   ├── screens (navigation destinations)
│   │   └── theme
│   └── viewmodel
└── service
    ├── player
    ├── audio_source
    ├── lyrics
    ├── library
    ├── notification
    └── audiofocus
```

### Key Interfaces

```kotlin
// PlayerService Interface
interface PlayerService {
    fun play()
    fun pause()
    fun stop()
    fun seekTo(position: Int)
    fun skipToNext()
    fun skipToPrevious()
    fun setQueue(queue: List<Music>, startIndex: Int = 0)
    fun setRepeatMode(mode: RepeatMode)
    fun setShuffleMode(mode: ShuffleMode)
    
    val playbackState: StateFlow<PlaybackState>
    val currentTrack: StateFlow<Music?>
    val position: StateFlow<Pair<Int, Int>>
    val queue: StateFlow<List<Music>>
}

// AudioSourceResolver Interface
interface AudioSourceResolver {
    fun resolveSource(music: Music): Source
    // Returns something playable by Player (FileDescriptor, Uri, etc.)
}

// LyricsService Interface
interface LyricsService {
    fun getLyrics(musicId: String): Flow<Lyrics>
    fun cacheLyrics(musicId: String, lyrics: Lyrics)
    fun clearCache(musicId: String?)
}

// MusicLibraryService Interface
interface MusicLibraryService {
    fun getMusicLibrary(): Flow<List<Music>>
    fun getPlaylists(): Flow<List<Playlist>>
    fun saveFavorite(music: Music)
    fun removeFavorite(music: Music)
    fun getFavorites(): Flow<List<Music>>
    // ... playlist CRUD
}

// PlaybackState Models
data class PlaybackState(
    val isPlaying: Boolean,
    val isBuffering: Boolean,
    val error: Throwable?,
    val audioAttributes: AudioAttributes? = null
)

data class AudioAttributes(
    val title: String?,
    val artist: String?,
    val albumArt: Uri?
)
```

## Migration Plan

### Phase 1: Foundation & State Management (Weeks 1-2)
**Goal**: Establish single source of truth for playback state
- Create PlaybackStateManager with StateFlows
- Refactor MediaPlayerService to update StateFlows instead of direct UI updates
- Replace Handler-based seekbar updates with StateFlow collection
- Update MainActivity/ViewModels to collect from StateFlows
- **Risk**: State synchronization issues during transition
- **Verification**: UI shows correct state, no regressions in playback controls

### Phase 2: Service Decomposition (Weeks 3-4)
**Goal**: Split MediaPlayerService responsibilities
- Extract AudioFocusManager from MediaPlayerService
- Extract NotificationHandler (MediaSession/notifications)
- Extract LyricsService (lyrics loading/caching)
- Keep core transport controls in PlayerService
- **Risk**: Breaking audio focus or notification behavior
- **Verification**: Audio focus works correctly, notifications display properly

### Phase 3: Architecture Layers (Weeks 5-6)
**Goal**: Establish clean dependency directions
- Create domain layer with use cases and repository interfaces
- Implement repository layer using existing StorageUtil/MusicRepo
- Update ViewModels to use use cases instead of direct service/repository calls
- Ensure no Android Framework dependencies in domain/data layers
- **Risk**: Circular dependencies, overly complex indirection
- **Dependency Check**: Verify no UI/Android imports in domain/data packages

### Phase 4: UI Modernization Preparation (Weeks 7-8)
**Goal**: Prepare for potential Compose migration or better UI separation
- Create presentation layer with clear ViewModel responsibilities
- Move UI-specific state to ViewModels
- Ensure Fragments/Activities are purely UI observers
- Extract UI logic from service callbacks
- **Risk**: ViewModels becoming too large
- **Verification**: UI layer only observes state, doesn't contain business logic

### Phase 5: Cleanup & Optimization (Weeks 9-10)
**Goal**: Remove legacy code and optimize
- Remove EventBus usage in favor of StateFlows
- Remove BroadcastIntents for internal communication
- Clean up SharedPreferences usage through repositories
- Optimize memory usage, remove singleton anti-patterns
- Add comprehensive unit tests for services and use cases
- **Risk**: Removing still-used legacy paths
- **Verification**: All functionality works, test coverage >80% for new code

## Testing Strategy

### Unit Testing:
- **Services**: Test PlayerService, AudioFocusManager, NotificationHandler in isolation
- **Use Cases**: Test business logic with mocked repositories
- **Repositories**: Test data mapping and error handling
- **ViewModels**: Test state transformations and use case triggering
- **StateFlows**: Test flow combination, distinctUntilChanged, buffering

### Instrumentation Testing:
- **Service Lifecycle**: Test service binding, foreground service behavior
- **Audio Focus**: Test focus gain/loss scenarios
- **Notifications**: Test notification actions and MediaSession callbacks
- **Playback Integration**: Test play/pause/skip sequences with mock audio sources

### Testing Approach:
1. Use MockK for mocking dependencies
2. Use coroutine-test for Flow/StateFlow testing
3. Use AndroidJUnitRunner for instrumentation tests
4. Test edge cases: interruptions, audio focus loss, phone calls
5. Verify no memory leaks in service/ViewModel scenarios

## Risks / Trade-offs

### Trade-offs Made:
1. **StateFlow vs LiveData**: Chose StateFlow for better Kotlin coroutines integration and flow operators
2. **Single Service vs Multiple Services**: Kept PlayerService as primary playback controller but extracted concerns
3. **Repository Layer**: Added abstraction layer even though current implementation is simple - prepares for future complexity (network, multiple sources)
4. **Explicit Interfaces**: Created interfaces for testability and flexibility despite some being "premature"

### Risks:
1. **State Synchronization**: During migration, ensuring StateFlow and legacy state stay in sync
2. **Performance Overhead**: Minimal from Flow operators, but StateFlow operations are very lightweight
3. **Team Familiarity**: Team may need to learn Flow patterns if unfamiliar
4. **Over-engineering Risk**: Mitigated by starting simple and only adding complexity as needed

### Risk Mitigation:
1. **Incremental Migration**: Keep legacy paths working until new paths verified
2. **Feature Flags**: Use build variants or flags to enable/disable new architecture during transition
3. **Comprehensive Logging**: Add trace logs to compare legacy vs new behavior
4. **Automated Regression Testing**: Existing manual test procedures automated where possible