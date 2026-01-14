# QuizBattle Clean Architecture Guidelines

## Struktur Package

```
com.mytheclipse.quizbattle/
├── core/                         # Komponen inti yang dapat digunakan ulang
│   ├── base/                     # Base classes
│   │   ├── BaseActivity.kt       # Base activity dengan ViewBinding
│   │   ├── BaseViewModel.kt      # Base ViewModel dengan loading/error handling
│   │   └── BaseAdapter.kt        # Base RecyclerView adapter
│   ├── domain/                   # Domain layer
│   │   ├── Result.kt             # Result wrapper untuk operasi
│   │   └── repository/           # Repository interfaces
│   ├── ui/                       # UI utilities
│   │   ├── UiState.kt            # Sealed class untuk UI state
│   │   └── UiEvent.kt            # One-time events
│   ├── extensions/               # Kotlin extensions
│   │   ├── ViewExtensions.kt     # View utilities
│   │   ├── FlowExtensions.kt     # Flow utilities
│   │   ├── ContextExtensions.kt  # Context utilities
│   │   └── CommonExtensions.kt   # String, Number, Date utilities
│   └── util/                     # Utility classes
│       ├── Constants.kt          # App constants
│       ├── Validators.kt         # Input validation
│       ├── Debouncer.kt          # Debounce/throttle utilities
│       └── NetworkStateMonitor.kt# Network connectivity
├── viewmodel/                    # ViewModels (sudah direfaktor)
├── adapter/                      # Adapters (sudah direfaktor)
├── data/                         # Data layer
│   ├── local/                    # Room Database
│   ├── remote/                   # API services
│   └── repository/               # Repositories
└── utils/                        # Legacy utilities
```

## Panduan Refactoring

### 1. Activities

**Sebelum (Anti-pattern):**
```kotlin
class MyActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMyBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Setup dan observe di sini...
    }
}
```

**Sesudah (Best Practice):**
```kotlin
class MyActivity : BaseActivity<ActivityMyBinding>() {
    
    override val bindingInflater = ActivityMyBinding::inflate
    
    override fun setupUI() {
        applySystemBarPadding()
        setupClickListeners()
    }
    
    override fun observeState() {
        viewModel.state.collectWithLifecycle { state ->
            handleState(state)
        }
    }
}
```

### 2. ViewModels

**Sebelum (Anti-pattern):**
```kotlin
class MyViewModel(application: Application) : AndroidViewModel(application) {
    private val _state = MutableStateFlow(MyState())
    val state: StateFlow<MyState> = _state.asStateFlow()
    
    fun doSomething() {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true)
                val result = repository.getData()
                _state.value = _state.value.copy(isLoading = false, data = result)
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, error = e.message)
            }
        }
    }
}
```

**Sesudah (Best Practice):**
```kotlin
class MyViewModel : BaseViewModel() {
    
    private val _state = MutableStateFlow<UiState<MyData>>(UiState.Empty)
    val state: StateFlow<UiState<MyData>> = _state.asStateFlow()
    
    fun doSomething() {
        launchWithLoading {
            executeWithState(_state) {
                repository.getData()
            }
        }
    }
}
```

### 3. UI State dengan Sealed Classes

```kotlin
sealed class MyScreenState {
    data object Initial : MyScreenState()
    data object Loading : MyScreenState()
    data class Success(val data: MyData) : MyScreenState()
    data class Error(val message: String) : MyScreenState()
}

// Di Activity:
when (state) {
    is MyScreenState.Initial -> hideAll()
    is MyScreenState.Loading -> showLoading()
    is MyScreenState.Success -> showData(state.data)
    is MyScreenState.Error -> showError(state.message)
}
```

### 4. One-time Events

Gunakan `SharedFlow` untuk events yang seharusnya tidak di-replay:

```kotlin
class MyViewModel : BaseViewModel() {
    private val _event = MutableSharedFlow<MyEvent>()
    val event: SharedFlow<MyEvent> = _event.asSharedFlow()
    
    fun onButtonClick() {
        viewModelScope.launch {
            _event.emit(MyEvent.NavigateToDetails)
        }
    }
}

sealed class MyEvent {
    data object NavigateToDetails : MyEvent()
    data class ShowToast(val message: String) : MyEvent()
}
```

### 5. Adapters

**Sebelum (Anti-pattern):**
```kotlin
class MyAdapter : ListAdapter<MyItem, MyAdapter.ViewHolder>(DiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMyBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }
    
    // ... boilerplate code
}
```

**Sesudah (Best Practice):**
```kotlin
class MyAdapter : BaseAdapter<MyItem, ItemMyBinding>(
    diffCallback = simpleDiffCallback(
        areItemsSame = { old, new -> old.id == new.id }
    )
) {
    override val bindingInflater = ItemMyBinding::inflate
    
    override fun bind(binding: ItemMyBinding, item: MyItem, position: Int) {
        binding.apply {
            titleText.text = item.title
            // ... binding logic
        }
    }
}
```

### 6. Validation

```kotlin
fun validateForm(): Boolean {
    val result = Validators.validateAll(
        { Validators.validateEmail(email) },
        { Validators.validatePassword(password) },
        { Validators.validatePasswordMatch(password, confirmPassword) }
    )
    
    result.onInvalid { error ->
        showError(error)
    }
    
    return result.isValid
}
```

### 7. Extensions Usage

```kotlin
// View visibility
view.show()
view.hide()
view.showIf(condition)

// Safe click dengan debounce
button.setOnSafeClickListener {
    performAction()
}

// Flow collection dengan lifecycle
viewModel.state.collectWithLifecycle { state ->
    handleState(state)
}

// Context extensions
context.toast("Message")
context.showConfirmDialog("Title", "Message") { onConfirm() }
if (context.isNetworkAvailable()) { /* ... */ }
```

### 8. Constants

Gunakan `Constants` object untuk semua magic numbers dan strings:

```kotlin
// Sebelum
val timeout = 30000L  // apa ini?

// Sesudah
val timeout = Constants.Network.CONNECT_TIMEOUT_SECONDS * 1000

// Intent extras
intent.putExtra(Constants.IntentExtra.GAME_ID, gameId)
```

## Checklist Refactoring

- [ ] Activity extends `BaseActivity<ViewBinding>`
- [ ] ViewModel extends `BaseViewModel`
- [ ] Gunakan `UiState<T>` untuk state
- [ ] Gunakan `SharedFlow` untuk one-time events
- [ ] Adapters menggunakan `BaseAdapter`
- [ ] Input validation dengan `Validators`
- [ ] Click listeners dengan `setOnSafeClickListener`
- [ ] Flow collection dengan `collectWithLifecycle`
- [ ] Magic numbers dipindah ke `Constants`
- [ ] Error handling terpusat
- [ ] Loading state terpusat

## Prinsip SOLID

1. **Single Responsibility**: Setiap class punya satu tanggung jawab
2. **Open/Closed**: Extend, jangan modify
3. **Liskov Substitution**: Subclass harus bisa replace parent
4. **Interface Segregation**: Interface kecil lebih baik
5. **Dependency Inversion**: Depend on abstractions

## Clean Architecture Layers

```
Presentation (UI) → Domain (Business Logic) → Data (Repository)
     ↓                      ↓                      ↓
 Activities           Use Cases              Repositories
 ViewModels           Entities               Data Sources
 Adapters                                    API/Database
```
## ViewModel Refactoring Patterns (Applied)

Berikut adalah pattern yang telah diterapkan pada semua ViewModels di project ini:

### Pattern 1: Structured State with Helper Properties

```kotlin
data class MyState(
    val items: List<Item> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
) {
    // Helper properties untuk UI
    val hasItems: Boolean get() = items.isNotEmpty()
    val itemCount: Int get() = items.size
    val filteredItems: List<Item> get() = items.filter { it.isActive }
}
```

### Pattern 2: Sealed Class for One-time Events

```kotlin
sealed class MyEvent {
    data class ShowSuccess(val message: String) : MyEvent()
    data class ShowError(val message: String) : MyEvent()
    data object NavigateBack : MyEvent()
}
```

### Pattern 3: CoroutineExceptionHandler

```kotlin
private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
    Log.e(TAG, "Coroutine error", throwable)
    updateState { copy(isLoading = false, error = throwable.message) }
}

private fun launchSafely(block: suspend () -> Unit) {
    viewModelScope.launch(exceptionHandler) { block() }
}
```

### Pattern 4: Inline State Update

```kotlin
private inline fun updateState(update: MyState.() -> MyState) {
    _state.update { it.update() }
}

// Usage
updateState { copy(isLoading = true) }
```

### Pattern 5: Region Organization

```kotlin
class MyViewModel(application: Application) : AndroidViewModel(application) {
    
    // region Dependencies
    private val repository = MyRepository()
    // endregion
    
    // region State
    private val _state = MutableStateFlow(MyState())
    val state: StateFlow<MyState> = _state.asStateFlow()
    // endregion
    
    // region Public Actions
    fun loadData() { /* ... */ }
    // endregion
    
    // region Private Methods
    private fun handleEvent() { /* ... */ }
    // endregion
    
    // region Utility Methods
    private fun logError() { /* ... */ }
    // endregion
    
    companion object {
        private const val TAG = "MyViewModel"
    }
}
```

### Pattern 6: Companion Object Constants

```kotlin
companion object {
    private const val TAG = "MyViewModel"
    
    // Error messages
    private const val ERROR_NO_USER = "User not found"
    private const val ERROR_NETWORK = "Network error"
    
    // Default values
    private const val DEFAULT_PAGE_SIZE = 20
    private const val DEFAULT_TIMEOUT = 30_000L
}
```

### ViewModels yang Telah Direfactor

| ViewModel | Patterns Applied |
|-----------|------------------|
| AuthViewModel | Sealed State, Events, ExceptionHandler |
| BattleViewModel | Events, Helper Properties, Constants |
| ChatViewModel | Events, Extension Functions |
| FriendListViewModel | Helper Properties, Utility Methods |
| GameHistoryViewModel | Stats Properties, Events |
| LobbyViewModel | Events, State Helpers |
| MainViewModel | ExceptionHandler, Constants |
| MatchmakingViewModel | Events, State Helpers |
| NotificationViewModel | Events, Filter Properties |
| OnlineGameViewModel | Events, Match Filtering |
| OnlineLeaderboardViewModel | Events, Top Players Helper |
| ProfileViewModel | Events, Extracted Methods |
| QuestionManagementViewModel | Events, CRUD Operations |
| RankedViewModel | Tier Display, Events |
| SettingsViewModel | Setting Change Events |
| SocialMediaViewModel | Events, Post Mapping |

## Activity Refactoring Patterns (Applied)

Berikut adalah pattern yang telah diterapkan pada semua Activities di project ini:

### Pattern 1: Region Organization

```kotlin
class MyActivity : BaseActivity() {

    // region Properties
    private lateinit var binding: ActivityMyBinding
    private val viewModel: MyViewModel by viewModels()
    // endregion

    // region Lifecycle
    override fun onCreate(savedInstanceState: Bundle?) { ... }
    override fun onResume() { ... }
    // endregion

    // region Setup
    private fun setupRecyclerView() { ... }
    private fun setupClickListeners() { ... }
    // endregion

    // region State Observation
    private fun observeState() { ... }
    private fun handleState(state: MyState) { ... }
    // endregion

    // region Actions
    private fun handleButtonClick() { ... }
    // endregion

    // region Navigation
    private fun navigateToDetail() { ... }
    // endregion

    // region Dialogs
    private fun showConfirmDialog() { ... }
    // endregion

    // region Utilities
    private fun cleanupResources() { ... }
    // endregion

    companion object {
        const val EXTRA_ID = "extra_id"
    }
}
```

### Pattern 2: BaseActivity Extensions

```kotlin
// Use collectState for state observation
private fun observeState() {
    collectState(viewModel.state) { state ->
        handleState(state)
    }
}

// Use withDebounce for click handling
binding.button.setOnClickListener {
    withDebounce { performAction() }
}

// Use navigateTo for type-safe navigation
navigateTo<DetailActivity> {
    putExtra(DetailActivity.EXTRA_ID, itemId)
}

// Use navigateBack instead of finish()
binding.backButton.setOnClickListener { navigateBack() }

// Use showToast for toast messages
showToast(getString(R.string.success_message))
```

### Pattern 3: Extracted Handler Methods

```kotlin
private fun handleState(state: MyState) {
    updateLoadingState(state.isLoading)
    updateList(state)
    handleError(state.error)
}

private fun updateLoadingState(isLoading: Boolean) {
    binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
}

private fun updateList(state: MyState) {
    val isEmpty = state.items.isEmpty() && !state.isLoading
    binding.recyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
    binding.emptyState.visibility = if (isEmpty) View.VISIBLE else View.GONE
}

private fun handleError(error: String?) {
    error?.let { showToast(it) }
}
```

### Pattern 4: Data Classes for Complex Data

```kotlin
// Use data classes for intent extras grouping
private data class MatchData(
    val matchId: String,
    val opponentName: String,
    val opponentLevel: Int,
    val category: String,
    val difficulty: String
)

private fun extractMatchData(): MatchData = MatchData(
    matchId = intent.getStringExtra(EXTRA_MATCH_ID) ?: "",
    opponentName = intent.getStringExtra(EXTRA_OPPONENT_NAME) ?: DEFAULT_OPPONENT,
    opponentLevel = intent.getIntExtra(EXTRA_OPPONENT_LEVEL, 1),
    category = intent.getStringExtra(EXTRA_CATEGORY) ?: DEFAULT_CATEGORY,
    difficulty = intent.getStringExtra(EXTRA_DIFFICULTY) ?: DEFAULT_DIFFICULTY
)
```

### Pattern 5: Constants in Companion Object

```kotlin
companion object {
    private const val TAG = "MyActivity"
    
    // Intent extras
    const val EXTRA_ID = "extra_id"
    const val EXTRA_NAME = "extra_name"
    
    // Default values
    private const val DEFAULT_TIMEOUT = 30_000L
    private const val ANIMATION_DURATION = 300L
    
    // Formatting
    private const val TIME_FORMAT = "HH:mm"
}
```

### Activities yang Telah Direfactor

| Activity | Key Features |
|----------|--------------|
| BaseActivity | withDebounce, collectState, navigateTo, showToast, logDebug |
| SplashActivity | Constants, cleanup, region organization |
| LoginActivity | Google Sign-In, validation, redirect handling |
| RegisterActivity | Form validation, error handling |
| ResetPasswordActivity | Simple flow, state observation |
| MainActivity | Navigation helpers, leaderboard display |
| BattleActivity | Animations, sound, timer, combat mechanics |
| BattleResultActivity | Rewards display, data class for rewards |
| OnlineMenuActivity | Matchmaking, timer, dialogs |
| OnlineBattleActivity | WebSocket game, animations, vibration |
| LeaderboardActivity | List with rank formatting |
| NotificationActivity | Badge formatting, type-based navigation |
| GameHistoryActivity | Simple list display |
| ChatListActivity | Room navigation |
| ChatRoomActivity | Typing indicator, message handling |
| ProfileActivity | Avatar display, stats |
| EditProfileActivity | Image picker, form validation |
| SettingsActivity | Switch listeners, dialogs |
| FriendListActivity | Tabs, action dialogs |
| QuestionManagementActivity | CRUD dialogs, form validation |

## Adapter Refactoring Patterns

### Pattern 1: KDoc Documentation

```kotlin
/**
 * RecyclerView adapter for displaying [MyItem] in a list.
 * 
 * Provides click handling via callbacks.
 *
 * @param onItemClick Callback invoked when an item is clicked
 */
class MyAdapter(
    private val onItemClick: (MyItem) -> Unit
) : ListAdapter<MyItem, MyAdapter.ViewHolder>(DiffCallback())
```

### Pattern 2: ViewHolder Method Extraction

```kotlin
class ViewHolder(
    private val binding: ItemBinding
) : RecyclerView.ViewHolder(binding.root) {
    
    fun bind(item: MyItem) {
        bindContent(item)
        bindStyle(item)
        setupClickListener(item)
    }
    
    private fun bindContent(item: MyItem) { ... }
    private fun bindStyle(item: MyItem) { ... }
    private fun setupClickListener(item: MyItem) { ... }
}
```

### Pattern 3: Constants in Companion Object

```kotlin
class ViewHolder(...) {
    companion object {
        private val TIME_FORMAT = SimpleDateFormat("HH:mm", Locale.getDefault())
        private const val MAX_BADGE_COUNT = 99
        private const val MAX_BADGE_TEXT = "99+"
    }
}
```