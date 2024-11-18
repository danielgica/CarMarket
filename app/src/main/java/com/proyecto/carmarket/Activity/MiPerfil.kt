package com.proyecto.carmarket.Activity

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.proyecto.carmarket.R
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

class MiPerfil : AppCompatActivity() {

    private lateinit var miPerfilFoto: ImageView
    private lateinit var miPerfilNombre: TextView
    private lateinit var miPerfilLocalidad: TextView
    private lateinit var miPerfilNumero: TextView
    private lateinit var miPerfilFecha: TextView
    private val GALLERY_REQUEST_CODE = 100
    var imagenSeleccionada = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mi_perfil)

        miPerfilFoto = findViewById(R.id.miPerfil_foto)
        miPerfilNombre = findViewById(R.id.miPerfil_nombre)
        miPerfilLocalidad = findViewById(R.id.miPerfil_localidad)
        miPerfilNumero = findViewById(R.id.miPerfil_numero)
        miPerfilFecha = findViewById(R.id.miPerfil_fecha)

        findViewById<ImageView>(R.id.miPerfil_volver).setOnClickListener {
            val progressBar = findViewById<ProgressBar>(R.id.miPerfil_progressBar)
            progressBar.visibility = View.VISIBLE
            startActivity(Intent(this, MenuActivity::class.java))
            finish()
            progressBar.visibility = View.GONE
        }

        findViewById<ImageView>(R.id.miPerfil_editaFoto).setOnClickListener {
            val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(galleryIntent, GALLERY_REQUEST_CODE)
            imagenSeleccionada = true
        }

        findViewById<ImageView>(R.id.miPerfil_editaNombre).setOnClickListener {
            showInputDialog("Editar Nombre", miPerfilNombre)
        }

        findViewById<ImageView>(R.id.miPerfil_editaLocalidad).setOnClickListener {
            showInputDialog("Editar Localidad", miPerfilLocalidad)
        }

        findViewById<ImageView>(R.id.miPerfil_editaNumero).setOnClickListener {
            showInputDialog("Editar Número", miPerfilNumero, true)
        }

        findViewById<ImageView>(R.id.miPerfil_editaFecha).setOnClickListener {
            showDatePickerDialog()
        }

        findViewById<Button>(R.id.miPerfil_desconectarse).setOnClickListener {
            val progressBar = findViewById<ProgressBar>(R.id.miPerfil_progressBar)
            progressBar.visibility = View.VISIBLE
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            progressBar.visibility = View.GONE
        }

        findViewById<Button>(R.id.miPerfil_actualizar).setOnClickListener {
            actualizarDatosUsuario()
        }

        cargarDatosUsuario()
    }

    private fun cargarDatosUsuario() {
        val email = MainActivity.email ?: return
        val db = FirebaseFirestore.getInstance()
        val storage = FirebaseStorage.getInstance()

        db.collection("personas").document(email).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    document.getString("nombre")?.let { miPerfilNombre.text = it }
                    document.getString("localidad")?.let { miPerfilLocalidad.text = it }
                    document.getString("numero")?.let { miPerfilNumero.text = it }
                    document.getString("fechaNacimiento")?.let { miPerfilFecha.text = it }
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
                    .into(miPerfilFoto)
                imagenSeleccionada = true
            }
            .addOnFailureListener {
                miPerfilFoto.setImageResource(R.drawable.perfil) // Imagen por defecto en caso de error
            }
    }

    private fun actualizarDatosUsuario() {
        val progressBar = findViewById<ProgressBar>(R.id.miPerfil_progressBar)
        progressBar.visibility = View.VISIBLE
        val email = MainActivity.email ?: return
        val db = FirebaseFirestore.getInstance()



        val updates = mutableMapOf<String, Any?>()
        var isImageUpdated = false

        if (miPerfilNombre.text.toString() != "Nombre por defecto") {
            updates["nombre"] = miPerfilNombre.text.toString()
        }

        if (miPerfilLocalidad.text.toString() != "Localidad por defecto") {
            updates["localidad"] = miPerfilLocalidad.text.toString()
        }

        if (miPerfilNumero.text.toString() != "Número por defecto") {
            updates["numero"] = miPerfilNumero.text.toString()
        }

        if (miPerfilFecha.text.toString() != "Fecha por defecto") {
            updates["fechaNacimiento"] = miPerfilFecha.text.toString()
        }

        // Subir la imagen si se ha seleccionado una
        if (imagenSeleccionada) {
            Log.d("Miperfil", "Estamos dentro")
            val bitmap = (miPerfilFoto.drawable as BitmapDrawable).bitmap
            val file = File(cacheDir, "temp_image.jpg")

            try {
                val outStream = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream)
                outStream.flush()
                outStream.close()

                val imagenSeleccionadaUri = Uri.fromFile(file)

                val storage = FirebaseStorage.getInstance()
                val nombreArchivo = "$email.jpg"
                val rutaImagen = "personas/$nombreArchivo"
                val storageReference = storage.reference.child(rutaImagen)

                storageReference.putFile(imagenSeleccionadaUri)
                    .addOnSuccessListener {
                        isImageUpdated = true
                        if (updates.isNotEmpty()) {
                            db.collection("personas").document(email).update(updates)
                                .addOnSuccessListener {
                                    progressBar.visibility = View.GONE
                                    showAlert("Éxito", "Datos actualizados correctamente")
                                }
                                .addOnFailureListener { e ->
                                    progressBar.visibility = View.GONE
                                    showAlert("Error", "Error al actualizar los datos: ${e.message}")
                                }
                        }
                    }
                    .addOnFailureListener { e ->
                        progressBar.visibility = View.GONE
                        showAlert("Error", "Error al subir la imagen: ${e.message}")
                    }

            } catch (e: IOException) {

                progressBar.visibility = View.GONE
                showAlert("Error", "Error al procesar la imagen: ${e.message}")
            }
        }

        if (!imagenSeleccionada && updates.isNotEmpty()) {
            db.collection("personas").document(email).update(updates)
                .addOnSuccessListener {
                    progressBar.visibility = View.GONE
                    showAlert("Éxito", "Datos actualizados correctamente")
                }
                .addOnFailureListener { e ->
                    progressBar.visibility = View.GONE
                    showAlert("Error", "Error al actualizar los datos: ${e.message}")
                }
        }
    }





    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            val selectedImage: Uri = data.data!!
            miPerfilFoto.setImageURI(selectedImage)
        }
    }

    private fun showInputDialog(title: String, textView: TextView, isPhoneNumber: Boolean = false) {
        val input = EditText(this).apply {
            setText(textView.text)
        }

        val builder = AlertDialog.Builder(this)
            .setTitle(title)
            .setView(input)
            .setPositiveButton("Aceptar") { _, _ ->
                val inputText = input.text.toString()
                if (!isPhoneNumber || isValidPhoneNumber(inputText)) {
                    textView.text = inputText
                } else {
                    showAlert("Error", "Número de movil no válido.")
                }
            }
            .setNegativeButton("Cancelar", null)

        builder.show()
    }

    private fun isValidPhoneNumber(phone: String): Boolean {
        return phone.matches(Regex("^6[0-9]{8}\$"))
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            miPerfilFecha.text = "$selectedDay/${selectedMonth + 1}/$selectedYear"
        }, year, month, day).show()
    }

    private fun showAlert(titulo: String, mensaje: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(titulo)
        builder.setMessage(mensaje)
        builder.setPositiveButton("Aceptar", null)
        builder.show()
    }
}
