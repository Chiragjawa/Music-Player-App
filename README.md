[# Lokal Music Player
Lokal Music Player is a modern Android music streaming app built using Kotlin, Jetpack Compose, and Media3 ExoPlayer.
The focus of this project is reliable background playback, persistent queue management, and a smooth, fully synchronized playback experience between the mini player and the full player.

⚙️ Setup Instructions

1. Clone the repository:
```bash
git clone https://github.com/Chiragjawa/Music-Player-App.git
```

2. Open the project in Android Studio

3. Let Gradle sync complete

4. Run the app on an emulator or physical device

No mock data or API keys are required.

✅ Core Features
🎶 Queue Management

Add songs to the queue from search, album, or artist screens

Remove songs from the queue at any time

Reorder songs using simple move up / move down controls

The currently playing song is clearly highlighted

Queue changes are reflected instantly across all screens

💾 Local Persistence

The playback queue and current song position are saved locally

Queue state is automatically restored when the app is reopened

Implemented using Jetpack DataStore for lightweight persistence

▶️ Background Playback

Music continues playing when:

The app is minimized

The screen is locked

The user navigates between different screens

This is achieved using Media3 ExoPlayer, MediaSession, and a foreground service, ensuring reliable playback even outside the app UI.

🔄 State Synchronization

The mini player and full player are always in sync

Both reflect the same song, play/pause state, seek position, and queue

The mini player includes a synchronized seek bar that updates in real time

Playback state is managed from a single source of truth using StateFlow

🛠 Technical Stack (Mandatory)

Kotlin

Jetpack Compose

MVVM Architecture

StateFlow / MutableStateFlow

Hilt (Dependency Injection)

Media3 ExoPlayer

MediaSession + Foreground Service

Retrofit (real API data)

Jetpack DataStore (local persistence)

🏗 Architecture Overview

The app follows a clean MVVM architecture, where each layer has a single, clear responsibility.
This keeps the code easy to understand, test, and maintain.

1️⃣ Compose UI (Screens & Components)

Contains all UI elements like SearchScreen, PlayerScreen, QueueScreen, and MiniPlayer

UI does not handle business or playback logic

It only observes state exposed by ViewModels using StateFlow

Whenever the state changes, the UI updates automatically

👉 This makes the UI reactive and lifecycle-safe.

2️⃣ ViewModels (State & Logic)

ViewModels act as the single source of truth

They hold:

Current song

Playback state (play/pause)

Queue and current index

Seek position

MusicPlayerViewModel

Owns the ExoPlayer

Handles play, pause, seek, next, previous

Manages queue add / remove / reorder

Exposes a unified PlayerState via StateFlow

Other ViewModels like SearchViewModel, ArtistViewModel, and AlbumViewModel:

Fetch data

Prepare UI-friendly state

Never directly control playback

3️⃣ Repository Layer

MusicRepository is the only layer that talks to data sources

Fetches real music data from the remote API

Maps API responses to domain models

Keeps ViewModels independent of networking logic

4️⃣ Playback Layer (Background & Media)

ExoPlayer (Media3) handles actual audio playback

MediaSession enables system-level media controls

MusicPlaybackService runs as a foreground service, which:

Keeps music playing in background

Shows media controls on notification & lock screen

Forwards user actions back to the ViewModel

This ensures:

Music keeps playing when app is minimized

Playback works even when the screen is locked

5️⃣ Local Persistence

Queue and current song index are saved using Jetpack DataStore

When the app restarts:

Queue is restored

Playback state remains consistent

✅ Why This Architecture Works Well

Clear separation of concerns

Single source of truth using StateFlow

Lifecycle-safe playback

UI stays simple and reactive

Easy to debug and explain in interviews
This design ensures the app is easy to reason about, testable, and reliable under lifecycle changes.
⚠️ Assumptions & Trade-offs
Assumptions

Internet connectivity is available for streaming music

Streaming URLs provided by the API are valid

Trade-offs

Queue reordering is implemented using move up / move down buttons instead of drag & drop to keep interactions simple and predictable

Offline playback is not supported since the app relies on streaming APIs

If the currently playing song is removed from the queue, playback stops instead of automatically selecting the next song, ensuring explicit and predictable behavior

](https://github.com/Chiragjawa/Music-Player-App.git)
