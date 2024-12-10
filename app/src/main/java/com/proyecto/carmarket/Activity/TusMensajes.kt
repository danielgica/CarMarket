package com.proyecto.carmarket.Activity

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.proyecto.carmarket.Adapter.AdaptadorListaMensajes
import com.proyecto.carmarket.Objetos.Mensaje
import com.proyecto.carmarket.R

class TusMensajes : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var cambiaVistaBoton: Button
    private lateinit var filtroEmailEditText: EditText
    private var estadoVista: String = "recibidos"
    private val listaMensajes = mutableListOf<Mensaje>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tus_mensajes)

        recyclerView = findViewById(R.id.tusMensajes_recyclerViewMensajes)
        progressBar = findViewById(R.id.tusMensajes_progressBar)
        cambiaVistaBoton = findViewById(R.id.tusMensajes_cambiaVista)
        filtroEmailEditText = findViewById(R.id.tusMensajes_emailEmisor)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = AdaptadorListaMensajes(this, listaMensajes, estadoVista)

        toggleEstadoVista()

        cargarMensajes()

        filtroEmailEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filtrarMensajes(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        cambiaVistaBoton.setOnClickListener {
            estadoVista = if (estadoVista == "recibidos") "enviados" else "recibidos"
            toggleEstadoVista()
        }

        findViewById<ImageView>(R.id.tusMensajes_volver).setOnClickListener {
            if(MainActivity.admin){
                progressBar.visibility = View.VISIBLE
                startActivity(Intent(this, MenuAdmin::class.java))
                finish()
                progressBar.visibility = View.GONE
            }
            else{
                progressBar.visibility = View.VISIBLE
                startActivity(Intent(this, MenuActivity::class.java))
                finish()
                progressBar.visibility = View.GONE
            }

        }
    }

    private fun cargarMensajes() {
        progressBar.visibility = View.VISIBLE

        val emailUsuario = MainActivity.email
        val db = FirebaseFirestore.getInstance()
        val query = when (estadoVista) {
            "recibidos" -> db.collection("mensajes")
                .whereEqualTo("emailReceptor", emailUsuario)
                .whereEqualTo("borradoReceptor", false)
            "enviados" -> db.collection("mensajes")
                .whereEqualTo("emailEmisor", emailUsuario)
                .whereEqualTo("borradoEmisor", false)
            else -> throw IllegalStateException("Estado desconocido: $estadoVista")
        }

        query.get()
            .addOnSuccessListener { documents ->
                listaMensajes.clear()
                for (document in documents) {
                    val mensaje = Mensaje(
                        document.id,
                        document.getString("asunto") ?: "",
                        document.getString("emailEmisor") ?: "",
                        document.getString("emailReceptor") ?: "",
                        document.getString("fecha") ?: "",
                        document.getBoolean("leido") ?: false,
                        document.getString("mensaje") ?: "",
                        document.getBoolean("borradoEmisor") ?: false,
                        document.getBoolean("borradoReceptor") ?: false
                    )
                    listaMensajes.add(mensaje)
                }
                recyclerView.adapter = AdaptadorListaMensajes(this, listaMensajes, estadoVista)
                recyclerView.adapter?.notifyDataSetChanged()
                progressBar.visibility = View.GONE
            }
            .addOnFailureListener {
                progressBar.visibility = View.GONE
            }

    }

    private fun filtrarMensajes(filtro: String) {
        val mensajesFiltrados = listaMensajes.filter { mensaje ->
            when (estadoVista) {
                "recibidos" -> mensaje.emailEmisor.contains(filtro, ignoreCase = true)
                "enviados" -> mensaje.emailReceptor.contains(filtro, ignoreCase = true)
                else -> false
            }
        }

        recyclerView.adapter = AdaptadorListaMensajes(this, mensajesFiltrados.toMutableList(), estadoVista)
        recyclerView.adapter?.notifyDataSetChanged()
    }

    private fun toggleEstadoVista() {
        val titulo = findViewById<TextView>(R.id.tusMensajes_titulo)
        val botonTexto = findViewById<Button>(R.id.tusMensajes_cambiaVista)

        if (estadoVista == "recibidos") {
            titulo.text = "Mensajes recibidos"
            botonTexto.text = "Ver mensajes enviados"
            filtroEmailEditText.hint ="Indica email emisor"
        } else {
            titulo.text = "Mensajes enviados"
            botonTexto.text = "Ver mensajes recibidos"
            filtroEmailEditText.hint ="Indica email receptor"
        }
        cargarMensajes()
    }

}
