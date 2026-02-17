# MOH TV 安卓TV直播客户端

English | [中文](./README_ZH.md)

A modern Android TV live streaming application built with Jetpack Compose and Media3 ExoPlayer.

## Features

- 📺 Live TV streaming via M3U8/HLS
- 🔄 Automatic channel updates from GitHub
- ⭐ Channel favorites
- 📱 Remote control support (Android TV)
- 🎯 Multiple source management
- 🌙 Dark theme optimized for TV viewing

## Tech Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose + Material 3
- **Player**: Media3 ExoPlayer
- **Architecture**: MVVM + Clean Architecture
- **DI**: Hilt
- **Database**: Room

## Build

### Prerequisites

- JDK 17+
- Android SDK 34

### Build Commands

```bash
# Debug build
./gradlew assembleDebug

# Release build
./gradlew assembleRelease
```

## Auto Build

This project uses GitHub Actions for automatic builds. On every push to main branch, the APK will be automatically built.

## License

MIT License
