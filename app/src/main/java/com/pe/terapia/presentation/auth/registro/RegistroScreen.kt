package com.pe.terapia.presentation.auth.registro

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pe.terapia.R
import com.pe.terapia.domain.usecase.auth.PasswordValidator
import com.pe.terapia.domain.usecase.auth.DniValidator
import com.pe.terapia.presentation.auth.AuthUiState
import com.pe.terapia.presentation.auth.AuthViewModel
import com.pe.terapia.ui.theme.TerapiaTheme
import org.koin.androidx.compose.koinViewModel

// SCREEN
@Composable
fun RegistroScreen(
    onRegistroExitoso: () -> Unit,
    onPerfilPendiente: (String?) -> Unit,
    onIrALogin: () -> Unit,
    viewModel: AuthViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Success) onRegistroExitoso()
        (uiState as? AuthUiState.ProfileCompletionRequired)?.let { onPerfilPendiente(it.mensaje) }
    }

    RegistroContent(
        uiState = uiState,
        onRegistroClick = { nombres, apellidos, email, password, telefono, dni ->
            viewModel.registro(nombres, apellidos, email, password, telefono, dni)
        },
        onIrALogin = onIrALogin
    )
}

// CONTENT
@Composable
fun RegistroContent(
    uiState: AuthUiState,
    onRegistroClick: (nombres: String, apellidos: String, email: String, password: String, telefono: String?, dni: String) -> Unit,
    onIrALogin: () -> Unit
) {
    var nombres by remember { mutableStateOf("") }
    var apellidos by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var dni by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmarPassword by remember { mutableStateOf("") }

    var passwordVisible by remember { mutableStateOf(false) }
    var confirmarPasswordVisible by remember { mutableStateOf(false) }
    var errorLocal by remember { mutableStateOf<String?>(null) }

    val errorPasswordNoCoincide = stringResource(R.string.registro_error_passwords_no_coinciden)

    // Animación de entrada
    var startAnimation by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { startAnimation = true }

    val contentAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(650, 100, FastOutSlowInEasing), label = "alpha"
    )
    val contentTranslationY by animateFloatAsState(
        targetValue = if (startAnimation) 0f else 35f,
        animationSpec = tween(650, 100, FastOutSlowInEasing), label = "translation"
    )

    // Colores de la marca (Mismos que el Login)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primaryContainer)
    ) {
        // CABECERA (35% de la pantalla)
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
                    text = stringResource(R.string.registro_encabezado),
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

        // TARJETA DEL FORMULARIO (65% de la pantalla)
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
                    text = stringResource(R.string.registro_titulo_formulario),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(R.string.registro_login_pregunta),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.registro_ir_login),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable(onClick = onIrALogin)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // NOMBRE
                OutlinedTextField(
                    value = nombres,
                    onValueChange = { nombres = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text(stringResource(R.string.registro_nombres)) },
                    leadingIcon = {
                        Icon(imageVector = Icons.Outlined.Person, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = apellidos,
                    onValueChange = { apellidos = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text(stringResource(R.string.registro_apellidos)) },
                    leadingIcon = { Icon(Icons.Outlined.Person, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // EMAIL
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text(stringResource(R.string.login_email)) },
                    leadingIcon = {
                        Icon(imageVector = Icons.Outlined.Email, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = telefono,
                    onValueChange = { telefono = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text(stringResource(R.string.registro_telefono_opcional)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = dni,
                    onValueChange = { dni = it.filter(Char::isDigit).take(8) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("DNI (8 dígitos)") },
                    supportingText = { Text("Dato declarado por ti; no se verifica automáticamente.") },
                    isError = dni.isNotEmpty() && !DniValidator.esValido(dni),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
                        Icon(imageVector = Icons.Outlined.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
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
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )

                // REQUISITOS DE CONTRASEÑA (Minimalistas y en Cuadrícula 2x2)
                Column(modifier = Modifier.fillMaxWidth().padding(top = 12.dp, start = 4.dp, end = 4.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Box(modifier = Modifier.weight(1f)) {
                            RequisitoPassword(stringResource(R.string.registro_requisito_longitud), PasswordValidator.tieneLongitudMinima(password))
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            RequisitoPassword(stringResource(R.string.registro_requisito_mayuscula), PasswordValidator.tieneMayuscula(password))
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Box(modifier = Modifier.weight(1f)) {
                            RequisitoPassword(stringResource(R.string.registro_requisito_numero), PasswordValidator.tieneNumero(password))
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            RequisitoPassword(stringResource(R.string.registro_requisito_especial), PasswordValidator.tieneCaracterEspecial(password))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // CONFIRMAR CONTRASEÑA
                OutlinedTextField(
                    value = confirmarPassword,
                    onValueChange = { confirmarPassword = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text(stringResource(R.string.registro_confirmar_password)) },
                    leadingIcon = {
                        Icon(imageVector = Icons.Outlined.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    },
                    trailingIcon = {
                        IconButton(onClick = { confirmarPasswordVisible = !confirmarPasswordVisible }) {
                            Icon(
                                imageVector = if (confirmarPasswordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                contentDescription = stringResource(
                                    if (confirmarPasswordVisible) R.string.login_ocultar_password else R.string.login_mostrar_password
                                ),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    visualTransformation = if (confirmarPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )

                val mensajeError = errorLocal ?: (uiState as? AuthUiState.Error)?.mensaje
                if (mensajeError != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = mensajeError,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.align(Alignment.Start).padding(start = 12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                // BOTÓN CREAR CUENTA
                Button(
                    onClick = {
                        errorLocal = if (password != confirmarPassword) errorPasswordNoCoincide else null
                        if (errorLocal == null) onRegistroClick(
                            nombres.trim(), apellidos.trim(), email.trim(), password,
                            telefono.trim().ifBlank { null }, dni
                        )
                    },
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
                        Text(stringResource(R.string.registro_boton_crear), style = MaterialTheme.typography.titleMedium)
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

// COMPONENTE MINIMALISTA PARA REQUISITOS (Con Strikethrough Animado)
@Composable
private fun RequisitoPassword(texto: String, cumplido: Boolean) {
    // Transición suave de color: De Gris a Turquesa
    val color by animateColorAsState(
        targetValue = if (cumplido) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(300),
        label = "colorRequisito"
    )

    // Si se cumple, lo tachamos sutilmente
    val textDecoration = if (cumplido) TextDecoration.LineThrough else TextDecoration.None

    // Icono: Check si cumple, un pequeño punto si no.
    val icono = if (cumplido) "✓" else "•"

    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = icono,
            color = color,
            style = MaterialTheme.typography.labelLarge
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = texto,
            style = MaterialTheme.typography.labelMedium,
            color = color,
            textDecoration = textDecoration
        )
    }
}

@Preview(showBackground = true, name = "Registro - Normal")
@Composable
private fun RegistroPreviewIdle() {
    TerapiaTheme {
        RegistroContent(AuthUiState.Idle, { _, _, _, _, _, _ -> }, {})
    }
}
