package com.pe.terapia.presentation.splash

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pe.terapia.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import androidx.compose.ui.platform.LocalContext
import com.pe.terapia.core.biometric.BiometricoPreferences
import com.pe.terapia.ui.components.BiometricoGate

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SplashScreen(
    onSessionResolved: (SplashUiState) -> Unit,
    viewModel: SplashViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var mostrarContenido by remember { mutableStateOf(false) }
    var mostrarSubtitulo by remember { mutableStateOf(false) }
    var animacionTerminada by remember { mutableStateOf(false) }
    val titulo = stringResource(R.string.splash_nombre_clinica)

    val animacionesLetras = remember {
        titulo.map { Animatable(0f) }
    }

    val context = LocalContext.current
    var mostrarGate by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        mostrarContenido = true
        delay(200)
        animacionesLetras.forEachIndexed { index, animatable ->
            launch {
                delay(index * 35L)
                animatable.animateTo(
                    targetValue = 1f,
                    animationSpec = spring(dampingRatio = 0.55f, stiffness = 80f)
                )
            }
        }
        delay((titulo.length * 35L) + 300)
        mostrarSubtitulo = true
        delay(600)
        animacionTerminada = true
    }

    LaunchedEffect(uiState, animacionTerminada) {
        if (uiState !is SplashUiState.Loading && animacionTerminada) {
            delay(600)
            val hayUsuario = (uiState as? SplashUiState.SessionResolved)?.usuario != null
            if (hayUsuario && BiometricoPreferences(context).isBiometricoActivo()) {
                mostrarGate = true          // pide huella antes de entrar
            } else {
                onSessionResolved(uiState)  // flujo normal
            }
        }
    }

    Box (modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AnimatedVisibility(
                visible = mostrarContenido,
                enter = fadeIn(animationSpec = tween(600)) + scaleIn(
                    initialScale = 0.90f,
                    animationSpec = tween(600)
                ),
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_terapia_logo),
                        contentDescription = stringResource(R.string.splash_logo_content_description),
                        modifier = Modifier.size(160.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    ) {
                        titulo.forEachIndexed { index, caracter ->
                            if (caracter == ' ') {
                                Spacer(modifier = Modifier.size(8.dp))
                            } else {
                                val progreso = animacionesLetras[index].value
                                Text(
                                    text = caracter.toString(),
                                    style = MaterialTheme.typography.titleLarge,
                                    letterSpacing = (-0.5).sp,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    modifier = Modifier.graphicsLayer {
                                        this.alpha = progreso.coerceIn(0f, 1f)
                                        this.translationY = 60f * (1f - progreso)
                                    }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier.height(26.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        SubtituloSplash(visible = mostrarSubtitulo)
                    }
                }
            }
        }

        if (mostrarGate) {
            BiometricoGate(
                onDesbloqueado = { onSessionResolved(uiState) },
                onCerrarSesion = {
                    viewModel.cerrarSesion {
                        onSessionResolved(SplashUiState.SessionResolved(null))
                    }
                }
            )
        }
    }
}
@Composable
private fun SubtituloSplash(visible: Boolean) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(1200)) + slideInVertically(
            initialOffsetY = {30},
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    ) {
        Text(
            text = stringResource(R.string.splash_subtitulo),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
