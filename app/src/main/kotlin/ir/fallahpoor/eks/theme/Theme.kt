package ir.fallahpoor.eks.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class Spacing(
    val default: Dp = 0.dp,
    val verySmall: Dp = 4.dp,
    val small: Dp = 8.dp,
    val normal: Dp = 16.dp
)

private val DefaultSpacing = Spacing()

@Suppress("UnusedReceiverParameter")
val MaterialTheme.spacing: Spacing
    get() = DefaultSpacing

private val DarkColorScheme = darkColorScheme(
    primary = Orange,
    onPrimary = White,
    secondary = Orange,
    onSecondary = White,
    error = DarkRed,
    background = Gray
)

private val LightColorScheme = lightColorScheme(
    primary = Blue,
    onPrimary = White,
    secondary = Orange,
    onSecondary = White,
    error = LightRed,
    onBackground = Gray
)

@Composable
fun EksTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}