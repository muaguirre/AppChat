package com.example.chat.adapters

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.chat.databinding.ContenedorConversacionRecienteBinding
import com.example.chat.listeners.ConversacionListener
import com.example.chat.models.ChatMensaje
import com.example.chat.models.Usuario

class AdaptadorConversacionReciente(
    private val mensajesChat: List<ChatMensaje>,
    private val escuchadorConversacion: ConversacionListener
) : RecyclerView.Adapter<AdaptadorConversacionReciente.VistaConversacionHolder>(){



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VistaConversacionHolder {
        val vistaBinding = ContenedorConversacionRecienteBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return VistaConversacionHolder(vistaBinding)
    }

    override fun onBindViewHolder(holder: VistaConversacionHolder, position: Int) {
        holder.establecerDatos(mensajesChat[position])
    }

    override fun getItemCount(): Int {
        return mensajesChat.size
    }

    inner class VistaConversacionHolder(private val binding: ContenedorConversacionRecienteBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun establecerDatos(mensajeChat: ChatMensaje) {
            val conversacionImagen = mensajeChat.conversacionImagen
            if (conversacionImagen != null) {
                binding.imagenPerfil.setImageBitmap(obtenerImagenConversacion(conversacionImagen))
            }
            binding.textoNombre.text = mensajeChat.conversacionNombre ?: ""
            binding.textoMensajeReciente.text = mensajeChat.mensaje ?: ""
            binding.root.setOnClickListener {
                val usuario = Usuario().apply {
                    id = mensajeChat.conversacionId ?: ""
                    nombre = mensajeChat.conversacionNombre ?: ""
                    imagen = mensajeChat.conversacionImagen ?: ""
                }
                escuchadorConversacion.onConversacionClicked(usuario)
            }
        }

    }


        private fun obtenerImagenConversacion(imagenCodificada: String): Bitmap {
        val bytes = Base64.decode(imagenCodificada, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }
}
