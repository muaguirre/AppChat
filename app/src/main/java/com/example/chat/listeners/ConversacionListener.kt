package com.example.chat.listeners

import com.example.chat.models.Usuario

interface ConversacionListener {
    fun onConversacionClicked(usuario: Usuario)
}