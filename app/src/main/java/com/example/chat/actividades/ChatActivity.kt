package com.example.chat.actividades

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.example.chat.adapters.AdaptadorChat
import com.example.chat.databinding.ActivityChatBinding
import com.example.chat.models.ChatMensaje
import com.example.chat.models.Usuario
import com.example.chat.red.ApiCliente
import com.example.chat.red.ApiServicio
import com.example.chat.utils.Constantes
import com.example.chat.utils.ManejoPreferencia
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.Firebase
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.storage.storage
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class ChatActivity : BaseActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var usuarioRecibido: Usuario
    private val chatMensajes = mutableListOf<ChatMensaje>()
    private lateinit var adaptadorChat: AdaptadorChat
    private lateinit var manejoPreferencia: ManejoPreferencia
    private lateinit var database: FirebaseFirestore
    private var conversacionId: String? = null
    private var destinatarioEnLinea: Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setListeners()
        cargarDatosRecibidos()
        init()
        escucharMensajes()
    }

    private fun init() {
        manejoPreferencia = ManejoPreferencia(applicationContext)
        adaptadorChat = AdaptadorChat(
            cargarImagen(usuarioRecibido.imagen!!)!!,
            chatMensajes,
            manejoPreferencia.getString(Constantes.KEY_ID_USUARIO) ?: "")

        binding.chatRecycler.adapter = adaptadorChat
        database = FirebaseFirestore.getInstance()
    }

    fun enviarMensaje() {
        val message = hashMapOf(
            Constantes.KEY_ID_REMITENTE to manejoPreferencia.getString(Constantes.KEY_ID_USUARIO),
            Constantes.KEY_ID_DESTINATARIO to usuarioRecibido.id,
            Constantes.KEY_MENSAJE to binding.entradaMensaje.text.toString(),
            Constantes.KEY_MARCATIEMPO to Date()
        )

        database.collection(Constantes.KEY_CHAT).add(message)

        if (conversacionId != null) {
            actualizacionConversacion(binding.entradaMensaje.text.toString())
        } else {
            val conversacion = hashMapOf<String, Any?>(
                Constantes.KEY_ID_REMITENTE to manejoPreferencia.getString(Constantes.KEY_ID_USUARIO),
                Constantes.KEY_NOMBRE_REMITENTE to manejoPreferencia.getString(Constantes.KEY_NOMBRE),
                Constantes.KEY_IMAGEN_REMITENTE to manejoPreferencia.getString(Constantes.KEY_IMAGEN),
                Constantes.KEY_ID_DESTINATARIO to usuarioRecibido.id,
                Constantes.KEY_NOMBRE_DESTINATARIO to usuarioRecibido.nombre,
                Constantes.KEY_IMAGEN_DESTINATARIO to usuarioRecibido.imagen,
                Constantes.KEY_ULTIMO_MENSAJE to binding.entradaMensaje.text.toString(),
                Constantes.KEY_MARCATIEMPO to Date()
            )
            anadirConversacion(conversacion)
        }
        if (!destinatarioEnLinea) {
            try {
                val tokens = JSONArray()
                tokens.put(usuarioRecibido.token)

                val data = JSONObject()
                data.put(Constantes.KEY_ID_USUARIO, manejoPreferencia.getString(Constantes.KEY_ID_USUARIO))
                    data.put(Constantes.KEY_NOMBRE, manejoPreferencia.getString(Constantes.KEY_NOMBRE))
                data.put(Constantes.KEY_FCM_TOKEN, manejoPreferencia.getString(Constantes.KEY_FCM_TOKEN))
                data.put(Constantes.KEY_MENSAJE, binding.entradaMensaje.text.toString())

                val body = JSONObject()
                body.put(Constantes.MENSAJE_DATA, data)
                body.put(Constantes.MENSAJE_REGISTRO_IDS, tokens)

                enviarNotificacion(body.toString())
            } catch (exception: Exception) {
                mostrarToast(exception.message.toString())
            }
        }
        binding.entradaMensaje.text = null
    }

    private fun mostrarToast(mensaje: String) {
        Toast.makeText(applicationContext, mensaje, Toast.LENGTH_SHORT).show()
    }

    private fun enviarNotificacion(mensajeBody: String) {
        val apiService = ApiCliente.getClient().create(ApiServicio::class.java)
        val call = apiService.sendMessage(Constantes.getCabecerasMensajes(), mensajeBody)

        call.enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.isSuccessful) {
                    try {
                        if (response.body() != null) {
                            val responseJson = JSONObject(response.body())
                            val results = responseJson.getJSONArray("results")
                            if (responseJson.getInt("failure") == 1) {
                                val error = results.getJSONObject(0)
                                mostrarToast(error.getString("error"))
                                return
                            }
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                    mostrarToast("Notification sent successfully")
                } else {
                    mostrarToast("Error: " + response.code())
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                mostrarToast(t.message.toString())
            }
        })
    }

    private fun escuchadorDestinatarioEnLinea() {
        database.collection(Constantes.KEY_USUARIOS)
            .document(usuarioRecibido.id!!)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                if (value != null) {
                    val availability = value.getLong(Constantes.KEY_ENLINEA)?.toInt() ?: 0
                    destinatarioEnLinea = availability == 1

                    usuarioRecibido.token = value.getString(Constantes.KEY_FCM_TOKEN)

                    if (usuarioRecibido.imagen == null) {
                        usuarioRecibido.imagen = value.getString(Constantes.KEY_IMAGEN)
                        adaptadorChat.setDestinatarioImagenPerfil(cargarImagen(usuarioRecibido.imagen!!)!!)
                        adaptadorChat.notifyItemRangeChanged(0, chatMensajes.size)
                    }
                }

                if (destinatarioEnLinea) {
                    binding.textEnLinea.visibility = View.VISIBLE
                } else {
                    binding.textEnLinea.visibility = View.GONE
                }
            }
    }


    private fun escucharMensajes() {
        val senderQuery = database.collection(Constantes.KEY_CHAT)
            .whereEqualTo(Constantes.KEY_ID_REMITENTE, manejoPreferencia.getString(Constantes.KEY_ID_USUARIO))
            .whereEqualTo(Constantes.KEY_ID_DESTINATARIO, usuarioRecibido.id)

        val receiverQuery = database.collection(Constantes.KEY_CHAT)
            .whereEqualTo(Constantes.KEY_ID_REMITENTE, usuarioRecibido.id)
            .whereEqualTo(Constantes.KEY_ID_DESTINATARIO, manejoPreferencia.getString(Constantes.KEY_ID_USUARIO))

        senderQuery.addSnapshotListener(eventListener)
        receiverQuery.addSnapshotListener(eventListener)
    }

    val eventListener = EventListener<QuerySnapshot> { value, error ->
        if (error != null) {
            return@EventListener
        }
        if (value != null) {
            val count = chatMensajes.size
            for (documentChange in value.documentChanges) {
                if (documentChange.type == DocumentChange.Type.ADDED) {
                    val chatMensaje = ChatMensaje(
                        remitenteId = documentChange.document.getString(Constantes.KEY_ID_REMITENTE) ?: "",
                        destinatarioId = documentChange.document.getString(Constantes.KEY_ID_DESTINATARIO) ?: "",
                        mensaje = documentChange.document.getString(Constantes.KEY_MENSAJE) ?: "",
                        dateTime = getFecha(documentChange.document.getDate(Constantes.KEY_MARCATIEMPO)!!),
                        dateObject = documentChange.document.getDate(Constantes.KEY_MARCATIEMPO) ?: Date()
                    )

                    chatMensajes.add(chatMensaje)
                }
            }
            chatMensajes.sortBy { it.dateObject }
            if (count == 0) {
                adaptadorChat.notifyDataSetChanged()
            } else {
                adaptadorChat.notifyItemRangeInserted(chatMensajes.size, chatMensajes.size)
                binding.chatRecycler.smoothScrollToPosition(chatMensajes.size - 1)
            }
            binding.chatRecycler.visibility = View.VISIBLE
        }
        binding.iconoCarga.visibility = View.GONE

        if (conversacionId == null) {
            checkForConversacion()
        }
    }

    private fun cargarImagen(imagen: String): Bitmap? {
        if (imagen != null) {
            val bytes = Base64.decode(imagen, Base64.DEFAULT)
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        } else {
            return null
        }
    }

    private fun cargarDatosRecibidos() {
        usuarioRecibido = intent.getSerializableExtra(Constantes.KEY_USUARIO) as Usuario
        binding.textoNombre.text = usuarioRecibido.nombre
    }

    private fun setListeners() {
        binding.iconBack.setOnClickListener {
            onBackPressed()
        }
        binding.layoutEnviar.setOnClickListener {
            enviarMensaje()
        }
    }
    /*private val archivoLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            result?.data?.data?.let {
                currentFile = it
                subirArchivoStorage("1")
            }
        } else {
            mostrarToast("Cancelar")
        }
    }*/

    /*private fun subirArchivoStorage(filename: String){
        try {
            currentFile?.let {
                archivoReference.child("images/$filename").putFile(it).addOnSuccessListener {
                    mostrarToast("Subida exitosa $currentFile")

                } .addOnFailureListener{
                    mostrarToast("Error en subida")
                }
            }
        } catch (e: Exception){
            mostrarToast(e.toString())
        }
    }*/

    private fun getFecha(date: Date): String {
        val dateFormat = SimpleDateFormat("MMMM dd, yyyy - hh:mm a", Locale.getDefault())
        return dateFormat.format(date)
    }

    private fun anadirConversacion(conversacion: HashMap<String, Any?>) {
        database.collection(Constantes.KEY_CONVERSACIONES)
            .add(conversacion)
            .addOnSuccessListener { documentReference ->
                conversacionId = documentReference.id
            }
            .addOnFailureListener { e ->
                println("Error al agregar la conversación: $e")
            }
    }

    private fun actualizacionConversacion(message: String) {
        val documentReference =
            database.collection(Constantes.KEY_CONVERSACIONES).document(conversacionId ?: "")
        documentReference.update(
            Constantes.KEY_ULTIMO_MENSAJE, message,
            Constantes.KEY_MARCATIEMPO, Date()
        )
    }

    private fun checkForConversacion() {
        if (chatMensajes.isNotEmpty()) {
            checkForConversacionRemotamente(
                manejoPreferencia.getString(Constantes.KEY_ID_USUARIO) ?: "",
                usuarioRecibido.id!!
            )
            checkForConversacionRemotamente(
                usuarioRecibido.id!!,
                manejoPreferencia.getString(Constantes.KEY_ID_USUARIO) ?: ""
            )
        }
    }

    private fun checkForConversacionRemotamente(remitenteId: String, destinatarioId: String) {
        database.collection(Constantes.KEY_CONVERSACIONES)
            .whereEqualTo(Constantes.KEY_ID_REMITENTE, remitenteId)
            .whereEqualTo(Constantes.KEY_ID_DESTINATARIO, destinatarioId)
            .get()
            .addOnCompleteListener(conversationOnCompleteListener)
    }

    private val conversationOnCompleteListener = OnCompleteListener<QuerySnapshot> { task ->
        if (task.isSuccessful && task.result != null && task.result!!.documents.size > 0) {
            val documentSnapshot = task.result!!.documents[0]
            conversacionId = documentSnapshot.id
        }
    }

    override fun onResume() {
        super.onResume()
        escuchadorDestinatarioEnLinea()
    }
}
