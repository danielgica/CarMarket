package com.proyecto.carmarket.Activity

import android.annotation.SuppressLint
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
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    private lateinit var contador: TextView
    var propietario: String = ""

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
        contador = findViewById(R.id.verAnuncio_contador)

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

        findViewById<Button>(R.id.verAnuncio_verPerfil).setOnClickListener {
            val intent = Intent(this, SuPerfil::class.java)
            intent.putExtra("email", propietario)
            intent.putExtra("ventanaAnterior", "anuncio")
            startActivity(intent)
            finish()
        }

        findViewById<Button>(R.id.verAnuncio_comparar).setOnClickListener {
            val intent = Intent(this, OtroAnuncio::class.java)
            startActivity(intent)
            finish()
        }

        findViewById<Button>(R.id.verAnuncio_enviaMensaje).setOnClickListener {
            val builder = AlertDialog.Builder(this)
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

                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val fechaActual = sdf.format(Date())

                val mensajeObj = hashMapOf(
                    "asunto" to asunto,
                    "borradoEmisor" to false,
                    "borradoReceptor" to false,
                    "emailReceptor" to propietario,
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

                    val precioRaw = document.getString("precio")?.toDoubleOrNull() ?: 0.0
                    val symbols = DecimalFormatSymbols(Locale.getDefault()).apply {
                        groupingSeparator = '.'
                    }
                    val decimalFormat = DecimalFormat("#,###", symbols)
                    val precioFormateado = decimalFormat.format(precioRaw)
                    precioTexto.text = "$precioFormateado €"

                    propietario = document.getString("propietario") ?: ""

                    if(MainActivity.email == propietario){
                        findViewById<Button>(R.id.verAnuncio_verPerfil).visibility = View.GONE
                        findViewById<Button>(R.id.verAnuncio_enviaMensaje).visibility = View.GONE
                    }

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

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {

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
            actualizarContador()
        }
    }

    private fun actualizarContador() {
        contador.text = "${fotoIndex + 1}/${listaFotos.size}"
    }

    private fun showAlert(titulo: String, mensaje: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(titulo)
        builder.setMessage(mensaje)
        builder.setPositiveButton("Aceptar", null)
        builder.show()
    }
}
