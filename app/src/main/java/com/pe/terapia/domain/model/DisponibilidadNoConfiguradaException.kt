package com.pe.terapia.domain.model

class DisponibilidadNoConfiguradaException : IllegalStateException(
    "La disponibilidad real requiere definir los horarios laborales y sus excepciones."
)
