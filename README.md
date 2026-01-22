# Lokal Music Player

**Lokal Music Player** is a modern Android music streaming application built using **Kotlin**, **Jetpack Compose**, and **Media3 ExoPlayer**.
The project focuses on **reliable background playback**, **persistent queue management**, and a **smooth, fully synchronized playback experience** between the mini player and the full player.

This app is designed with clarity and correctness in mind, following modern Android best practices without unnecessary over-engineering.

---

## ⚙️ Setup Instructions
APK is in the Git Repo Itself.

1. Clone the repository:

```bash
git clone https://github.com/Chiragjawa/Music-Player-App.git
```

2. Open the project in **Android Studio**

3. Let **Gradle sync** complete

4. Run the app on an **emulator or physical device**

> ✅ No mock data or API keys are required.
> The app uses real API data.

---

## ✅ Core Features

### 🎶 Queue Management

* Add songs to the queue from **search**, **album**, or **artist** screens
* Remove songs from the queue at any time
* Reorder songs using simple **move up / move down** controls
* The currently playing song is clearly highlighted
* Queue changes are reflected instantly across all screens

---

### 💾 Local Persistence

* Playback queue and current song index are stored locally
* Queue state is automatically restored when the app is reopened
* Implemented using **Jetpack DataStore** for lightweight persistence

---

### ▶️ Background Playback

Music continues playing when:

* The app is minimized
* The screen is locked
* The user navigates between different screens

This is achieved using **Media3 ExoPlayer**, **MediaSession**, and a **foreground service**, ensuring uninterrupted playback even outside the app UI.

---

### 🔄 State Synchronization

* The **mini player** and **full player** are always in sync
* Both reflect the same song, play/pause state, seek position, and queue
* The mini player includes a **synchronized seek bar** that updates in real time
* Playback state is managed from a **single source of truth using StateFlow**

---

## 🛠 Technical Stack (Mandatory)

* **Kotlin**
* **Jetpack Compose**
* **MVVM Architecture**
* **StateFlow / MutableStateFlow**
* **Hilt (Dependency Injection)**
* **Media3 ExoPlayer**
* **MediaSession + Foreground Service**
* **Retrofit** (real API data)
* **Jetpack DataStore** (local persistence)

## 🏗 Architecture Overview (Mapped to Project Files)

The app follows a clean **MVVM (Model–View–ViewModel)** architecture.
Each layer has a clear responsibility, and all communication flows in one direction to keep the app predictable and easy to maintain.

---

### 📱 UI Layer (Jetpack Compose)

**Files involved:**

* `SearchScreen.kt`
* `AlbumScreen.kt`
* `ArtistScreen.kt`
* `PlayerScreen.kt`
* `QueueScreen.kt`
* `MiniPlayer.kt`
* `SongItem.kt`
* `AppNavigation.kt`

**Responsibilities:**

* Display UI and handle user interactions
* Observe state exposed by ViewModels using **StateFlow**
* Do **not** contain business logic or playback logic
* Automatically recompose when state changes

The UI only reacts to state and forwards user actions (play, pause, add to queue, reorder) to the ViewModels.

---

### 🧠 ViewModel Layer (Single Source of Truth)

**Files involved:**

* `SearchViewModel.kt`
* `AlbumViewModel.kt`
* `ArtistViewModel.kt`
* `MusicPlayerViewModel.kt`

**Responsibilities:**

* Hold UI and playback state
* Expose state using **StateFlow**
* Coordinate between UI, repository, and playback layer

#### `MusicPlayerViewModel.kt` (Core of the App)

* Owns the **ExoPlayer** instance
* Manages:

  * Current song
  * Playback state (play / pause)
  * Seek position
  * Playback queue (add / remove / reorder)
* Exposes a unified `PlayerState` via `StateFlow`
* Acts as the **single source of truth** for both the mini player and full player

Other ViewModels (`SearchViewModel`, `AlbumViewModel`, `ArtistViewModel`):

* Fetch and prepare data for UI
* Never control playback directly

---

### 📦 Repository Layer

**File involved:**

* `MusicRepository.kt`

**Responsibilities:**

* Fetch real music data from the remote API
* Convert API responses into domain models (`Song`, `Album`)
* Keep ViewModels independent of networking logic

---

### 🌐 Remote Data Layer

**Files involved:**

* `SaavnApi.kt`
* `ApiResponseModels.kt`
* `ApiExtraResponses.kt`
* `NetworkModule.kt`

**Responsibilities:**

* Define API endpoints using Retrofit
* Handle network configuration and dependency injection
* Communicate with the **JioSaavn REST API**

---

### 🎵 Playback & Background Audio Layer

**Files involved:**

* `MusicPlaybackService.kt`
* `AudioFocusManager.kt`

**Playback Flow:**

```
MusicPlayerViewModel
   ↓
ExoPlayer (Media3)
   ↓
MediaSession
   ↓
MusicPlaybackService (Foreground Service)
   ↓
Notification / Lock Screen
```

**Responsibilities:**

* `MusicPlaybackService.kt`

  * Runs as a **foreground service**
  * Keeps music playing when app is backgrounded
  * Shows media controls on notification and lock screen
* `AudioFocusManager.kt`

  * Handles audio focus changes (calls, other apps)

This ensures reliable playback even when the app is minimized or the screen is locked.

---

### 💾 Local Persistence Layer

**File involved:**

* `QueueDataStore.kt`

**Responsibilities:**

* Persist playback queue and current song index
* Restore queue state when the app restarts
* Implemented using **Jetpack DataStore**

---

## ✅ Why This Architecture Works Well

* Clear separation of concerns
* Single source of truth using `MusicPlayerViewModel`
* Lifecycle-safe background playback
* Fully synchronized mini and full player UI
* Easy to explain, debug, and extend


## ⚠️ Assumptions & Trade-offs

### Assumptions

* Internet connectivity is available for streaming music
* Streaming URLs provided by the API are valid

### Trade-offs

* Queue reordering is implemented using **move up / move down** buttons instead of drag-and-drop to keep interactions simple and predictable
* Offline playback is not supported since the app relies on streaming APIs
* If the currently playing song is removed from the queue, playback stops instead of automatically selecting the next song, ensuring explicit and predictable behavior
