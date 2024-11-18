package com.proyecto.carmarket.Activity

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.proyecto.carmarket.R
import org.mindrot.jbcrypt.BCrypt

class CreaCuentaActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_crea_cuenta)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        fun showAlert(
            titulo: String,
            mensaje: String,
            onPositiveClick: (() -> Unit)? = null
        ) {
            val builder = AlertDialog.Builder(this)
            builder.setTitle(titulo)
            builder.setMessage(mensaje)
            builder.setPositiveButton("Aceptar") { _, _ ->
                onPositiveClick?.invoke()
            }
            val dialog = builder.create()
            dialog.show()
        }

        val cancelarButton = findViewById<Button>(R.id.creaCuenta_cancelar)
        val crearCuentaButton = findViewById<Button>(R.id.creaCuenta_crear)

        val emailTextView = findViewById<TextView>(R.id.creaCuenta_email)
        val contrasennaTextView = findViewById<TextView>(R.id.creaCuenta_contrasenna)
        val contrasenna2TextView = findViewById<TextView>(R.id.creaCuenta_contrasenna2)

        cancelarButton.setOnClickListener {
            val progressBar = findViewById<ProgressBar>(R.id.creaCuenta_progressBar)
            progressBar.visibility = View.VISIBLE
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            progressBar.visibility = View.GONE
        }

        crearCuentaButton.setOnClickListener {
            val progressBar = findViewById<ProgressBar>(R.id.creaCuenta_progressBar)
            progressBar.visibility = View.VISIBLE
            val email = emailTextView.text.toString()
            val contrasenna = contrasennaTextView.text.toString()
            val contrasenna2 = contrasenna2TextView.text.toString()

            if (email.isEmpty() || contrasenna.isEmpty() || contrasenna2.isEmpty()) {
                progressBar.visibility = View.GONE
                showAlert("Error", "Todos los campos son obligatorios")
            } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                progressBar.visibility = View.GONE
                showAlert("Error", "El email no tiene un formato válido")
            } else if (contrasenna != contrasenna2) {
                progressBar.visibility = View.GONE
                showAlert("Error", "Las contraseñas no coinciden")
            } else {
                val hashedPassword = BCrypt.hashpw(contrasenna, BCrypt.gensalt())

                val db = FirebaseFirestore.getInstance()
                db.collection("personas").document(email).get()
                    .addOnSuccessListener { document ->
                        if (document.exists()) {
                            progressBar.visibility = View.GONE
                            showAlert("Error", "El email ya está registrado")
                        } else {
                            db.collection("personas").document(email).set(
                                hashMapOf(
                                    "admin" to false,
                                    "contrasenna" to hashedPassword,
                                    "fechaNacimiento" to null,
                                    "nombre" to null,
                                    "numero" to null,
                                    "localidad" to null
                                )
                            ).addOnSuccessListener {
                                progressBar.visibility = View.GONE
                                showAlert("Confirmación", "Cuenta creada exitosamente") {
                                    // Redirigir al LoginActivity después de aceptar
                                    val intent = Intent(this, LoginActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                }
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
        }
    }
}
