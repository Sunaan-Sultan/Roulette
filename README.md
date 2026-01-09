# Wheel of Names - Roulette Android App

## 🎯 Project Overview

A feature-rich **Wheel of Names roulette application** built with Jetpack Compose, following **SOLID principles**, **Clean Architecture**, and **OOP best practices**. This app allows users to create custom wheels with segments, spin them randomly, track spin history, and view statistics.

## ✨ Features

### Core Features
- ✅ **Interactive Spinning Wheel**: Animated wheel with realistic physics and deceleration
- ✅ **Create/Edit/Delete Wheels**: Full CRUD operations for wheel management
- ✅ **Customize Segments**: Add, remove, edit names and colors for wheel segments
- ✅ **Spin History**: Track all previous spins with timestamps and results
- ✅ **Statistics Dashboard**: View selection distribution and frequency analysis
- ✅ **Search Wheels**: Find wheels quickly by name
- ✅ **Responsive UI**: Material Design 3 with Jetpack Compose

### Advanced Features
- ✅ **Multiple Selection Algorithms**:
  - Uniform Random (equal probability)
  - Weighted Random (segment weight-based)
  - Seeded Random (deterministic/reproducible)
  - Round-Robin (sequential selection)
  
- ✅ **Haptic & Audio Feedback**:
  - Vibration patterns during spin
  - Success notification pattern
  - Light tap feedback for UI interactions
  - (Audio implementation ready for sound manager integration)

- ✅ **Physics-based Animation**:
  - Easing functions (cubic, expo, cosine)
  - Configurable spin duration (1-10 seconds)
  - Realistic deceleration simulation

- ✅ **Data Persistence**:
  - Room database for wheels and spin history
  - Automatic timestamp tracking
  - Data recovery and persistence

## 🏗️ Architecture

### Clean Architecture Layers

```
Domain Layer (Business Logic)
├── Models: Wheel, Segment, SpinResult, WheelStatistics
├── Repositories (Interfaces): WheelRepository, SpinHistoryRepository, StatisticsRepository
├── Use Cases: SpinWheelUseCase, GetWheelByIdUseCase, GetWheelStatisticsUseCase, etc.
└── Selection Algorithms: UniformRandom, WeightedRandom, Seeded, RoundRobin

Data Layer (Persistence)
├── Local Database: Room entities, DAOs, RouletteDatabase
├── Mappers: WheelMapper, SpinHistoryMapper
└── Repository Implementations: WheelRepositoryImpl, SpinHistoryRepositoryImpl, StatisticsRepositoryImpl

Presentation Layer (UI & State Management)
├── ViewModels: HomeViewModel, WheelViewModel, HistoryViewModel, StatisticsViewModel, EditorViewModel
├── UI State: HomeUiState, WheelUiState, HistoryUiState, StatisticsUiState, EditorUiState
├── Screens: HomeScreen, WheelScreen, HistoryScreen, StatisticsScreen, EditorScreen
├── Components: WheelCanvas (custom drawing)
└── Navigation: RouletteNavHost with type-safe routing

Utilities
├── Physics: SpinPhysicsSimulator, EasingFunctions
├── Audio: SoundManager, HapticFeedback
└── DI: DatabaseModule, RepositoryModule, UseCaseModule
```

### SOLID Principles Implementation

| Principle | Implementation |
|-----------|---|
| **Single Responsibility** | Each class has one reason to change (e.g., `SpinPhysicsSimulator` handles physics only) |
| **Open/Closed** | Use cases, repositories, selection algorithms are open for extension, closed for modification |
| **Liskov Substitution** | All `SelectionAlgorithm` implementations are interchangeable |
| **Interface Segregation** | Separate repository interfaces for wheels, spin history, and statistics |
| **Dependency Inversion** | ViewModels depend on use case abstractions; Hilt provides concrete implementations |

### OOP Principles

- **Abstraction**: `SelectionAlgorithm` interface abstracts different selection strategies
- **Encapsulation**: Private fields in models with public accessors; business logic hidden in use cases
- **Inheritance**: Base classes and sealed classes for type-safe state management
- **Polymorphism**: Strategy pattern for selection algorithms; sealed classes for result types

## 📁 Project Structure

```
com/project/roulette/
├── domain/
│   ├── model/
│   │   ├── Segment.kt
│   │   ├── Wheel.kt
│   │   ├── SpinResult.kt
│   │   ├── WheelStatistics.kt
│   │   └── Result.kt (sealed class for error handling)
│   ├── repository/
│   │   ├── WheelRepository.kt
│   │   ├── SpinHistoryRepository.kt
│   │   └── StatisticsRepository.kt
│   └── usecase/
│       ├── selection/
│       │   └── SelectionAlgorithm.kt (+ implementations & factory)
│       ├── wheel/
│       │   └── WheelUseCases.kt
│       ├── spin/
│       │   └── SpinUseCases.kt
│       └── statistics/
│           └── StatisticsUseCases.kt
├── data/
│   ├── local/
│   │   ├── database/
│   │   │   ├── entity/
│   │   │   │   └── Entities.kt
│   │   │   ├── RouletteDao.kt
│   │   │   └── RouletteDatabase.kt
│   ├── mapper/
│   │   ├── WheelMapper.kt
│   │   └── SpinHistoryMapper.kt
│   └── repository/
│       ├── WheelRepositoryImpl.kt
│       ├── SpinHistoryRepositoryImpl.kt
│       └── StatisticsRepositoryImpl.kt
├── presentation/
│   ├── model/
│   │   └── UiState.kt (sealed classes for type-safe state)
│   ├── viewmodel/
│   │   ├── HomeViewModel.kt
│   │   ├── WheelViewModel.kt
│   │   ├── HistoryViewModel.kt
│   │   ├── StatisticsViewModel.kt
│   │   └── EditorViewModel.kt
│   ├── screen/
│   │   ├── home/
│   │   │   └── HomeScreen.kt
│   │   ├── wheel/
│   │   │   └── WheelScreen.kt
│   │   ├── history/
│   │   │   └── HistoryScreen.kt
│   │   ├── statistics/
│   │   │   └── StatisticsScreen.kt
│   │   └── editor/
│   │       └── EditorScreen.kt
│   ├── component/
│   │   └── WheelCanvas.kt (custom Canvas drawable)
│   ├── navigation/
│   │   ├── RouletteNavigation.kt (sealed navigation routes)
│   │   └── RouletteNavHost.kt
│   └── theme/
│       ├── Color.kt
│       ├── Theme.kt
│       └── Type.kt
├── util/
│   ├── physics/
│   │   └── PhysicsSimulator.kt
│   └── audio/
│       └── AudioAndHapticManager.kt
├── di/
│   ├── DatabaseModule.kt
│   ├── RepositoryModule.kt
│   └── UseCaseModule.kt
├── RouletteApplication.kt (@HiltAndroidApp)
└── MainActivity.kt
```

## 🛠️ Technology Stack

### Core
- **Kotlin** 2.0.21 - Modern language
- **Jetpack Compose** - Declarative UI framework
- **Material Design 3** - UI components & theming

### Architecture & Dependency Injection
- **Hilt** - Dependency injection framework
- **Kotlin Coroutines** - Asynchronous programming
- **Flow/StateFlow** - Reactive streams for state management

### Data Persistence
- **Room Database** - Type-safe SQLite wrapper
- **Kotlin Serialization** - JSON serialization for complex types
- **SharedPreferences** (optional) - Simple key-value storage

### Navigation
- **Jetpack Navigation Compose** - Type-safe navigation with serialization

### Utilities
- **Kotlinx DateTime** - Date/time handling
- **Android VibrationEffect** - Haptic feedback API

## 📦 Dependencies Added

Key additions to `libs.versions.toml`:
```toml
[versions]
navigationCompose = "2.8.5"
roomVersion = "2.6.1"
hiltVersion = "2.51.1"
hiltNavigationCompose = "1.2.0"
kotlinxSerializationJson = "1.7.3"
kotlinxDatetimeVersion = "0.6.1"

[plugins]
kotlin-serialization = { id = "org.jetbrains.kotlin.plugin.serialization" }
hilt-android = { id = "com.google.dagger.hilt.android" }
```

## 🚀 Getting Started

### Prerequisites
- Android Studio Flamingo or later
- Android SDK 24+ (API level 24)
- Java 11+

### Building the Project

1. **Clone/Open the project** in Android Studio
2. **Let Gradle sync** - Dependencies will be downloaded automatically
3. **Build the project**:
   ```bash
   ./gradlew build
   ```
4. **Run on emulator/device**:
   ```bash
   ./gradlew installDebug
   ```

### Key Configuration Files
- `gradle/libs.versions.toml` - Centralized dependency management
- `app/build.gradle` - App-level build configuration with Hilt and Kotlin Serialization plugins
- `AndroidManifest.xml` - Application manifest with RouletteApplication and vibration permission

## 📱 Usage Examples

### Creating a Wheel
```kotlin
val segments = listOf(
    Segment(UUID.randomUUID().toString(), "Red", Color.Red, 1f),
    Segment(UUID.randomUUID().toString(), "Blue", Color.Blue, 1f),
    Segment(UUID.randomUUID().toString(), "Green", Color.Green, 1f)
)
val wheel = Wheel(
    id = UUID.randomUUID().toString(),
    name = "My Wheel",
    segments = segments,
    createdAt = Clock.System.now(),
    updatedAt = Clock.System.now()
)
viewModel.createWheel(wheel)
```

### Spinning with Different Algorithms
```kotlin
// Uniform random
viewModel.setSelectionAlgorithm(SelectionAlgorithmFactory.AlgorithmType.UNIFORM)
viewModel.spinWheel()

// Weighted random
viewModel.setSelectionAlgorithm(SelectionAlgorithmFactory.AlgorithmType.WEIGHTED)
viewModel.spinWheel()
```

### Accessing Statistics
```kotlin
statisticsViewModel.loadStatistics(wheelId)
// Observe uiState for WheelStatistics with selection distribution
```

## 🧪 Testing Considerations

The architecture supports testing:
- **Use case tests**: Mock repositories, verify orchestration
- **Repository tests**: In-memory Room database for quick testing
- **ViewModel tests**: Mock use cases, verify state emission
- **UI tests**: Compose testing APIs with Hilt injection

Example structure:
```kotlin
@HiltAndroidTest
class WheelViewModelTest {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var getWheelByIdUseCase: GetWheelByIdUseCase

    // Test methods...
}
```

## 🔄 Future Enhancements

1. **Cloud Sync**: Remote backup of wheels and statistics
2. **Social Features**: Share wheels, compare statistics
3. **Custom Themes**: User-selectable color schemes
4. **Sound Effects**: Integrate audio playback for spin animations
5. **Advanced Physics**: More realistic spin deceleration with friction simulation
6. **Wheel Templates**: Pre-built wheel templates for common use cases
7. **Export/Import**: JSON export for wheel configurations
8. **Multiple Selections**: Spin and select multiple segments at once
9. **Accessibility**: TalkBack support, high contrast modes
10. **Performance Optimizations**: Canvas caching, lazy loading for large wheels

## 📚 Design Patterns Used

- **Repository Pattern**: Abstract data access, enable swapping implementations
- **Strategy Pattern**: `SelectionAlgorithm` for pluggable selection strategies
- **Factory Pattern**: `SelectionAlgorithmFactory` for algorithm creation
- **Singleton Pattern**: Database instance, use case providers
- **Observer Pattern**: StateFlow for reactive UI updates
- **Builder Pattern**: Composable function composition for UI
- **Sealed Classes**: Type-safe result and state representation

## 📄 License

This project is provided as-is for educational purposes.

## 📞 Support

For questions or issues regarding the architecture and implementation, refer to the inline code comments and SOLID principle explanations throughout the codebase.

---

**Built with ❤️ following Android best practices and architectural patterns.**

