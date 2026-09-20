package com.pe.terapia.presentation.auth.utils

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.CancellationException
import androidx.credentials.CustomCredential

class GoogleAuthClient (private val context: Context) {
    suspend fun obtenerIdToken(): String? {
        return try {
            val credentialManager = CredentialManager.create(context)

            // Este es nuestro Web Client ID (Google) de Firebase
            val webClientId = "483555042386-jdeusa4c09k02udu9ggi3oghlcu4jmlq.apps.googleusercontent.com"

            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(webClientId)
                .setAutoSelectEnabled(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            // Lanza el pop-up de Google
            val result = credentialManager.getCredential(context, request)
            val credential = result.credential

            // Extraer correctamente el token del CustomCredential
            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                googleIdTokenCredential.idToken
            } else {
                println("ERROR: Credencial no reconocida.")
                null
            }
        } catch (e: CancellationException) {
            // El usuario cerró el pop-up deslizando hacia abajo
            throw e
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}