package com.example.chat.actividades

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.Toast
import com.example.chat.adapters.AdaptadorConversacionReciente
import com.example.chat.databinding.ActivityMainBinding
import com.example.chat.listeners.ConversacionListener
import com.example.chat.models.ChatMensaje
import com.example.chat.models.Usuario
import com.example.chat.utils.Constantes
import com.example.chat.utils.ManejoPreferencia
import com.google.firebase.firestore.*
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : BaseActivity(), ConversacionListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var manejoPreferencia: ManejoPreferencia
    private lateinit var conversaciones: MutableList<ChatMensaje>
    private lateinit var adaptadorConversacion: AdaptadorConversacionReciente
    private lateinit var database: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        manejoPreferencia = ManejoPreferencia(applicationContext)
        init()
        cargarDetallesUsuario()
        getToken()
        setListeners()
        listenConversaciones()
    }

    private fun init() {
        conversaciones = ArrayList()
        adaptadorConversacion = AdaptadorConversacionReciente(conversaciones, this)
        binding.recyclerConversacion.adapter = adaptadorConversacion
        database = FirebaseFirestore.getInstance()
    }


    private fun setListeners() {
        binding.iconoLogOut.setOnClickListener {
            logOut()
        }
        binding.fabChatNuevo.setOnClickListener{
            startActivity(Intent(applicationContext, UsuariosActivity::class.java))
        }
    }

    private fun cargarDetallesUsuario() {
        binding.textoNombre.text = manejoPreferencia.getString(Constantes.KEY_NOMBRE)
        val bytes = Base64.decode(manejoPreferencia.getString(Constantes.KEY_IMAGEN), Base64.DEFAULT)
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        binding.imagenPerfil.setImageBitmap(bitmap)
    }

    private fun mostrarToast(mensaje: String) {
        Toast.makeText(applicationContext, mensaje, Toast.LENGTH_SHORT).show()
    }
    private fun listenConversaciones() {
        database.collection(Constantes.KEY_CONVERSACIONES)
            .whereEqualTo(
                Constantes.KEY_ID_REMITENTE,
                manejoPreferencia.getString(Constantes.KEY_ID_USUARIO)
            )
            .addSnapshotListener(eventListener)
        database.collection(Constantes.KEY_CONVERSACIONES)
            .whereEqualTo(
                Constantes.KEY_ID_DESTINATARIO,
                manejoPreferencia.getString(Constantes.KEY_ID_USUARIO)
            )
            .addSnapshotListener(eventListener)
    }


    @SuppressLint("NotifyDataSetChanged")
    private val eventListener = EventListener<QuerySnapshot> { value, error ->
        if (error != null) {
            return@EventListener
        }
        if (value != null) {
            for (documentChange in value.documentChanges) {
                when (documentChange.type) {
                    DocumentChange.Type.ADDED -> {
                        val remitenteId = documentChange.document.getString(Constantes.KEY_ID_REMITENTE)
                        val destinatarioId = documentChange.document.getString(Constantes.KEY_ID_DESTINATARIO)
                        val chatMensaje = ChatMensaje()
                        chatMensaje.remitenteId = remitenteId
                        chatMensaje.destinatarioId = destinatarioId

                        if (manejoPreferencia.getString(Constantes.KEY_ID_USUARIO) == remitenteId) {
                            chatMensaje.conversacionImagen = documentChange.document.getString(Constantes.KEY_IMAGEN_DESTINATARIO)
                            chatMensaje.conversacionNombre = documentChange.document.getString(Constantes.KEY_NOMBRE_DESTINATARIO)
                            chatMensaje.conversacionId = documentChange.document.getString(Constantes.KEY_ID_DESTINATARIO)
                        } else {
                            chatMensaje.conversacionImagen = documentChange.document.getString(Constantes.KEY_IMAGEN_REMITENTE)
                            chatMensaje.conversacionNombre = documentChange.document.getString(Constantes.KEY_NOMBRE_REMITENTE)
                            chatMensaje.conversacionId = documentChange.document.getString(Constantes.KEY_ID_REMITENTE)
                        }

                        chatMensaje.mensaje = documentChange.document.getString(Constantes.KEY_ULTIMO_MENSAJE)
                        chatMensaje.dateObject = documentChange.document.getDate(Constantes.KEY_MARCATIEMPO)
                        conversaciones.add(chatMensaje)
                    }
                    DocumentChange.Type.MODIFIED -> {
                        for (i in 0 until conversaciones.size) {
                            val senderId = documentChange.document.getString(Constantes.KEY_ID_REMITENTE)
                            val receiverId = documentChange.document.getString(Constantes.KEY_ID_DESTINATARIO)

                            if (conversaciones[i].remitenteId == senderId && conversaciones[i].destinatarioId == receiverId) {
                                conversaciones[i].mensaje = documentChange.document.getString(Constantes.KEY_ULTIMO_MENSAJE)
                                conversaciones[i].dateObject = documentChange.document.getDate(Constantes.KEY_MARCATIEMPO)
                                break
                            }
                        }
                    }
                    else -> {
                        // Manejar otros tipos de cambios si es necesario
                    }
                }
            }

            conversaciones.sortByDescending { it.dateObject }
            adaptadorConversacion.notifyDataSetChanged()
            binding.recyclerConversacion.smoothScrollToPosition(0)
            binding.recyclerConversacion.visibility = View.VISIBLE
            binding.iconoCarga.visibility = View.GONE
        }
    }




    private fun getToken() {
        FirebaseMessaging.getInstance().token.addOnSuccessListener(this::actualizarToken)
    }

    private fun actualizarToken(token: String) {
        manejoPreferencia.putString(Constantes.KEY_FCM_TOKEN, token)
        val database = FirebaseFirestore.getInstance()
        val documentReference = database.collection(Constantes.KEY_USUARIOS)
            .document(manejoPreferencia.getString(Constantes.KEY_ID_USUARIO)!!)

        documentReference.update(Constantes.KEY_FCM_TOKEN, token)
            .addOnSuccessListener {

            }
            .addOnFailureListener {
                mostrarToast("No se ha podido actualizar el token")
            }
    }

    private fun logOut() {
        mostrarToast("Cerrando sesión...")
        val database = FirebaseFirestore.getInstance()
        val documentReference = database.collection(Constantes.KEY_USUARIOS)
            .document(manejoPreferencia.getString(Constantes.KEY_ID_USUARIO)!!)
        val actualizaciones = hashMapOf<String, Any>()
        actualizaciones[Constantes.KEY_FCM_TOKEN] = FieldValue.delete()
        documentReference.update(actualizaciones)
            .addOnSuccessListener {
                manejoPreferencia.clear()
                startActivity(Intent(applicationContext, InicioSesionActivity::class.java))
                finish()
            }
            .addOnFailureListener {
                mostrarToast("No se ha podido cerrar sesión")
            }
    }

    override fun onConversacionClicked(usuario: Usuario) {
        val intent = Intent(this, ChatActivity::class.java)
        intent.putExtra(Constantes.KEY_USUARIO, usuario)
        startActivity(intent)
    }


}