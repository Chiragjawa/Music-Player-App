# Lokal Music Player

**Lokal Music Player** is a modern Android music streaming application built using **Kotlin**, **Jetpack Compose**, and **Media3 ExoPlayer**.
The project focuses on **reliable background playback**, **persistent queue management**, and a **smooth, fully synchronized playback experience** between the mini player and the full player.

This app is designed with clarity and correctness in mind, following modern Android best practices without unnecessary over-engineering.

---

## ⚙️ Setup Instructions

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

---

## 🏗 Architecture Overview

The app follows a clean **MVVM (Model–View–ViewModel)** architecture with a clear separation of responsibilities.
UI layers are kept stateless, while all business logic and playback control are handled by ViewModels.

### High-Level Architecture

```
Compose UI
   ↓
ViewModels (StateFlow)
   ↓
Repository
   ↓
Remote API
```

### Playback Architecture

```
MusicPlayerViewModel
   ↓
ExoPlayer (Media3)
   ↓
MediaSession
   ↓
Foreground Service
   ↓
System Notification / Lock Screen
```

---

## 🧠 Architecture Explanation (In Simple Words)

### Compose UI

* Contains screens such as Search, Player, Queue, and Mini Player
* Displays data and reacts to state changes
* Does not contain business or playback logic
* Observes `StateFlow` exposed by ViewModels

---

### ViewModels

* Act as the **single source of truth** for UI and playback state
* Hold:

  * Current song
  * Playback status (play/pause)
  * Queue and current index
  * Seek position

**MusicPlayerViewModel**

* Owns the **ExoPlayer** instance
* Handles play, pause, seek, next, and previous actions
* Manages queue add, remove, and reorder operations
* Exposes a unified `PlayerState` via **StateFlow**

Other ViewModels (Search, Artist, Album):

* Fetch data
* Prepare UI-friendly state
* Never directly control playback

---

### Repository Layer

* `MusicRepository` handles all data operations
* Fetches real music data from the remote API
* Maps API responses to domain models
* Keeps ViewModels independent of networking logic

---

### Playback & Background Service

* **ExoPlayer (Media3)** handles audio playback
* **MediaSession** enables system-level media controls
* **MusicPlaybackService** runs as a foreground service to:

  * Keep music playing in the background
  * Show playback controls in notifications and lock screen
  * Forward media actions back to the ViewModel

This ensures playback remains stable across lifecycle changes.

---

### Local Persistence

* Queue and current song index are saved using **Jetpack DataStore**
* Queue is restored automatically when the app restarts

---

## ⚠️ Assumptions & Trade-offs

### Assumptions

* Internet connectivity is available for streaming music
* Streaming URLs provided by the API are valid

### Trade-offs

* Queue reordering is implemented using **move up / move down** buttons instead of drag-and-drop to keep interactions simple and predictable
* Offline playback is not supported since the app relies on streaming APIs
* If the currently playing song is removed from the queue, playback stops instead of automatically selecting the next song, ensuring explicit and predictable behavior


