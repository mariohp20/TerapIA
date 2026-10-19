package com.pe.terapia.domain.model

data class ChatMessagge (
    val id: String = "",
    val content: String = "",
    val fromUser: String = "",
    val timestamp: Long = System.currentTimeMillis()
    )