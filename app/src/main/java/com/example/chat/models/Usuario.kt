package com.example.chat.models

import java.io.Serializable

data class Usuario(
    var imagen: String? = "",
    var nombre: String? = "",
    var email: String? = "",
    var token: String? = "",
    var id: String? = ""
) : Serializable
