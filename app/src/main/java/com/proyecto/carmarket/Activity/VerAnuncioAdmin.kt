package com.proyecto.carmarket.Activity

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
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
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat

class VerAnuncioAdmin : AppCompatActivity() {

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

    private lateinit var actualizar: Button
    private lateinit var eliminarAnuncio: Button
    private lateinit var verPerfil: Button
    private lateinit var enviarMensaje: Button

    private lateinit var gestureDetector: GestureDetectorCompat
    private val listaFotos = mutableListOf<Uri>()
    private var fotoIndex = 0
    private var precioLimpio = ""
    private var propietario = ""
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
        setContentView(R.layout.activity_ver_anuncio_admin)
        MainActivity.idAnuncio1 = intent.getStringExtra("ID_ANUNCIO").toString()

        contadorFotos = findViewById(R.id.verAnuncioAdmin_contador)
        volverMenu = findViewById(R.id.verAnuncioAdmin_volver)
        agregarImagen = findViewById(R.id.verAnuncioAdmin_anadir)
        imagenActual = findViewById(R.id.verAnuncioAdmin_foto)
        borrarImagen = findViewById(R.id.verAnuncioAdmin_eliminar)
        marcas = findViewById(R.id.verAnuncioAdmin_marca)
        modelo = findViewById(R.id.verAnuncioAdmin_modelo)
        fecha = findViewById(R.id.verAnuncioAdmin_imagenFecha)
        fechaTextView = findViewById(R.id.verAnuncioAdmin_textoFecha)
        kilometraje = findViewById(R.id.verAnuncioAdmin_fotoKm)
        kilometrajeTextView = findViewById(R.id.verAnuncioAdmin_textoKm)
        localidad = findViewById(R.id.verAnuncioAdmin_fotoLocalidad)
        localidadTextView = findViewById(R.id.verAnuncioAdmin_textoLocalidad)
        numeroPlazas = findViewById(R.id.verAnuncioAdmin_fotoPlazas)
        numeroPlazasTextView = findViewById(R.id.verAnuncioAdmin_textoPlazas)
        potencia = findViewById(R.id.verAnuncioAdmin_fotoPotencia)
        potenciaTextView = findViewById(R.id.verAnuncioAdmin_textoPotencia)
        tipoCombustible = findViewById(R.id.verAnuncioAdmin_fotoCombustible)
        tipoCombustibleTextView = findViewById(R.id.verAnuncioAdmin_textoCombustible)
        precio = findViewById(R.id.verAnuncioAdmin_clickPrecio)
        actualizar = findViewById(R.id.verAnuncioAdmin_actualizar)
        eliminarAnuncio = findViewById(R.id.verAnuncioAdmin_eliminarAnuncio)
        precioTexto = findViewById(R.id.verAnuncioAdmin_textoPrecio)
        progressBar = findViewById(R.id.verAnuncioAdmin_progressBar)

        verPerfil = findViewById(R.id.verAnuncioAdmin_verPerfil)
        enviarMensaje = findViewById(R.id.verAnuncioAdmin_enviaMensaje)

        val linearFecha = findViewById<LinearLayout>(R.id.verAnuncioAdmin_linearFecha)
        val linearKm = findViewById<LinearLayout>(R.id.verAnuncioAdmin_linearkm)
        val linearLocalidad = findViewById<LinearLayout>(R.id.verAnuncioAdmin_linearLocalidad)
        val linearPlazas = findViewById<LinearLayout>(R.id.verAnuncioAdmin_linearPlazas)
        val linearPotencia = findViewById<LinearLayout>(R.id.verAnuncioAdmin_linearPotencia)
        val linearCombustible = findViewById<LinearLayout>(R.id.verAnuncioAdmin_linearCombustible)

        progressBar.visibility = View.VISIBLE
        cargarDatosAnuncio(MainActivity.idAnuncio1)

        volverMenu.setOnClickListener {
            progressBar.visibility = View.VISIBLE
            startActivity(Intent(this, MenuAdmin::class.java))
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
                    groupingSeparator = '.'
                }
                val decimalFormat = DecimalFormat("#,###", symbols)
                val precioFormateado = decimalFormat.format(precioRaw)
                precioTexto.text = "$precioFormateado €"

                datosAnuncio["precio"] = texto
            }
        }

        verPerfil.setOnClickListener{
            val intent = Intent(this, SuPerfil::class.java)
            intent.putExtra("email", propietario)
            intent.putExtra("ventanaAnterior", "anuncioAdmin")
            startActivity(intent)
            finish()
        }

        enviarMensaje.setOnClickListener{
            val builder = androidx.appcompat.app.AlertDialog.Builder(this)
            builder.setTitle("Enviar Mensaje")

            val layout = LinearLayout(this)
            layout.orientation = LinearLayout.VERTICAL
            layout.setPadding(32, 32, 32, 32)

            val asuntoInput = EditText(this).apply {
                hint = "Asunto (máx. 40 caracteres)"
                inputType = android.text.InputType.TYPE_CLASS_TEXT
                filters = arrayOf(android.text.InputFilter.LengthFilter(40)) // Máximo de 40 caracteres
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


        actualizar.setOnClickListener {
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
                    "propietario" to propietario
                )

                val vehiculoCollection = FirebaseFirestore.getInstance().collection("vehiculo")
                val vehiculoDoc = vehiculoCollection.document(MainActivity.idAnuncio1)

                vehiculoDoc.update(datosAnuncio)
                    .addOnSuccessListener {
                        val storageRef = FirebaseStorage.getInstance().reference.child("anuncios/${MainActivity.idAnuncio1}")
                        guardarFotosEnStorage(storageRef, MainActivity.idAnuncio1)
                    }
                    .addOnFailureListener {
                        progressBar.visibility = View.GONE
                        showAlert("Error", "No se pudo actualizar el anuncio. Inténtalo de nuevo.")
                    }
            } else {
                progressBar.visibility = View.GONE
                showAlert("Error", "Por favor, completa todos los campos y agrega al menos una foto.")
            }
        }

        eliminarAnuncio.setOnClickListener {
            alertConfirmar(
                "Confirmación",
                "¿Estás seguro de que deseas eliminar este anuncio?",
                onConfirm = {
                    progressBar.visibility = View.VISIBLE

                    val vehiculoCollection = FirebaseFirestore.getInstance().collection("vehiculo")
                    val storageRef = FirebaseStorage.getInstance().reference.child("anuncios/${MainActivity.idAnuncio1}")

                    vehiculoCollection.document(MainActivity.idAnuncio1).get()
                        .addOnSuccessListener { document ->
                            if (document.exists()) {
                                val propietario = document.getString("propietario") ?: "SinPropietario"
                                val asunto = "Eliminacion de anuncio"
                                val mensaje = "Estimado usuario, el anuncio del vehiculo  ${marcas.text}  ${modelo.text} ha sido eliminado por un administrador"

                                vehiculoCollection.document(MainActivity.idAnuncio1).delete()
                                    .addOnSuccessListener {
                                        storageRef.listAll()
                                            .addOnSuccessListener { listResult ->
                                                val deleteTasks = mutableListOf<Task<Void>>()
                                                listResult.items.forEach { item ->
                                                    deleteTasks.add(item.delete())
                                                }

                                                Tasks.whenAllComplete(deleteTasks)
                                                    .addOnSuccessListener {
                                                        enviarMensaje(propietario, asunto, mensaje)
                                                        progressBar.visibility = View.GONE
                                                        showAlert("Éxito", "El anuncio se eliminó correctamente.") {
                                                            val intent = Intent(this, MenuAdmin::class.java)
                                                            startActivity(intent)
                                                            finish()
                                                        }
                                                    }
                                                    .addOnFailureListener {
                                                        progressBar.visibility = View.GONE
                                                        showAlert("Error", "No se pudieron eliminar las fotos del anuncio. Inténtalo de nuevo.")
                                                    }
                                            }
                                            .addOnFailureListener {
                                                progressBar.visibility = View.GONE
                                                showAlert("Error", "No se pudo acceder a la carpeta de fotos. Inténtalo de nuevo.")
                                            }
                                    }
                                    .addOnFailureListener {
                                        progressBar.visibility = View.GONE
                                        showAlert("Error", "No se pudo eliminar el anuncio. Inténtalo de nuevo.")
                                    }
                            } else {
                                progressBar.visibility = View.GONE
                                showAlert("Error", "El documento no existe o no tiene un propietario válido.")
                            }
                        }
                        .addOnFailureListener {
                            progressBar.visibility = View.GONE
                            showAlert("Error", "No se pudo obtener los datos del anuncio. Inténtalo de nuevo.")
                        }
                },
                onCancel = {

                }
            )
        }


        actualizarContadorFotos()
    }

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {


    }

    private fun cargarDatosAnuncio(anuncioId: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("vehiculo").document(anuncioId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    propietario = document.getString("propietario") ?: "propietario"
                    marcas.text = document.getString("marca") ?: "Marca"
                    modelo.text = document.getString("modelo") ?: "Modelo"
                    fechaTextView.text = document.getString("annoMatriculacion") ?: "-"
                    kilometrajeTextView.text = "${document.getString("kilometraje") ?: "-"} Km"
                    localidadTextView.text = document.getString("localidad") ?: "-"
                    numeroPlazasTextView.text = document.getString("nPlazas") ?: "-"
                    potenciaTextView.text = "${document.getString("potencia") ?: "-"} CV"
                    tipoCombustibleTextView.text = document.getString("tipoCombustible") ?: "-"
                    precioLimpio = document.getString("precio") ?: ""

                    val precioRaw = precioLimpio.toDoubleOrNull() ?: 0.0
                    val symbols = DecimalFormatSymbols(Locale.getDefault()).apply {
                        groupingSeparator = '.' // Usar punto como separador de miles
                    }
                    val decimalFormat = DecimalFormat("#,###", symbols)
                    val precioFormateado = decimalFormat.format(precioRaw)
                    precioTexto.text = "$precioFormateado €"

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

                val downloadTasks = listResult.items.map { storageItem ->
                    storageItem.getBytes(Long.MAX_VALUE).addOnSuccessListener { bytes ->
                        val uri = Uri.parse(
                            "data:image/jpeg;base64," + android.util.Base64.encodeToString(bytes, android.util.Base64.DEFAULT)
                        )
                        listaFotos.add(uri)
                    }
                }

                Tasks.whenAllComplete(downloadTasks)
                    .addOnSuccessListener {
                        if (listaFotos.isNotEmpty()) {
                            mostrarImagenActual()
                        }
                        progressBar.visibility = View.GONE
                    }
                    .addOnFailureListener {
                        progressBar.visibility = View.GONE
                        showAlert("Error", "No se pudieron descargar todas las fotos.")
                    }
            }
            .addOnFailureListener {
                progressBar.visibility = View.GONE
                showAlert("Error", "No se pudieron cargar las fotos del anuncio.")
            }
    }

    private fun guardarFotosEnStorage(storageRef: StorageReference, vehiculoId: String) {
        val directorioFotos = storageRef

        directorioFotos.listAll()
            .addOnSuccessListener { listResult ->
                val deleteTasks = listResult.items.map { it.delete() }
                Tasks.whenAllComplete(deleteTasks)
                    .addOnSuccessListener {
                        // Paso 2: Subir nuevas fotos
                        subirNuevasFotos(storageRef)
                    }
                    .addOnFailureListener {
                        progressBar.visibility = View.GONE
                        showAlert("Error", "No se pudieron borrar las fotos antiguas. Inténtalo de nuevo.")
                    }
            }
            .addOnFailureListener {
                progressBar.visibility = View.GONE
                showAlert("Error", "No se pudo acceder a las fotos existentes. Inténtalo de nuevo.")
            }
    }

    private fun subirNuevasFotos(directorioFotos: StorageReference) {
        val uploadTasks = mutableListOf<Task<Uri>>()

        listaFotos.forEachIndexed { index, uri ->
            val fileRef = directorioFotos.child("foto_$index.jpg")

            if (uri.toString().startsWith("data:image/jpeg;base64,")) {
                val base64String = uri.toString().substringAfter("data:image/jpeg;base64,")
                val bytes = android.util.Base64.decode(base64String, android.util.Base64.DEFAULT)

                val uploadTask = fileRef.putBytes(bytes).continueWithTask { task ->
                    if (!task.isSuccessful) throw task.exception!!
                    fileRef.downloadUrl
                }
                uploadTasks.add(uploadTask)
            } else {
                val uploadTask = fileRef.putFile(uri).continueWithTask { task ->
                    if (!task.isSuccessful) throw task.exception!!
                    fileRef.downloadUrl
                }
                uploadTasks.add(uploadTask)
            }
        }

        Tasks.whenAllComplete(uploadTasks)
            .addOnSuccessListener {
                progressBar.visibility = View.GONE
                showAlert("Éxito", "Anuncio actualizado con éxito") {
                    startActivity(Intent(this, MenuAdmin::class.java))
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
            Glide.with(this)
                .load(listaFotos[fotoIndex])
                .placeholder(R.drawable.coche)
                .error(R.drawable.coche)
                .into(imagenActual)
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

    private fun alertConfirmar(title: String, message: String, onConfirm: () -> Unit, onCancel: () -> Unit) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
        builder.setMessage(message)
        builder.setPositiveButton("Confirmar") { _, _ ->
            onConfirm()
        }
        builder.setNegativeButton("Cancelar") { dialog, _ ->
            dialog.dismiss()
            onCancel()
        }
        val alertDialog = builder.create()
        alertDialog.show()
    }

    private fun enviarMensaje(propietario: String, asunto: String, mensaje: String) {
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
            }
            .addOnFailureListener {
                showAlert("Error", "No se pudo enviar el mensaje al propietario. Inténtalo más tarde.")
            }
    }
}
