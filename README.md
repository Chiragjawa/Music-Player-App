# Lokal Music Player
Perfect — here’s a **concise, evaluator-friendly README** updated to **explicitly include**:
A modern Android music player built with **Kotlin**, **Jetpack Compose**, and **Media3 ExoPlayer**, focusing on **reliable background playback**, **persistent queue management**, and **fully synchronized playback UI**.

---

## ✅ Core Features

### 🎶 Queue Management

* Add songs to the queue (from search, album, or artist screens)
* Remove songs from the queue
* Reorder songs (move up / down)
* Currently playing song is highlighted
* Queue updates are reflected instantly across all screens

### 💾 Local Persistence

* Playback queue and current index are persisted locally
* Queue is restored automatically on app restart
* Implemented using **Jetpack DataStore**

### ▶️ Background Playback

Audio continues playing when:

* App is minimized
* Screen is locked
* User navigates across screens

Implemented using **Media3 ExoPlayer + MediaSession + Foreground Service**.

### 🔄 State Synchronization

* Mini player and full player are fully synchronized
* Same song, play/pause state, seek position, and queue
* Mini player includes a **synchronized seek bar** reflecting real-time playback
* Single source of truth via **StateFlow**

---

## 🛠 Technical Stack (Mandatory)

* **Kotlin**
* **Jetpack Compose**
* **MVVM Architecture**
* **StateFlow / MutableStateFlow**
* **Hilt (Dependency Injection)**
* **Media3 ExoPlayer**
* **MediaSession + Foreground Service**
* Retrofit (real API data)
* DataStore (local persistence)

---

## 🏗 Architecture Overview

```
Compose UI
   ↓
ViewModels (StateFlow)
   ↓
Repository
   ↓
Remote API
```

### Playback Flow

```
MusicPlayerViewModel
   ↓
ExoPlayer
   ↓
MediaSession
   ↓
Foreground Service
   ↓
System Notification
```

**Key Points**

* ExoPlayer is owned by the ViewModel (not the UI)
* UI is stateless and reacts to StateFlow
* ViewModel acts as the single source of truth for playback and queue state

---

## ⚙️ Setup Instructions

1. Clone the repository:

   ```bash
   git clone https://github.com/Chiragjawa/Music-Player-App.git
   ```
2. Open in Android Studio
3. Sync Gradle
4. Run on emulator or physical device

> No mock data or API keys required.

---

## ⚠️ Assumptions & Trade-offs

**Assumptions**

* Internet connectivity is available
* Streaming URLs provided by the API are valid

**Trade-offs**

* Queue reordering uses move up/down buttons instead of drag & drop (kept simple and explicit)
* No offline playback (streaming-only by API design)
* If the currently playing song is removed, playback stops instead of auto-selecting the next song (predictable behavior)

---
