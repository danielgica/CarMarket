package com.proyecto.carmarket.Activity

import android.content.Intent
import android.os.Bundle
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

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        fun showAlert(titulo: String, mensaje: String) {
            val builder = AlertDialog.Builder(this)
            builder.setTitle(titulo)
            builder.setMessage(mensaje)
            builder.setPositiveButton("Aceptar", null)
            val dialog = builder.create()
            dialog.show()
        }

        val crearCuentaButton = findViewById<Button>(R.id.login_crearCuenta)
        val loginButton = findViewById<Button>(R.id.login_login)

        val emailTextView = findViewById<TextView>(R.id.login_email)
        val contrasennaTextView = findViewById<TextView>(R.id.login_contrasenna)

        crearCuentaButton.setOnClickListener {
            val progressBar = findViewById<ProgressBar>(R.id.login_progressBar)
            progressBar.visibility = View.VISIBLE
            startActivity(Intent(this, CreaCuentaActivity::class.java))
            finish()
            progressBar.visibility = View.GONE
        }

        loginButton.setOnClickListener {
            val progressBar = findViewById<ProgressBar>(R.id.login_progressBar)
            progressBar.visibility = View.VISIBLE
            val email = emailTextView.text.toString()
            val contrasenna = contrasennaTextView.text.toString()

            if (email.isEmpty() || contrasenna.isEmpty()) {
                progressBar.visibility = View.GONE
                showAlert("Error", "Todos los campos son obligatorios")
                return@setOnClickListener
            }

            val db = FirebaseFirestore.getInstance()

            db.collection("personas").document(email).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val storedPassword = document.getString("contrasenna")
                        val isAdmin = document.getBoolean("admin") ?: false

                        if (storedPassword != null && BCrypt.checkpw(contrasenna, storedPassword)) {
                            if (isAdmin) {
                                progressBar.visibility = View.GONE
                                showAlert("Error", "Proximamente")
                                // Si es administrador, redirigir a la ventana de administración
                                //val intent = Intent(this, AdminActivity::class.java) // AdminActivity es el nombre de la nueva actividad para admin
                                //startActivity(intent)
                                //finish()
                            } else {
                                progressBar.visibility = View.GONE
                                MainActivity.email = email
                                val intent = Intent(this, MenuActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                        } else {
                            progressBar.visibility = View.GONE
                            showAlert("Error", "Contraseña incorrecta")
                        }
                    } else {
                        progressBar.visibility = View.GONE
                        showAlert("Error", "El email ingresado no está registrado")
                    }
                }
                .addOnFailureListener { e ->
                    progressBar.visibility = View.GONE
                    showAlert("Error", "Error al verificar los datos: ${e.message}")
                }
        }
    }
}
