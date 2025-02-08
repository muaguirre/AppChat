import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.chat.databinding.ContenedorUsuarioBinding
import com.example.chat.listeners.UsuarioListener
import com.example.chat.models.Usuario

class AdaptadorUsuario(private val usuarios: List<Usuario>, private val usuarioListener: UsuarioListener) :
    RecyclerView.Adapter<AdaptadorUsuario.VistaUsuarioHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VistaUsuarioHolder {
        val binding =
            ContenedorUsuarioBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VistaUsuarioHolder(binding)
    }

    override fun onBindViewHolder(holder: VistaUsuarioHolder, position: Int) {
        val usuario = usuarios[position]
        holder.setDataUsuario(usuario)
    }

    override fun getItemCount(): Int {
        return usuarios.size
    }

    inner class VistaUsuarioHolder(private val binding: ContenedorUsuarioBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun cargarImagenUsuario(imagen: String) {
            val bytes = Base64.decode(imagen, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            binding.imagenPerfil.setImageBitmap(bitmap)
        }

        fun setDataUsuario(usuario: Usuario) {
            binding.textoNombre.text = usuario.nombre
            binding.textoEmail.text = usuario.email
            cargarImagenUsuario(usuario.imagen!!)
            binding.root.setOnClickListener{
                usuarioListener.onUserClicked(usuario)
            }
        }
    }
}
