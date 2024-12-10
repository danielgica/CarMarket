package com.proyecto.carmarket.Activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
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
    private lateinit var suPerfilCorreo: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_su_perfil)

        suPerfilFoto = findViewById(R.id.suPerfil_foto)
        suPerfilNombre = findViewById(R.id.suPerfil_nombre)
        suPerfilLocalidad = findViewById(R.id.suPerfil_localidad)
        suPerfilNumero = findViewById(R.id.suPerfil_numero)
        suPerfilFecha = findViewById(R.id.suPerfil_fecha)
        suPerfilCorreo = findViewById(R.id.suPerfil_correo)

        val email = intent.getStringExtra("email")
        ventanaAnterior = intent.getStringExtra("ventanaAnterior").toString()

        if (email != null) {
            cargarDatosUsuario(email)
        } else {
            Toast.makeText(this, "No se ha proporcionado un email válido", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.suPerfil_enviaMensaje).setOnClickListener {
            val builder = android.app.AlertDialog.Builder(this)
            builder.setTitle("Enviar Mensaje")

            val layout = LinearLayout(this)
            layout.orientation = LinearLayout.VERTICAL
            layout.setPadding(32, 32, 32, 32)

            val asuntoInput = EditText(this).apply {
                hint = "Asunto (máx. 40 caracteres)"
                inputType = android.text.InputType.TYPE_CLASS_TEXT
                filters = arrayOf(android.text.InputFilter.LengthFilter(40))
            }
            layout.addView(asuntoInput)

            val mensajeInput = EditText(this).apply {
                hint = "Escribe tu mensaje"
                inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE
                setLines(4)
                maxLines = 6
                scrollBarStyle = View.SCROLLBARS_INSIDE_INSET
            }
            layout.addView(mensajeInput)

            builder.setView(layout)

            builder.setPositiveButton("Enviar") { _, _ ->
                val asunto = asuntoInput.text.toString().trim()
                val mensaje = mensajeInput.text.toString().trim()

                if (asunto.isEmpty() || asunto.length > 40) {
                    showAlert("Error", "El asunto debe tener entre 1 y 40 caracteres.")
                    return@setPositiveButton
                }
                if (mensaje.isEmpty()) {
                    showAlert("Error", "El mensaje no puede estar vacío.")
                    return@setPositiveButton
                }

                val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
                val fechaActual = sdf.format(java.util.Date())

                val emailReceptor = intent.getStringExtra("email") ?: ""
                if (emailReceptor.isEmpty()) {
                    showAlert("Error", "No se pudo enviar el mensaje. Email del receptor no disponible.")
                    return@setPositiveButton
                }

                val mensajeObj = hashMapOf(
                    "asunto" to asunto,
                    "borradoEmisor" to false,
                    "borradoReceptor" to false,
                    "emailReceptor" to emailReceptor,
                    "emailEmisor" to MainActivity.email,
                    "fecha" to fechaActual,
                    "leido" to false,
                    "mensaje" to mensaje
                )

                val db = FirebaseFirestore.getInstance()
                db.collection("mensajes")
                    .add(mensajeObj)
                    .addOnSuccessListener {
                        showAlert("Éxito", "Mensaje enviado correctamente.")
                    }
                    .addOnFailureListener {
                        showAlert("Error", "No se pudo enviar el mensaje. Inténtalo más tarde.")
                    }
            }

            builder.setNegativeButton("Cancelar", null)

            builder.show()
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
            if(ventanaAnterior.equals("anuncioAdmin")){
                val intent = Intent(this, VerAnuncioAdmin::class.java)
                intent.putExtra("ID_ANUNCIO", MainActivity.idAnuncio1)
                startActivity(intent)
                finish()
                progressBar.visibility = View.GONE
            }
            if(ventanaAnterior.equals("mensajes")){
                val intent = Intent(this, TusMensajes::class.java)
                startActivity(intent)
                finish()
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
        suPerfilCorreo.text = email

        db.collection("personas").document(email).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    document.getString("nombre")?.let { suPerfilNombre.text = it }
                    document.getString("localidad")?.let { suPerfilLocalidad.text = it }
                    document.getString("numero")?.let { suPerfilNumero.text = it }
                    document.getString("fechaNacimiento")?.let { suPerfilFecha.text = it }
                    val isAdmin = document.getBoolean("admin") ?: false
                    if(!isAdmin){
                        findViewById<ImageView>(R.id.suPerfil_verificado).visibility = View.GONE
                    }
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
                    .circleCrop()
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
