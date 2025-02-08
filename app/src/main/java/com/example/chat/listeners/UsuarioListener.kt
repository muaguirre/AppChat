package com.example.chat.listeners

import com.example.chat.models.Usuario

interface UsuarioListener {
    fun onUserClicked(usuario: Usuario)
}