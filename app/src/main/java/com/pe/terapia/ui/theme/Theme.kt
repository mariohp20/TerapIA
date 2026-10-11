package com.pe.terapia.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = TerapiaTurquesa,
    onPrimary = Color.Black,

    secondary = TerapiaLavanda,
    onSecondary = Color.Black,

    tertiary = TerapiaCoral,

    background = TerapiaFondoOscuro,
    surface = TerapiaSuperficieOscura,
    primaryContainer = TerapiaFondoMarcaOscuro,
    onPrimaryContainer = TerapiaTextoMarcaOscuro,

    onBackground = TerapiaBlancoHueso,
    onSurface = TerapiaBlancoHueso,

    onSurfaceVariant = TerapiaBlancoHueso.copy(alpha = 0.70f),

    error = TerapiaError,

    outline = TerapiaGrisSuave.copy(alpha = 0.45f)
)

private val LightColorScheme = lightColorScheme(
    primary = TerapiaTurquesa,
    onPrimary = Color.White,

    secondary = TerapiaLavanda,
    onSecondary = Color.White,

    tertiary = TerapiaCoral,

    background = TerapiaFondoClaro,
    surface = TerapiaSuperficieClaro,
    primaryContainer = TerapiaFondoMarcaClaro,
    onPrimaryContainer = TerapiaTextoMarcaClaro,

    onBackground = TerapiaGrisTexto,
    onSurface = TerapiaGrisTexto,

    onSurfaceVariant = TerapiaGrisTexto.copy(alpha = 0.65f),

    error = TerapiaError,

    outline = TerapiaGrisSuave
)

@Composable
fun TerapiaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor &&
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {

            val context = LocalContext.current

            if (darkTheme) {
                dynamicDarkColorScheme(context)
            } else {
                dynamicLightColorScheme(context)
            }
        }

        darkTheme -> DarkColorScheme

        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
            content = content
        )
    }
}
