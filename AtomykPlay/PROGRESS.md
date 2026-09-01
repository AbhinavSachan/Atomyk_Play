# Progress Report: Atomyk_Play Architecture Refactoring

## Build status: COMPILES CLEANLY (verified 2026-09-02)
`./gradlew.bat :app:assembleDebug` succeeds and produces `app/build/outputs/apk/debug/app-debug.apk`.
This was NOT true at the start of this session — the working tree had ~40 real compile errors
spread across 4 files, left by an earlier session that ran out of context mid-edit. All are now
fixed (see "Fixed this session" below).

**Manual verification (user-reported, 2026-09-02, later in the same day): music plays and the
player UI updates correctly.** This confirms the core `currentTrack`/`playbackState`/`position`
StateFlow wiring in `BottomSheetPlayerFragment`/`MainActivity` works end to end. Favorites
list sync, the "remove from playlist" option-sheet action, and the sleep timer countdown text
(all fixed in the second round of changes below, *after* that manual test) have **not**
specifically been re-verified — the manual test predates those fixes.

## Architecture direction
See [NEW_PLAN.md](../NEW_PLAN.md) (repo root) for the full target architecture and 5-phase
migration plan: monolithic `MediaPlayerService` + EventBus + SharedPreferences →
Presentation → ViewModel → Domain/UseCase → Repository, with `PlaybackStateManager` /
`FavoriteStateManager` (StateFlow-based, in `state/`) as the single source of truth.

## Fixed this session (2026-09-02)
The previous session's WIP compiled to ~40 errors. Root causes and fixes:

1. **`MainActivity.kt` missing `refreshLyricsInBottomSheet()`** — `AddLyricsFragment.kt` called
   `(activity as MainActivity).refreshLyricsInBottomSheet()` but the method didn't exist.
   Added it: calls `bottomSheetPlayerFragment?.runnableSyncLyrics(null)`.
2. **`BottomSheetPlayerFragment.kt` — dead EventBus post.** The fragment is never registered
   with `EventBus.getDefault().register(...)` anywhere (confirmed via repo-wide grep), so its
   `@Subscribe` methods only ever ran when called directly as plain functions. The one exception,
   `EventBus.getDefault().post(PrepareRunnableEvent())` inside `runnableSyncLyrics`, was a real
   latent bug — it posted to nobody, so the lyrics auto-scroll runnable never started. Replaced
   with a direct call to `prepareRunnable(null)` and dropped `@Subscribe` from both methods.
3. **Duplicated/wrong imports in `BottomSheetPlayerFragment.kt` and `MainActivity.kt`.** A prior
   edit pass deleted the `com.atomykcoder.atomykplay.events.*` imports (needed by the 6 remaining
   `@Subscribe` methods per file: `setMainPlayerLayout`, `setPlayerImages`,
   `handleMusicProgressUpdate`, `handleMusicImageUpdate`, `setTimerLiveText`, `setTimerFinished`,
   plus `stopAnimText`/`StopTextAnim`) while adding new `state.*`/coroutines imports, but pasted
   the new import block in **twice** and used a nonexistent package (`kotlinx.coroutines.lifecycle.viewLifecycleOwner` /
   `kotlinx.coroutines.lifecycle.lifecycleScope` — the real symbol is `androidx.lifecycle.lifecycleScope`,
   and `viewLifecycleOwner` needs no import at all inside a `Fragment`). Restored the event
   imports, deduped, and fixed the `lifecycleScope` import in both files.
4. **`BottomSheetPlayerFragment.kt` — `stopAnimText()` deleted entirely**, not just its
   `@Subscribe` annotation, even though it's called directly from 4 places. Restored the method
   (without `@Subscribe`, same reasoning as #2).
5. **Sequential `collectLatest` calls inside one coroutine — real functional bug.** Both
   `BottomSheetPlayerFragment.setObserverForBottomSheetHeight`-adjacent StateFlow setup (7 flows)
   and `MainActivity.setupViewModel()` (3 flows) had multiple `SomeFlow.collectLatest { }` calls
   back-to-back inside a single `launchWhenStarted { }` block. `collectLatest` suspends forever,
   so **only the first flow in each block was ever actually collected** — playbackState, position,
   queue, repeatMode, shuffleMode, and loadState collection in the fragment, and playbackState/
   position collection in MainActivity, were dead code that never ran. Fixed by wrapping each
   `collectLatest` in its own `launch { }` child coroutine.
6. **`state == PlaybackStateManager.PlaybackState.PLAYING`** — `PlaybackState` is a data class
   (`isPlaying: Boolean, isBuffering: Boolean, error: Throwable?`), not an enum with a `PLAYING`
   constant. Fixed to `state.isPlaying`. Same bug for `loadState == PlaybackStateManager.LoadState.LOADING`
   → fixed to `loadState.isLoading`.
7. **`StateHolder.playbackStateManager.getCurrentIndex()`** — method doesn't exist on
   `PlaybackStateManager`. Fixed by computing the index inline from the queue + current track id
   inside the `queue.collectLatest` block instead.
8. **`FavoriteStateManager.kt` called `StorageUtil` methods that don't exist**:
   `getFavoriteList()` → real API is the `favouriteList: ArrayList<Music>` property;
   `addFavorite(music)` → real method is `saveFavorite(music)`. Fixed both call sites.
9. **Missing `kotlinx.coroutines.flow.asStateFlow` import** in `PlaybackStateManager.kt` and
   `FavoriteStateManager.kt` (both call `.asStateFlow()` without importing it).
10. **`StateHolder.kt` missing `import com.atomykcoder.atomykplay.utils.StorageUtil`** —
    referenced as a parameter type in `initFavoriteStateManager`.
11. **`service/player/PlayerService.kt`** declared its own nested `PlaybackState`/`RepeatMode`/
    `ShuffleMode` types, separate from and incompatible with `PlaybackStateManager`'s versions of
    the same names, even though `PlayerServiceImpl` assigns `PlaybackStateManager` values to them.
    Removed the interface's duplicate nested types; it now references `PlaybackStateManager.*`
    directly (the actual single source of truth). Also removed the `= 0` default argument from
    `override fun setQueue(...)` — Kotlin forbids default parameter values on an override (only
    the base declaration may have one).
12. **`service/audiofocus/AudioFocusManager.kt`** used `AudioManager.AudioFocusRequest`, which
    doesn't exist — the real class is the top-level `android.media.AudioFocusRequest`. Fixed the
    import and both usages.
13. **`services/MediaPlayerService.kt`** — the Handler/Runnable-based seekbar polling
    (`seekBarRunnable`/`seekBarHandler` fields) was replaced with a coroutine
    (`positionUpdateJob: Job?`), but: the `import com.atomykcoder.atomykplay.ui.MainActivity`
    was deleted even though `MainActivity.service_bound` etc. are still used throughout the file;
    `kotlinx.coroutines.Job`/`delay`/`isActive` were used without being imported; and 3 other call
    sites in the same file plus 3 more in `BottomSheetPlayerFragment.kt` still referenced the old
    `seekBarRunnable`/`seekBarHandler` fields directly. Added the missing imports, and added a
    public `MediaPlayerService.cancelPositionUpdates()` method that all of those call sites (in
    both files, plus one in `MainActivity.onStop()`) now call instead of reaching into a private
    field.
14. **`MainActivity.setupViewModel()` — real type bug.** `music.albumUri` is a non-null `String`
    (a path), not a `Bitmap?`, but was being passed to `setImageInNavigation(bitmap: Bitmap?)`.
    Split into two overloads: `setImageInNavigation(path: String?)` (uses the existing
    `loadAlbumArt` Glide extension, used by the new StateFlow-driven nav-cover update) and
    `setImageInNavigation(bitmap: Bitmap?)` (uses `loadImageFromBitmap`, used by the older
    `BottomSheetPlayerFragment.setImages()` path which already has a decoded `Bitmap` on hand).

## Fixed in round 2 (same day, after user confirmed playback/UI works)
Investigated follow-up #3 below and found it was based on incomplete analysis — plus one real,
previously-undetected regression:

15. **Follow-up #3 was wrong: `setMainPlayerLayout`/`setPlayerImages` are NOT orphaned.**
    `currentTrack.collectLatest` calls `setPreviousData(track)`, which itself calls
    `setMainPlayerLayout(SetMainLayoutEvent(activeMusic))` directly (not via EventBus) and, via a
    Glide callback, `setPlayerImages(...)`. So every field listed in follow-up #3 (`mimeTv`,
    `durationTv`, `bitrateTv`, song/artist text, seekbar max, shuffle icon, nav text,
    `runnableSyncLyrics()`) is already set on every track change — confirmed by grep (both
    methods' only callers are inside this same file, at the `setPreviousData` call sites) and by
    the user's manual test. Their `@Subscribe` annotations are genuinely dead, though: a
    repo-wide grep found **zero** `EventBus.getDefault().post(...)` calls left anywhere in the
    app. Removed `@Subscribe` from both (same treatment as `runnableSyncLyrics`/`prepareRunnable`
    earlier). There is some redundant work now (the `currentTrack.collectLatest` block sets
    `miniNameText`/`miniArtistText`/favorite icon, and `setMainPlayerLayout` sets them again a
    moment later) — harmless, not fixed, not worth the risk of touching right now.
16. **`handleMusicProgressUpdate`/`handleMusicImageUpdate` were truly dead** (unlike #15's
    methods, nothing calls them directly either) — deleted both, plus the now-unused
    `UpdateMusicProgressEvent`/`UpdateMusicImageEvent` classes (verified zero other references
    first). Their functionality is fully covered by the `position`/`playbackState` StateFlow
    collectors already in place.
17. **Real regression: "remove from playlist" was silently rewired to remove from the live
    playback queue instead.** `MainActivity.removeFromList()`'s `OPEN_PLAYLIST` branch used to
    `EventBus.post(RemoveFromPlaylistEvent(music))`, picked up by whichever `OpenPlayListFragment`
    (a fragment that displays one specific saved `Playlist`, opened from the Playlists screen —
    unrelated to what's currently playing) was on screen, which called
    `openPlayListAdapter.removeItem(music)` — the only thing that actually persists the removal,
    via `storage.removeItemInPlaylist(music, playlistName)`. The WIP session replaced this with
    `StateHolder.playbackStateManager.removeFromQueue(music)`, which removes from the **currently
    playing queue** — a completely different, unrelated list — and never touches the saved
    playlist's storage at all. Fixed by having `MainActivity` look up the open
    `OpenPlayListFragment` by its fragment tag and call a new plain `removeMusicFromList(music)`
    method on it directly (`OpenPlayListFragment.kt` dropped its EventBus
    register/unregister/`@Subscribe` entirely — nothing else was subscribed there).
18. **`FavoritesFragment.kt` — same EventBus-consumer pattern, but the state-manager side was
    already correct** (`MainActivity` already calls `StateHolder.favoriteStateManager.removeFavorite()`
    directly). Only the fragment needed fixing: replaced its EventBus
    register/unregister/`@Subscribe fun removeFromPlaylist` with a
    `viewLifecycleOwner.lifecycleScope.launchWhenStarted { StateHolder.favoriteStateManager.favoriteIds.collectLatest { ... } }`
    collector that calls a new `FavoriteListAdapter.removeItemsNotIn(ids)` method — this is more
    general than the old single-item removal (self-heals from any favoriteIds change while the
    screen is open, not just the specific option-sheet action).
19. **Sleep timer countdown text had no StateFlow equivalent** (follow-up #5) — the orphaned
    `setTimerLiveText`/`setTimerFinished` (`@Subscribe`, never posted to, never called directly:
    genuinely dead, confirmed by grep) meant the sleep timer's live countdown text and
    finished-state icon never updated at all. Added `PlaybackStateManager.timerText: StateFlow<String?>`
    (null = no timer running) with a `setTimerText(text: String?)` setter; wired
    `MediaPlayerService.setTimer()`'s `onTick`/`onFinish` and `cancelTimer()` to call it; added an
    8th `launch { }` block in `BottomSheetPlayerFragment`'s StateFlow collector to show/hide
    `timerTv`/`timerImg` from it. Deleted the two dead methods and the now-fully-dead
    `SetTimerText`/`TimerFinished` event classes.
20. **Deleted dead code**: `RemoveFromFavoriteEvent.kt`, `RemoveFromPlaylistEvent.kt`,
    `UpdateMusicImageEvent.kt`, `UpdateMusicProgressEvent.kt`, `SetTimerText.kt`,
    `TimerFinished.kt`, `RemoveLyricsHandlerEvent.kt` — all confirmed zero references anywhere in
    `app/src/` before deletion. Also removed now-unused `org.greenrobot.eventbus.EventBus`
    imports left over in `MainActivity.kt` and `AddLyricsFragment.kt` (only comments referenced
    EventBus in those files, no live calls).

**Net result: `EventBus.getDefault().register(...)`/`.unregister(...)` no longer appears anywhere
in `app/src/main/java/com/atomykcoder/atomykplay` (verified by repo-wide grep) — the EventBus →
StateFlow/direct-call migration described in NEW_PLAN.md Phase 5 is functionally complete.** Five
event-wrapper classes remain (`PrepareRunnableEvent`, `RunnableSyncLyricsEvent`,
`SetImageInMainPlayer`, `SetMainLayoutEvent`, `StopTextAnim`) but are only ever constructed and
consumed as plain parameter objects passed to direct method calls within the same file — not a
bug, just slightly misleading names inherited from the old EventBus design (see follow-up #2
below).

## Known follow-ups (not yet done)
1. **No emulator/UI verification of round 2's changes.** The user's manual test ("music plays,
   UI updates") happened *before* round 2 (favorites sync, playlist removal fix, timer StateFlow,
   dead code deletion). Specifically still unverified: removing a song from a saved playlist via
   the option sheet (fix #17), the favorites list updating live when a favorite is removed
   elsewhere while `FavoritesFragment` is open (fix #18), and the sleep timer countdown text
   actually appearing/counting down/hiding on finish (fix #19).
2. **Rename or repurpose the 5 remaining `*Event` classes** (`PrepareRunnableEvent`,
   `RunnableSyncLyricsEvent`, `SetImageInMainPlayer`, `SetMainLayoutEvent`, `StopTextAnim`) — pure
   naming/clarity cleanup now that they're plain parameter objects, not EventBus events. Low
   priority, cosmetic only.
3. **`MainViewModel` is constructed directly** (`MainViewModel(StateHolder.playbackStateManager)`)
   instead of via `ViewModelProvider`. It works because `PlaybackStateManager` itself is a
   `ViewModel` held statically in `StateHolder` (so its `viewModelScope` doesn't leak), but
   `MainViewModel`'s own `ViewModel()` base is pointless this way — its `viewModelScope` is tied
   to nothing and it won't survive `MainActivity` recreation. Not a compile bug, just not
   idiomatic; revisit when doing real DI (see NEW_PLAN.md Phase 3).
4. **`MediaPlayerService.kt` / `MainActivity.kt` still use static `is_playing` / `ui_visible`**
   fields alongside the new `PlaybackStateManager` StateFlows (comment at the `companion object`
   says "kept for backward compatibility but should be migrated away from"). Not broken, just
   dual-sourced state — migrate call sites to read from `StateHolder.playbackStateManager`
   instead, then delete the static fields.
5. **Minor redundant work in `currentTrack.collectLatest`** (see fix #15) — `miniNameText`/
   `miniArtistText`/favorite icon get set twice per track change (once directly in the collector,
   once inside `setMainPlayerLayout` via `setPreviousData`). Harmless, low priority.

## Files touched this session
Round 1 (compile fixes):
- `app/src/main/java/com/atomykcoder/atomykplay/ui/MainActivity.kt`
- `app/src/main/java/com/atomykcoder/atomykplay/fragments/BottomSheetPlayerFragment.kt`
- `app/src/main/java/com/atomykcoder/atomykplay/services/MediaPlayerService.kt`
- `app/src/main/java/com/atomykcoder/atomykplay/state/PlaybackStateManager.kt`
- `app/src/main/java/com/atomykcoder/atomykplay/state/FavoriteStateManager.kt`
- `app/src/main/java/com/atomykcoder/atomykplay/state/StateHolder.kt`
- `app/src/main/java/com/atomykcoder/atomykplay/service/player/PlayerService.kt`
- `app/src/main/java/com/atomykcoder/atomykplay/service/audiofocus/AudioFocusManager.kt`

Round 2 (EventBus completion + timer StateFlow, on top of the same files plus):
- `app/src/main/java/com/atomykcoder/atomykplay/fragments/FavoritesFragment.kt`
- `app/src/main/java/com/atomykcoder/atomykplay/fragments/OpenPlayListFragment.kt`
- `app/src/main/java/com/atomykcoder/atomykplay/adapters/FavoriteListAdapter.kt`
- Deleted: `events/RemoveFromFavoriteEvent.kt`, `events/RemoveFromPlaylistEvent.kt`,
  `events/UpdateMusicImageEvent.kt`, `events/UpdateMusicProgressEvent.kt`,
  `events/SetTimerText.kt`, `events/TimerFinished.kt`, `events/RemoveLyricsHandlerEvent.kt`

(`fragments/AddLyricsFragment.kt` was already correct from an earlier session — only referenced
in this report because it's what exposed bug #1.)

## Next session focus (in priority order)
1. Run the app and manually verify round 2's changes specifically: remove a song from a saved
   playlist (option sheet → "remove from list" while viewing a `Playlist`, not the queue), remove
   a favorite while `FavoritesFragment` is open and confirm it disappears live, and start a sleep
   timer and confirm the countdown text appears/updates/hides correctly on finish.
2. Follow-up #4: migrate `is_playing`/`ui_visible` static-field call sites to
   `StateHolder.playbackStateManager` and delete the static fields.
3. Follow-up #2/#5: cosmetic cleanup (rename leftover `*Event` classes, dedupe the
   `currentTrack.collectLatest` redundancy) — low priority, do only if nothing higher-value is
   pending.
4. Re-check NEW_PLAN.md's Phase 2/3 (service decomposition, domain layer) — Phase 5 (EventBus
   removal) is now functionally done; decide whether to start on `AudioSourceResolver`/
   `LyricsService`/`MusicLibraryService` extraction from `MediaPlayerService`, or on real DI to
   fix follow-up #3.

## Architecture notes
- Moving toward clean separation: UI → ViewModel → StateManagers/UseCases → Repositories
- StateFlows provide reactive state updates without EventBus, **but every StateFlow collector
  block that observes more than one flow must `launch { }` each one separately** —
  `collectLatest` never returns, so sharing one coroutine across multiple `.collectLatest` calls
  silently drops every flow after the first. (This was the single biggest bug found in round 1.)
- ViewModels mediate between UI and business logic
- StateManagers (like `PlaybackStateManager`, `FavoriteStateManager`) hold single source of truth
  state, but the real backing storage stays `StorageUtil` (SharedPreferences) for now — double
  check `StorageUtil`'s actual method names before calling them from a new state manager; several
  didn't match what was assumed (see fix #8).
- Before assuming a method reachable only via a suspicious-looking `@Subscribe`/event-typed
  parameter is dead, grep for **direct** calls to it too (by method name, not just for
  `EventBus.post(...)` of its event type) — `setMainPlayerLayout`/`setPlayerImages` looked
  orphaned at a glance but weren't (see fix #15). Grep for both before deleting anything.
