package com.example.chat.actividades

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.example.chat.databinding.ActivityRegistroBinding
import com.example.chat.utils.Constantes
import com.example.chat.utils.ManejoPreferencia
import com.google.firebase.firestore.FirebaseFirestore
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import java.io.InputStream

class RegistroActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegistroBinding;
    private lateinit var manejoPreferencia: ManejoPreferencia
    private var imagen: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistroBinding.inflate(layoutInflater);
        setContentView(binding.root)
        manejoPreferencia = ManejoPreferencia(applicationContext)
        setListeners()
    }

    private fun setListeners() {
        binding.textoIniciarSesion.setOnClickListener { onBackPressed() }
        binding.botonRegistro.setOnClickListener(){
            if (datosValidos()){
                registro()
            }
        }
        binding.imagenLayout.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            elegirImagen.launch(intent)
        }

    }

    private fun mostrarToast(mensaje: String) {
        Toast.makeText(applicationContext, mensaje, Toast.LENGTH_SHORT).show()
    }

    private fun registro() {
        cargando(true)
        val database = FirebaseFirestore.getInstance()
        val usuario = hashMapOf<String, Any>()
        usuario.put(Constantes.KEY_NOMBRE, binding.entradaNombre.text.toString())
        usuario.put(Constantes.KEY_EMAIL, binding.entradaEmail.text.toString())
        usuario.put(Constantes.KEY_PASSWORD, binding.entradaPassword.text.toString())
        usuario.put(Constantes.KEY_IMAGEN, imagen!!)
        database.collection(Constantes.KEY_USUARIOS)
            .add(usuario)
            .addOnSuccessListener {
                cargando(false)
                manejoPreferencia.putBoolean(Constantes.KEY_ESTA_LOGEADO, true)
                manejoPreferencia.putString(Constantes.KEY_ID_USUARIO, it.id)
                manejoPreferencia.putString(Constantes.KEY_NOMBRE, binding.entradaNombre.text.toString())
                manejoPreferencia.putString(Constantes.KEY_IMAGEN, imagen!!)
                val intent = Intent(applicationContext, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
            }
            .addOnFailureListener {
                cargando(false)
                mostrarToast(it.message.toString())
            }
    }

    fun cargando(estaCargando: Boolean) {
        if (estaCargando) {
            binding.botonRegistro.visibility = View.INVISIBLE
            binding.iconoCarga.visibility = View.VISIBLE
        } else {
            binding.botonRegistro.visibility = View.VISIBLE
            binding.iconoCarga.visibility = View.INVISIBLE
        }
    }
    private fun imagenCarga(bitmap: Bitmap): String {
        val preAncho = 500
        val preAlto = bitmap.height - preAncho / bitmap.width
        val preBitmap = Bitmap.createScaledBitmap(bitmap, preAncho, preAlto, false)
        val byteArrayOutputStream = ByteArrayOutputStream()
        preBitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream)
        val bytes = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(bytes, Base64.DEFAULT)

    }

    private val elegirImagen = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { imagenUri ->
                try {
                    val inputStream: InputStream? = contentResolver.openInputStream(imagenUri)
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    binding.imagenPerfil.setImageBitmap(bitmap)
                    binding.textoEditarImagen.visibility= View.GONE
                    imagen = imagenCarga(bitmap)

                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                }
            }
        }
    }


    private fun datosValidos(): Boolean {
        if (imagen == null) {
            mostrarToast("Seleccione una imagen")
            return false
        } else if (binding.entradaNombre.text.toString().trim().isEmpty()) {
            mostrarToast("Introduzca nombre")
            return false
        } else if (binding.entradaEmail.text.toString().trim().isEmpty()) {
            mostrarToast("Introduzca email")
            return false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(binding.entradaEmail.text.toString())
                .matches()
        ) {
            mostrarToast("Formato de correo electrónico inválido")
            return false
        } else if (binding.entradaPassword.text.toString().trim().isEmpty()) {
            mostrarToast("Introduzca contraseña")
            return false
        } else if (binding.entradaConfirmarPassword.text.toString().trim().isEmpty()) {
            mostrarToast("Confirma contraseña")
            return false
        } else if (!binding.entradaPassword.text.toString()
                .equals(binding.entradaConfirmarPassword.text.toString())
        ) {
            mostrarToast("Las contraseñas deben ser iguales")
            return false
        } else {
            return true
        }
    }

}

