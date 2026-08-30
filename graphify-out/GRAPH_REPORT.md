# Graph Report - Atomyk_Play  (2026-08-30)

## Corpus Check
- cluster-only mode — file stats not available

## Summary
- 1214 nodes · 2661 edges · 71 communities (50 shown, 16 thin omitted)
- Extraction: 99% EXTRACTED · 1% INFERRED · 0% AMBIGUOUS · INFERRED: 33 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `f9162319`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- TagEditorFragment.kt
- SearchFragment
- AddLyricsFragment
- MainActivity
- SettingsStorage
- LastAddedFragment
- SettingsFragment
- MediaPlayerService
- AudioFileCover
- Music
- BottomSheetPlayerFragment
- SimpleTouchCallback
- MusicRepo
- .resumeMedia
- .onViewCreated
- AbhinavUtil
- FavoriteListAdapter
- Playlist
- TimerFinished
- PackageValidator
- OpenPlayListAdapter
- MainActivity.kt
- .setUpServiceAndScanner
- GenericViewHolder
- MusicMainAdapter
- OptionSheetEnum
- .setPlayerImages
- BottomSheetPlayerFragment.kt
- MusicQueueAdapter
- PrepareRunnableEvent
- .updateMetaData
- BeautifyListAdapter
- .initiateMediaPlayer
- View
- MusicEnhancerUtil
- MusicHelper
- FoundLyricsAdapter
- MusicMainAdapter.kt
- PlaylistDialogAdapter
- SeekBar
- PermissionListener
- BlockFolderListAdapter
- MusicQueueAdapter.kt
- PlaylistAdapter.kt
- GenericRecyclerAdapter
- CenterSmoothScroller
- CustomBottomSheet
- .deleteItemWithContentResolver
- CustomCallStateListener
- ApplicationClass
- AnimatorListener
- Intent
- BackPressCompatActivity
- AnimatorListenerAdapter
- BlackListViewHolder
- PhoneStateCallback
- UpdateMusicProgressEvent
- Bundle
- gradlew
- RemoveLyricsHandlerEvent
- .replaceFragment
- AudioFileCoverUtils
- Logger
- .onLoadChildren
- BroadcastStrings.kt
- FragmentTags.kt

## God Nodes (most connected - your core abstractions)
1. `MainActivity` - 157 edges
2. `Music` - 139 edges
3. `BottomSheetPlayerFragment` - 103 edges
4. `MediaPlayerService` - 97 edges
5. `SettingsStorage` - 74 edges
6. `StorageUtil` - 69 edges
7. `GenericViewHolder` - 55 edges
8. `Playlist` - 40 edges
9. `SettingsFragment` - 36 edges
10. `SearchFragment` - 28 edges

## Surprising Connections (you probably didn't know these)
- `TagEditorFragment` --inherits--> `BaseFragment`  [EXTRACTED]
  AtomykPlay/app/src/main/java/com/atomykcoder/atomykplay/fragments/TagEditorFragment.kt → AtomykPlay/app/src/main/java/com/atomykcoder/atomykplay/data/BaseFragment.kt
- `TagEditorFragment` --references--> `Music`  [EXTRACTED]
  AtomykPlay/app/src/main/java/com/atomykcoder/atomykplay/fragments/TagEditorFragment.kt → AtomykPlay/app/src/main/java/com/atomykcoder/atomykplay/models/Music.kt
- `AddLyricsFragment` --inherits--> `BaseFragment`  [EXTRACTED]
  AtomykPlay/app/src/main/java/com/atomykcoder/atomykplay/fragments/AddLyricsFragment.kt → AtomykPlay/app/src/main/java/com/atomykcoder/atomykplay/data/BaseFragment.kt
- `BottomSheetPlayerFragment` --inherits--> `BaseFragment`  [EXTRACTED]
  AtomykPlay/app/src/main/java/com/atomykcoder/atomykplay/fragments/BottomSheetPlayerFragment.kt → AtomykPlay/app/src/main/java/com/atomykcoder/atomykplay/data/BaseFragment.kt
- `FavoritesFragment` --inherits--> `BaseFragment`  [EXTRACTED]
  AtomykPlay/app/src/main/java/com/atomykcoder/atomykplay/fragments/FavoritesFragment.kt → AtomykPlay/app/src/main/java/com/atomykcoder/atomykplay/data/BaseFragment.kt

## Import Cycles
- None detected.

## Communities (71 total, 16 thin omitted)

### Community 0 - "TagEditorFragment.kt"
Cohesion: 0.06
Nodes (36): ActivityResultLauncher, Bundle, LayoutInflater, LiveData, MultiplePermissionsListener, MultiplePermissionsReport, PermissionRequest, PermissionToken (+28 more)

### Community 1 - "SearchFragment"
Cohesion: 0.06
Nodes (26): BaseFragment, Fragment, AboutFragment, Bitmap, Bundle, LayoutInflater, Uri, View (+18 more)

### Community 2 - "AddLyricsFragment"
Cohesion: 0.06
Nodes (19): RunnableSyncLyricsEvent, AddLyricsFragment, AlertDialog, Bundle, EditText, LayoutInflater, ProgressBar, View (+11 more)

### Community 3 - "MainActivity"
Cohesion: 0.07
Nodes (5): Handler, OnClickListener, MainActivity, DrawerListener, OnNavigationItemSelectedListener

### Community 5 - "LastAddedFragment"
Cohesion: 0.08
Nodes (22): RemoveFromPlaylistEvent, Bundle, Context, Dialog, LayoutInflater, ProgressBar, RecyclerView, TextView (+14 more)

### Community 6 - "SettingsFragment"
Cohesion: 0.08
Nodes (19): AlertDialog, Bundle, Context, LayoutInflater, MultiplePermissionsListener, MultiplePermissionsReport, OnSeekBarChangeListener, PermissionRequest (+11 more)

### Community 7 - "MediaPlayerService"
Cohesion: 0.08
Nodes (26): RepeatModes, CustomCallStateListener, Bundle, PhoneStateListener, TelephonyManager, LocalBinder, MediaPlayerService, PhoneStateListener (+18 more)

### Community 8 - "AudioFileCover"
Cohesion: 0.07
Nodes (23): AppGlideModule, AtomykMusicGlideModule, Context, AudioFileCover, AudioFileCoverFetcher, AudioFileCoverLoader, Factory, FastScrollListener (+15 more)

### Community 9 - "Music"
Cohesion: 0.09
Nodes (4): ShuffleModes, Music, StorageUtil, SharedPreferences

### Community 10 - "BottomSheetPlayerFragment"
Cohesion: 0.08
Nodes (3): BottomSheetPlayerFragment, Context, RecyclerView

### Community 11 - "SimpleTouchCallback"
Cohesion: 0.11
Nodes (12): RecyclerView, SimpleTouchCallback, AndroidUtil, Bitmap, View, BlurBuilder, Bitmap, Context (+4 more)

### Community 12 - "MusicRepo"
Cohesion: 0.11
Nodes (15): Activity, android.content.Context, MusicDaoI, Context, MusicDaoImpl, LiveData, MusicRepo, Context (+7 more)

### Community 13 - ".resumeMedia"
Cohesion: 0.13
Nodes (5): PlaybackStatus, PAUSED, PLAYING, UpdateMusicImageEvent, Callback

### Community 14 - ".onViewCreated"
Cohesion: 0.10
Nodes (8): StopTextAnim, OnGlobalLayoutListener, OnGlobalLayoutListener, OnGlobalLayoutListener, BottomSheetCallback, ItemTouchHelper, View, OnGlobalLayoutListener

### Community 15 - "AbhinavUtil"
Cohesion: 0.15
Nodes (13): GridSpacing, RecyclerView, View, AbhinavUtil, ActivityResultLauncher, Bitmap, Context, Intent (+5 more)

### Community 16 - "FavoriteListAdapter"
Cohesion: 0.12
Nodes (11): FavoriteListAdapter, RemoveFromFavoriteEvent, FavoritesFragment, Bundle, ItemTouchHelper, LayoutInflater, RecyclerView, View (+3 more)

### Community 17 - "Playlist"
Cohesion: 0.14
Nodes (8): PlaylistAdapter, Bundle, LayoutInflater, View, ViewGroup, PlaylistsFragment, Playlist, Serializable

### Community 18 - "TimerFinished"
Cohesion: 0.11
Nodes (3): SetTimerText, TimerFinished, CountDownTimer

### Community 19 - "PackageValidator"
Cohesion: 0.23
Nodes (9): CallerPackageInfo, KnownCallerInfo, KnownSignature, Context, PackageValidator, ByteArray, PackageInfo, PackageManager (+1 more)

### Community 20 - "OpenPlayListAdapter"
Cohesion: 0.15
Nodes (7): ViewGroup, OpenPlayListAdapter, ImageView, TextView, View, OpenPlayListViewHolder, ItemTouchHelperAdapter

### Community 21 - "MainActivity.kt"
Cohesion: 0.11
Nodes (15): BaseActivity, AppCompatActivity, AlertDialog, Bitmap, ImageView, ProgressBar, RecyclerView, TelephonyManager (+7 more)

### Community 22 - ".setUpServiceAndScanner"
Cohesion: 0.16
Nodes (6): MultiplePermissionsListener, MultiplePermissionsReport, PermissionRequest, PermissionToken, MultiplePermissionsListener, MultiplePermissionsListener

### Community 23 - "GenericViewHolder"
Cohesion: 0.17
Nodes (8): GenericViewHolder, T, ViewHolder, ViewGroup, MusicLyricsAdapter, TextView, MusicLyricsViewHolder, IBindableViewHolder

### Community 24 - "MusicMainAdapter"
Cohesion: 0.21
Nodes (3): MusicAdapter, MusicMainAdapter, SectionIndexer

### Community 25 - "OptionSheetEnum"
Cohesion: 0.17
Nodes (9): ViewGroup, FavoriteViewHolder, ImageView, TextView, View, OptionSheetEnum, FAVORITE_LIST, MAIN_LIST (+1 more)

### Community 26 - ".setPlayerImages"
Cohesion: 0.23
Nodes (5): SetImageInMainPlayer, CustomTarget, CustomTarget, Drawable, Transition

### Community 27 - "BottomSheetPlayerFragment.kt"
Cohesion: 0.15
Nodes (12): Bitmap, Dialog, ImageView, LayoutInflater, LinearLayoutManager, LottieAnimationView, OnClickListener, TextView (+4 more)

### Community 29 - "PrepareRunnableEvent"
Cohesion: 0.15
Nodes (3): PrepareRunnableEvent, Bundle, Handler

### Community 30 - ".updateMetaData"
Cohesion: 0.26
Nodes (6): SetMainLayoutEvent, Bitmap, CustomTarget, Drawable, Transition, CustomTarget

### Community 31 - "BeautifyListAdapter"
Cohesion: 0.23
Nodes (6): BeautifyListAdapter, Context, ViewGroup, BeautifyListViewHolder, ImageView, TextView

### Community 33 - "View"
Cohesion: 0.19
Nodes (4): BottomSheetBehavior, MotionEvent, View, Size

### Community 34 - "MusicEnhancerUtil"
Cohesion: 0.20
Nodes (3): MusicEnhancerUtil, BassBoost, Virtualizer

### Community 36 - "FoundLyricsAdapter"
Cohesion: 0.31
Nodes (5): FoundLyricsAdapter, Adapter, ViewGroup, FoundLyricsViewHolder, TextView

### Community 37 - "MusicMainAdapter.kt"
Cohesion: 0.29
Nodes (6): ViewGroup, ImageView, TextView, MusicMainViewHolder, loadAlbumArt(), RelativeLayout

### Community 38 - "PlaylistDialogAdapter"
Cohesion: 0.29
Nodes (5): ViewGroup, PlaylistDialogAdapter, TextView, View, PlayListDialogViewHolder

### Community 39 - "SeekBar"
Cohesion: 0.24
Nodes (3): OnSeekBarChangeListener, OnSeekBarChangeListener, SeekBar

### Community 41 - "PermissionListener"
Cohesion: 0.24
Nodes (5): PermissionListener, PermissionListener, PermissionDeniedResponse, PermissionGrantedResponse, PermissionListener

### Community 42 - "BlockFolderListAdapter"
Cohesion: 0.31
Nodes (3): BlockFolderListAdapter, Context, ViewGroup

### Community 43 - "MusicQueueAdapter.kt"
Cohesion: 0.33
Nodes (5): ViewGroup, ImageView, TextView, View, MusicQueueViewHolder

### Community 44 - "PlaylistAdapter.kt"
Cohesion: 0.33
Nodes (6): ViewGroup, ImageView, TextView, View, PlaylistViewHolder, loadImageFromUri()

### Community 45 - "GenericRecyclerAdapter"
Cohesion: 0.36
Nodes (4): GenericRecyclerAdapter, Adapter, T, ViewGroup

### Community 46 - "CenterSmoothScroller"
Cohesion: 0.39
Nodes (5): CenterSmoothScroller, CenterSmoothScrollScript, LinearLayoutManager, RecyclerView, LinearSmoothScroller

### Community 47 - "CustomBottomSheet"
Cohesion: 0.39
Nodes (5): CustomBottomSheet, BottomSheetBehavior, MotionEvent, CoordinatorLayout, V

### Community 49 - "CustomCallStateListener"
Cohesion: 0.38
Nodes (3): CustomCallStateListener, PhoneStateListener, PhoneStateListener

### Community 50 - "ApplicationClass"
Cohesion: 0.47
Nodes (3): Application, ApplicationClass, NotificationManager

### Community 53 - "BackPressCompatActivity"
Cohesion: 0.53
Nodes (3): BackPressCompatActivity, AppCompatActivity, BackPressCallback

### Community 54 - "AnimatorListenerAdapter"
Cohesion: 0.53
Nodes (3): AbhinavAnimationUtil, AnimatorListenerAdapter, Animator

### Community 55 - "BlackListViewHolder"
Cohesion: 0.60
Nodes (3): BlackListViewHolder, ImageView, TextView

### Community 56 - "PhoneStateCallback"
Cohesion: 0.60
Nodes (3): PhoneStateCallback, CallStateListener, TelephonyCallback

### Community 59 - "gradlew"
Cohesion: 0.70
Nodes (4): gradlew script, die(), save(), warn()

## Knowledge Gaps
- **10 isolated node(s):** `BroadcastStrings`, `FragmentTags`, `FAILURE`, `LOADING`, `SUCCESS` (+5 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 208 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **16 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `MainActivity` connect `MainActivity` to `TagEditorFragment.kt`, `SearchFragment`, `AddLyricsFragment`, `SettingsStorage`, `LastAddedFragment`, `SettingsFragment`, `MediaPlayerService`, `Music`, `BottomSheetPlayerFragment`, `MusicRepo`, `.onViewCreated`, `FavoriteListAdapter`, `Playlist`, `OpenPlayListAdapter`, `MainActivity.kt`, `.setUpServiceAndScanner`, `GenericViewHolder`, `MusicMainAdapter`, `OptionSheetEnum`, `BottomSheetPlayerFragment.kt`, `MusicQueueAdapter`, `BeautifyListAdapter`, `View`, `FoundLyricsAdapter`, `MusicMainAdapter.kt`, `PlaylistDialogAdapter`, `.showToast`, `PermissionListener`, `BlockFolderListAdapter`, `MusicQueueAdapter.kt`, `PlaylistAdapter.kt`, `CustomBottomSheet`, `.deleteItemWithContentResolver`, `CustomCallStateListener`, `PhoneStateCallback`, `Bundle`, `RemoveLyricsHandlerEvent`, `.replaceFragment`?**
  _High betweenness centrality (0.228) - this node is a cross-community bridge._
- **Why does `Music` connect `Music` to `TagEditorFragment.kt`, `SearchFragment`, `AddLyricsFragment`, `MainActivity`, `LastAddedFragment`, `MediaPlayerService`, `BottomSheetPlayerFragment`, `MusicRepo`, `FavoriteListAdapter`, `Playlist`, `OpenPlayListAdapter`, `MainActivity.kt`, `MusicMainAdapter`, `OptionSheetEnum`, `.setPlayerImages`, `BottomSheetPlayerFragment.kt`, `MusicQueueAdapter`, `.updateMetaData`, `MusicHelper`, `MusicMainAdapter.kt`, `PlaylistDialogAdapter`, `.showToast`, `MusicQueueAdapter.kt`, `.deleteItemWithContentResolver`, `.replaceFragment`?**
  _High betweenness centrality (0.212) - this node is a cross-community bridge._
- **Why does `MediaPlayerService` connect `MediaPlayerService` to `.initiateMediaPlayer`, `.onLoadChildren`, `MusicEnhancerUtil`, `.takeActionOnCall`, `SettingsStorage`, `MainActivity`, `Music`, `.resumeMedia`, `TimerFinished`, `PackageValidator`, `Intent`, `ApplicationClass`, `MainActivity.kt`, `PhoneStateCallback`, `UpdateMusicProgressEvent`, `.updateMetaData`?**
  _High betweenness centrality (0.200) - this node is a cross-community bridge._
- **What connects `BroadcastStrings`, `FragmentTags`, `FAILURE` to the rest of the system?**
  _10 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `TagEditorFragment.kt` be split into smaller, more focused modules?**
  _Cohesion score 0.055130784708249496 - nodes in this community are weakly interconnected._
- **Should `SearchFragment` be split into smaller, more focused modules?**
  _Cohesion score 0.06140350877192982 - nodes in this community are weakly interconnected._
- **Should `AddLyricsFragment` be split into smaller, more focused modules?**
  _Cohesion score 0.06184012066365008 - nodes in this community are weakly interconnected._