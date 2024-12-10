package com.proyecto.carmarket.Activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.proyecto.carmarket.Objetos.Mensaje
import com.proyecto.carmarket.R
import java.text.SimpleDateFormat
import java.util.*

class VerMensaje : AppCompatActivity() {

    private lateinit var verMensaje_emailEmisor: TextView
    private lateinit var verMensaje_fecha: TextView
    private lateinit var verMensaje_asunto: TextView
    private lateinit var verMensaje_mensaje: TextView
    private lateinit var verMensaje_responder: Button
    private lateinit var verMensaje_verPerfil: Button
    private lateinit var verMensaje_eliminar: Button

    private lateinit var mensajeId: String
    private lateinit var estadoVista: String
    private lateinit var emailReceptor: String

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ver_mensaje)

        verMensaje_emailEmisor = findViewById(R.id.verMensaje_emailEmisor)
        verMensaje_fecha = findViewById(R.id.verMensaje_fecha)
        verMensaje_asunto = findViewById(R.id.verMensaje_asunto)
        verMensaje_mensaje = findViewById(R.id.verMensaje_mensaje)
        verMensaje_responder = findViewById(R.id.verMensaje_responder)
        verMensaje_verPerfil = findViewById(R.id.verMensaje_verPerfil)
        verMensaje_eliminar = findViewById(R.id.verMensaje_eliminar)

        mensajeId = intent.getStringExtra("mensajeId") ?: run {
            Toast.makeText(this, "Error: mensajeId no encontrado.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        estadoVista = intent.getStringExtra("estadoVista") ?: run {
            Toast.makeText(this, "Error: estadoVista no encontrado.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        configurarInterfaz()
        cargarMensaje()

        findViewById<ImageView>(R.id.verMensaje_volver).setOnClickListener {
            val intent = Intent(this, TusMensajes::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun configurarInterfaz() {
        if (estadoVista == "enviados") {
            verMensaje_responder.visibility = View.GONE
            verMensaje_verPerfil.visibility = View.GONE
            verMensaje_eliminar.setOnClickListener {
                alertConfirmar(
                    "Confirmación",
                    "¿Estás seguro de que deseas eliminar este mensaje?",
                    onConfirm = {
                        eliminarMensajeEnviado()
                    },
                    onCancel = {

                    }
                )
            }
        } else if (estadoVista == "recibidos") {
            verMensaje_responder.setOnClickListener { responderMensaje(verMensaje_emailEmisor.text.toString()) }
            verMensaje_verPerfil.setOnClickListener {
                val intent = Intent(this, SuPerfil::class.java).apply {
                    putExtra("email", verMensaje_emailEmisor.text.toString())
                    putExtra("ventanaAnterior", "mensajes")
                }
                startActivity(intent)
                finish()
            }
            verMensaje_eliminar.setOnClickListener {
                alertConfirmar(
                    "Confirmación",
                    "¿Estás seguro de que deseas eliminar este mensaje?",
                    onConfirm = {
                        eliminarMensajeRecibido()
                    },
                    onCancel = {

                    }
                )
            }
        }
    }

    private fun cargarMensaje() {
        db.collection("mensajes").document(mensajeId).get().addOnSuccessListener { document ->
            if (document != null) {
                val mensaje = document.toObject(Mensaje::class.java)
                if (mensaje != null) {
                    verMensaje_asunto.text = mensaje.asunto
                    verMensaje_fecha.text = "Recibido el ${mensaje.fecha}"
                    verMensaje_emailEmisor.text = mensaje.emailEmisor
                    verMensaje_mensaje.text = mensaje.mensaje
                    emailReceptor = mensaje.emailReceptor

                    if (estadoVista == "recibidos") {
                        db.collection("mensajes").document(mensajeId)
                            .update("leido", true)
                            .addOnFailureListener {
                                Toast.makeText(this, "Error al marcar como leído", Toast.LENGTH_SHORT).show()
                            }
                    }
                } else {
                    Toast.makeText(this, "Mensaje no encontrado", Toast.LENGTH_SHORT).show()
                }
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Error al cargar el mensaje", Toast.LENGTH_SHORT).show()
        }
    }

    private fun responderMensaje(emailReceptor: String) {
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
                "emailReceptor" to emailReceptor,
                "emailEmisor" to MainActivity.email,
                "fecha" to fechaActual,
                "leido" to false,
                "mensaje" to mensaje
            )

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

    private fun eliminarMensajeRecibido() {
        db.collection("mensajes").document(mensajeId)
            .update("borradoReceptor", true)
            .addOnSuccessListener {
                verificarEliminacionTotal()
            }
            .addOnFailureListener {
                showAlert("Error", "Error al eliminar el mensaje.")
            }
    }

    private fun eliminarMensajeEnviado() {
        db.collection("mensajes").document(mensajeId)
            .update("borradoEmisor", true)
            .addOnSuccessListener {
                verificarEliminacionTotal()
            }
            .addOnFailureListener {
                showAlert("Error", "Error al eliminar el mensaje.")
            }
    }

    private fun verificarEliminacionTotal() {
        db.collection("mensajes").document(mensajeId).get().addOnSuccessListener { document ->
            if (document.exists()) {
                val borradoEmisor = document.getBoolean("borradoEmisor") ?: false
                val borradoReceptor = document.getBoolean("borradoReceptor") ?: false
                if (borradoEmisor && borradoReceptor) {
                    db.collection("mensajes").document(mensajeId).delete()
                        .addOnSuccessListener {
                            showAlert("Éxito", "Mensaje eliminado completamente.")
                        }
                        .addOnFailureListener {
                            showAlert("Error", "Error al eliminar completamente el mensaje.")
                        }
                } else {
                    showAlert("Éxito", "Mensaje eliminado para ti.")
                }
            } else {
                showAlert("Error", "Mensaje no encontrado.")
            }
        }.addOnFailureListener {
            showAlert("Error", "Error al verificar el estado del mensaje.")
        }
    }

    private fun showAlert(titulo: String, mensaje: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(titulo)
        builder.setMessage(mensaje)
        builder.setPositiveButton("Aceptar", null)
        builder.show()
    }

    private fun alertConfirmar(title: String, message: String, onConfirm: () -> Unit, onCancel: () -> Unit) {
        val builder = android.app.AlertDialog.Builder(this)
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
}
