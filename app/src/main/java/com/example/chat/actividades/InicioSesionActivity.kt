package com.example.chat.actividades

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import com.example.chat.databinding.ActivityInicioSesionBinding
import com.example.chat.utils.Constantes
import com.example.chat.utils.ManejoPreferencia
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

class InicioSesionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInicioSesionBinding
    private lateinit var manejoPreferencia: ManejoPreferencia

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInicioSesionBinding.inflate(layoutInflater);
        setContentView(binding.root)
        manejoPreferencia = ManejoPreferencia(applicationContext)
        setListeners()
        if (manejoPreferencia.getBoolean(Constantes.KEY_ESTA_LOGEADO)) {
            startActivity(Intent(applicationContext, MainActivity::class.java))
            startActivity(intent)
            finish()
        }
    }


    private fun setListeners() {
        binding.textoCrearCuenta.setOnClickListener {
            startActivity(Intent(applicationContext, RegistroActivity::class.java))
        }
        binding.botonInicioSesion.setOnClickListener(){
            if (datosValidos()){
                iniciarSesion()
            }
        }
    }

    private fun iniciarSesion() {
        cargando(true)
        val database = FirebaseFirestore.getInstance()
        database.collection(Constantes.KEY_USUARIOS)
            .whereEqualTo(Constantes.KEY_EMAIL, binding.entradaEmail.text.toString())
            .whereEqualTo(Constantes.KEY_PASSWORD, binding.entradaPassword.text.toString())
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful && task.result != null && task.result!!.documents.size > 0) {
                    val documentSnapshot = task.result!!.documents[0]
                    manejoPreferencia.putBoolean(Constantes.KEY_ESTA_LOGEADO, true)
                    manejoPreferencia.putString(Constantes.KEY_ID_USUARIO, documentSnapshot.id)
                    manejoPreferencia.putString(Constantes.KEY_NOMBRE, documentSnapshot.getString(Constantes.KEY_NOMBRE)!!)
                    manejoPreferencia.putString(Constantes.KEY_IMAGEN, documentSnapshot.getString(Constantes.KEY_IMAGEN)!!)
                    val intent = Intent(applicationContext, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(intent)
                } else {
                    cargando(false)
                    mostrarToast("Error al iniciar sesión")
                }
            }
    }


    fun cargando(estaCargando: Boolean) {
        if (estaCargando) {
            binding.botonInicioSesion.visibility = View.INVISIBLE
            binding.iconoCarga.visibility = View.VISIBLE
        } else {
            binding.botonInicioSesion.visibility = View.VISIBLE
            binding.iconoCarga.visibility = View.INVISIBLE
        }
    }

    private fun mostrarToast(mensaje: String) {
        Toast.makeText(applicationContext, mensaje, Toast.LENGTH_SHORT).show()
    }

    private fun datosValidos(): Boolean {
        if (binding.entradaEmail.text.toString().trim().isEmpty()) {
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
        } else {
            return true
        }
    }
}