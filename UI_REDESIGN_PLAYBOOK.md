# UI Redesign Playbook — Compose (Android)

Drop this file into any Jetpack Compose project and tell the agent:

> Redesign this app's UI following UI_REDESIGN_PLAYBOOK.md. Keep all existing behaviour and navigation; only change the presentation layer.

This is the exact system used for the "Wheel of Names" redesign. It is a grouped-inset, iOS-Settings-flavoured design language built on Material3 primitives, with a custom token layer on top so nothing in screen code ever hardcodes a colour, radius, or spacing value.

---

## 0. Non-negotiable rules

1. **No raw values in screen code.** Every colour, spacing, radius, and size comes from `AppTheme.colors` / `AppTheme.dimens` / `AppTheme.shapes`. If a screen needs a number, add a token.
2. **No `MaterialTheme.colorScheme.*` in screens.** Only the token layer touches it. Screens read `AppTheme.colors`.
3. **No `androidx.compose.material.icons.*`.** Drop the `material-icons-extended` dependency entirely and ship local vector drawables (see §5). It is a multi-MB dependency that R8 cannot shrink well.
4. **Light and dark are first-class.** Every token exists in both. Nothing is "dark theme only". Test every screen in both.
5. **Screens are dumb.** Build the reusable component set first, then rewrite screens as compositions of those components. If a screen invents its own card/row/button, it is a bug.
6. **Behaviour is frozen.** Redesign touches `presentation/` and `ui/theme/` only. ViewModels/domain/data change only when a new screen genuinely needs new data.

---

## 1. Order of work

Do it in this order. Each step compiles on its own.

| Step | Deliverable |
|---|---|
| 1 | `ui/theme/Color.kt` — raw palette constants + accent palettes |
| 2 | `ui/theme/Dimens.kt` — spacing/size tokens |
| 3 | `ui/theme/Shape.kt` — corner radius tokens |
| 4 | `ui/theme/Type.kt` — type scale with the app font |
| 5 | `ui/theme/ColorUtils.kt` — contrast helpers |
| 6 | `ui/theme/Theme.kt` — `AppColors`, CompositionLocals, `AppTheme { }` |
| 7 | `presentation/component/AppIcons.kt` + vector drawables |
| 8 | `presentation/component/design/*` — the component library |
| 9 | Rewrite screens one by one, simplest first (Settings → lists → dashboard) |
| 10 | Theme-mode + accent-palette picker wired to prefs |
| 11 | R8/ProGuard + shrinkResources on release |

Rewriting a screen before the component library exists is the single most common way this goes wrong.

---

## 2. The token layer

### 2.1 Colors (`ui/theme/Color.kt`)

Two neutral ramps (light + dark) and a set of selectable accent palettes. Neutrals are the design; the accent is user-chosen and must never be assumed.

```kotlin
val GroupedBgLight = Color(0xFFF2F3F5)
val SurfaceLight = Color(0xFFFFFFFF)
val SurfaceElevatedLight = Color(0xFFFFFFFF)
val SurfacePressedLight = Color(0xFFE9EAEE)
val DividerLight = Color(0xFFE8E8E8)
val OutlineLight = Color(0xFFD8DADF)
val TextPrimaryLight = Color(0xFF1A1A1A)
val TextSecondaryLight = Color(0xFF8A8F99)
val TextTertiaryLight = Color(0xFFBFC3CB)
val ScrimLight = Color(0x66000000)

val GroupedBgDark = Color(0xFF000000)
val SurfaceDarkNew = Color(0xFF1C1C1E)
val SurfaceElevatedDark = Color(0xFF2C2C2E)
val SurfacePressedDark = Color(0xFF2C2C2E)
val DividerDark = Color(0xFF2C2C2E)
val OutlineDark = Color(0xFF3A3A3C)
val TextPrimaryDark = Color(0xFFFFFFFF)
val TextSecondaryDark = Color(0xFF8E8E93)
val TextTertiaryDark = Color(0xFF48484A)
val ScrimDark = Color(0x99000000)

val DangerLight = Color(0xFFD32F2F);  val DangerDark = Color(0xFFFF6B6B)
val SuccessLight = Color(0xFF1B7F46); val SuccessDark = Color(0xFF3DDC84)
val WarningLight = Color(0xFFA15C00); val WarningDark = Color(0xFFFFB300)
val InfoLight = Color(0xFF0B6BC0);    val InfoDark = Color(0xFF4DA3FF)
```

**Key idea — per-mode accent variants.** A saturated brand colour that reads fine on black is unreadable on white. So every accent ships three values: the swatch shown in the picker, the variant used in light mode, and the variant used in dark mode.

```kotlin
data class AccentPalette(
    val name: String,
    val primary: Color,
    val onLight: Color,
    val onDark: Color
)

val Palettes = listOf(
    AccentPalette("Purple",  Color(0xFF6C5CE7), Color(0xFF6C5CE7), Color(0xFF9B8CFF)),
    AccentPalette("Teal",    Color(0xFF00B894), Color(0xFF00775E), Color(0xFF22D3A5)),
    AccentPalette("Rose",    Color(0xFFE84393), Color(0xFFC2185B), Color(0xFFFF6FB0)),
    AccentPalette("Ocean",   Color(0xFF0984E3), Color(0xFF0B6BC0), Color(0xFF4DA3FF)),
    AccentPalette("Amber",   Color(0xFFFFA000), Color(0xFFA15C00), Color(0xFFFFB300)),
    AccentPalette("Coral",   Color(0xFFFF7675), Color(0xFFC74240), Color(0xFFFF8E8D)),
    AccentPalette("Mint",    Color(0xFF27AE60), Color(0xFF1B7F46), Color(0xFF3DDC84)),
    AccentPalette("Crimson", Color(0xFFD63031), Color(0xFFD63031), Color(0xFFFF5A5A))
)
```

Also define a small fixed set of *categorical tile colours* for charts/avatars/legends, independent of the accent, so multi-series visuals stay readable whichever accent is active.

**If the app persists a palette index**, freeze the list order with a unit test — reordering silently changes every existing user's theme:

```kotlin
class PaletteOrderTest {
    private val frozenNames = listOf("Purple", "Teal", "Rose", "Ocean", "Amber", "Coral", "Mint", "Crimson")

    @Test fun paletteNamesAndOrderAreFrozen() = assertEquals(frozenNames, Palettes.map { it.name })
}
```

### 2.2 Dimens (`ui/theme/Dimens.kt`)

```kotlin
@Immutable
data class Dimens(
    val screenPadding: Dp = 16.dp,
    val groupGutter: Dp = 16.dp,
    val headerToCard: Dp = 8.dp,
    val listTopPadding: Dp = 8.dp,
    val listBottomPadding: Dp = 40.dp,

    val rowHeight: Dp = 56.dp,
    val rowHeightTwoLine: Dp = 68.dp,
    val rowHorizontalPadding: Dp = 16.dp,
    val rowVerticalPadding: Dp = 12.dp,
    val rowIconGap: Dp = 14.dp,

    val iconSize: Dp = 24.dp,
    val iconSizeSmall: Dp = 20.dp,
    val chevronSize: Dp = 20.dp,
    val iconTileSize: Dp = 30.dp,

    val dividerThickness: Dp = Dp.Hairline,
    val dividerInset: Dp = 16.dp,
    val dividerInsetWithIcon: Dp = 54.dp,
    val borderWidth: Dp = 1.dp,

    val buttonHeight: Dp = 50.dp,
    val buttonHeightSmall: Dp = 38.dp,
    val textFieldHeight: Dp = 52.dp,
    val fabSize: Dp = 56.dp,
    val bottomBarHeight: Dp = 64.dp,
    val bottomBarSpace: Dp = 96.dp,
    val topBarHeight: Dp = 56.dp,

    val space2: Dp = 2.dp,   val space4: Dp = 4.dp,
    val space8: Dp = 8.dp,   val space12: Dp = 12.dp,
    val space16: Dp = 16.dp, val space20: Dp = 20.dp,
    val space24: Dp = 24.dp, val space32: Dp = 32.dp
)

val LocalDimens = staticCompositionLocalOf { Dimens() }
```

`bottomBarSpace` matters: with a floating bottom bar, every scrollable screen must add it to `contentPadding.bottom` or the last row hides behind the bar.

### 2.3 Shapes (`ui/theme/Shape.kt`)

```kotlin
val M3Shapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

@Immutable
data class AppShapes(
    val card: Shape = RoundedCornerShape(16.dp),
    val cardSmall: Shape = RoundedCornerShape(12.dp),
    val groupTop: Shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
    val groupBottom: Shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
    val groupMiddle: Shape = RoundedCornerShape(0.dp),
    val button: Shape = RoundedCornerShape(14.dp),
    val buttonLarge: Shape = RoundedCornerShape(16.dp),
    val textField: Shape = RoundedCornerShape(12.dp),
    val dialog: Shape = RoundedCornerShape(20.dp),
    val sheet: Shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
    val chip: Shape = CircleShape,
    val iconTile: Shape = RoundedCornerShape(8.dp),
    val barTop: Shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp),
    val avatar: Shape = CircleShape,
    val thumbnail: Shape = RoundedCornerShape(12.dp)
)

val LocalAppShapes = staticCompositionLocalOf { AppShapes() }
```

### 2.4 Type (`ui/theme/Type.kt`)

One font family, three weights (Regular / Medium / Bold). Ship `.ttf` in `res/font/`, not `.otf` — and never ship a variable/full font dump; the redesign replaced a 6 MB `sf_pro.ttf` with five ~145 KB static faces and used only three of them.

```kotlin
val AppFontFamily = FontFamily(
    Font(R.font.app_sans_regular, FontWeight.Normal),
    Font(R.font.app_sans_medium,  FontWeight.Medium),
    Font(R.font.app_sans_bold,    FontWeight.Bold)
)

private fun appStyle(weight: FontWeight, size: Int, lineHeight: Int, letterSpacing: Double = 0.0) =
    TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = weight,
        fontSize = size.sp,
        lineHeight = lineHeight.sp,
        letterSpacing = letterSpacing.sp
    )

val Typography = Typography(
    displayLarge  = appStyle(FontWeight.Bold, 40, 48, -0.5),
    displayMedium = appStyle(FontWeight.Bold, 34, 42, -0.4),
    displaySmall  = appStyle(FontWeight.Bold, 28, 36, -0.3),
    headlineLarge = appStyle(FontWeight.Bold, 28, 36, -0.3),
    headlineMedium= appStyle(FontWeight.Bold, 24, 32, -0.2),
    headlineSmall = appStyle(FontWeight.Bold, 22, 28, -0.2),
    titleLarge    = appStyle(FontWeight.Bold, 20, 26, -0.1),
    titleMedium   = appStyle(FontWeight.Medium, 17, 22),
    titleSmall    = appStyle(FontWeight.Medium, 15, 20),
    bodyLarge     = appStyle(FontWeight.Normal, 17, 22),
    bodyMedium    = appStyle(FontWeight.Normal, 16, 21),
    bodySmall     = appStyle(FontWeight.Normal, 14, 19),
    labelLarge    = appStyle(FontWeight.Medium, 15, 20),
    labelMedium   = appStyle(FontWeight.Medium, 12, 16, 0.6),
    labelSmall    = appStyle(FontWeight.Medium, 11, 14, 0.4)
)
```

Role mapping used throughout: `bodyLarge` = row title, `bodySmall` = row subtitle/footnote, `labelMedium` = uppercase section header, `labelLarge` = button/tab/chip text, `headlineSmall` = screen title & stat value.

### 2.5 Contrast helpers (`ui/theme/ColorUtils.kt`)

Because the accent is user-chosen, text-on-accent must be computed, never assumed.

```kotlin
fun contrastRatio(a: Color, b: Color): Float {
    val la = a.luminance() + 0.05f
    val lb = b.luminance() + 0.05f
    return if (la > lb) la / lb else lb / la
}

private val DarkContent = Color(0xFF14141A)

fun contentColorOn(background: Color): Color =
    if (contrastRatio(DarkContent, background) >= contrastRatio(Color.White, background)) DarkContent
    else Color.White

fun Color.ensureContrast(on: Color, ratio: Float = 4.5f): Color {
    if (contrastRatio(this, on) >= ratio) return this
    val hsv = FloatArray(3)
    android.graphics.Color.colorToHSV(this.toArgb(), hsv)
    val goingDarker = on.luminance() > 0.5f
    var v = hsv[2]
    repeat(25) {
        v = (v + if (goingDarker) -0.04f else 0.04f).coerceIn(0f, 1f)
        val candidate = Color(android.graphics.Color.HSVToColor(floatArrayOf(hsv[0], hsv[1], v)))
        if (contrastRatio(candidate, on) >= ratio) return candidate
    }
    return if (goingDarker) Color.Black else Color.White
}
```

`ensureContrast` is what lets arbitrary user-chosen or data-derived colours (wheel segments, chart series, avatar tints) be used as text/icon colour safely. In dark mode, instead of darkening, desaturate and lift value — that reads better on black than a darkened hue.

### 2.6 Theme (`ui/theme/Theme.kt`)

The semantic colour object screens actually consume:

```kotlin
@Immutable
data class AppColors(
    val background: Color,
    val surface: Color,
    val surfaceElevated: Color,
    val surfacePressed: Color,
    val divider: Color,
    val outline: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val primary: Color,
    val onPrimary: Color,
    val primarySubtle: Color,
    val primaryBorder: Color,
    val danger: Color,
    val dangerSubtle: Color,
    val success: Color,
    val successSubtle: Color,
    val warning: Color,
    val warningSubtle: Color,
    val info: Color,
    val scrim: Color,
    val isLight: Boolean
)
```

Builders derive the accent-dependent slots from the chosen palette:

```kotlin
fun lightAppColors(p: AccentPalette) = AppColors(
    background = GroupedBgLight,
    surface = SurfaceLight,
    ...
    primary = p.onLight,
    onPrimary = contentColorOn(p.onLight),
    primarySubtle = p.onLight.copy(alpha = 0.10f),
    primaryBorder = p.onLight.copy(alpha = 0.28f),
    isLight = true
)

fun darkAppColors(p: AccentPalette) = AppColors(
    background = GroupedBgDark,
    surface = SurfaceDarkNew,
    ...
    primary = p.onDark,
    onPrimary = contentColorOn(p.onDark),
    primarySubtle = p.onDark.copy(alpha = 0.16f),
    primaryBorder = p.onDark.copy(alpha = 0.32f),
    isLight = false
)
```

Subtle-tint alpha is **0.10 light / 0.16 dark**; border alpha **0.28 light / 0.32 dark**. Dark needs more alpha to register against black.

Bridge to M3 so any stock Material component picks up the design without extra styling:

```kotlin
private fun AppColors.toColorScheme(): ColorScheme {
    val base = if (isLight) lightColorScheme() else darkColorScheme()
    return base.copy(
        primary = primary, onPrimary = onPrimary,
        primaryContainer = primarySubtle, onPrimaryContainer = primary,
        secondary = primary, onSecondary = onPrimary,
        secondaryContainer = primarySubtle, onSecondaryContainer = primary,
        tertiary = info, onTertiary = Color.White,
        background = background, onBackground = textPrimary,
        surface = surface, onSurface = textPrimary,
        surfaceVariant = surfacePressed, onSurfaceVariant = textSecondary,
        surfaceContainer = surfaceElevated,
        surfaceContainerHigh = surfaceElevated,
        surfaceContainerLow = surface,
        surfaceContainerHighest = surfacePressed,
        inverseSurface = textPrimary, inverseOnSurface = surface,
        error = danger, onError = Color.White,
        errorContainer = dangerSubtle, onErrorContainer = danger,
        outline = outline, outlineVariant = divider, scrim = scrim
    )
}
```

Accessor object + wrapper:

```kotlin
object AppTheme {
    val colors: AppColors @Composable @ReadOnlyComposable get() = LocalAppColors.current
    val shapes: AppShapes @Composable @ReadOnlyComposable get() = LocalAppShapes.current
    val dimens: Dimens   @Composable @ReadOnlyComposable get() = LocalDimens.current
}

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    paletteIndex: Int = 0,
    content: @Composable () -> Unit
) {
    val palette = Palettes.getOrElse(paletteIndex) { Palettes[0] }
    val colors = remember(darkTheme, palette) {
        if (darkTheme) darkAppColors(palette) else lightAppColors(palette)
    }
    val colorScheme = remember(colors) { colors.toColorScheme() }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(colorScheme = colorScheme, typography = Typography, shapes = M3Shapes) {
        CompositionLocalProvider(
            LocalAppColors provides colors,
            LocalAppShapes provides AppShapes(),
            LocalDimens provides Dimens(),
            content = content
        )
    }
}
```

**Migrating an existing app:** keep the old colour names alive as `@Deprecated(... ReplaceWith("AppTheme.colors.x"))` aliases so the project keeps compiling while screens are converted one at a time, then delete them at the end.

---

## 3. Theme mode (System / Light / Dark)

Three pieces, all required:

1. **Persist** a `ThemeMode` enum in prefs.

```kotlin
enum class ThemeMode(val label: String) {
    SYSTEM("System"), LIGHT("Light"), DARK("Dark");
    companion object {
        fun fromName(name: String?) = entries.firstOrNull { it.name == name } ?: SYSTEM
    }
}
```

2. **Override the configuration in `attachBaseContext`** so the forced mode applies before the first frame — this kills the light-flash on cold start and makes non-Compose resources (`res/values-night/`) agree with Compose.

```kotlin
override fun attachBaseContext(newBase: Context) {
    val mode = ThemeMode.fromName(
        newBase.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_THEME_MODE, null)
    )
    val context = if (mode == ThemeMode.SYSTEM) newBase else {
        val config = Configuration(newBase.resources.configuration)
        config.uiMode = (config.uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()) or
            if (mode == ThemeMode.DARK) Configuration.UI_MODE_NIGHT_YES
            else Configuration.UI_MODE_NIGHT_NO
        newBase.createConfigurationContext(config)
    }
    super.attachBaseContext(context)
}
```

3. **Resolve in `setContent`** and drive edge-to-edge from it.

```kotlin
val darkTheme = when (themeMode) {
    ThemeMode.SYSTEM -> isSystemInDarkTheme()
    ThemeMode.LIGHT -> false
    ThemeMode.DARK -> true
}
LaunchedEffect(darkTheme) {
    enableEdgeToEdge(
        statusBarStyle = SystemBarStyle.auto(TRANSPARENT, TRANSPARENT) { darkTheme },
        navigationBarStyle = SystemBarStyle.auto(TRANSPARENT, TRANSPARENT) { darkTheme }
    )
}
AppTheme(darkTheme = darkTheme, paletteIndex = paletteIndex) { ... }
```

XML side — transparent bars, matching window background in both:

```xml
<!-- values/themes.xml -->
<style name="Theme.App" parent="android:Theme.Material.Light.NoActionBar">
    <item name="android:windowBackground">@color/window_background</item>
    <item name="android:statusBarColor">@android:color/transparent</item>
    <item name="android:navigationBarColor">@android:color/transparent</item>
    <item name="android:windowLightStatusBar">true</item>
</style>
```
plus a `values-night/themes.xml` with the non-Light parent and `windowLightStatusBar` false, and `window_background` defined in both `values/colors.xml` and `values-night/colors.xml`.

---

## 4. Component library (`presentation/component/design/`)

Five files. This is the whole vocabulary; screens use nothing else.

### `AppBars.kt`
- `AppTopBar(title, eyebrow?, onNavigateBack?, actions)` — M3 `TopAppBar`, transparent container, title column with an optional uppercase `labelMedium` eyebrow above a `titleLarge` title.
- `AppLargeHeader(eyebrow, title, trailing?)` — the in-content big header for scrollable top-level screens (no app bar chrome).
- `AppScaffold(...)` — wraps `Scaffold` with `containerColor = colors.background`, `contentColor = colors.textPrimary`. **Every screen uses this, never raw `Scaffold`.**
- `NotificationBellButton(unreadCount, onClick)` — circular bordered surface + `BadgedBox`.

### `Buttons.kt`
- `PrimaryButton(text, onClick, icon?, containerColor = colors.primary, contentColor = contentColorOn(containerColor), height = dimens.buttonHeight)` — filled, `shapes.button`, disabled = container at 0.4 alpha / content at 0.6.
- `SecondaryButton(...)` — `TextButton` on `colors.surface`, 38 dp, `textSecondary` content.
- `PrimaryFab(text, icon, onClick)` — extended pill FAB, `shapes.chip`, `fabSize` height.

Passing a non-default `containerColor` still gets correct text colour for free via `contentColorOn`.

### `Cards.kt`
- `StatCard(value, label, accent, style, icon?, height?, onClick?)` with `StatCardStyle { Plain, Tinted }`. Plain = surface + divider border; Tinted = accent at 0.08/0.12 alpha + accent border at 0.22. Value is `headlineSmall`, label `bodySmall` secondary.
- `AppFilterChip(label, selected, onClick, count?)` — pill; selected fills with `primary` + `onPrimary`.
- `EmptyState(icon, title, message, actionLabel?, onAction?)` — 64 dp `primarySubtle` rounded icon tile, centred title/message, optional primary button. **Every list screen must have one.**
- `Pill(text, accent, tinted, showDot)` — small status tag.
- `MetricBar(name, value, progress, accent, leading?)` — label row + rounded `LinearProgressIndicator`.

### `GroupedList.kt` — the heart of the design language
- `SectionHeader(text, trailing?)` — uppercase `labelMedium` secondary, 16 dp side padding, 8 dp gap to the card.
- `GroupedCard(shape, color, border) { }` — `Surface` with `shapes.card` + hairline divider border wrapping a `Column`.
- `SettingsGroup(title?, footnote?) { }` — `SectionHeader` + `GroupedCard` + optional `bodySmall` footnote below.
- `InsetDivider(startInset = dimens.dividerInset)` — hairline; use `dividerInsetWithIcon` (54 dp) between rows that have leading icons so the divider starts at the text.
- `Chevron(tint = textTertiary)`.
- `SettingsRow(...)` — the universal row. One composable covers navigation rows, toggles, value rows, radio rows, and destructive rows:

```kotlin
SettingsRow(
    title: String,
    subtitle: String? = null,
    leadingIcon: Painter? = null,
    leadingIconTint: Color = colors.textSecondary,
    leadingIconContainer: Color? = null,
    value: String? = null,
    valueColor: Color = colors.textSecondary,
    titleColor: Color = colors.textPrimary,
    showChevron: Boolean? = null,
    checked: Boolean? = null,
    onCheckedChange: ((Boolean) -> Unit)? = null,
    enabled: Boolean = true,
    minHeight: Dp = dimens.rowHeight,
    onClick: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null
)
```

Behaviour that makes it work everywhere: chevron auto-shows when `onClick != null && checked == null && trailing == null`; when `checked != null` the whole row toggles; `leadingIconContainer` switches the icon from a bare 24 dp glyph to a 30 dp tinted rounded tile; `defaultMinSize` (not fixed height) so two-line rows grow.

### `Charts.kt` — pure Compose, zero chart libraries
- `ChartBar(label, value, highlighted)`, `ChartSlice(label, value, color)` data holders.
- `ActivityBarChart(bars, accent, plotHeight)` — track behind each bar (`Black 0.05` light / `White 0.07` dark), `animateFloatAsState` per bar with `delayMillis = index * 55`, 620 ms `FastOutSlowInEasing`.
- `RingChart(slices, thickness, gapDegrees, center)` — `Canvas` `drawArc` with a 900 ms sweep and a composable centre slot.
- `LegendRow(...)`, `SegmentedToggle(options, selectedIndex, onSelect)` — pill toggle with 220 ms animated fill.

Animation convention: `var play by remember(data) { mutableStateOf(false) }` + `LaunchedEffect(data) { play = true }` so charts re-animate when data changes, not on every recomposition.

### `Inputs.kt`
- `appTextFieldColors(accent)` — single source for all `OutlinedTextField`s.
- `AppSearchField(value, onValueChange, placeholder)` — 52 dp, `shapes.textField`, leading search icon.

### Floating bottom nav (`presentation/component/BottomBar.kt`)
Not a `NavigationBar`. A 64 dp `Surface` with `RoundedCornerShape(32.dp)`, 24 dp horizontal + 32 dp bottom margin, shadow 8 dp light / 12 dp dark, hairline border. Selected tab animates to a filled `primary` pill showing icon + label; unselected shows icon only. `animateColorAsState` + `animateContentSize`, 300 ms.

Navigation click uses the standard single-top pattern:
```kotlin
navController.navigate(tab.route) {
    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
    launchSingleTop = true
    restoreState = true
}
```

---

## 5. Icons — drop `material-icons-extended`

Replace it with local vector drawables and a typed accessor object.

1. Export the icons you use from Google Fonts / Material Symbols as Android vectors into `res/drawable/ic_*.xml`, 24 dp, `viewportWidth/Height 960`, `fillColor="#FF000000"` (tinted at use site).
2. Central accessor:

```kotlin
object AppIcons {
    object Res {
        @DrawableRes val Home = R.drawable.ic_home
        @DrawableRes val Settings = R.drawable.ic_settings
        ...
    }

    val Home: Painter @Composable get() = painterResource(Res.Home)
    val Settings: Painter @Composable get() = painterResource(Res.Settings)
    ...
}
```

The `Res` layer exists for places needing `@DrawableRes Int` (nav tabs, notifications); the `Painter` layer for composables. Components take `Painter`, never `ImageVector`.

3. Delete the `material-icons-extended` entry from `libs.versions.toml` and `build.gradle`, then `grep -r "material.icons"` to confirm zero references.

---

## 6. Screen pattern

Every screen looks like this:

```kotlin
@Composable
fun SomeScreen(viewModel: SomeViewModel, onNavigateBack: () -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens

    AppScaffold(
        topBar = { AppTopBar(title = "Settings", eyebrow = "App", onNavigateBack = onNavigateBack) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(
                start = dimens.screenPadding,
                end = dimens.screenPadding,
                top = dimens.listTopPadding,
                bottom = dimens.listBottomPadding + dimens.bottomBarSpace
            ),
            verticalArrangement = Arrangement.spacedBy(dimens.space24)
        ) {
            item {
                SettingsGroup(title = "Appearance", footnote = "System follows your device setting.") {
                    ThemeMode.entries.forEachIndexed { i, mode ->
                        if (i > 0) InsetDivider(dimens.dividerInsetWithIcon)
                        SettingsRow(
                            title = mode.label,
                            leadingIcon = iconFor(mode),
                            leadingIconTint = if (themeMode == mode) colors.primary else colors.textSecondary,
                            showChevron = false,
                            trailing = { if (themeMode == mode) Icon(AppIcons.Check, null, tint = colors.primary) },
                            onClick = { viewModel.updateThemeMode(mode) }
                        )
                    }
                }
            }
        }
    }
}
```

Rules baked in above: `AppScaffold`, `LazyColumn` with `space24` between groups, side padding via `contentPadding` (not `Modifier.padding`, so scroll bars/ripples reach the edge), bottom padding includes `bottomBarSpace`, dividers only *between* rows.

**State:** expose a `sealed class XUiState { Loading; data class Success(...); data class Error(message) }` per screen and `when` over it. Success holds pre-computed display data (counts, chart series, formatted labels) — screens should not compute aggregates inline.

---

## 7. Release build

Turn on shrinking once the redesign lands; the icon-library removal + font swap only pays off with R8 on.

```groovy
buildTypes {
    release {
        minifyEnabled true
        shrinkResources true
        proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
    }
    debug {
        minifyEnabled false
        shrinkResources false
    }
}
```

```proguard
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
-keepattributes *Annotation*, InnerClasses, Signature, RuntimeVisibleAnnotations, AnnotationDefault

-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class kotlinx.serialization.json.** { kotlinx.serialization.KSerializer serializer(...); }
-if @kotlinx.serialization.Serializable class **
-keepclassmembers class <1> { static <1>$Companion Companion; static **$* *; }
-keepclassmembers class **$* implements kotlinx.serialization.internal.GeneratedSerializer {
    kotlinx.serialization.KSerializer[] childSerializers();
    kotlinx.serialization.descriptors.SerialDescriptor getDescriptor();
}

-dontwarn java.lang.invoke.StringConcatFactory
-dontwarn org.slf4j.**
```

Always install and smoke-test the minified release build; Compose + Hilt + Room survive R8 fine, reflection-based serialization does not without the rules above.

---

## 8. Acceptance checklist

- [ ] `grep -rn "\.dp" presentation/screen/` returns (almost) nothing — all sizes are tokens
- [ ] `grep -rn "Color(0x" presentation/` returns nothing outside `ui/theme/`
- [ ] `grep -rn "MaterialTheme.colorScheme" presentation/` returns nothing
- [ ] `grep -rn "material.icons" .` returns nothing; dependency removed
- [ ] No raw `Scaffold` — all `AppScaffold`
- [ ] Every screen verified in light **and** dark, and with at least 3 different accent palettes
- [ ] Every list/empty path has an `EmptyState`
- [ ] Every scrollable screen clears the floating bottom bar (`+ dimens.bottomBarSpace`)
- [ ] Text on accent-filled surfaces uses `onPrimary` / `contentColorOn`, never hardcoded white
- [ ] Cold start shows no light-flash in dark mode (`attachBaseContext` override present)
- [ ] Palette order frozen by a unit test if the index is persisted
- [ ] Release build installs and runs with `minifyEnabled true`
- [ ] Deprecated colour aliases deleted once migration is complete
