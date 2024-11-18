package com.proyecto.carmarket.Activity

import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.proyecto.carmarket.R

class VerAnuncio : AppCompatActivity() {

    private lateinit var volverMenu: ImageView
    private lateinit var imagenActual: ImageView
    private lateinit var marcas: TextView
    private lateinit var modelo: TextView
    private lateinit var fechaTextView: TextView
    private lateinit var kilometrajeTextView: TextView
    private lateinit var localidadTextView: TextView
    private lateinit var numeroPlazasTextView: TextView
    private lateinit var potenciaTextView: TextView
    private lateinit var tipoCombustibleTextView: TextView
    private lateinit var precioTexto: TextView
    private lateinit var progressBar: ProgressBar

    private val listaFotos = mutableListOf<String>()
    private var fotoIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ver_anuncio)
        MainActivity.idAnuncio1 = intent.getStringExtra("ID_ANUNCIO").toString()

        volverMenu = findViewById(R.id.verAnuncio_volver)
        imagenActual = findViewById(R.id.verAnuncio_foto)
        marcas = findViewById(R.id.verAnuncio_marca)
        modelo = findViewById(R.id.verAnuncio_modelo)
        fechaTextView = findViewById(R.id.verAnuncio_textoFecha)
        kilometrajeTextView = findViewById(R.id.verAnuncio_textoKm)
        localidadTextView = findViewById(R.id.verAnuncio_textoLocalidad)
        numeroPlazasTextView = findViewById(R.id.verAnuncio_textoPlazas)
        potenciaTextView = findViewById(R.id.verAnuncio_textoPotencia)
        tipoCombustibleTextView = findViewById(R.id.verAnuncio_textoCombustible)
        precioTexto = findViewById(R.id.verAnuncio_textoPrecio)
        progressBar = findViewById(R.id.verAnuncio_progressBar)

        progressBar.visibility = View.VISIBLE

        cargarDatosAnuncio(MainActivity.idAnuncio1)

        volverMenu.setOnClickListener {
            progressBar.visibility = View.VISIBLE
            startActivity(Intent(this, MenuActivity::class.java))
            finish()
            progressBar.visibility = View.GONE
        }

        imagenActual.setOnTouchListener { _, event ->
            val width = imagenActual.width
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    val x = event.x
                    if (x < width / 2) previousImage()
                    else nextImage()
                }
            }
            true
        }
    }

    private fun cargarDatosAnuncio(anuncioId: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("vehiculo").document(anuncioId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    marcas.text = document.getString("marca") ?: "Marca"
                    modelo.text = document.getString("modelo") ?: "Modelo"
                    fechaTextView.text = document.getString("annoMatriculacion") ?: "-"
                    kilometrajeTextView.text = "${document.getString("kilometraje") ?: "-"} Km"
                    localidadTextView.text = document.getString("localidad") ?: "-"
                    numeroPlazasTextView.text = document.getString("nPlazas") ?: "-"
                    potenciaTextView.text = "${document.getString("potencia") ?: "-"} CV"
                    tipoCombustibleTextView.text = document.getString("tipoCombustible") ?: "-"
                    precioTexto.text = "${document.getString("precio") ?: "0"} €"

                    // Cargar fotos
                    cargarFotos(anuncioId)
                } else {
                    progressBar.visibility = View.GONE
                    showAlert("Error", "El anuncio no existe.")
                }
            }
            .addOnFailureListener {
                progressBar.visibility = View.GONE
                showAlert("Error", "No se pudo cargar el anuncio. Inténtalo de nuevo.")
            }
    }

    private fun cargarFotos(anuncioId: String) {
        val storageRef = FirebaseStorage.getInstance().reference.child("anuncios/$anuncioId")
        storageRef.listAll()
            .addOnSuccessListener { listResult ->
                listaFotos.clear()
                listaFotos.addAll(listResult.items.map { it.path })

                if (listaFotos.isNotEmpty()) {
                    mostrarImagenActual()
                }
                progressBar.visibility = View.GONE
            }
            .addOnFailureListener {
                progressBar.visibility = View.GONE
                showAlert("Error", "No se pudieron cargar las fotos del anuncio.")
            }
    }

    private fun nextImage() {
        if (listaFotos.isNotEmpty()) {
            fotoIndex = (fotoIndex + 1) % listaFotos.size
            mostrarImagenActual()
        }
    }

    private fun previousImage() {
        if (listaFotos.isNotEmpty()) {
            fotoIndex = if (fotoIndex - 1 < 0) listaFotos.size - 1 else fotoIndex - 1
            mostrarImagenActual()
        }
    }

    private fun mostrarImagenActual() {
        val storageRef = FirebaseStorage.getInstance().reference
        val imagenRef = storageRef.child(listaFotos[fotoIndex])
        imagenRef.downloadUrl.addOnSuccessListener { uri ->
            Glide.with(this)
                .load(uri)
                .placeholder(R.drawable.coche)
                .error(R.drawable.coche)
                .into(imagenActual)
        }
    }

    private fun showAlert(titulo: String, mensaje: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(titulo)
        builder.setMessage(mensaje)
        builder.setPositiveButton("Aceptar", null)
        builder.show()
    }
}
