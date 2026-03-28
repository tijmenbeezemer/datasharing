package nl.tijmen.articlereader.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import nl.tijmen.articlereader.data.FeedSource

private val FoodLightColors = lightColorScheme(
    primary = FoodPrimary,
    onPrimary = FoodOnPrimary,
    primaryContainer = FoodPrimaryContainer,
    onPrimaryContainer = FoodOnPrimaryContainer
)

private val FoodDarkColors = darkColorScheme(
    primary = FoodDarkPrimary,
    onPrimary = FoodDarkOnPrimary,
    primaryContainer = FoodDarkPrimaryContainer,
    onPrimaryContainer = FoodPrimaryContainer
)

private val EconLightColors = lightColorScheme(
    primary = EconPrimary,
    onPrimary = EconOnPrimary,
    primaryContainer = EconPrimaryContainer,
    onPrimaryContainer = EconOnPrimaryContainer
)

private val EconDarkColors = darkColorScheme(
    primary = EconDarkPrimary,
    onPrimary = EconDarkOnPrimary,
    primaryContainer = EconDarkPrimaryContainer,
    onPrimaryContainer = EconPrimaryContainer
)

@Composable
fun ArticleReaderTheme(
    source: FeedSource? = null,
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        source == FeedSource.ESB ->
            if (darkTheme) EconDarkColors else EconLightColors
        else ->
            if (darkTheme) FoodDarkColors else FoodLightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
