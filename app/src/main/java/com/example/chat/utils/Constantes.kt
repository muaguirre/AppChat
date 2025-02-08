package com.example.chat.utils

class Constantes {
    companion object {
        const val KEY_USUARIOS = "usuarios"
        const val KEY_NOMBRE = "nombre"
        const val KEY_EMAIL = "email"
        const val KEY_PASSWORD = "password"
        const val KEY_PREFERENCIA_NOMBRE = "chatPreferencia"
        const val KEY_ESTA_LOGEADO = "estaLogeado"
        const val KEY_ID_USUARIO = "idUsuario"
        const val KEY_IMAGEN = "imagen"
        const val KEY_FCM_TOKEN = "fcmToken"
        const val KEY_USUARIO = "usuario"
        const val KEY_CHAT = "chat"
        const val KEY_ID_REMITENTE = "remitenteId"
        const val KEY_ID_DESTINATARIO = "destinatarioId"
        const val KEY_MENSAJE = "mensaje"
        const val KEY_MARCATIEMPO = "marcatiempo"
        const val KEY_CONVERSACIONES = "conversaciones"
        const val KEY_NOMBRE_DESTINATARIO = "nombreRemitente"
        const val KEY_NOMBRE_REMITENTE = "nombreDestinatario"
        const val KEY_IMAGEN_DESTINATARIO = "imagenDestinatario"
        const val KEY_IMAGEN_REMITENTE = "imagenRemitente"
        const val KEY_ULTIMO_MENSAJE = "ultimoMensaje"
        const val KEY_ENLINEA = "enLinea"
        const val AUTORIZACION_MENSAJE = "Authorization"
        const val MENSAJE_TIPO = "Content-Type"
        const val MENSAJE_DATA = "data"
        const val MENSAJE_REGISTRO_IDS = "registration_ids"

        private var mensajesCabeceras: HashMap<String, String>? = null
        fun getCabecerasMensajes(): HashMap<String, String> {
            if (mensajesCabeceras == null) {
                mensajesCabeceras = HashMap()
                mensajesCabeceras?.put(
                    AUTORIZACION_MENSAJE,
                    "key=AAAA7N6W6-g:APA91bEb_u7lVuKx-epRH7VpUCwE7L_BVhqnF-eV3AktRZsNdwZ_refTx6wm8mLwhO3owOvhXFs7frIhHetQJ7eS1C5rN0vZR-KTE-FYwBe-rakfDZQ07EFG0i2cyMbGD9g-mWYB3y3L"
                )
                mensajesCabeceras?.put(
                    MENSAJE_TIPO,
                    "application/json"
                )
            }
            return mensajesCabeceras!!
        }


    }
}

