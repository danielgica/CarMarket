package com.proyecto.carmarket.Activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.proyecto.carmarket.R

class SuPerfil : AppCompatActivity() {

    private lateinit var suPerfilFoto: ImageView
    private lateinit var suPerfilNombre: TextView
    private lateinit var suPerfilLocalidad: TextView
    private lateinit var suPerfilNumero: TextView
    private lateinit var suPerfilFecha: TextView
    private lateinit var ventanaAnterior: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_su_perfil)

        suPerfilFoto = findViewById(R.id.suPerfil_foto)
        suPerfilNombre = findViewById(R.id.suPerfil_nombre)
        suPerfilLocalidad = findViewById(R.id.suPerfil_localidad)
        suPerfilNumero = findViewById(R.id.suPerfil_numero)
        suPerfilFecha = findViewById(R.id.suPerfil_fecha)

        val email = intent.getStringExtra("email")
        ventanaAnterior = intent.getStringExtra("ventanaAnterior").toString()

        if (email != null) {
            cargarDatosUsuario(email)
        } else {
            Toast.makeText(this, "No se ha proporcionado un email válido", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.suPerfil_enviaMensaje).setOnClickListener {

        }

        findViewById<ImageView>(R.id.suPerfil_volver).setOnClickListener {
            val progressBar = findViewById<ProgressBar>(R.id.suPerfil_progressBar)
            progressBar.visibility = View.VISIBLE
            if(ventanaAnterior.equals("anuncio")){
                val intent = Intent(this, VerAnuncio::class.java)
                intent.putExtra("ID_ANUNCIO", MainActivity.idAnuncio1)
                startActivity(intent)
                finish()
                progressBar.visibility = View.GONE
            }
            else{

            }

            progressBar.visibility = View.GONE
        }
    }


    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {

    }

    private fun cargarDatosUsuario(email: String) {
        val db = FirebaseFirestore.getInstance()
        val storage = FirebaseStorage.getInstance()

        // Cargar los datos de Firestore
        db.collection("personas").document(email).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    document.getString("nombre")?.let { suPerfilNombre.text = it }
                    document.getString("localidad")?.let { suPerfilLocalidad.text = it }
                    document.getString("numero")?.let { suPerfilNumero.text = it }
                    document.getString("fechaNacimiento")?.let { suPerfilFecha.text = it }
                }
            }
            .addOnFailureListener { e ->
                showAlert("Error", "Error al cargar los datos: ${e.message}")
            }

        val nombreArchivo = "$email.jpg"
        val rutaImagen = "personas/$nombreArchivo"
        val storageReference = storage.reference.child(rutaImagen)

        storageReference.downloadUrl
            .addOnSuccessListener { uri ->
                Glide.with(this)
                    .load(uri.toString())
                    .placeholder(R.drawable.perfil)
                    .error(R.drawable.perfil)
                    .into(suPerfilFoto)
            }
            .addOnFailureListener {
                suPerfilFoto.setImageResource(R.drawable.perfil)
            }
    }

    private fun showAlert(titulo: String, mensaje: String) {
        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle(titulo)
        builder.setMessage(mensaje)
        builder.setPositiveButton("Aceptar", null)
        builder.show()
    }
}
