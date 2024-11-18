package com.proyecto.carmarket.Activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
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
import java.io.File

class MenuActivity : AppCompatActivity() {
    private var db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_menu)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        cargaNombreYFotoUsuario()
        cargaListaMarcas()
        cargaListaAnuncios()

        val miPerfilButton = findViewById<ImageView>(R.id.menu_miPerfil)

        miPerfilButton.setOnClickListener {
            val progressBar = findViewById<ProgressBar>(R.id.menu_progressBar)
            progressBar.visibility = View.VISIBLE
            startActivity(Intent(this, MiPerfil::class.java))
            finish()
            progressBar.visibility = View.GONE
        }

        val creaAnuncioButton = findViewById<ImageView>(R.id.menu_creaAnuncio)

        creaAnuncioButton.setOnClickListener {
            val progressBar = findViewById<ProgressBar>(R.id.menu_progressBar)
            progressBar.visibility = View.VISIBLE
            startActivity(Intent(this, CreaAnuncio::class.java))
            finish()
            progressBar.visibility = View.GONE
        }
    }


    private fun cargaNombreYFotoUsuario() {
        val email = MainActivity.email
        val nombreTextView = findViewById<TextView>(R.id.menu_nombrePersona)
        val fotoImageView = findViewById<ImageView>(R.id.menu_foto)


        db.collection("personas").document(email).get().addOnSuccessListener { document ->
            if (document != null && document.exists()) {
                val nombre = document.getString("nombre")

                if (!nombre.isNullOrEmpty() && nombre != "Nombre") {
                    nombreTextView.text = "Bienvenido $nombre"
                } else {
                    nombreTextView.text = "Bienvenido"
                }
            } else {
                nombreTextView.text = "Bienvenido"
            }
        }.addOnFailureListener { e ->
            Log.e("MenuActivity", "Error al obtener datos del usuario: $email", e)
            nombreTextView.text = "Bienvenido"
        }


        val storageRef = FirebaseStorage.getInstance().reference.child("personas/$email.jpg")
        storageRef.downloadUrl.addOnSuccessListener { uri ->
            Glide.with(this)
                .load(uri)
                .placeholder(R.drawable.perfil)
                .error(R.drawable.perfil)
                .into(fotoImageView)
        }.addOnFailureListener { e ->
            Log.e("MenuActivity", "Error al cargar la foto del usuario: $email.jpg", e)
        }
    }


    private fun cargaListaMarcas() {
        val progressBar = findViewById<View>(R.id.menu_progressBarMarcas)
        progressBar.visibility = View.VISIBLE

        db.collection("marcas").get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val listaMarcas = ArrayList<Marca>()
                for (document in task.result) {
                    val nombre = document.id
                    val urlIcono = document.getString("url") ?: ""
                    listaMarcas.add(Marca(nombre, urlIcono))
                }

                val adaptador = AdaptadorListaMarcas(listaMarcas)
                findViewById<RecyclerView>(R.id.menu_recyclerViewMarcas).apply {
                    layoutManager = LinearLayoutManager(this@MenuActivity, LinearLayoutManager.HORIZONTAL, false)
                    adapter = adaptador
                }
                progressBar.visibility = View.GONE
            } else {
                Log.d("MenuActivity", "Error obteniendo documentos: ", task.exception)
                progressBar.visibility = View.GONE
            }
        }
    }

    private fun cargaListaAnuncios() {
        val progressBar = findViewById<View>(R.id.menu_progressBarAnuncios)
        progressBar.visibility = View.VISIBLE

        db.collection("vehiculo").get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val listaAnuncios = ArrayList<Anuncio>()
                val storageReference = FirebaseStorage.getInstance().reference

                val tareasDescarga = mutableListOf<Task<Uri>>()

                for (document in task.result) {
                    val id = document.id
                    val annoMatriculacion = document.getString("annoMatriculacion") ?: ""
                    val kilometraje = document.getString("kilometraje") ?: ""
                    val localidad = document.getString("localidad") ?: ""
                    val marca = document.getString("marca") ?: ""
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
                                if (item.name == "foto_0.jpg") {
                                    if (fotoPrincipal.isEmpty()) {
                                        fotoPrincipal.add(uri.toString())
                                    }
                                }
                            }
                            tareasDescarga.add(tarea)
                        }

                        Tasks.whenAllComplete(tareasDescarga).addOnCompleteListener {
                            if (fotoPrincipal.isEmpty() && fotos.isNotEmpty()) {
                                fotoPrincipal.add(fotos.first())
                            }

                            val anuncio = Anuncio(
                                id, annoMatriculacion, kilometraje, localidad,
                                marca, modelo, nPlazas, potencia,
                                precio, tipoCombustible, propietario,
                                fotos, fotoPrincipal.firstOrNull()
                            )
                            listaAnuncios.add(anuncio)

                            if (listaAnuncios.size == task.result.size()) {
                                configurarRecyclerView(listaAnuncios)
                                progressBar.visibility = View.GONE
                            }
                        }
                    }.addOnFailureListener {
                        Log.e("MenuActivity", "Error al listar fotos en la carpeta: $carpetaFotos", it)

                        val anuncio = Anuncio(
                            id, annoMatriculacion, kilometraje, localidad,
                            marca, modelo, nPlazas, potencia,
                            precio, tipoCombustible, propietario, null, null
                        )
                        listaAnuncios.add(anuncio)

                        if (listaAnuncios.size == task.result.size()) {
                            configurarRecyclerView(listaAnuncios)
                            progressBar.visibility = View.GONE
                        }
                    }
                }
            } else {
                Log.d("MenuActivity", "Error obteniendo documentos: ", task.exception)
                progressBar.visibility = View.GONE
            }
        }
    }

    private fun configurarRecyclerView(listaAnuncios: List<Anuncio>) {
        val adaptador = AdaptadorListaAnuncios(this, listaAnuncios)
        findViewById<RecyclerView>(R.id.menu_recyclerViewAnuncios).apply {
            layoutManager = GridLayoutManager(this@MenuActivity, 2)
            adapter = adaptador
        }
    }


}
