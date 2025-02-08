package com.example.chat.models

import java.io.Serializable
import java.util.Date

class ChatMensaje(
    var remitenteId: String? = "",
    var destinatarioId: String? = "",
    var mensaje: String? = "",
    var dateTime: String? = "",
    var dateObject: Date? = Date(),
    var conversacionId: String? = "",
    var conversacionNombre: String? = "",
    var conversacionImagen: String? = ""
)

