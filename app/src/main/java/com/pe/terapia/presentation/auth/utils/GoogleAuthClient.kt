package com.pe.terapia.presentation.auth.utils

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.pe.terapia.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.CancellationException
import androidx.credentials.CustomCredential

class GoogleAuthClient(private val context: Context) {
    suspend fun obtenerIdToken(): Result<String> = try {
            val credentialManager = CredentialManager.create(context)
            val webClientId = context.getString(R.string.default_web_client_id)

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
                Result.success(googleIdTokenCredential.idToken)
            } else {
                Result.failure(IllegalStateException("Google no devolvió una credencial de acceso válida."))
            }
        } catch (exception: CancellationException) {
            // El usuario cerró el pop-up deslizando hacia abajo
            throw exception
        } catch (exception: Exception) {
            Result.failure(exception)
        }
}
