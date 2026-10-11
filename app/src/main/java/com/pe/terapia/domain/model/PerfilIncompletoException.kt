package com.pe.terapia.domain.model

class PerfilIncompletoException(val detalle: String? = null) : IllegalStateException(
    detalle ?: "Completa los datos obligatorios de tu perfil para continuar."
)
