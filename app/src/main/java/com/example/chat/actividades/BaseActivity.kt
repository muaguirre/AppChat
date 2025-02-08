package com.example.chat.actividades

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.chat.utils.Constantes
import com.example.chat.utils.ManejoPreferencia
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore

open class BaseActivity : AppCompatActivity() {

    private lateinit var documentReference: DocumentReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val manejoPreferencia = ManejoPreferencia(applicationContext)
        val database = FirebaseFirestore.getInstance()
        documentReference = database.collection(Constantes.KEY_USUARIOS)
            .document(manejoPreferencia.getString(Constantes.KEY_ID_USUARIO) ?: "")
    }

    override fun onPause() {
        super.onPause()
        documentReference.update(Constantes.KEY_ENLINEA, 0)
    }

    override fun onResume() {
        super.onResume()
        documentReference.update(Constantes.KEY_ENLINEA, 1)
    }
}
