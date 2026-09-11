package com.project.roulette.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

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
    val topBarHeight: Dp = 56.dp,

    val space2: Dp = 2.dp,
    val space4: Dp = 4.dp,
    val space8: Dp = 8.dp,
    val space12: Dp = 12.dp,
    val space16: Dp = 16.dp,
    val space20: Dp = 20.dp,
    val space24: Dp = 24.dp,
    val space32: Dp = 32.dp
)

val LocalDimens = staticCompositionLocalOf { Dimens() }
