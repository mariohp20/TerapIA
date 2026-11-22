package com.pe.terapia

import android.os.Bundle
import androidx.activity.compose.setContent
import com.pe.terapia.core.navigation.NavGraph
import com.pe.terapia.ui.theme.TerapiaTheme
import androidx.fragment.app.FragmentActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.pe.terapia.domain.model.ModoTemaApp
import com.pe.terapia.domain.repository.PreferenciasTemaRepository
import org.koin.android.ext.android.inject

class MainActivity : FragmentActivity(){
    private val preferenciasTema: PreferenciasTemaRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val modo by preferenciasTema.modo.collectAsState()
            val temaOscuro = when (modo) {
                ModoTemaApp.SISTEMA -> isSystemInDarkTheme()
                ModoTemaApp.CLARO -> false
                ModoTemaApp.OSCURO -> true
            }
            TerapiaTheme(darkTheme = temaOscuro) {
                NavGraph()
            }
        }
    }

}
