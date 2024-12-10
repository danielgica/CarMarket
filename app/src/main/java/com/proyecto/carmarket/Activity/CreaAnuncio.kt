package com.proyecto.carmarket.Activity

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.google.firebase.firestore.FirebaseFirestore
import com.proyecto.carmarket.R
import java.util.*
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.core.view.GestureDetectorCompat
import androidx.core.widget.addTextChangedListener
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols

class CreaAnuncio : AppCompatActivity() {

    private lateinit var volverMenu: ImageView
    private lateinit var agregarImagen: ImageView
    private lateinit var imagenActual: ImageView
    private lateinit var borrarImagen: ImageView
    private lateinit var marcas: TextView
    private lateinit var modelo: TextView
    private lateinit var fecha: ImageView
    private lateinit var fechaTextView: TextView
    private lateinit var kilometraje: ImageView
    private lateinit var kilometrajeTextView: TextView
    private lateinit var localidad: ImageView
    private lateinit var localidadTextView: TextView
    private lateinit var numeroPlazas: ImageView
    private lateinit var numeroPlazasTextView: TextView
    private lateinit var potencia: ImageView
    private lateinit var potenciaTextView: TextView
    private lateinit var tipoCombustible: ImageView
    private lateinit var tipoCombustibleTextView: TextView
    private lateinit var precio: TextView
    private lateinit var precioTexto: TextView
    private lateinit var publicar: Button
    private lateinit var gestureDetector: GestureDetectorCompat
    private val listaFotos = mutableListOf<Uri>()
    private var fotoIndex = 0
    private var precioLimpio = ""
    private var datosAnuncio = mutableMapOf<String, String>()
    private lateinit var progressBar: ProgressBar
    private lateinit var contadorFotos: TextView

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            listaFotos.add(it)
            fotoIndex = listaFotos.size - 1
            mostrarImagenActual()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crea_anuncio)

        contadorFotos = findViewById(R.id.creaAnuncio_contador)
        volverMenu = findViewById(R.id.creaAnuncio_volver)
        agregarImagen = findViewById(R.id.creaAnuncio_anadir)
        imagenActual = findViewById(R.id.creaAnuncio_foto)
        borrarImagen = findViewById(R.id.creaAnuncio_eliminar)
        marcas = findViewById(R.id.creaAnuncio_marca)
        modelo = findViewById(R.id.creaAnuncio_modelo)
        fecha = findViewById(R.id.creaAnuncio_imagenFecha)
        fechaTextView = findViewById(R.id.creaAnuncio_textoFecha)
        kilometraje = findViewById(R.id.creaAnuncio_fotoKm)
        kilometrajeTextView = findViewById(R.id.creaAnuncio_textoKm)
        localidad = findViewById(R.id.creaAnuncio_fotoLocalidad)
        localidadTextView = findViewById(R.id.creaAnuncio_textoLocalidad)
        numeroPlazas = findViewById(R.id.creaAnuncio_fotoPlazas)
        numeroPlazasTextView = findViewById(R.id.creaAnuncio_textoPlazas)
        potencia = findViewById(R.id.creaAnuncio_fotoPotencia)
        potenciaTextView = findViewById(R.id.creaAnuncio_textoPotencia)
        tipoCombustible = findViewById(R.id.creaAnuncio_fotoCombustible)
        tipoCombustibleTextView = findViewById(R.id.creaAnuncio_textoCombustible)
        precio = findViewById(R.id.creaAnuncio_clickPrecio)
        publicar = findViewById(R.id.creaAnuncio_crear)
        precioTexto = findViewById(R.id.creaAnuncio_textoPrecio)
        progressBar = findViewById(R.id.creaAnuncio_progressBar)

        val linearFecha = findViewById<LinearLayout>(R.id.creaAnuncio_linearFecha)
        val linearKm = findViewById<LinearLayout>(R.id.creaAnuncio_linearkm)
        val linearLocalidad = findViewById<LinearLayout>(R.id.creaAnuncio_linearLocalidad)
        val linearPlazas = findViewById<LinearLayout>(R.id.creaAnuncio_linearPlazas)
        val linearPotencia = findViewById<LinearLayout>(R.id.creaAnuncio_linearPotencia)
        val linearCombustible = findViewById<LinearLayout>(R.id.creaAnuncio_linearCombustible)

        imagenActual.setImageResource(R.drawable.coche)

        volverMenu.setOnClickListener {
            progressBar.visibility = View.VISIBLE
            startActivity(Intent(this, MenuActivity::class.java))
            finish()
            progressBar.visibility = View.GONE
        }

        agregarImagen.setOnClickListener {
            pickImageLauncher.launch("image/jpeg")
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


        borrarImagen.setOnClickListener {
            if (listaFotos.isNotEmpty()) {
                listaFotos.removeAt(fotoIndex)
                if (listaFotos.isNotEmpty()) {
                    fotoIndex %= listaFotos.size
                    mostrarImagenActual()
                } else {
                    imagenActual.setImageResource(R.drawable.coche)
                    fotoIndex = 0
                }
                actualizarContadorFotos()
            } else {
                showAlert("Error", "No hay imágenes para borrar")
            }
        }

        marcas.setOnClickListener {
            FirebaseFirestore.getInstance().collection("marcas").get()
                .addOnSuccessListener { documents ->
                    val marcaNames = documents.map { it.id }
                    mostrarSelectorConBuscador(marcaNames) { seleccion ->
                        datosAnuncio["marca"] = seleccion
                        marcas.text = seleccion
                    }
                }
                .addOnFailureListener {
                    showAlert("Error", "No se pudieron cargar las marcas. Por favor, intenta de nuevo.")
                }
        }


        modelo.setOnClickListener {
            mostrarPopupTexto("Modelo") { texto ->
                if (texto.length <= 15) {
                    modelo.text = texto
                } else {
                    showAlert("Error", "El texto no puede tener más de 15 caracteres")
                }
            }
        }

        linearFecha.setOnClickListener {
            mostrarSelectorFecha("Fecha") { fechaTextView.text = it }
        }

        linearKm.setOnClickListener {
            mostrarPopupNumero("Kilometraje") { texto -> kilometrajeTextView.text = "$texto Km" }
        }

        linearLocalidad.setOnClickListener {
            mostrarPopupTexto("Localidad") { texto -> localidadTextView.text = texto }
        }

        linearPlazas.setOnClickListener {
            mostrarPopupNumero("Número de plazas") { texto -> numeroPlazasTextView.text = texto }
        }

        linearPotencia.setOnClickListener {
            mostrarPopupNumero("Potencia") { texto -> potenciaTextView.text = "$texto CV" }
        }

        linearCombustible.setOnClickListener {
            mostrarSelector(listOf("Gasolina", "Diésel", "Eléctrico")) { seleccion ->
                tipoCombustibleTextView.text = seleccion
            }
        }

        precio.setOnClickListener {
            mostrarPopupNumero("Precio") { texto ->
                precioLimpio = texto
                val precioRaw = precioLimpio.toDoubleOrNull() ?: 0.0
                val symbols = DecimalFormatSymbols(Locale.getDefault()).apply {
                    groupingSeparator = '.' // Usar punto como separador de miles
                }
                val decimalFormat = DecimalFormat("#,###", symbols)
                val precioFormateado = decimalFormat.format(precioRaw)
                precioTexto.text = "$precioFormateado €"

                datosAnuncio["precio"] = texto
            }
        }

        publicar.setOnClickListener {
            progressBar.visibility = View.VISIBLE
            if (validarDatos()) {
                val datosAnuncio = mapOf(
                    "annoMatriculacion" to fechaTextView.text.toString(),
                    "kilometraje" to kilometrajeTextView.text.toString().removeSuffix(" Km"),
                    "localidad" to localidadTextView.text.toString(),
                    "marca" to marcas.text.toString(),
                    "modelo" to modelo.text.toString(),
                    "nPlazas" to numeroPlazasTextView.text.toString(),
                    "potencia" to potenciaTextView.text.toString().removeSuffix(" CV"),
                    "precio" to precioLimpio,
                    "tipoCombustible" to tipoCombustibleTextView.text.toString(),
                    "propietario" to MainActivity.email // Usar el email del usuario
                )

                val vehiculoCollection = FirebaseFirestore.getInstance().collection("vehiculo")

                val vehiculoDoc = vehiculoCollection.document()

                vehiculoDoc.set(datosAnuncio)
                    .addOnSuccessListener {
                        val storageRef = FirebaseStorage.getInstance().reference.child("anuncios/${vehiculoDoc.id}")
                        guardarFotosEnStorage(storageRef, vehiculoDoc.id)
                    }
                    .addOnFailureListener {
                        progressBar.visibility = View.GONE
                        showAlert("Error", "No se pudo publicar el anuncio. Inténtalo de nuevo.")
                    }
            } else {
                progressBar.visibility = View.GONE
                showAlert("Error", "Por favor, completa todos los campos y agrega al menos una foto.")
            }
        }

        actualizarContadorFotos()
    }

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {


    }

    private fun guardarFotosEnStorage(storageRef: StorageReference, vehiculoId: String) {
        val uploadTasks = mutableListOf<Task<Uri>>()

        listaFotos.forEachIndexed { index, uri ->
            val fileRef = storageRef.child("foto_$index.jpg")
            val uploadTask = fileRef.putFile(uri).continueWithTask { task ->
                if (!task.isSuccessful) throw task.exception!!
                fileRef.downloadUrl
            }
            uploadTasks.add(uploadTask)
        }

        Tasks.whenAllComplete(uploadTasks)
            .addOnSuccessListener {
                progressBar.visibility = View.GONE
                showAlert("Éxito", "¡Anuncio publicado con éxito!") {
                    startActivity(Intent(this, MenuActivity::class.java))
                    finish()
                }
            }
            .addOnFailureListener {
                progressBar.visibility = View.GONE
                showAlert("Error", "No se pudieron subir las fotos. Inténtalo de nuevo.")
            }
    }

    private fun showAlert(titulo: String, mensaje: String, onDismiss: (() -> Unit)? = null) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(titulo)
        builder.setMessage(mensaje)
        builder.setPositiveButton("Aceptar") { _, _ -> onDismiss?.invoke() }
        builder.show()
    }


    private fun mostrarSelectorConBuscador(options: List<String>, callback: (String) -> Unit) {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 16, 16, 16)
        }

        val editTextBuscar = EditText(this).apply {
            hint = "Buscar..."
            setPadding(16, 16, 16, 16)
        }

        val listViewOpciones = ListView(this)
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, options)
        listViewOpciones.adapter = adapter

        layout.addView(editTextBuscar)
        layout.addView(listViewOpciones)

        val dialog = AlertDialog.Builder(this)
            .setView(layout)
            .create()

        editTextBuscar.addTextChangedListener { texto ->
            val filtro = texto.toString()
            val opcionesFiltradas = options.filter { it.contains(filtro, ignoreCase = true) }
            listViewOpciones.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, opcionesFiltradas)
        }

        listViewOpciones.setOnItemClickListener { _, _, position, _ ->
            val seleccion = (listViewOpciones.adapter.getItem(position) as String)
            val seleccionFormateada = seleccion.replaceFirstChar { char -> char.uppercaseChar() }
            callback(seleccionFormateada)
            dialog.dismiss()
        }

        dialog.show()
    }




    private fun mostrarImagenActual() {
        if (listaFotos.isNotEmpty()) {
            imagenActual.setImageURI(listaFotos[fotoIndex])
        } else {
            imagenActual.setImageResource(R.drawable.coche)
        }
        actualizarContadorFotos()
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

    private fun actualizarContadorFotos() {
        if (listaFotos.isNotEmpty()) {
            contadorFotos.text = "${fotoIndex + 1} / ${listaFotos.size}"
        } else {
            contadorFotos.text = "0 / 0"
        }
    }

    private fun mostrarSelector(options: List<String>, callback: (String) -> Unit) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Selecciona una opción")
        builder.setItems(options.toTypedArray()) { _, which -> callback(options[which]) }
        builder.show()
    }

    private fun mostrarPopupTexto(titulo: String, callback: (String) -> Unit) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(titulo)
        val input = EditText(this)
        builder.setView(input)
        builder.setPositiveButton("Aceptar") { _, _ -> callback(input.text.toString()) }
        builder.show()
    }

    private fun mostrarPopupNumero(titulo: String, callback: (String) -> Unit) {
        mostrarPopupTexto(titulo) { texto ->
            if (texto.toIntOrNull() != null && texto.toInt() >= 0) callback(texto)
            else showAlert("Error", "Por favor, introduce un número válido.")
        }
    }

    private fun mostrarSelectorFecha(titulo: String, callback: (String) -> Unit) {
        val calendario = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(this, { _, año, mes, día ->
            callback("$día/${mes + 1}/$año")
        }, calendario.get(Calendar.YEAR), calendario.get(Calendar.MONTH), calendario.get(Calendar.DAY_OF_MONTH))
        datePickerDialog.show()
    }

    private fun validarDatos(): Boolean {
        val datosCompletos = datosAnuncio.values.all { it != "-" }
        val precioValido = precio.text.toString() != "0€"
        val marcaValida = marcas.text.toString() != "Marca"
        val modeloValido = modelo.text.toString() != "Modelo"
        val fotosCompletas = listaFotos.isNotEmpty()

        return datosCompletos && precioValido && marcaValida && modeloValido && fotosCompletas
    }

    private fun showAlert(titulo: String, mensaje: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(titulo)
        builder.setMessage(mensaje)
        builder.setPositiveButton("Aceptar", null)
        builder.show()
    }
}
