# 🎮 Quiz Battle - Online Multiplayer Quiz Game

[![Build Status](https://img.shields.io/badge/build-passing-brightgreen)](https://github.com/MythEclipse/QuizBattle)
[![Version](https://img.shields.io/badge/version-3.0-blue)](https://github.com/MythEclipse/QuizBattle)
[![Android](https://img.shields.io/badge/platform-Android%208.0%2B-green)](https://www.android.com)
[![Kotlin](https://img.shields.io/badge/kotlin-1.9.0-purple)](https://kotlinlang.org)
[![License](https://img.shields.io/badge/license-MIT-orange)](LICENSE)

An interactive multiplayer quiz game for Android featuring real-time battles, social features, and comprehensive offline support.

---

## 🎯 Features

### 🎮 Core Gameplay
- **Solo Mode**: Practice with offline quiz battles against AI
- **Online Battles**: Real-time 1v1 multiplayer quiz competitions
- **Matchmaking**: Smart matchmaking based on skill level and game mode
- **Ranked Mode**: Competitive ladder system with tiers and divisions
- **Lobby System**: Create/join custom game lobbies with friends

### 👥 Social Features
- **Friends System**: Add friends, see online status, send challenges
- **Global Chat**: Community chat rooms
- **Private Messages**: Direct messaging with friends
- **Social Feed**: Share posts, like, and comment
- **Leaderboards**: Global and friends-only rankings

### 🏆 Progress & Rewards
- **Daily Missions**: Complete tasks for rewards
- **Achievements**: Unlock achievements and badges
- **Player Profile**: Track stats, level, wins/losses
- **Notifications**: Real-time alerts for friend requests, challenges, etc.

### 🌐 Offline Support
- **WebSocket Message Queue**: Auto-queues up to 50 messages when disconnected
- **Offline Action Queue**: Stores up to 100 user actions with retry logic
- **Connection Status UI**: Real-time visual feedback on connection state
- **Auto-Sync**: Automatically syncs data when connection returns

### ✨ User Experience
- **Smooth Animations**: Navigation transitions and button press feedback
- **Haptic Feedback**: Vibration feedback on interactions (94% coverage)
- **Loading States**: Skeleton screens and loading indicators
- **Error Handling**: Comprehensive error states with retry options
- **Empty States**: Helpful messages and CTAs for empty screens
- **Dark/Light Theme**: Modern Material Design 3

---

## 📱 Screenshots

> Add screenshots here after beta testing

---

## 🛠️ Tech Stack

### Frontend
- **Kotlin**: Modern Android development
- **Jetpack Compose**: Declarative UI framework
- **Material Design 3**: Latest design system
- **Navigation Component**: Single-activity architecture
- **Coil**: Image loading and caching

### Backend Integration
- **Retrofit**: REST API communication
- **OkHttp**: HTTP client with WebSocket support
- **Gson**: JSON serialization/deserialization
- **WebSocket**: Real-time bidirectional communication

### Local Storage
- **Room Database**: Local data persistence
- **DataStore**: Preferences and settings storage
- **Offline Queue**: Action queue with retry mechanism

### Architecture
- **MVVM**: Model-View-ViewModel pattern
- **Repository Pattern**: Data layer abstraction
- **Kotlin Coroutines**: Asynchronous programming
- **StateFlow**: Reactive state management

### Testing
- **JUnit**: Unit testing framework
- **Kotlin Test**: Kotlin-specific testing
- **71 Automated Tests**: Comprehensive test coverage

---

## 📦 Installation

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or later
- JDK 11 or higher
- Android SDK (API 26 - 36)
- Gradle 8.0+

### Setup

1. **Clone the repository**:
   ```bash
   git clone https://github.com/MythEclipse/QuizBattle.git
   cd QuizBattle
   ```

2. **Open in Android Studio**:
   - Open Android Studio
   - Select "Open an existing project"
   - Navigate to the cloned directory

3. **Sync Gradle**:
   - Android Studio will automatically sync Gradle
   - Wait for dependencies to download

4. **Run the app**:
   - Connect an Android device or start an emulator
   - Click "Run" (Shift + F10) or use the play button

### Configuration

The app connects to the backend API at:
```
https://elysia.asepharyana.tech
```

To configure a different backend:
1. Open `app/src/main/java/com/mytheclipse/quizbattle/data/remote/ApiConfig.kt`
2. Update `BASE_URL` constant
3. Rebuild the project

---

## 🧪 Testing

### Run Unit Tests
```bash
# PowerShell
.\gradlew.bat testDebugUnitTest

# Output: 71 tests passing
```

### Run on Device
```bash
# Debug build
.\gradlew.bat assembleDebug
adb install app\build\outputs\apk\debug\app-debug.apk

# Release build (requires signing)
.\gradlew.bat assembleRelease
```

### Test Coverage
- **71 Automated Tests**:
  - 20 ViewModel logic tests
  - 12 Validation utility tests
  - 11 Matchmaking repository tests
  - 14 Core utility tests
  - 14 UI component tests

---

## 🚀 Building for Release

### ProGuard/R8 Configuration
The app is configured with comprehensive ProGuard rules for code obfuscation and optimization.

```bash
# Build release APK (unsigned)
.\gradlew.bat assembleRelease

# Output: app\build\outputs\apk\release\app-release-unsigned.apk
# Size: ~13 MB
```

### Signing the APK
See [RELEASE_GUIDE.md](RELEASE_GUIDE.md) for detailed instructions on:
- Generating a signing key
- Configuring signing in Gradle
- Building signed APKs/AABs
- Play Store deployment

---

## 📚 Documentation

- **[TODO_ONLINE_MODE.md](TODO_ONLINE_MODE.md)**: Complete implementation checklist
- **[RELEASE_GUIDE.md](RELEASE_GUIDE.md)**: Release build and deployment guide
- **[BETA_TESTING_GUIDE.md](BETA_TESTING_GUIDE.md)**: Comprehensive beta testing plan
- **[api.md](api.md)**: Backend API documentation
- **[CHANGELOG_GUEST_MODE.md](CHANGELOG_GUEST_MODE.md)**: Guest mode implementation log
- **[FIX_GAMEPLAY.md](FIX_GAMEPLAY.md)**: Gameplay fixes documentation
- **[IMPLEMENTATION.md](IMPLEMENTATION.md)**: Technical implementation details
- **[UI_ENHANCEMENT.md](UI_ENHANCEMENT.md)**: UI/UX improvements log

---

## 🏗️ Project Structure

```
QuizBattle/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/mytheclipse/quizbattle/
│   │   │   │   ├── data/
│   │   │   │   │   ├── local/          # Room database
│   │   │   │   │   │   ├── dao/        # Data Access Objects
│   │   │   │   │   │   ├── entity/     # Database entities
│   │   │   │   │   ├── remote/         # API and WebSocket
│   │   │   │   │   │   ├── api/        # Retrofit service interfaces
│   │   │   │   │   │   ├── model/      # Response models
│   │   │   │   │   │   ├── websocket/  # WebSocket manager
│   │   │   │   │   ├── repository/     # Repository pattern
│   │   │   │   ├── ui/
│   │   │   │   │   ├── components/     # Reusable UI components
│   │   │   │   │   ├── screens/        # 18 screen composables
│   │   │   │   │   ├── theme/          # Material Design 3 theme
│   │   │   │   ├── utils/              # Utilities (haptic, network, etc.)
│   │   │   │   ├── viewmodel/          # ViewModels (10 total)
│   │   │   │   └── MainActivity.kt     # Single activity
│   │   │   ├── res/                    # Resources
│   │   │   └── AndroidManifest.xml
│   │   └── test/                       # Unit tests (71 tests)
│   ├── build.gradle.kts                # App-level Gradle
│   └── proguard-rules.pro              # ProGuard configuration
├── gradle/                             # Gradle wrapper
├── RELEASE_GUIDE.md                    # Release documentation
├── BETA_TESTING_GUIDE.md               # Testing documentation
├── TODO_ONLINE_MODE.md                 # Implementation checklist
└── README.md                           # This file
```

---

## 🎯 Version History

### Version 3.0 (November 15, 2025) - Current
**🚀 Release Preparation Update**

**New Features:**
- ✅ ProGuard/R8 configuration for optimized release builds
- ✅ Comprehensive beta testing plan (60+ test cases)
- ✅ Complete release documentation (RELEASE_GUIDE.md)
- ✅ Beta testing guide (BETA_TESTING_GUIDE.md)
- ✅ Production-ready release builds (~13 MB APK)

**From Version 3.0 (November 14, 2025):**
- ✅ WebSocket message queue (max 50 messages)
- ✅ Offline action queue (max 100 actions, 10 types)
- ✅ Connection status UI with animated banners
- ✅ Enhanced network monitoring
- ✅ 71 automated tests

**From Version 2.9:**
- ✅ Navigation animations throughout the app
- ✅ Button press animations with spring effects
- ✅ ToastUtils, SnackbarUtils, DialogComponents
- ✅ Comprehensive haptic feedback (94% coverage)

### Version 2.8 and Earlier
- ✅ All 18 UI screens implemented
- ✅ Complete backend infrastructure (11 repositories, 10 ViewModels)
- ✅ WebSocket system with auto-reconnect
- ✅ Friends, chat, leaderboard, lobby systems
- ✅ Error handling and loading states (100% coverage)
- ✅ Local database with Room
- ✅ REST API integration with Retrofit

---

## 🤝 Contributing

Contributions are welcome! Please follow these guidelines:

1. **Fork the repository**
2. **Create a feature branch**: `git checkout -b feature/AmazingFeature`
3. **Commit your changes**: `git commit -m 'Add some AmazingFeature'`
4. **Push to the branch**: `git push origin feature/AmazingFeature`
5. **Open a Pull Request**

### Code Style
- Follow [Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use meaningful variable and function names
- Add comments for complex logic
- Write unit tests for new features

### Testing
- Ensure all existing tests pass
- Add new tests for new features
- Aim for >80% code coverage

---

## 🐛 Bug Reports & Feature Requests

Found a bug or have a feature request?

- **Bug Reports**: [GitHub Issues](https://github.com/MythEclipse/QuizBattle/issues)
- **Feature Requests**: [GitHub Discussions](https://github.com/MythEclipse/QuizBattle/discussions)
- **Email**: mytheclipse@support.com

### Bug Report Template
```markdown
**Describe the bug**
A clear description of what the bug is.

**To Reproduce**
Steps to reproduce:
1. Go to...
2. Click on...
3. Observe...

**Expected behavior**
What you expected to happen.

**Screenshots**
If applicable, add screenshots.

**Device (please complete):**
 - Device: [e.g. Samsung Galaxy S23]
 - OS: [e.g. Android 13]
 - App Version: [e.g. 3.0]

**Additional context**
Any other context about the problem.
```

---

## 📊 Project Status

### Completion Status
| Component | Status | Coverage |
|-----------|--------|----------|
| Backend Infrastructure | ✅ Complete | 100% |
| UI Screens | ✅ Complete | 18/18 screens |
| Navigation | ✅ Complete | With animations |
| Error Handling | ✅ Complete | 100% coverage |
| Loading States | ✅ Complete | 18/18 screens |
| Haptic Feedback | ✅ Complete | 94% (17/18 screens) |
| Offline Support | ✅ Complete | Full queue system |
| WebSocket | ✅ Complete | With message queue |
| Unit Tests | ✅ Complete | 71 tests passing |
| ProGuard | ✅ Complete | Optimized builds |
| Beta Testing | ✅ Prepared | Comprehensive plan |
| Release Docs | ✅ Complete | All guides ready |

### Production Readiness: ✅ **READY FOR BETA TESTING**

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 👨‍💻 Author

**MythEclipse**
- GitHub: [@MythEclipse](https://github.com/MythEclipse)
- Email: mytheclipse@support.com

---

## 🙏 Acknowledgments

- [Jetpack Compose](https://developer.android.com/jetpack/compose) - Modern UI toolkit
- [Material Design 3](https://m3.material.io/) - Design system
- [Retrofit](https://square.github.io/retrofit/) - HTTP client
- [OkHttp](https://square.github.io/okhttp/) - WebSocket support
- [Room](https://developer.android.com/training/data-storage/room) - Local database
- [Coil](https://coil-kt.github.io/coil/) - Image loading

---

## 🔗 Links

- **API Documentation**: [api.md](api.md)
- **Backend Server**: [https://elysia.asepharyana.tech](https://elysia.asepharyana.tech)
- **Release Guide**: [RELEASE_GUIDE.md](RELEASE_GUIDE.md)
- **Beta Testing Guide**: [BETA_TESTING_GUIDE.md](BETA_TESTING_GUIDE.md)
- **Implementation TODO**: [TODO_ONLINE_MODE.md](TODO_ONLINE_MODE.md)

---

## 📞 Support

Need help?

- 📧 Email: mytheclipse@support.com
- 💬 Discord: [Join our community](#) (coming soon)
- 📖 Documentation: [GitHub Wiki](https://github.com/MythEclipse/QuizBattle/wiki)

---

<div align="center">

**Made with ❤️ by MythEclipse**

⭐ Star this repository if you find it helpful!

</div>
