# 📋 TODO List - Implementasi Online Mode untuk Quiz Battle

> **API Base URL**: `https://elysia.asepharyana.tech`
> 
> **Target**: Mengimplementasikan fitur online multiplayer dengan WebSocket dan REST API
dokumentasi api "D:\QuizBattle\api.md"
---

## 🎯 Phase 1: Setup & Infrastructure

### 1.1 Dependencies & Gradle Setup
- [ ] Tambahkan dependencies di `app/build.gradle.kts`:
  - [ ] Retrofit untuk REST API (`com.squareup.retrofit2:retrofit:2.9.0`)
  - [ ] Retrofit Gson Converter (`com.squareup.retrofit2:converter-gson:2.9.0`)
  - [ ] OkHttp untuk WebSocket (`com.squareup.okhttp3:okhttp:4.12.0`)
  - [ ] Coroutines untuk async operations (jika belum ada)
  - [ ] DataStore untuk token storage (`androidx.datastore:datastore-preferences:1.0.0`)

### 1.2 Network Configuration
- [ ] Buat `data/remote/ApiConfig.kt` dengan base URL
- [ ] Setup Retrofit instance dengan interceptors
- [ ] Tambahkan logging interceptor untuk debugging
- [ ] Buat auth interceptor untuk menambahkan Bearer token otomatis
- [ ] Tambahkan permission internet di `AndroidManifest.xml`

### 1.3 Data Models
- [ ] Buat package `data/remote/model/` untuk response models
- [ ] Buat `ApiResponse.kt` (generic response wrapper)
- [ ] Buat `UserResponse.kt`, `PostResponse.kt`, dll.
- [ ] Buat `WebSocketMessage.kt` untuk WebSocket messages
- [ ] Buat mapper untuk konversi dari remote ke local entities

---

## 🔐 Phase 2: Authentication System

### 2.1 REST API - Auth Endpoints
- [ ] Buat `data/remote/api/AuthApiService.kt`:
  - [ ] `suspend fun login(email: String, password: String): ApiResponse<User>`
  - [ ] `suspend fun register(name: String, email: String, password: String): ApiResponse<User>`
  - [ ] `suspend fun refreshToken(): ApiResponse<String>`

### 2.2 Token Management
- [ ] Buat `data/repository/TokenRepository.kt`:
  - [ ] `suspend fun saveToken(token: String)`
  - [ ] `suspend fun getToken(): String?`
  - [ ] `suspend fun clearToken()`
- [ ] Implementasikan token storage dengan DataStore Preferences

### 2.3 Update Auth ViewModel
- [ ] Update `AuthViewModel.kt` untuk menggunakan API:
  - [ ] Integrasikan login dengan REST API
  - [ ] Integrasikan register dengan REST API
  - [ ] Simpan JWT token setelah login/register berhasil
  - [ ] Handle error responses dari API

### 2.4 Sync System
- [ ] Buat logic untuk sync user lokal dengan server:
  - [ ] Saat login berhasil, update/create user di database lokal
  - [ ] Saat register berhasil, simpan user ke database lokal dan server

---

## 🎮 Phase 3: Online Battle System - WebSocket

### 3.1 WebSocket Manager
- [ ] Buat `data/remote/websocket/WebSocketManager.kt`:
  - [ ] Implementasi connection ke `wss://elysia.asepharyana.tech/api/quiz/battle`
  - [ ] Handle authentication (`auth:connect`)
  - [ ] Handle reconnection logic
  - [ ] Implement ping/pong untuk keep-alive
  - [ ] Message parser (JSON ke data class)
  - [ ] Event listener/callback system

### 3.2 WebSocket Message Types
- [ ] Buat sealed class `WebSocketEvent.kt` untuk semua event types:
  - [ ] `AuthConnected`, `AuthError`
  - [ ] `MatchmakingSearching`, `MatchFound`, `MatchCancelled`
  - [ ] `GameAnswerResult`, `GameStarting`, `GameFinished`
  - [ ] `OpponentAnswered`, `OpponentDisconnected`

### 3.3 Matchmaking System
- [ ] Buat `data/repository/MatchmakingRepository.kt`:
  - [ ] `suspend fun findMatch(gameMode: String, difficulty: String)`
  - [ ] `suspend fun cancelMatchmaking()`
  - [ ] Collect WebSocket events dan transform ke Flow/StateFlow

### 3.4 Game Session Repository
- [ ] Buat `data/repository/OnlineGameRepository.kt`:
  - [ ] `suspend fun submitAnswer(matchId: String, questionId: String, answer: String, timeSpent: Int)`
  - [ ] `fun observeGameEvents(): Flow<GameEvent>`
  - [ ] Handle real-time game state updates

---

## 🖥️ Phase 4: UI Layer - Online Battle

### 4.1 Matchmaking Screen
- [ ] Buat `ui/screens/MatchmakingScreen.kt`:
  - [ ] Loading animation saat mencari lawan
  - [ ] Queue position display
  - [ ] Estimated wait time
  - [ ] Cancel button
  - [ ] Match found dialog dengan info opponent

### 4.2 Update Navigation
- [ ] Tambahkan route baru di `Navigation.kt`:
  - [ ] `Screen.OnlineMenu` untuk pilihan mode online
  - [ ] `Screen.Matchmaking` untuk searching match
  - [ ] `Screen.OnlineBattle` untuk battle online
  - [ ] `Screen.OnlineBattleResult` untuk hasil

### 4.3 Online Battle Screen
- [ ] Buat `ui/screens/OnlineBattleScreen.kt`:
  - [ ] Layout serupa dengan `BattleScreen.kt` tapi dengan real opponent
  - [ ] Display opponent username, level, avatar
  - [ ] Real-time opponent score update
  - [ ] Indicator saat opponent menjawab
  - [ ] Handle disconnection scenarios

### 4.4 Online Battle ViewModel
- [ ] Buat `viewmodel/OnlineBattleViewModel.kt`:
  - [ ] Koneksi ke WebSocket
  - [ ] Submit answer via WebSocket
  - [ ] Listen untuk opponent updates
  - [ ] Handle game timer
  - [ ] Calculate scores berdasarkan server response
  - [ ] Handle game end conditions

### 4.5 Update Main Screen
- [ ] Update `MainScreen.kt`:
  - [ ] Aktifkan button "Main Online"
  - [ ] Navigasi ke `OnlineMenuScreen` atau langsung ke matchmaking
  - [ ] Show badge "NEW" atau "ONLINE" jika fitur aktif

---

## 👥 Phase 5: Friends System

### 5.1 REST API - Friends
- [ ] Buat `data/remote/api/FriendsApiService.kt`:
  - [ ] `suspend fun getFriendList(): ApiResponse<List<Friend>>`
  - [ ] `suspend fun getPendingRequests(): ApiResponse<List<FriendRequest>>`

### 5.2 WebSocket - Friends
- [ ] Implementasikan WebSocket events di `WebSocketManager.kt`:
  - [ ] `friend.request.send`
  - [ ] `friend.request.accept`
  - [ ] `friend.request.reject`
  - [ ] `friend.remove`
  - [ ] `friend.list.request`
  - [ ] `friend.challenge`

### 5.3 Friends Repository
- [ ] Buat `data/repository/OnlineFriendsRepository.kt`:
  - [ ] `suspend fun getFriends(): Flow<List<Friend>>`
  - [ ] `suspend fun sendFriendRequest(username: String)`
  - [ ] `suspend fun acceptFriendRequest(requestId: String)`
  - [ ] `suspend fun rejectFriendRequest(requestId: String)`
  - [ ] `suspend fun challengeFriend(friendId: String, gameSettings: GameSettings)`
  - [ ] Sync dengan database lokal

### 5.4 Update Friend List Screen
- [ ] Update `ui/screens/FriendListScreen.kt`:
  - [ ] Display online/offline status dari WebSocket
  - [ ] Add friend button dengan search by username
  - [ ] Pending requests section
  - [ ] Challenge friend button
  - [ ] Show friend stats (wins, losses, level)

### 5.5 Friend List ViewModel
- [ ] Update atau buat `viewmodel/FriendListViewModel.kt`:
  - [ ] Load friends dari API/WebSocket
  - [ ] Handle send/accept/reject friend requests
  - [ ] Handle challenge friend
  - [ ] Real-time status updates

---

## 🏆 Phase 6: Leaderboard - Online Integration

### 6.1 REST API - Users
- [ ] Buat `data/remote/api/UsersApiService.kt`:
  - [ ] `suspend fun getAllUsers(): ApiResponse<List<User>>`
  - [ ] `suspend fun getUserById(id: String): ApiResponse<User>`

### 6.2 Leaderboard WebSocket
- [ ] Implement di `WebSocketManager.kt`:
  - [ ] `leaderboard.global.sync`
  - [ ] `leaderboard.friends.sync`

### 6.3 Leaderboard Repository
- [ ] Buat `data/repository/OnlineLeaderboardRepository.kt`:
  - [ ] `suspend fun getGlobalLeaderboard(limit: Int, offset: Int): List<LeaderboardEntry>`
  - [ ] `suspend fun getFriendsLeaderboard(): List<LeaderboardEntry>`
  - [ ] Cache dengan database lokal
  - [ ] Auto-refresh setiap X menit

### 6.4 Update Main Screen Leaderboard
- [ ] Update `MainScreen.kt`:
  - [ ] Toggle antara "Local" dan "Global" leaderboard
  - [ ] Toggle "Friends Only" filter
  - [ ] Show user's global rank
  - [ ] Pull-to-refresh

### 6.5 Update Main ViewModel
- [ ] Update `MainViewModel.kt`:
  - [ ] Add `loadOnlineLeaderboard()` function
  - [ ] Add state untuk toggle local/global
  - [ ] Handle refresh dari API

---

## 💬 Phase 7: Chat System

### 7.1 REST API - Chat Rooms
- [ ] Buat `data/remote/api/ChatApiService.kt`:
  - [ ] `suspend fun getChatRooms(): ApiResponse<List<ChatRoom>>`
  - [ ] `suspend fun createChatRoom(name: String, isPrivate: Boolean): ApiResponse<ChatRoom>`
  - [ ] `suspend fun getRoomMessages(roomId: String, limit: Int): ApiResponse<List<Message>>`

### 7.2 WebSocket - Chat
- [ ] Implement di `WebSocketManager.kt`:
  - [ ] `chat:global:send`
  - [ ] `chat:private:send`
  - [ ] `chat:global:message` (receive)
  - [ ] `chat:private:message` (receive)
  - [ ] `chat:typing` indicator
  - [ ] `chat:mark:read`

### 7.3 Chat Repository
- [ ] Buat `data/repository/ChatRepository.kt`:
  - [ ] `suspend fun sendMessage(roomId: String, message: String)`
  - [ ] `fun observeMessages(roomId: String): Flow<List<Message>>`
  - [ ] `suspend fun sendPrivateMessage(userId: String, message: String)`
  - [ ] `suspend fun getChatHistory(userId: String)`
  - [ ] Local cache dengan Room database

### 7.4 Chat UI
- [ ] Buat `ui/screens/ChatRoomScreen.kt`:
  - [ ] Message list (LazyColumn)
  - [ ] Input field dengan send button
  - [ ] Typing indicator
  - [ ] Message timestamps
  - [ ] User avatars

- [ ] Buat `ui/screens/ChatListScreen.kt`:
  - [ ] List chat rooms
  - [ ] Unread message badges
  - [ ] Create new room button
  - [ ] Search rooms

### 7.5 Chat ViewModel
- [ ] Buat `viewmodel/ChatViewModel.kt`:
  - [ ] Load messages
  - [ ] Send message
  - [ ] Listen real-time messages via WebSocket
  - [ ] Handle typing indicator
  - [ ] Mark as read

---

## 🎮 Phase 8: Advanced Game Features

### 8.1 Lobby System
- [ ] Buat `ui/screens/LobbyScreen.kt`:
  - [ ] Create lobby form (max players, game settings)
  - [ ] Lobby code display
  - [ ] Players list dengan ready status
  - [ ] Ready/Unready button
  - [ ] Start game button (host only)
  - [ ] Kick player button (host only)

- [ ] Buat `ui/screens/LobbyListScreen.kt`:
  - [ ] List available lobbies
  - [ ] Join lobby dengan kode
  - [ ] Lobby info cards

- [ ] Buat `viewmodel/LobbyViewModel.kt`:
  - [ ] Create/join/leave lobby
  - [ ] Handle ready status
  - [ ] Start game (host)
  - [ ] Listen lobby events via WebSocket

### 8.2 Ranked Mode
- [ ] Buat `ui/screens/RankedScreen.kt`:
  - [ ] Display user tier, division, MMR
  - [ ] Ranked points progress bar
  - [ ] Win/loss stats
  - [ ] Ranked leaderboard
  - [ ] Play ranked button

- [ ] Buat `viewmodel/RankedViewModel.kt`:
  - [ ] Load ranked stats dari API
  - [ ] Start ranked matchmaking
  - [ ] Update stats setelah game

- [ ] WebSocket ranked events:
  - [ ] `ranked.stats.sync`
  - [ ] `ranked.leaderboard.sync`

### 8.3 Daily Missions & Achievements
- [ ] Buat `ui/screens/MissionsScreen.kt`:
  - [ ] Daily missions list dengan progress bars
  - [ ] Claim reward button
  - [ ] Timer untuk reset missions
  - [ ] Achievement showcase

- [ ] Buat `viewmodel/MissionsViewModel.kt`:
  - [ ] Load missions dari WebSocket
  - [ ] Claim rewards
  - [ ] Auto-update progress

- [ ] WebSocket events:
  - [ ] `daily.mission.list.sync`
  - [ ] `daily.mission.claim`
  - [ ] `achievement.list.sync`
  - [ ] `achievement.unlocked` (notification)

---

## 📱 Phase 9: Social Media Integration

### 9.1 Posts API
- [ ] Buat `data/remote/api/SocialMediaApiService.kt`:
  - [ ] `suspend fun getPosts(): ApiResponse<List<Post>>`
  - [ ] `suspend fun createPost(content: String, imageUrl: String?): ApiResponse<Post>`
  - [ ] `suspend fun updatePost(id: String, content: String): ApiResponse<Post>`
  - [ ] `suspend fun deletePost(id: String): ApiResponse<Unit>`
  - [ ] `suspend fun likePost(id: String): ApiResponse<Like>`
  - [ ] `suspend fun unlikePost(id: String): ApiResponse<Unit>`
  - [ ] `suspend fun addComment(postId: String, content: String): ApiResponse<Comment>`

### 9.2 Social Media Repository
- [ ] Buat `data/repository/SocialMediaRepository.kt`:
  - [ ] CRUD operations untuk posts
  - [ ] Like/unlike post
  - [ ] Add/edit/delete comments
  - [ ] Cache posts di local database

### 9.3 Social Media UI
- [ ] Buat `ui/screens/FeedScreen.kt`:
  - [ ] Post cards (Instagram-like)
  - [ ] Like button dengan animation
  - [ ] Comment section
  - [ ] Create post FAB
  - [ ] Pull-to-refresh
  - [ ] Infinite scroll

- [ ] Buat `ui/screens/CreatePostScreen.kt`:
  - [ ] Text input
  - [ ] Image picker (optional)
  - [ ] Post button

- [ ] Buat `viewmodel/SocialMediaViewModel.kt`:
  - [ ] Load posts
  - [ ] Create/update/delete post
  - [ ] Like/unlike
  - [ ] Add comment

---

## 🔔 Phase 10: Notifications System

### 10.1 Notification Repository
- [ ] Buat `data/repository/NotificationRepository.kt`:
  - [ ] `suspend fun getNotifications(): List<Notification>`
  - [ ] `suspend fun markAsRead(notificationId: String)`
  - [ ] `suspend fun markAllAsRead()`
  - [ ] `suspend fun deleteNotification(notificationId: String)`
  - [ ] Local cache dengan Room

### 10.2 WebSocket Notifications
- [ ] Handle notification events:
  - [ ] `notification.list.sync`
  - [ ] Auto-receive untuk:
    - Friend requests
    - Game challenges
    - Achievement unlocks
    - Daily mission rewards

### 10.3 Notification UI
- [ ] Buat `ui/screens/NotificationScreen.kt`:
  - [ ] Notification list (grouped by type)
  - [ ] Mark as read functionality
  - [ ] Tap to navigate ke related screen
  - [ ] Delete notification swipe action

- [ ] Update `MainScreen.kt`:
  - [ ] Add notification bell icon dengan badge count
  - [ ] Navigate to NotificationScreen

- [ ] Buat `viewmodel/NotificationViewModel.kt`:
  - [ ] Load notifications
  - [ ] Mark as read
  - [ ] Delete notification
  - [ ] Real-time updates via WebSocket

### 10.4 Push Notifications (Optional)
- [ ] Setup Firebase Cloud Messaging (FCM)
- [ ] Register device token ke server
- [ ] Handle notification clicks
- [ ] Show local notifications

---

## 🔧 Phase 11: Settings & Profile

### 11.1 Profile Management
- [ ] Buat `ui/screens/ProfileScreen.kt`:
  - [ ] User avatar
  - [ ] Username, email, level
  - [ ] Stats (wins, losses, win rate)
  - [ ] Edit profile button
  - [ ] Logout button

- [ ] Buat `ui/screens/EditProfileScreen.kt`:
  - [ ] Edit username
  - [ ] Upload avatar
  - [ ] Save changes

### 11.2 Settings Screen
- [ ] Buat `ui/screens/SettingsScreen.kt`:
  - [ ] Account settings
  - [ ] Notification preferences
  - [ ] Sound & vibration toggles
  - [ ] Language selection
  - [ ] Privacy settings
  - [ ] Logout & delete account

### 11.3 Profile ViewModel
- [ ] Buat `viewmodel/ProfileViewModel.kt`:
  - [ ] Load user profile
  - [ ] Update profile (API call)
  - [ ] Upload avatar (jika ada image upload API)
  - [ ] Logout logic (clear token, navigate to login)

---

## 🧪 Phase 12: Testing & Error Handling

### 12.1 Error Handling
- [ ] Buat `data/remote/ApiException.kt` untuk custom exceptions
- [ ] Implement global error handler di Retrofit
- [ ] Handle network errors:
  - [ ] No internet connection
  - [ ] Timeout
  - [ ] Server errors (500)
  - [ ] Authentication errors (401)
  - [ ] Not found (404)

### 12.2 Loading States
- [ ] Buat `data/model/Resource.kt` untuk wrapper (Loading, Success, Error)
- [ ] Update semua ViewModels untuk emit loading states
- [ ] UI loading indicators di semua screens

### 12.3 WebSocket Reliability
- [ ] Auto-reconnect saat connection lost
- [ ] Queue messages saat disconnected
- [ ] Show connection status di UI
- [ ] Handle "opponent disconnected" scenario

### 12.4 Testing
- [ ] Unit tests untuk repositories
- [ ] Integration tests untuk API calls
- [ ] UI tests untuk critical flows (login, matchmaking, game)
- [ ] WebSocket connection tests

---

## 📊 Phase 13: Analytics & Monitoring

### 13.1 Game Analytics
- [ ] Track game sessions:
  - [ ] Match start/end
  - [ ] Questions answered
  - [ ] Average response time
  - [ ] Win/loss rates per difficulty

### 13.2 User Behavior
- [ ] Track screen views
- [ ] Track button clicks
- [ ] Track feature usage
- [ ] Track errors & crashes

### 13.3 Performance Monitoring
- [ ] Monitor API response times
- [ ] Monitor WebSocket connection stability
- [ ] Monitor app performance (frame drops, memory)

---

## 🎨 Phase 14: Polish & UX Improvements

### 14.1 Animations
- [ ] Match found celebration animation
- [ ] Victory/defeat animations untuk online
- [ ] Loading skeletons untuk list screens
- [ ] Smooth transitions antar screens

### 14.2 Feedback
- [ ] Haptic feedback untuk button clicks
- [ ] Sound effects untuk game events
- [ ] Toast messages untuk success/error
- [ ] Dialogs untuk confirmations

### 14.3 Empty States
- [ ] Empty leaderboard
- [ ] No friends yet
- [ ] No notifications
- [ ] No chat messages

### 14.4 Offline Support
- [ ] Detect offline mode
- [ ] Show "offline" banner
- [ ] Queue actions untuk saat online kembali
- [ ] Graceful degradation (disable online features)

---

## 🚀 Phase 15: Deployment & Launch

### 15.1 Build Configuration
- [ ] Setup ProGuard rules untuk release build
- [ ] Optimize APK size
- [ ] Configure signing key
- [ ] Version bump

### 15.2 Documentation
- [ ] User manual untuk online mode
- [ ] API integration documentation
- [ ] Code documentation (KDoc)
- [ ] README update dengan fitur baru

### 15.3 Beta Testing
- [ ] Internal testing (tim development)
- [ ] Closed beta (selected users)
- [ ] Collect feedback
- [ ] Fix critical bugs

### 15.4 Launch Preparation
- [ ] Prepare release notes
- [ ] Create promotional materials
- [ ] Setup monitoring & alerts
- [ ] Plan rollout strategy

### 15.5 Post-Launch
- [ ] Monitor crash reports
- [ ] Monitor API errors
- [ ] Monitor user feedback
- [ ] Plan updates & improvements

---

## 📝 Notes & Best Practices

### Development Guidelines
- **Architecture**: Tetap gunakan MVVM pattern
- **Dependency Injection**: Consider menggunakan Hilt/Dagger untuk DI
- **Error Handling**: Selalu handle exceptions dan berikan feedback ke user
- **Code Quality**: Follow Kotlin coding conventions
- **Testing**: Write tests untuk critical business logic
- **Security**: Jangan hardcode API keys/secrets

### API Integration Tips
- **Token Management**: Auto-refresh token saat expired
- **Caching**: Cache data untuk improve performance
- **Pagination**: Implement pagination untuk list yang panjang
- **Retry Logic**: Implement retry untuk network failures

### WebSocket Best Practices
- **Reconnection**: Implement exponential backoff
- **Heartbeat**: Use ping/pong untuk keep connection alive
- **Message Queue**: Queue messages saat disconnected
- **State Management**: Sync WebSocket state dengan UI state

---

## 🔗 Resources

### API Documentation
- Base URL: `https://elysia.asepharyana.tech`
- API Docs: `D:\QuizBattle\api.md`

### Libraries Documentation
- [Retrofit](https://square.github.io/retrofit/)
- [OkHttp](https://square.github.io/okhttp/)
- [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-guide.html)
- [Room Database](https://developer.android.com/training/data-storage/room)
- [DataStore](https://developer.android.com/topic/libraries/architecture/datastore)

---

**Created**: November 13, 2025
**Version**: 1.0
**Status**: Planning Phase
**Estimated Completion**: 8-12 weeks (tergantung tim size)
