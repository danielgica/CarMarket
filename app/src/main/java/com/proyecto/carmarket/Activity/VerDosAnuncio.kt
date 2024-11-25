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

class VerDosAnuncio : AppCompatActivity() {

    private lateinit var imagenActual1: ImageView
    private lateinit var marcas1: TextView
    private lateinit var modelo1: TextView
    private lateinit var fechaTextView1: TextView
    private lateinit var kilometrajeTextView1: TextView
    private lateinit var localidadTextView1: TextView
    private lateinit var numeroPlazasTextView1: TextView
    private lateinit var potenciaTextView1: TextView
    private lateinit var tipoCombustibleTextView1: TextView
    private lateinit var precioTexto1: TextView
    private lateinit var contador1: TextView

    private lateinit var imagenActual2: ImageView
    private lateinit var marcas2: TextView
    private lateinit var modelo2: TextView
    private lateinit var fechaTextView2: TextView
    private lateinit var kilometrajeTextView2: TextView
    private lateinit var localidadTextView2: TextView
    private lateinit var numeroPlazasTextView2: TextView
    private lateinit var potenciaTextView2: TextView
    private lateinit var tipoCombustibleTextView2: TextView
    private lateinit var precioTexto2: TextView
    private lateinit var contador2: TextView

    private lateinit var volverMenu: ImageView
    private lateinit var seleccion1: Button
    private lateinit var seleccion2: Button
    private lateinit var progressBar: ProgressBar
    private val listaFotos1 = mutableListOf<String>()
    private val listaFotos2 = mutableListOf<String>()
    private var fotoIndex1 = 0
    private var fotoIndex2 = 0
    private var propietario1: String = ""
    private var propietario2: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ver_dos_anuncio)

        seleccion1 = findViewById(R.id.verDosAnuncio_selecciona1)
        seleccion2 = findViewById(R.id.verDosAnuncio_selecciona2)

        volverMenu = findViewById(R.id.verDosAnuncio_volver)
        progressBar = findViewById(R.id.verDosAnuncio_progressBar)

        imagenActual1 = findViewById(R.id.verDosAnuncio_foto1)
        marcas1 = findViewById(R.id.verDosAnuncio_marca1)
        modelo1 = findViewById(R.id.verDosAnuncio_modelo1)
        fechaTextView1 = findViewById(R.id.verDosAnuncio_textoFecha1)
        kilometrajeTextView1 = findViewById(R.id.verDosAnuncio_textoKm1)
        localidadTextView1 = findViewById(R.id.verDosAnuncio_textoLocalidad1)
        numeroPlazasTextView1 = findViewById(R.id.verDosAnuncio_textoPlazas1)
        potenciaTextView1 = findViewById(R.id.verDosAnuncio_textoPotencia1)
        tipoCombustibleTextView1 = findViewById(R.id.verDosAnuncio_textoCombustible1)
        precioTexto1 = findViewById(R.id.verDosAnuncio_textoPrecio1)
        contador1 = findViewById(R.id.verDosAnuncio_contador1)

        imagenActual2 = findViewById(R.id.verDosAnuncio_foto2)
        marcas2 = findViewById(R.id.verDosAnuncio_marca2)
        modelo2 = findViewById(R.id.verDosAnuncio_modelo2)
        fechaTextView2 = findViewById(R.id.verDosAnuncio_textoFecha2)
        kilometrajeTextView2 = findViewById(R.id.verDosAnuncio_textoKm2)
        localidadTextView2 = findViewById(R.id.verDosAnuncio_textoLocalidad2)
        numeroPlazasTextView2 = findViewById(R.id.verDosAnuncio_textoPlazas2)
        potenciaTextView2 = findViewById(R.id.verDosAnuncio_textoPotencia2)
        tipoCombustibleTextView2 = findViewById(R.id.verDosAnuncio_textoCombustible2)
        precioTexto2 = findViewById(R.id.verDosAnuncio_textoPrecio2)
        contador2 = findViewById(R.id.verDosAnuncio_contador2)

        progressBar.visibility = View.VISIBLE
        MainActivity.idAnuncio2 = intent.getStringExtra("ID_ANUNCIO").toString()

        cargarDatosAnuncio(MainActivity.idAnuncio1, true)
        cargarDatosAnuncio(MainActivity.idAnuncio2, false)

        volverMenu.setOnClickListener {
            startActivity(Intent(this, MenuActivity::class.java))
            finish()
        }

        seleccion1.setOnClickListener {
            val intent = Intent(this, VerAnuncio::class.java)
            intent.putExtra("ID_ANUNCIO", MainActivity.idAnuncio1)
            startActivity(intent)
            finish()
        }

        seleccion2.setOnClickListener {
            val intent = Intent(this, VerAnuncio::class.java)
            intent.putExtra("ID_ANUNCIO", MainActivity.idAnuncio2)
            startActivity(intent)
            finish()
        }


        imagenActual1.setOnTouchListener { _, event -> handleImageTouch(event, true) }
        imagenActual2.setOnTouchListener { _, event -> handleImageTouch(event, false) }
    }

    private fun handleImageTouch(event: MotionEvent, isAnuncio1: Boolean): Boolean {
        val width = if (isAnuncio1) imagenActual1.width else imagenActual2.width
        if (event.action == MotionEvent.ACTION_DOWN) {
            val x = event.x
            if (x < width / 2) previousImage(isAnuncio1)
            else nextImage(isAnuncio1)
        }
        return true
    }

    private fun cargarDatosAnuncio(anuncioId: String, isAnuncio1: Boolean) {
        val db = FirebaseFirestore.getInstance()
        db.collection("vehiculo").document(anuncioId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    if (isAnuncio1) {
                        marcas1.text = document.getString("marca") ?: "Marca"
                        modelo1.text = document.getString("modelo") ?: "Modelo"
                        fechaTextView1.text = document.getString("annoMatriculacion") ?: "-"
                        kilometrajeTextView1.text = "${document.getString("kilometraje") ?: "-"} Km"
                        localidadTextView1.text = document.getString("localidad") ?: "-"
                        numeroPlazasTextView1.text = document.getString("nPlazas") ?: "-"
                        potenciaTextView1.text = "${document.getString("potencia") ?: "-"} CV"
                        tipoCombustibleTextView1.text = document.getString("tipoCombustible") ?: "-"
                        precioTexto1.text = "${document.getString("precio") ?: "0"} €"
                        propietario1 = document.getString("propietario") ?: ""
                        cargarFotos(anuncioId, true)
                    } else {
                        marcas2.text = document.getString("marca") ?: "Marca"
                        modelo2.text = document.getString("modelo") ?: "Modelo"
                        fechaTextView2.text = document.getString("annoMatriculacion") ?: "-"
                        kilometrajeTextView2.text = "${document.getString("kilometraje") ?: "-"} Km"
                        localidadTextView2.text = document.getString("localidad") ?: "-"
                        numeroPlazasTextView2.text = document.getString("nPlazas") ?: "-"
                        potenciaTextView2.text = "${document.getString("potencia") ?: "-"} CV"
                        tipoCombustibleTextView2.text = document.getString("tipoCombustible") ?: "-"
                        precioTexto2.text = "${document.getString("precio") ?: "0"} €"
                        propietario2 = document.getString("propietario") ?: ""
                        cargarFotos(anuncioId, false)
                    }
                } else {
                    progressBar.visibility = View.GONE
                    showAlert("Error", "Uno de los anuncios no existe.")
                }
            }
            .addOnFailureListener {
                progressBar.visibility = View.GONE
                showAlert("Error", "No se pudo cargar uno de los anuncios. Inténtalo de nuevo.")
            }
    }

    private fun cargarFotos(anuncioId: String, isAnuncio1: Boolean) {
        val storageRef = FirebaseStorage.getInstance().reference.child("anuncios/$anuncioId")
        storageRef.listAll()
            .addOnSuccessListener { listResult ->
                if (isAnuncio1) {
                    listaFotos1.clear()
                    listaFotos1.addAll(listResult.items.map { it.path })
                    if (listaFotos1.isNotEmpty()) mostrarImagenActual(true)
                } else {
                    listaFotos2.clear()
                    listaFotos2.addAll(listResult.items.map { it.path })
                    if (listaFotos2.isNotEmpty()) mostrarImagenActual(false)
                }
                progressBar.visibility = View.GONE
            }
            .addOnFailureListener {
                progressBar.visibility = View.GONE
                showAlert("Error", "No se pudieron cargar las fotos de uno de los anuncios.")
            }
    }

    private fun mostrarImagenActual(isAnuncio1: Boolean) {
        val listaFotos = if (isAnuncio1) listaFotos1 else listaFotos2
        val fotoIndex = if (isAnuncio1) fotoIndex1 else fotoIndex2
        val imagenActual = if (isAnuncio1) imagenActual1 else imagenActual2
        val contador = if (isAnuncio1) contador1 else contador2

        if (listaFotos.isNotEmpty()) {
            val storageRef = FirebaseStorage.getInstance().reference
            val imagenRef = storageRef.child(listaFotos[fotoIndex])
            imagenRef.downloadUrl.addOnSuccessListener { uri ->
                Glide.with(this)
                    .load(uri)
                    .placeholder(R.drawable.coche)
                    .error(R.drawable.coche)
                    .into(imagenActual)
                contador.text = "${fotoIndex + 1}/${listaFotos.size}"
            }
        }
    }

    private fun nextImage(isAnuncio1: Boolean) {
        if (isAnuncio1) {
            if (listaFotos1.isNotEmpty()) {
                fotoIndex1 = (fotoIndex1 + 1) % listaFotos1.size
                mostrarImagenActual(true)
            }
        } else {
            if (listaFotos2.isNotEmpty()) {
                fotoIndex2 = (fotoIndex2 + 1) % listaFotos2.size
                mostrarImagenActual(false)
            }
        }
    }

    private fun previousImage(isAnuncio1: Boolean) {
        if (isAnuncio1) {
            if (listaFotos1.isNotEmpty()) {
                fotoIndex1 = if (fotoIndex1 - 1 < 0) listaFotos1.size - 1 else fotoIndex1 - 1
                mostrarImagenActual(true)
            }
        } else {
            if (listaFotos2.isNotEmpty()) {
                fotoIndex2 = if (fotoIndex2 - 1 < 0) listaFotos2.size - 1 else fotoIndex2 - 1
                mostrarImagenActual(false)
            }
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
