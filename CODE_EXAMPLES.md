# Code Examples & Common Tasks

## 🎯 Common Development Tasks

This guide shows how to accomplish common tasks in the Wheel of Names Roulette app.

---

## 1. Creating a Wheel Programmatically

### Example: Create a Decision Wheel
```kotlin
// In HomeViewModel or any use case
val segments = listOf(
    Segment(
        id = UUID.randomUUID().toString(),
        name = "Yes",
        color = Color.Green,
        weight = 1f
    ),
    Segment(
        id = UUID.randomUUID().toString(),
        name = "No",
        color = Color.Red,
        weight = 1f
    ),
    Segment(
        id = UUID.randomUUID().toString(),
        name = "Maybe",
        color = Color.Yellow,
        weight = 1f
    )
)

val wheel = Wheel(
    id = UUID.randomUUID().toString(),
    name = "Decision Maker",
    segments = segments,
    createdAt = Clock.System.now(),
    updatedAt = Clock.System.now(),
    description = "A simple yes/no/maybe wheel"
)

// Use the create wheel use case
val result = createWheelUseCase(wheel)
when (result) {
    is Result.Success -> {
        // Wheel created successfully
        Toast.makeText(context, "Wheel created!", Toast.LENGTH_SHORT).show()
    }
    is Result.Error -> {
        // Handle error
        Log.e("WheelCreation", "Failed: ${result.exception.message}")
    }
    else -> {}
}
```

---

## 2. Spinning a Wheel with Different Algorithms

### Example: Try All Selection Algorithms
```kotlin
// In WheelViewModel
fun spinWithAllAlgorithms(wheel: Wheel) {
    SelectionAlgorithmFactory.AlgorithmType.entries.forEach { algorithmType ->
        viewModelScope.launch {
            // Spin with this algorithm
            val result = spinWheelUseCase(
                wheelId = wheel.id,
                algorithmType = algorithmType,
                spinDuration = 3000L
            )

            when (result) {
                is Result.Success -> {
                    val outcome = result.data
                    Log.i("Spin", "${algorithmType}: Selected ${outcome.selectedSegment.name}")
                }
                is Result.Error -> {
                    Log.e("Spin", "Failed: ${result.exception.message}")
                }
                else -> {}
            }
        }
    }
}
```

### Example: Spin with Weighted Algorithm
```kotlin
// Use case to demonstrate weighted selection
val weightedSegments = listOf(
    Segment(id = "1", name = "Rare", Color.Purple, weight = 0.1f), // 10%
    Segment(id = "2", name = "Uncommon", Color.Blue, weight = 0.3f), // 30%
    Segment(id = "3", name = "Common", Color.Green, weight = 0.6f)   // 60%
)

viewModel.setSelectionAlgorithm(SelectionAlgorithmFactory.AlgorithmType.WEIGHTED)
viewModel.spinWheel() // "Common" will appear ~60% of the time
```

---

## 3. Accessing Spin History

### Example: Get Last 10 Spins
```kotlin
// In HistoryViewModel
fun loadRecentSpins(wheelId: String) {
    viewModelScope.launch {
        _uiState.value = HistoryUiState.Loading
        
        getRecentSpinsUseCase(wheelId, limit = 10).collect { result ->
            _uiState.value = when (result) {
                is Result.Success -> {
                    // result.data is List<SpinResult>
                    result.data.forEach { spin ->
                        Log.i("Spin", 
                            "${spin.selectedSegmentName} at ${spin.spinTimestamp}")
                    }
                    HistoryUiState.Success(
                        spinResults = result.data,
                        wheelName = wheelName
                    )
                }
                is Result.Error -> HistoryUiState.Error(result.exception.message)
                is Result.Loading -> HistoryUiState.Loading
            }
        }
    }
}
```

### Example: Export History as CSV
```kotlin
// Utility function
fun exportHistoryToCSV(spins: List<SpinResult>): String {
    val header = "Segment,Time,Duration,Angle\n"
    val rows = spins.map { spin ->
        val dateTime = spin.spinTimestamp.toLocalDateTime(TimeZone.currentSystemDefault())
        "${spin.selectedSegmentName},$dateTime,${spin.spinDuration},${spin.finalAngle}"
    }.joinToString("\n")
    return header + rows
}
```

---

## 4. Viewing & Interpreting Statistics

### Example: Check if Wheel is Fair
```kotlin
// In StatisticsViewModel
fun analyzeWheelFairness(statistics: WheelStatistics) {
    val segmentCount = statistics.selectionCounts.size
    val expectedPercentage = 100f / segmentCount
    val fairnessThreshold = 10f // Allow ±10% deviation
    
    val unfairSegments = statistics.selectionPercentages
        .filter { (_, percentage) ->
            val deviation = (percentage - expectedPercentage).absoluteValue
            deviation > fairnessThreshold
        }
    
    if (unfairSegments.isEmpty()) {
        Log.i("Fairness", "✅ Wheel is fair!")
    } else {
        Log.w("Fairness", "⚠️ Unfair segments:")
        unfairSegments.forEach { (name, percentage) ->
            Log.w("Fairness", "  $name: ${String.format("%.1f", percentage)}%")
        }
    }
}
```

### Example: Find Most Likely Outcome
```kotlin
fun getProbabilityDistribution(statistics: WheelStatistics): Map<String, String> {
    return statistics.selectionPercentages.mapValues { (_, percentage) ->
        when {
            percentage > 50 -> "Very Likely (${String.format("%.1f", percentage)}%)"
            percentage > 25 -> "Likely (${String.format("%.1f", percentage)}%)"
            percentage > 10 -> "Moderate (${String.format("%.1f", percentage)}%)"
            else -> "Rare (${String.format("%.1f", percentage)}%)"
        }
    }
}
```

---

## 5. Custom UI: Adding New Screen

### Example: Add a Simple Result Dialog
```kotlin
// In presentation/component/
@Composable
fun SpinResultDialog(
    spinResult: SpinResult,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("🎉 Selected!") },
        text = {
            Column {
                Text(
                    text = spinResult.selectedSegmentName,
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.Green
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Spin duration: ${spinResult.spinDuration}ms")
                Text("Angle: ${String.format("%.1f", spinResult.finalAngle)}°")
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}

// Usage in WheelScreen:
if (state.lastSpinResult != null) {
    SpinResultDialog(
        spinResult = state.lastSpinResult,
        onDismiss = { /* clear result */ }
    )
}
```

---

## 6. Adding a New Selection Algorithm

### Example: Create a Fibonacci-Weighted Algorithm
```kotlin
// In domain/usecase/selection/SelectionAlgorithm.kt

/**
 * Fibonacci-weighted selection: weights increase by Fibonacci sequence
 * Last segment has highest probability
 */
class FibonacciWeightedAlgorithm : SelectionAlgorithm {
    override fun selectSegment(segments: List<Segment>): Segment? {
        if (segments.isEmpty()) return null
        
        // Generate Fibonacci weights
        val weights = generateFibonacciSequence(segments.size)
        val total = weights.sum()
        
        var random = Math.random() * total
        segments.forEachIndexed { index, segment ->
            random -= weights[index]
            if (random <= 0) return segment
        }
        return segments.last()
    }

    private fun generateFibonacciSequence(size: Int): List<Float> {
        if (size <= 0) return emptyList()
        if (size == 1) return listOf(1f)
        
        val sequence = mutableListOf(1f, 1f)
        repeat(size - 2) {
            sequence.add(sequence[sequence.size - 1] + sequence[sequence.size - 2])
        }
        return sequence.take(size)
    }

    override fun getName(): String = "Fibonacci Weighted"
}

// Register in SelectionAlgorithmFactory:
enum class AlgorithmType {
    UNIFORM, WEIGHTED, SEEDED, ROUND_ROBIN, FIBONACCI // Add here
}

fun createAlgorithm(type: AlgorithmType): SelectionAlgorithm {
    return when (type) {
        // ... existing cases ...
        AlgorithmType.FIBONACCI -> FibonacciWeightedAlgorithm()
    }
}
```

---

## 7. Database Operations

### Example: Bulk Import Wheels
```kotlin
// In repository
suspend fun importWheels(wheelsJson: String): Result<Unit> = try {
    val wheelsList = Json.decodeFromString<List<Wheel>>(wheelsJson)
    wheelsList.forEach { wheel ->
        wheelDao.insertWheel(wheelMapper.wheelToEntity(wheel))
    }
    Result.Success(Unit)
} catch (e: Exception) {
    Result.Error(e)
}

// Usage:
val jsonData = """[
    {"id":"1","name":"Wheel 1","segments":[...]},
    {"id":"2","name":"Wheel 2","segments":[...]}
]"""
val result = wheelRepository.importWheels(jsonData)
```

### Example: Query Wheels by Segment Count
```kotlin
// In WheelDao:
@Query("""
    SELECT * FROM wheels 
    WHERE json_array_length(segments) > :minSegments
    ORDER BY updatedAt DESC
""")
fun getWheelsByMinSegments(minSegments: Int): Flow<List<WheelEntity>>

// Usage:
dao.getWheelsByMinSegments(5).collect { wheels ->
    Log.i("Wheels", "Found ${wheels.size} wheels with 5+ segments")
}
```

---

## 8. Error Handling Patterns

### Example: Graceful Degradation
```kotlin
// In ViewModel
fun loadWithFallback(wheelId: String) {
    viewModelScope.launch {
        val result = getWheelByIdUseCase(wheelId).firstOrNull()
        
        _uiState.value = when {
            result is Result.Success -> {
                WheelUiState.Success(wheel = result.data)
            }
            result is Result.Error -> {
                // Try fallback: load from cache
                val cachedWheel = getCachedWheel(wheelId)
                if (cachedWheel != null) {
                    WheelUiState.Success(wheel = cachedWheel, isStale = true)
                } else {
                    WheelUiState.Error("Failed to load wheel")
                }
            }
            else -> WheelUiState.Loading
        }
    }
}
```

### Example: Result Chaining
```kotlin
// Use Result.map() for chaining transformations
val processSpinResult: (SpinResult) -> Result<String> = { spinResult ->
    spinResult
        .also { Log.i("Spin", "Result: ${it.selectedSegmentName}") }
        .selectedSegmentName
        .let { Result.Success(it) }
}

// Or with extension function:
spinResult.onSuccess { result ->
    Log.i("Spin", "Success: ${result.selectedSegmentName}")
}.onError { exception ->
    Log.e("Spin", "Error: ${exception.message}")
}
```

---

## 9. Compose State Management

### Example: Multi-Step Flow
```kotlin
// Complex ViewModel managing multiple states
@HiltViewModel
class WheelFlowViewModel @Inject constructor(
    private val wheelUseCase: GetWheelByIdUseCase,
    private val spinUseCase: SpinWheelUseCase,
    private val statsUseCase: GetWheelStatisticsUseCase
) : ViewModel() {
    
    private val _flowState = MutableStateFlow<FlowState>(FlowState.Loading)
    val flowState: StateFlow<FlowState> = _flowState.asStateFlow()

    sealed class FlowState {
        object Loading : FlowState()
        data class WheelLoaded(val wheel: Wheel) : FlowState()
        data class Spinning(val progress: Float) : FlowState()
        data class SpinComplete(
            val wheel: Wheel,
            val result: SpinResult,
            val statistics: WheelStatistics
        ) : FlowState()
        data class Error(val message: String) : FlowState()
    }

    fun startFlow(wheelId: String) {
        viewModelScope.launch {
            _flowState.value = FlowState.Loading
            
            // Load wheel
            wheelUseCase(wheelId).collect { result ->
                if (result is Result.Success) {
                    _flowState.value = FlowState.WheelLoaded(result.data)
                }
            }
        }
    }

    fun executeSpinFlow(wheel: Wheel) {
        viewModelScope.launch {
            // Spin
            val spinResult = spinUseCase(wheel.id, UNIFORM)
            if (spinResult is Result.Success) {
                _flowState.value = FlowState.Spinning(0.5f)
                
                // Get stats
                statsUseCase(wheel.id).collect { statsResult ->
                    if (statsResult is Result.Success) {
                        _flowState.value = FlowState.SpinComplete(
                            wheel = wheel,
                            result = spinResult.data.spinResult,
                            statistics = statsResult.data
                        )
                    }
                }
            }
        }
    }
}
```

---

## 10. Testing Examples

### Example: Unit Test a Use Case
```kotlin
@Test
fun `spinWheel selects from active segments only`() {
    // Arrange
    val segments = listOf(
        Segment("1", "A", Color.Red, 1f, isActive = true),
        Segment("2", "B", Color.Blue, 1f, isActive = false),
        Segment("3", "C", Color.Green, 1f, isActive = true)
    )
    val wheel = Wheel("wheel", "Test", segments, Clock.System.now(), Clock.System.now())
    
    val mockWheelRepo = mockk<WheelRepository>()
    val mockHistoryRepo = mockk<SpinHistoryRepository>()
    val factory = SelectionAlgorithmFactory
    
    coEvery { mockWheelRepo.getWheelById(any()) } returns 
        flowOf(Result.Success(wheel))
    coEvery { mockHistoryRepo.recordSpin(any()) } returns 
        Result.Success("spin-id")
    
    val useCase = SpinWheelUseCase(mockWheelRepo, mockHistoryRepo, factory)
    
    // Act
    val result = useCase("wheel", UNIFORM)
    
    // Assert
    assertTrue(result is Result.Success)
    val outcome = (result as Result.Success).data
    assertTrue(outcome.selectedSegment in segments.filter { it.isActive })
}
```

### Example: Integration Test a Repository
```kotlin
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class WheelRepositoryIntegrationTest {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var wheelRepository: WheelRepository
    
    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun `createAndRetrieveWheel succeeds`() = runBlocking {
        // Arrange
        val testWheel = Wheel(
            id = "test-wheel",
            name = "Test",
            segments = listOf(Segment("1", "A", Color.Red)),
            createdAt = Clock.System.now(),
            updatedAt = Clock.System.now()
        )
        
        // Act
        val createResult = wheelRepository.createWheel(testWheel)
        
        // Assert
        assertTrue(createResult is Result.Success)
        
        // Retrieve and verify
        val getResult = wheelRepository.getWheelById("test-wheel")
            .firstOrNull()
        assertTrue(getResult is Result.Success)
        assertEquals((getResult as Result.Success).data.name, "Test")
    }
}
```

---

## 11. Performance Optimization Tips

### Tip: Optimize Statistics Computation
```kotlin
// BAD: Recomputes on every collection
getSpinHistoryUseCase(wheelId).collect { result ->
    if (result is Result.Success) {
        val stats = computeStatsInline(result.data)
    }
}

// GOOD: Separate computation, reuse cache
private val statsCache = mutableMapOf<String, WheelStatistics>()

fun getOrComputeStats(wheelId: String, spins: List<SpinResult>): WheelStatistics {
    val cacheKey = "$wheelId-${spins.lastOrNull()?.id ?: "empty"}"
    return statsCache.getOrPut(cacheKey) {
        computeStats(spins)
    }
}
```

### Tip: Use distinctUntilChanged for State
```kotlin
// Avoid unnecessary recompositions
val filteredState: StateFlow<WheelUiState> = _uiState
    .distinctUntilChanged()
    .asStateFlow()
```

---

## 📚 More Examples

For more detailed examples, refer to:
- **[FEATURES.md](FEATURES.md)** - Feature examples with code
- **[ARCHITECTURE.md](ARCHITECTURE.md)** - Design pattern examples
- Source files with inline KDoc comments

---

**Happy coding! 🚀**

