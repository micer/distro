package eu.micer.distro.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = androidx.compose.ui.graphics.Color(0xFF9CA3AF),
    onPrimary = androidx.compose.ui.graphics.Color(0xFF1F2937),
    primaryContainer = androidx.compose.ui.graphics.Color(0xFF374151),
    onPrimaryContainer = androidx.compose.ui.graphics.Color(0xFFE5E7EB),
    secondary = androidx.compose.ui.graphics.Color(0xFF9CA3AF),
    onSecondary = androidx.compose.ui.graphics.Color(0xFF1F2937),
    secondaryContainer = androidx.compose.ui.graphics.Color(0xFF374151),
    onSecondaryContainer = androidx.compose.ui.graphics.Color(0xFFE5E7EB),
    tertiary = Success,
    onTertiary = androidx.compose.ui.graphics.Color(0xFF1F2937),
    tertiaryContainer = Success.copy(alpha = 0.2f),
    onTertiaryContainer = Success,
    surface = androidx.compose.ui.graphics.Color(0xFF111827),
    onSurface = androidx.compose.ui.graphics.Color(0xFFE5E7EB),
    surfaceVariant = androidx.compose.ui.graphics.Color(0xFF1F2937),
    onSurfaceVariant = androidx.compose.ui.graphics.Color(0xFF9CA3AF),
    background = androidx.compose.ui.graphics.Color(0xFF0F172A),
    onBackground = androidx.compose.ui.graphics.Color(0xFFE5E7EB),
    error = Error,
    onError = OnError,
    errorContainer = androidx.compose.ui.graphics.Color(0xFF7F1D1D),
    onErrorContainer = androidx.compose.ui.graphics.Color(0xFFFEE2E2),
    outline = androidx.compose.ui.graphics.Color(0xFF4B5563)
)

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = OnPrimaryContainer,
    secondary = Secondary,
    onSecondary = OnSecondary,
    secondaryContainer = SecondaryContainer,
    onSecondaryContainer = OnSecondaryContainer,
    tertiary = Success,
    onTertiary = OnSuccess,
    tertiaryContainer = androidx.compose.ui.graphics.Color(0xFFD1FAE5),
    onTertiaryContainer = androidx.compose.ui.graphics.Color(0xFF065F46),
    surface = Surface,
    onSurface = OnSurface,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = OnSurfaceVariant,
    background = Background,
    onBackground = OnBackground,
    error = Error,
    onError = OnError,
    errorContainer = ErrorContainer,
    onErrorContainer = OnErrorContainer,
    outline = Outline
)



@Composable
fun DistroTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
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
        shapes = Shapes,
        content = content
    )
}
