package com.proyecto.carmarket.Activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.proyecto.carmarket.Adapter.AdaptadorListaAnuncios
import com.proyecto.carmarket.Adapter.AdaptadorListaMarcas
import com.proyecto.carmarket.Objetos.Anuncio
import com.proyecto.carmarket.Objetos.Marca
import com.proyecto.carmarket.R
import org.mindrot.jbcrypt.BCrypt
import java.security.MessageDigest

class MenuAdmin : AppCompatActivity() {
    private var db = FirebaseFirestore.getInstance()
    private var marcaSeleccionada: String = "Todos"
    private var localidadSeleccionada: String = ""

    private var archivoUri: Uri? = null
    private var archivoSeleccionado = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_menu_admin)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        cargaNombreYFotoUsuario()
        cargaListaMarcas()
        cargaAnunciosPorMarcaYLocalidad(marcaSeleccionada, localidadSeleccionada)
        actualizarEstadoPuntoRojo()

        val localidadEditText = findViewById<EditText>(R.id.menuAdmin_textoLocalidad)
        localidadEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                localidadSeleccionada = s.toString()
                cargaAnunciosPorMarcaYLocalidad(marcaSeleccionada, localidadSeleccionada)
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        val miPerfilButton = findViewById<ImageView>(R.id.menuAdmin_miPerfil)

        miPerfilButton.setOnClickListener {
            val progressBar = findViewById<ProgressBar>(R.id.menuAdmin_progressBar)
            progressBar.visibility = View.VISIBLE
            startActivity(Intent(this, MiPerfil::class.java))
            finish()
            progressBar.visibility = View.GONE
        }

        val creaAdminButton = findViewById<ImageView>(R.id.menuAdmin_creaAdmin)

        creaAdminButton.setOnClickListener {
            val layout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(32, 32, 32, 32)
            }

            val emailEditText = EditText(this).apply {
                hint = "Correo"
                inputType = android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
            }

            val passwordEditText = EditText(this).apply {
                hint = "Contraseña"
                inputType = android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
                transformationMethod = android.text.method.PasswordTransformationMethod.getInstance()
            }

            val confirmPasswordEditText = EditText(this).apply {
                hint = "Repetir Contraseña"
                inputType = android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
                transformationMethod = android.text.method.PasswordTransformationMethod.getInstance()
            }

            layout.addView(emailEditText)
            layout.addView(passwordEditText)
            layout.addView(confirmPasswordEditText)

            val builder = AlertDialog.Builder(this)
            builder.setTitle("Crear nuevo admin")
            builder.setView(layout)
            builder.setPositiveButton("Registrar") { _, _ ->
                val email = emailEditText.text.toString().trim()
                val password = passwordEditText.text.toString()
                val confirmPassword = confirmPasswordEditText.text.toString()

                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    showAlert("Error", "El correo ingresado no tiene un formato válido.")
                    return@setPositiveButton
                }

                if (password != confirmPassword) {
                    showAlert("Error", "Las contraseñas no coinciden.")
                    return@setPositiveButton
                }

                val hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt())

                val progressBar = findViewById<ProgressBar>(R.id.menuAdmin_progressBar)
                progressBar.visibility = View.VISIBLE

                db.collection("personas").document(email).get()
                    .addOnSuccessListener { document ->
                        if (document.exists()) {
                            progressBar.visibility = View.GONE
                            showAlert("Error", "El email ya está registrado")
                        } else {
                            db.collection("personas").document(email).set(
                                hashMapOf(
                                    "admin" to true,
                                    "contrasenna" to hashedPassword,
                                    "fechaNacimiento" to null,
                                    "nombre" to null,
                                    "numero" to null,
                                    "localidad" to null
                                )
                            ).addOnSuccessListener {
                                progressBar.visibility = View.GONE
                                showAlert("Confirmación", "Cuenta creada exitosamente")
                            }.addOnFailureListener { e ->
                                progressBar.visibility = View.GONE
                                showAlert("Error", "Error al crear la cuenta: ${e.message}")
                            }
                        }
                    }.addOnFailureListener { e ->
                        progressBar.visibility = View.GONE
                        showAlert("Error", "Error al verificar el email: ${e.message}")
                    }
            }
            builder.setNegativeButton("Cancelar", null)
            builder.show()
        }

        val creaMarcaButton = findViewById<ImageView>(R.id.menuAdmin_creaMarca)

        creaMarcaButton.setOnClickListener {
            val layout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(32, 32, 32, 32)
            }

            val marcaEditText = EditText(this).apply {
                hint = "Nombre de la marca (todo en minúsculas)"
                inputType = android.text.InputType.TYPE_CLASS_TEXT
            }

            val archivoButton = Button(this).apply {
                text = "Seleccionar archivo PNG"
            }

            archivoButton.setOnClickListener {
                val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                    type = "image/png"
                    addCategory(Intent.CATEGORY_OPENABLE)
                }
                startActivityForResult(intent, REQUEST_CODE_SELECCIONAR_ARCHIVO)
            }

            layout.addView(marcaEditText)
            layout.addView(archivoButton)

            val builder = AlertDialog.Builder(this)
            builder.setTitle("Añadir Marca")
            builder.setView(layout)
            builder.setPositiveButton("Añadir") { _, _ ->
                val nombreMarca = marcaEditText.text.toString().trim().lowercase()
                if (nombreMarca.isEmpty()) {
                    showAlert("Error", "El nombre de la marca no puede estar vacío.")
                    return@setPositiveButton
                }
                if (!nombreMarca.matches(Regex("^[a-z0-9]+$"))) {
                    showAlert("Error", "El nombre de la marca solo debe contener letras y números.")
                    return@setPositiveButton
                }
                if (!archivoSeleccionado) {
                    showAlert("Error", "Debes seleccionar un archivo PNG.")
                    return@setPositiveButton
                }

                val progressBar = findViewById<ProgressBar>(R.id.menuAdmin_progressBar)
                progressBar.visibility = View.VISIBLE

                val archivoNombre = "$nombreMarca.png"
                val storageRef = FirebaseStorage.getInstance().reference.child("marcas/$archivoNombre")

                db.collection("marcas").document(nombreMarca).get()
                    .addOnSuccessListener { document ->
                        if (document.exists()) {
                            progressBar.visibility = View.GONE
                            showAlert("Error", "La marca ya está registrada.")
                        } else {
                            storageRef.putFile(archivoUri!!)
                                .addOnSuccessListener {
                                    db.collection("marcas").document(nombreMarca).set(
                                        mapOf("url" to archivoNombre)
                                    ).addOnSuccessListener {
                                        progressBar.visibility = View.GONE
                                        cargaListaMarcas()
                                        showAlert("Confirmación", "Marca añadida exitosamente.")
                                    }.addOnFailureListener { e ->
                                        progressBar.visibility = View.GONE
                                        showAlert("Error", "Error al añadir la marca: ${e.message}")
                                    }
                                }.addOnFailureListener { e ->
                                    progressBar.visibility = View.GONE
                                    showAlert("Error", "Error al subir el archivo: ${e.message}")
                                }
                        }
                    }.addOnFailureListener { e ->
                        progressBar.visibility = View.GONE
                        showAlert("Error", "Error al verificar la marca: ${e.message}")
                    }
            }
            builder.setNegativeButton("Cancelar", null)
            builder.show()
        }

        val tusMensajesButton = findViewById<ImageView>(R.id.menuAdmin_mensajes)

        tusMensajesButton.setOnClickListener {
            val progressBar = findViewById<ProgressBar>(R.id.menuAdmin_progressBar)
            progressBar.visibility = View.VISIBLE
            startActivity(Intent(this, TusMensajes::class.java))
            finish()
            progressBar.visibility = View.GONE
        }
    }

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {


    }


    private fun cargaNombreYFotoUsuario() {
        val email = MainActivity.email
        val nombreTextView = findViewById<TextView>(R.id.menuAdmin_nombrePersona)
        val fotoImageView = findViewById<ImageView>(R.id.menuAdmin_foto)


        db.collection("personas").document(email).get().addOnSuccessListener { document ->
            if (document != null && document.exists()) {
                val nombre = document.getString("nombre")

                if (!nombre.isNullOrEmpty() && nombre != "Nombre") {
                    nombreTextView.text = "Bienvenido admin $nombre"
                } else {
                    nombreTextView.text = "Bienvenido admin"
                }
            } else {
                nombreTextView.text = "Bienvenido admin"
            }
        }.addOnFailureListener { e ->
            Log.e("MenuActivity", "Error al obtener datos del usuario: $email", e)
            nombreTextView.text = "Bienvenido admin"
        }


        val storageRef = FirebaseStorage.getInstance().reference.child("personas/$email.jpg")
        storageRef.downloadUrl.addOnSuccessListener { uri ->
            Glide.with(this)
                .load(uri)
                .circleCrop()
                .placeholder(R.drawable.perfil)
                .error(R.drawable.perfil)
                .into(fotoImageView)
        }.addOnFailureListener { e ->
            Log.e("MenuActivity", "Error al cargar la foto del usuario: $email.jpg", e)
        }
    }


    private fun cargaListaMarcas() {
        val progressBar = findViewById<View>(R.id.menuAdmin_progressBarMarcas)
        progressBar.visibility = View.VISIBLE

        db.collection("marcas").get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val listaMarcas = ArrayList<Marca>()
                listaMarcas.add(Marca("Todos", "coche"))

                for (document in task.result) {
                    val nombre = document.id
                    val urlIcono = document.getString("url") ?: ""
                    listaMarcas.add(Marca(nombre, urlIcono))
                }

                val adaptador = AdaptadorListaMarcas(listaMarcas) { marca ->
                    marcaSeleccionada = marca
                    cargaAnunciosPorMarcaYLocalidad(marcaSeleccionada, localidadSeleccionada)
                }

                findViewById<RecyclerView>(R.id.menuAdmin_recyclerViewMarcas).apply {
                    layoutManager = LinearLayoutManager(this@MenuAdmin, LinearLayoutManager.HORIZONTAL, false)
                    adapter = adaptador
                }
                progressBar.visibility = View.GONE
            } else {
                Log.d("MenuActivity", "Error obteniendo documentos: ", task.exception)
                progressBar.visibility = View.GONE
            }
        }
    }

    private fun cargaAnunciosPorMarcaYLocalidad(marca: String, localidad: String) {
        val progressBar = findViewById<View>(R.id.menuAdmin_progressBarAnuncios)
        progressBar.visibility = View.VISIBLE

        val marcaFormateada = if (marca != "Todos") marca.replaceFirstChar { it.uppercaseChar() } else null

        val consulta = if (marcaFormateada != null) {
            db.collection("vehiculo").whereEqualTo("marca", marcaFormateada)
        } else {
            db.collection("vehiculo")
        }

        consulta.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val listaAnuncios = ArrayList<Anuncio>()
                val storageReference = FirebaseStorage.getInstance().reference
                val tareasDescarga = mutableListOf<Task<Uri>>()

                val documentosFiltrados = task.result.filter { document ->
                    val localidadDoc = document.getString("localidad") ?: ""
                    localidad.isEmpty() || localidadDoc.contains(localidad, ignoreCase = true)
                }

                for (document in documentosFiltrados) {
                    val id = document.id
                    val annoMatriculacion = document.getString("annoMatriculacion") ?: ""
                    val kilometraje = document.getString("kilometraje") ?: ""
                    val localidadDoc = document.getString("localidad") ?: ""
                    val modelo = document.getString("modelo") ?: ""
                    val nPlazas = document.getString("nPlazas") ?: ""
                    val potencia = document.getString("potencia") ?: ""
                    val precio = document.getString("precio") ?: ""
                    val tipoCombustible = document.getString("tipoCombustible") ?: ""
                    val propietario = document.getString("propietario") ?: ""

                    val carpetaFotos = "anuncios/$id/"
                    val fotos = mutableListOf<String>()
                    val fotoPrincipal = mutableListOf<String>()

                    storageReference.child(carpetaFotos).listAll().addOnSuccessListener { listResult ->
                        for (item in listResult.items) {
                            val tarea = item.downloadUrl.addOnSuccessListener { uri ->
                                fotos.add(uri.toString())
                                if (item.name == "foto_0.jpg" && fotoPrincipal.isEmpty()) {
                                    fotoPrincipal.add(uri.toString())
                                }
                            }
                            tareasDescarga.add(tarea)
                        }

                        Tasks.whenAllComplete(tareasDescarga).addOnCompleteListener {
                            if (fotoPrincipal.isEmpty() && fotos.isNotEmpty()) {
                                fotoPrincipal.add(fotos.first())
                            }

                            val anuncio = Anuncio(
                                id, annoMatriculacion, kilometraje, localidadDoc,
                                document.getString("marca") ?: "Desconocida", modelo, nPlazas, potencia,
                                precio, tipoCombustible, propietario,
                                fotos, fotoPrincipal.firstOrNull()
                            )
                            listaAnuncios.add(anuncio)

                            if (listaAnuncios.size == documentosFiltrados.size) {
                                configurarRecyclerView(listaAnuncios)
                                progressBar.visibility = View.GONE
                            }
                        }
                    }.addOnFailureListener {
                        Log.e("MenuActivity", "Error al listar fotos en la carpeta: $carpetaFotos", it)

                        val anuncio = Anuncio(
                            id, annoMatriculacion, kilometraje, localidadDoc,
                            marcaFormateada ?: "Todos", modelo, nPlazas, potencia,
                            precio, tipoCombustible, propietario, null, null
                        )
                        listaAnuncios.add(anuncio)

                        if (listaAnuncios.size == documentosFiltrados.size) {
                            configurarRecyclerView(listaAnuncios)
                            progressBar.visibility = View.GONE
                        }
                    }
                }

                if (documentosFiltrados.isEmpty()) {
                    configurarRecyclerView(listaAnuncios)
                    progressBar.visibility = View.GONE
                }
            } else {
                Log.d("MenuActivity", "Error obteniendo documentos: ", task.exception)
                progressBar.visibility = View.GONE
            }
        }

        val tituloTextView = findViewById<TextView>(R.id.menuAdmin_tituloQueVes)
        tituloTextView.text = marcaFormateada?.let { it } ?: "Todas las marcas"
    }



    private fun configurarRecyclerView(listaAnuncios: List<Anuncio>) {
        val adaptador = AdaptadorListaAnuncios(this, listaAnuncios, VerAnuncioAdmin::class.java)
        findViewById<RecyclerView>(R.id.menuAdmin_recyclerViewAnuncios).apply {
            layoutManager = GridLayoutManager(this@MenuAdmin, 2)
            adapter = adaptador
        }
    }

    private fun showAlert(titulo: String, mensaje: String, onAccept: (() -> Unit)? = null) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(titulo)
        builder.setMessage(mensaje)
        builder.setPositiveButton("Aceptar") { _, _ -> onAccept?.invoke() }
        builder.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SELECCIONAR_ARCHIVO && resultCode == Activity.RESULT_OK) {
            archivoUri = data?.data
            archivoSeleccionado = true
            Toast.makeText(this, "Archivo PNG seleccionado correctamente", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        private const val REQUEST_CODE_SELECCIONAR_ARCHIVO = 1001
    }

    private fun actualizarEstadoPuntoRojo() {
        val db = FirebaseFirestore.getInstance()
        val emailReceptor = MainActivity.email

        db.collection("mensajes")
            .whereEqualTo("emailReceptor", emailReceptor)
            .whereEqualTo("leido", false)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val menuPunto = findViewById<ImageView>(R.id.menuAdmin_punto)
                    menuPunto.visibility = View.VISIBLE
                } else {
                    val menuPunto = findViewById<ImageView>(R.id.menuAdmin_punto)
                    menuPunto.visibility = View.GONE
                }
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseError", "Error al buscar mensajes: ${e.message}")
                val menuPunto = findViewById<ImageView>(R.id.menuAdmin_punto)
                menuPunto.visibility = View.INVISIBLE
            }
    }

}
