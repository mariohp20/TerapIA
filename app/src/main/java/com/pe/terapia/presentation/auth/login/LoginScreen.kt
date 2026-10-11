package com.pe.terapia.presentation.auth.login

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pe.terapia.R
import com.pe.terapia.domain.model.Usuario
import com.pe.terapia.presentation.auth.AuthUiState
import com.pe.terapia.presentation.auth.AuthViewModel
import com.pe.terapia.presentation.auth.utils.GoogleAuthClient
import com.pe.terapia.ui.theme.TerapiaTheme
import org.koin.androidx.compose.koinViewModel

// SCREEN
@Composable
fun LoginScreen(
    onLoginExitoso: (Usuario) -> Unit,
    onPerfilPendiente: (String?) -> Unit,
    onIrARegistro: () -> Unit,
    onIrARecuperar: () -> Unit,
    mensajeInicial: String? = null,
    viewModel: AuthViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val googleAuthClient = remember(context) { GoogleAuthClient(context) }

    LaunchedEffect(uiState) {
        val currentState = uiState
        if (currentState is AuthUiState.Success) {
            onLoginExitoso(currentState.usuario)
        }
        if (currentState is AuthUiState.ProfileCompletionRequired) onPerfilPendiente(currentState.mensaje)
    }

    LaunchedEffect(mensajeInicial) {
        mensajeInicial?.let(viewModel::informarError)
    }

    LoginContent(
        uiState = uiState,
        onLoginClick = { email, password -> viewModel.login(email, password) },
        onGoogleLoginClick = {
            viewModel.loginConGoogle { googleAuthClient.obtenerIdToken() }
        },
        onIrARegistro = onIrARegistro,
        onIrARecuperar = onIrARecuperar
    )
}

// CONTENT
@Composable
fun LoginContent(
    uiState: AuthUiState,
    onLoginClick: (email: String, password: String) -> Unit,
    onGoogleLoginClick: () -> Unit,
    onIrARegistro: () -> Unit,
    onIrARecuperar: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    var startAnimation by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { startAnimation = true }

    val contentAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 650, delayMillis = 100, easing = FastOutSlowInEasing),
        label = "alpha"
    )

    val contentTranslationY by animateFloatAsState(
        targetValue = if (startAnimation) 0f else 35f,
        animationSpec = tween(durationMillis = 650, delayMillis = 100, easing = FastOutSlowInEasing),
        label = "translation"
    )
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primaryContainer)
    ) {
        // CABECERA
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.35f)
                .statusBarsPadding()
                .padding(horizontal = 32.dp)
                .padding(top = 32.dp),
            contentAlignment = Alignment.TopStart
        ) {
            Column(modifier = Modifier.fillMaxWidth(0.65f)) {
                Text(
                    text = stringResource(R.string.login_encabezado),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Image(
                painter = painterResource(id = R.drawable.ic_terapia_logo),
                contentDescription = stringResource(R.string.auth_logo_content_description),
                modifier = Modifier
                    .size(170.dp)
                    .align(Alignment.BottomEnd)
                    .offset(y = -16.dp)
            )
        }

        // TARJETA DEL FORMULARIO
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.65f)
                .graphicsLayer {
                    alpha = contentAlpha
                    translationY = contentTranslationY
                },
            shape = RoundedCornerShape(topStart = 36.dp, topEnd = 36.dp),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
                    .padding(top = 32.dp)
                    .navigationBarsPadding()
                    .imePadding(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.login_titulo_formulario),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(R.string.login_registro_pregunta),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.login_ir_registro),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable(onClick = onIrARegistro)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // EMAIL
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text(stringResource(R.string.login_email)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Email,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // CONTRASEÑA
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text(stringResource(R.string.login_password)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Lock,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                contentDescription = stringResource(
                                    if (passwordVisible) R.string.login_ocultar_password else R.string.login_mostrar_password
                                ),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // RECORDAR / RECUPERAR
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.login_olvide_password),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable(onClick = onIrARecuperar)
                    )
                }

                // ERROR
                if (uiState is AuthUiState.Error) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = uiState.mensaje,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Start).padding(start = 12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // BOTÓN PRINCIPAL
                Button(
                    onClick = { onLoginClick(email.trim(), password) },
                    enabled = uiState !is AuthUiState.Loading,
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    if (uiState is AuthUiState.Loading) {
                        CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text(text = stringResource(R.string.login_boton_ingresar), style = MaterialTheme.typography.titleMedium)
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // DIVISOR
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
                    Text(
                        text = stringResource(R.string.login_google_divisor),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
                }

                Spacer(modifier = Modifier.height(28.dp))

                // GOOGLE
                OutlinedButton(
                    onClick = onGoogleLoginClick,
                    enabled = uiState !is AuthUiState.Loading,
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_google),
                        contentDescription = stringResource(R.string.login_google_continuar),
                        modifier = Modifier.size(24.dp),
                        tint = Color.Unspecified
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = stringResource(R.string.login_google_nombre),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}
