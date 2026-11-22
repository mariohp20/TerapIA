package com.pe.terapia.presentation.cuenta

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AyudaSoporteScreen(onVolver: () -> Unit) {
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    Scaffold(
        topBar = { TopAppBar(title = { Text("Ayuda y soporte") }, navigationIcon = {
            IconButton(onClick = onVolver) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Volver") }
        }) },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("¿Necesitas ayuda?", style = MaterialTheme.typography.titleLarge)
            Text("Escríbenos por WhatsApp al +51 933 734 545.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Button(onClick = {
                val message = Uri.encode("Hola, necesito ayuda con la aplicación TerapIA.")
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/51933734545?text=$message"))
                try { context.startActivity(intent) }
                catch (_: ActivityNotFoundException) { scope.launch { snackbar.showSnackbar("Instala WhatsApp o comunícate con la clínica por sus canales habituales.") } }
            }, modifier = Modifier.fillMaxWidth()) { Text("Contactar por WhatsApp") }
            Text("Documento de trabajo", style = MaterialTheme.typography.titleMedium)
            Text("Términos y condiciones en borrador académico. Requieren completar los datos de la clínica y revisión legal antes de usarse con pacientes reales.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Button(onClick = {
                runCatching {
                    val directory = File(context.cacheDir, "pdf").apply { mkdirs() }
                    val file = File(directory, "terminos_condiciones_terapia_borrador.pdf")
                    context.assets.open("terminos_condiciones_terapia_borrador.pdf").use { input -> file.outputStream().use(input::copyTo) }
                    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(uri, "application/pdf")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(intent, "Abrir términos y condiciones"))
                }.onFailure { scope.launch { snackbar.showSnackbar("No se pudo abrir el PDF. Comprueba que haya un lector de documentos instalado.") } }
            }, modifier = Modifier.fillMaxWidth()) { Text("Ver términos y condiciones (PDF)") }
        }
    }
}
