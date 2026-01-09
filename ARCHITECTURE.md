# Architecture Documentation - Wheel of Names App

## 1. Clean Architecture Overview

This project implements **Clean Architecture** with three distinct layers:

### 1.1 Domain Layer (Business Logic)
**Location**: `com.project.roulette.domain`

**Responsibility**: Contains business logic and domain models. Independent of frameworks and databases.

**Components**:
- **Models** (`domain/model/`):
  - `Segment`: Individual wheel segment with name, color, and weight
  - `Wheel`: Container of segments with metadata
  - `SpinResult`: Outcome of a spin operation
  - `WheelStatistics`: Aggregated statistics
  - `Result<T>`: Type-safe result handling (sealed class)

- **Repositories** (`domain/repository/`):
  - `WheelRepository`: Interface for wheel persistence
  - `SpinHistoryRepository`: Interface for spin record management
  - `StatisticsRepository`: Interface for statistics computation
  
  **Key Principle**: Repositories are **interfaces** (abstractions), not implementations. This allows:
  - Easy mocking for tests
  - Swapping implementations (e.g., Room ↔ Firestore)
  - Decoupling business logic from persistence details

- **Use Cases** (`domain/usecase/`):
  - Each use case encapsulates a single, focused business operation
  - Inject repository abstractions (not implementations)
  - Coordinate multiple repositories if needed
  - Examples: `SpinWheelUseCase`, `GetWheelByIdUseCase`, `GetWheelStatisticsUseCase`

- **Selection Algorithms** (`domain/usecase/selection/`):
  - `SelectionAlgorithm`: Interface defining selection strategy
  - Implementations: `UniformRandomAlgorithm`, `WeightedRandomAlgorithm`, `SeededRandomAlgorithm`, `RoundRobinAlgorithm`
  - `SelectionAlgorithmFactory`: Factory for creating algorithms
  - **Pattern**: Strategy pattern enables swapping algorithms at runtime

### 1.2 Data Layer (Persistence)
**Location**: `com.project.roulette.data`

**Responsibility**: Implements domain repositories and handles data access.

**Components**:
- **Local Database** (`data/local/database/`):
  - `RouletteDatabase`: Room database definition
  - `WheelDao`, `SpinHistoryDao`: Data access objects
  - `Entities`: Room entity classes (separate from domain models)

- **Mappers** (`data/mapper/`):
  - `WheelMapper`: Converts between `WheelEntity` ↔ `Wheel` domain model
  - `SpinHistoryMapper`: Converts between `SpinHistoryEntity` ↔ `SpinResult`
  - **Purpose**: Maintain architectural boundary between layers
  - **Benefit**: Entity schema changes don't affect domain logic

- **Repository Implementations** (`data/repository/`):
  - `WheelRepositoryImpl`: Implements `WheelRepository` using Room DAO
  - `SpinHistoryRepositoryImpl`: Implements `SpinHistoryRepository` using Room DAO
  - `StatisticsRepositoryImpl`: Computes statistics from spin history and wheel data
  - **Key Pattern**: Implementations are injected via Hilt, client code depends on interfaces

### 1.3 Presentation Layer (UI)
**Location**: `com.project.roulette.presentation`

**Responsibility**: Manages UI state and renders user interface using Jetpack Compose.

**Components**:
- **ViewModels** (`presentation/viewmodel/`):
  - `HomeViewModel`: Manages wheel list, search, CRUD operations
  - `WheelViewModel`: Handles wheel spinning, animations, selection algorithm changes
  - `HistoryViewModel`: Retrieves and displays spin history
  - `StatisticsViewModel`: Fetches and displays statistics
  - `EditorViewModel`: Manages wheel creation and editing
  - **Pattern**: Each ViewModel orchestrates use cases and manages associated UI state
  - **Lifecycle**: Survives configuration changes; auto-cleanup in `onCleared()`

- **UI State** (`presentation/model/`):
  - Sealed classes representing screen states:
    - `HomeUiState`: Loading | Success | Error
    - `WheelUiState`: Loading | Success (with wheel, spinning flag, last result) | Error
    - `HistoryUiState`: Loading | Success | Error
    - `StatisticsUiState`: Loading | Success | Error
    - `EditorUiState`: Loading | Success | Error
  - **Benefit**: Type-safe state handling; compiler enforces exhaustive when statements

- **Screens** (`presentation/screen/`):
  - `HomeScreen`: Wheel list with search and CRUD buttons
  - `WheelScreen`: Interactive spinning wheel with algorithm selector
  - `HistoryScreen`: Scrollable list of past spins
  - `StatisticsScreen`: Charts and distribution of selections
  - `EditorScreen`: Form for creating/editing wheels and segments
  - **Pattern**: Stateless composables; state managed by ViewModels

- **Components** (`presentation/component/`):
  - `WheelCanvas`: Custom Compose `Canvas` composable drawing the spinning wheel
  - Reusable UI elements extracted from screens

- **Navigation** (`presentation/navigation/`):
  - `RouletteScreen`: Sealed interface defining navigation destinations with type-safe parameters
  - `RouletteNavHost`: Jetpack Navigation graph; handles transitions and backstack
  - **Type Safety**: Serialization-based navigation (no string routes)

## 2. Dependency Injection (Hilt)

**Location**: `com.project.roulette.di`

### 2.1 Modules

**DatabaseModule**: Provides Room database and DAOs
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    fun provideRouletteDatabase(context: Context): RouletteDatabase
    
    @Provides
    fun provideWheelDao(db: RouletteDatabase): WheelDao
    // ... other DAOs
}
```

**RepositoryModule**: Binds repository implementations to interfaces
```kotlin
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    abstract fun bindWheelRepository(impl: WheelRepositoryImpl): WheelRepository
    // ... other bindings
}
```

**UseCaseModule**: Provides all use case instances
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {
    @Provides
    fun provideSpinWheelUseCase(
        wheelRepo: WheelRepository,
        historyRepo: SpinHistoryRepository,
        factory: SelectionAlgorithmFactory
    ): SpinWheelUseCase
    // ... other use cases
}
```

**RouletteApplication**: Hilt entry point
```kotlin
@HiltAndroidApp
class RouletteApplication : Application()
```

### 2.2 Injection Flow

```
MainActivity (Activity)
  ↓ (inject NavController)
RouletteNavHost
  ↓ (navigate to screen)
WheelScreen
  ↓ (hiltViewModel())
WheelViewModel (injected with all dependencies)
  ├─ SpinWheelUseCase (injected)
  ├─ GetWheelByIdUseCase (injected)
  ├─ SoundManager (injected)
  └─ HapticFeedback (injected)
      ↓ (depends on)
      WheelRepository (injected as WheelRepositoryImpl)
        ↓ (depends on)
        WheelDao, WheelMapper
          ↓ (depends on)
          RouletteDatabase
```

## 3. SOLID Principles Explained

### 3.1 Single Responsibility Principle (SRP)
**Definition**: A class should have one reason to change.

**Examples**:
- `SpinPhysicsSimulator`: Only calculates animation angles; doesn't handle UI
- `SpinWheelUseCase`: Only orchestrates spin logic; doesn't fetch statistics
- `WheelMapper`: Only converts between entity and domain models
- `SoundManager`: Only manages sound playback; doesn't handle vibration

**Benefit**: Easier to test, modify, and understand each class

### 3.2 Open/Closed Principle (OCP)
**Definition**: Open for extension, closed for modification.

**Examples**:
- `SelectionAlgorithm` interface allows new algorithms without changing existing code
  ```kotlin
  // Add new algorithm without touching existing code
  class BiasedRandomAlgorithm : SelectionAlgorithm {
      override fun selectSegment(segments: List<Segment>): Segment? { ... }
  }
  ```

- Use cases are open for extension via new repository implementations

**Benefit**: New features don't break existing functionality

### 3.3 Liskov Substitution Principle (LSP)
**Definition**: Derived classes must be substitutable for base classes.

**Examples**:
- All `SelectionAlgorithm` implementations can replace each other:
  ```kotlin
  val algorithm: SelectionAlgorithm = when (type) {
      UNIFORM -> UniformRandomAlgorithm()
      WEIGHTED -> WeightedRandomAlgorithm()
      SEEDED -> SeededRandomAlgorithm()
      ROUND_ROBIN -> RoundRobinAlgorithm()
  }
  // Use algorithm uniformly, regardless of actual type
  val selected = algorithm.selectSegment(segments)
  ```

- Repository implementations are interchangeable for tests (in-memory) and production (Room)

**Benefit**: Polymorphism enables flexible, testable code

### 3.4 Interface Segregation Principle (ISP)
**Definition**: Clients shouldn't depend on interfaces they don't use.

**Examples**:
- Separate repositories for different concerns:
  ```kotlin
  interface WheelRepository { /* wheel CRUD */ }
  interface SpinHistoryRepository { /* history ops */ }
  interface StatisticsRepository { /* statistics */ }
  ```
  
  NOT a monolithic:
  ```kotlin
  interface AppRepository { /* everything */ } // ❌ Bad
  ```

- `SelectionAlgorithm` only requires `selectSegment()` method; no irrelevant methods

**Benefit**: Loose coupling; easier to mock specific behaviors in tests

### 3.5 Dependency Inversion Principle (DIP)
**Definition**: High-level modules shouldn't depend on low-level modules; both should depend on abstractions.

**Examples**:
- ViewModels depend on use case abstractions, not implementations:
  ```kotlin
  @HiltViewModel
  class WheelViewModel @Inject constructor(
      private val spinWheelUseCase: SpinWheelUseCase, // abstraction from Hilt
      private val getWheelByIdUseCase: GetWheelByIdUseCase
  ) { ... }
  ```

- Use cases depend on repository interfaces:
  ```kotlin
  class SpinWheelUseCase @Inject constructor(
      private val wheelRepository: WheelRepository, // interface, not WheelRepositoryImpl
      private val spinHistoryRepository: SpinHistoryRepository
  ) { ... }
  ```

- Hilt wires implementations at compile time:
  ```kotlin
  @Binds
  abstract fun bindWheelRepository(impl: WheelRepositoryImpl): WheelRepository
  ```

**Benefit**: Easy testing (mock repositories); flexible architecture

## 4. OOP Principles

### 4.1 Abstraction
**Purpose**: Hide complexity behind simple interfaces.

**Examples**:
- `SelectionAlgorithm` abstracts different selection strategies
- `WheelRepository` abstracts database access details
- `SpinPhysicsSimulator` abstracts animation calculations
- `HapticFeedback` abstracts vibration API complexity

### 4.2 Encapsulation
**Purpose**: Hide internal state; expose only necessary methods.

**Examples**:
- `Wheel` model validates segments and provides helper methods:
  ```kotlin
  data class Wheel(...) {
      init {
          require(segments.isNotEmpty()) // Validation
      }
      fun getActiveSegments(): List<Segment> // Derived data
      fun getTotalWeight(): Float // Computation
  }
  ```

- ViewModels expose only necessary state via StateFlow:
  ```kotlin
  private val _uiState = MutableStateFlow<WheelUiState>(...)
  val uiState: StateFlow<WheelUiState> = _uiState.asStateFlow() // Read-only
  ```

### 4.3 Inheritance
**Purpose**: Share behavior; extend functionality.

**Usage**:
- Sealed classes for type-safe result handling:
  ```kotlin
  sealed class Result<out T> {
      data class Success<T>(val data: T) : Result<T>()
      data class Error(val exception: Exception) : Result<Nothing>()
      object Loading : Result<Nothing>()
  }
  ```

- Sealed UI state classes for type safety:
  ```kotlin
  sealed class WheelUiState {
      object Loading : WheelUiState()
      data class Success(...) : WheelUiState()
      data class Error(...) : WheelUiState()
  }
  ```

### 4.4 Polymorphism
**Purpose**: Same interface, different behaviors.

**Examples**:
- Selection algorithms implementing common interface:
  ```kotlin
  interface SelectionAlgorithm {
      fun selectSegment(segments: List<Segment>): Segment?
  }
  ```

- Repository implementations for different data sources:
  ```kotlin
  interface WheelRepository { ... }
  class WheelRepositoryImpl(...) : WheelRepository { ... }
  class MockWheelRepository(...) : WheelRepository { ... } // for tests
  ```

## 5. Data Flow Diagram

```
User Interaction (UI)
         ↓
    Screen Composable
         ↓
    ViewModel.method()
         ↓
    Use Case invoke
         ↓
    Repository interface
         ↓
    Repository Implementation
         ↓
    Mapper (Entity ↔ Domain)
         ↓
    Room DAO
         ↓
    SQLite Database
```

**Return Flow** (Results):
```
Database Results
       ↓
    Entity
       ↓
    Mapper
       ↓
    Domain Model
       ↓
    Result<T>
       ↓
    Use Case returns
       ↓
    ViewModel updates StateFlow
       ↓
    Composable observes and re-renders
       ↓
    UI Update
```

## 6. State Management Pattern

### 6.1 ViewModel + StateFlow + Sealed State

**Pattern**:
```kotlin
@HiltViewModel
class ExampleViewModel @Inject constructor(
    private val useCase: ExampleUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val result = useCase.execute()
            _uiState.value = when (result) {
                is Result.Success -> UiState.Success(result.data)
                is Result.Error -> UiState.Error(result.exception.message)
                is Result.Loading -> UiState.Loading
            }
        }
    }
}
```

**Composable**:
```kotlin
@Composable
fun ExampleScreen(viewModel: ExampleViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    when (val state = uiState) {
        is UiState.Loading -> ShowLoadingSpinner()
        is UiState.Success -> ShowContent(state.data)
        is UiState.Error -> ShowError(state.message)
    }
}
```

**Benefits**:
- Type-safe state (compiler ensures all cases handled)
- Automatic lifecycle management
- Survives configuration changes
- Easy to test (mock use case, verify state)

## 7. Key Design Patterns

| Pattern | Usage | Benefit |
|---------|-------|---------|
| **Repository** | Abstract data access | Swap implementations; easy testing |
| **Factory** | `SelectionAlgorithmFactory` | Encapsulate object creation |
| **Strategy** | `SelectionAlgorithm` implementations | Runtime algorithm switching |
| **Singleton** | Database, use cases | Single instance across app |
| **Observer** | `StateFlow` for reactive updates | Automatic UI re-renders on state change |
| **Sealed Classes** | Type-safe results and states | Compiler-enforced exhaustive handling |
| **Mapper** | Entity ↔ Domain conversions | Maintain architectural boundaries |

## 8. Testing Architecture Support

The architecture enables:

### Unit Tests
```kotlin
// Test use case with mocked repository
@Test
fun `spinWheel returns success`() {
    val mockWheelRepo = mockk<WheelRepository>()
    val mockHistoryRepo = mockk<SpinHistoryRepository>()
    val useCase = SpinWheelUseCase(mockWheelRepo, mockHistoryRepo, factory)
    
    coEvery { mockWheelRepo.getWheelById(any()) } returns flowOf(Result.Success(wheel))
    
    val result = useCase(wheelId, UNIFORM)
    
    assertTrue(result is Result.Success)
}
```

### Integration Tests
```kotlin
// Test repository with in-memory database
@HiltAndroidTest
class WheelRepositoryTest {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var wheelRepository: WheelRepository
    
    @Test
    fun `create and retrieve wheel`() {
        wheelRepository.createWheel(testWheel)
        val result = wheelRepository.getWheelById(wheelId)
        assertTrue(result is Result.Success)
    }
}
```

### ViewModel Tests
```kotlin
// Test ViewModel with mocked use cases
@Test
fun `loadWheel updates UI state`() {
    val mockUseCase = mockk<GetWheelByIdUseCase>()
    val viewModel = HomeViewModel(mockUseCase, ...)
    
    coEvery { mockUseCase(wheelId) } returns flowOf(Result.Success(wheel))
    
    viewModel.loadWheel(wheelId)
    
    advanceUntilIdle()
    assertTrue(viewModel.uiState.value is HomeUiState.Success)
}
```

## 9. Conclusion

This architecture provides:
- ✅ Clear separation of concerns (domain, data, presentation)
- ✅ SOLID principles throughout
- ✅ Testable, maintainable code
- ✅ Type-safe navigation and state management
- ✅ Easy to extend with new features
- ✅ Industry-standard patterns and practices

The result is a scalable, professional Android application following best practices.

