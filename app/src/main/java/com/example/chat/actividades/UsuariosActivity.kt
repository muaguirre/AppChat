package com.example.chat.actividades

import AdaptadorUsuario
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.example.chat.databinding.ActivityUsuariosBinding
import com.example.chat.listeners.UsuarioListener
import com.example.chat.models.Usuario
import com.example.chat.utils.Constantes
import com.example.chat.utils.ManejoPreferencia
import com.google.firebase.firestore.FirebaseFirestore

class UsuariosActivity : BaseActivity(), UsuarioListener{

    private lateinit var binding: ActivityUsuariosBinding
    private lateinit var manejoPreferencia: ManejoPreferencia

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUsuariosBinding.inflate(layoutInflater);
        setContentView(binding.root)
        manejoPreferencia = ManejoPreferencia(applicationContext)
        setListeners()
        getUsers()
    }

    private fun setListeners() {
        binding.iconoBack.setOnClickListener(){
            onBackPressed()
        }
    }

    private fun getUsers() {
        cargando(true)
        val database = FirebaseFirestore.getInstance()
        database.collection(Constantes.KEY_USUARIOS)
            .get()
            .addOnCompleteListener { task ->
                cargando(false)
                val usuarioActual = manejoPreferencia.getString(Constantes.KEY_NOMBRE)
                if (task.isSuccessful && task.result != null) {
                    val usuarios = mutableListOf<Usuario>()
                    for (queryDocumentSnapshot in task.result!!) {
                        if (usuarioActual == queryDocumentSnapshot.id) {
                            continue
                        }
                        var usuario: Usuario = Usuario(
                            nombre = queryDocumentSnapshot.getString(Constantes.KEY_NOMBRE)!!,
                            email = queryDocumentSnapshot.getString(Constantes.KEY_EMAIL)!!,
                            imagen = queryDocumentSnapshot.getString(Constantes.KEY_IMAGEN)!!,
                            token = queryDocumentSnapshot.getString(Constantes.KEY_FCM_TOKEN)!!,
                            id = queryDocumentSnapshot.id
                        )
                        usuarios.add(usuario)
                    }
                    if (usuarios.isNotEmpty()) {
                        val usuarioAdapter = AdaptadorUsuario(usuarios, this)
                        binding.recyclerUsuarios.adapter = usuarioAdapter
                        binding.recyclerUsuarios.visibility = View.VISIBLE
                    } else {
                        mostrarMensajeError()
                    }
                } else {
                    mostrarMensajeError()
                }
            }
    }


    private fun mostrarMensajeError(){
        binding.textoErrorMensaje.text = String.format("%s", "Usuario no disponible")
        binding.textoErrorMensaje.visibility = View.VISIBLE
    }

    private fun cargando(estaCargando: Boolean) {
        if (estaCargando) {
            binding.iconoCarga.visibility = View.VISIBLE
        } else {
            binding.iconoCarga.visibility = View.INVISIBLE
        }
    }

    override fun onUserClicked(usuario: Usuario) {
        val intent = Intent(applicationContext, ChatActivity::class.java)
        intent.putExtra(Constantes.KEY_USUARIO, usuario)
        startActivity(intent)
        finish()
    }
}