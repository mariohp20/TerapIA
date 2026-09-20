package com.pe.terapia.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// Modo Oscuro
private val DarkColorScheme = darkColorScheme(
    primary = TerapiaTurquesa,
    onPrimary = Color.Black,
    secondary = TerapiaLavanda,
    onSecondary = Color.Black,
    tertiary = TerapiaCoral,
    background = Color(0xFF202124),
    surface = Color(0xFF2D2E31),
    onBackground = TerapiaBlancoHueso,
    onSurface = TerapiaBlancoHueso,
    onSurfaceVariant = TerapiaGrisSuave,
    error = TerapiaError,
    outline = TerapiaGrisTexto
)

// Modo Claro
private val LightColorScheme = lightColorScheme(
    primary = TerapiaTurquesa,
    onPrimary = TerapiaBlancoHueso,
    secondary = TerapiaLavanda,
    onSecondary = TerapiaBlancoHueso,
    tertiary = TerapiaCoral,
    background = TerapiaBlancoHueso,
    surface = Color.White,
    onBackground = TerapiaGrisTexto,
    onSurface = TerapiaGrisTexto,
    onSurfaceVariant = TerapiaGrisTexto.copy(alpha = 0.6f),
    error = TerapiaError,
    outline = TerapiaGrisSuave
)

@Composable
fun TerapiaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Apagamos dynamicColor para que la app siempre use la marca "TerapIA"
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
        content = content
    )
}