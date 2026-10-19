package com.pe.terapia.presentation.home.paciente.perfil

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ExitToApp
import androidx.compose.material.icons.automirrored.outlined.Help
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pe.terapia.ui.components.BiometricoSwitchItem
import com.pe.terapia.ui.components.PerfilOpcionItem
import org.koin.androidx.compose.koinViewModel

@Composable
fun PerfilScreen(
    onCerrarSesion: () -> Unit,
    onVerHistorialClinico: (String, String) -> Unit,
    onVerPagos: () -> Unit,
    viewModel: PerfilViewModel = koinViewModel()
) {
    val usuario by viewModel.usuario.collectAsStateWithLifecycle()

    val iniciales = usuario?.let { user ->
        user.nombres.split(" ").filter { it.isNotBlank() }
            .take(2).mapNotNull { it.firstOrNull()?.uppercase() }.joinToString("")
    } ?: "PA"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Mi Perfil", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Icon(Icons.Outlined.Settings, contentDescription = "Configuración")
        }

        Spacer(modifier = Modifier.height(4.dp))

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.size(96.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
                Text(iniciales, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = usuario?.let { "${it.nombres} ${it.apellidos}" } ?: "Cargando...", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(text = usuario?.correo ?: "", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                PerfilOpcionItem(Icons.Outlined.PersonOutline, "Mis datos", onClick = {})
                PerfilOpcionItem(Icons.Outlined.Description, "Mi historial clínico", onClick = { usuario?.let { onVerHistorialClinico(it.uid, "${it.nombres} ${it.apellidos}") } })
                PerfilOpcionItem(Icons.Outlined.ReceiptLong, "Mis pagos y comprobantes", onClick = onVerPagos)

                HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), color = MaterialTheme.colorScheme.outlineVariant)

                // COMPONENTE COMPARTIDO DEL SWITCH BIOMÉTRICO
                BiometricoSwitchItem()

                PerfilOpcionItem(Icons.AutoMirrored.Outlined.Help, "Ayuda y soporte", onClick = {})
                PerfilOpcionItem(Icons.AutoMirrored.Outlined.ExitToApp, "Cerrar sesión", esRojo = true, onClick = onCerrarSesion)
            }
        }

        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))) {
            Column(modifier = Modifier.fillMaxWidth().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = "💚", fontSize = 24.sp)
                Text("Recuerda: pedir ayuda también es un acto de amor propio.", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onPrimaryContainer, textAlign = TextAlign.Center)
            }
        }
    }
}