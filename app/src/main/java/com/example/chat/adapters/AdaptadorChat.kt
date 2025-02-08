package com.example.chat.adapters

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.chat.databinding.ContenedorMensajeEnviadoBinding
import com.example.chat.databinding.ContenedorMensajeRecibidoBinding
import com.example.chat.models.ChatMensaje

class AdaptadorChat(
    private var destinatarioImagenPerfil: Bitmap,
    private val chatMensajes: List<ChatMensaje>,
    private val remitenteId: String
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val VIEW_TIPO_ENVIADO: Int = 1
    val VIEW_TIPO_RECIBIDO: Int = 2

    fun setDestinatarioImagenPerfil(bitmap: Bitmap) {
        destinatarioImagenPerfil = bitmap
    }

    inner class MensajeEnviadoViewHolder(private val binding: ContenedorMensajeEnviadoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun setData(chatMensaje: ChatMensaje) {
            binding.textoMensaje.text = chatMensaje.mensaje
            binding.textoFecha.text = chatMensaje.dateTime
        }
    }

    inner class MensajeRecibidoViewHolder(private val binding: ContenedorMensajeRecibidoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun setData(chatMensaje: ChatMensaje) {
            binding.textoMensaje.text = chatMensaje.mensaje
            binding.textoFecha.text = chatMensaje.dateTime
            if (destinatarioImagenPerfil != null){
            binding.imagenPerfil.setImageBitmap(destinatarioImagenPerfil)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TIPO_ENVIADO -> {
                val binding = ContenedorMensajeEnviadoBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                MensajeEnviadoViewHolder(binding)
            }
            VIEW_TIPO_RECIBIDO -> {
                val binding = ContenedorMensajeRecibidoBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                MensajeRecibidoViewHolder(binding)
            }
            else -> throw IllegalArgumentException("Tipo de vista no válido")
        }
    }

    override fun getItemCount(): Int {
        return chatMensajes.size
    }

    override fun getItemViewType(position: Int): Int {
        val chatMensaje = chatMensajes[position]
        return if (chatMensaje.remitenteId == remitenteId) {
            VIEW_TIPO_ENVIADO
        } else {
            VIEW_TIPO_RECIBIDO
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val chatMensaje = chatMensajes[position]
        when (holder) {
            is MensajeEnviadoViewHolder -> {
                holder.setData(chatMensaje)
            }
            is MensajeRecibidoViewHolder -> {
                holder.setData(chatMensaje)
            }
        }
    }
}
