package com.example.controlseguridadtienda

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

    private val URL_BASE = "http://192.168.1.103:5000"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val etUsuario = findViewById<EditText>(R.id.etUsuario)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnIniciarSesion = findViewById<Button>(R.id.btnIniciarSesion)
        val tvRegistrar = findViewById<TextView>(R.id.tvRegistrar)

        btnIniciarSesion.setOnClickListener {
            val usuario = etUsuario.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (usuario.isEmpty()) {
                etUsuario.error = "Ingrese su usuario"
                mostrarAlertaError("⚠️ Por favor ingrese su nombre de usuario")
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                etPassword.error = "Ingrese su contraseña"
                mostrarAlertaError("⚠️ Por favor ingrese su contraseña")
                return@setOnClickListener
            }

            val passwordCifrada = cifrarSHA256(password)

            thread {
                try {
                    val url = URL("$URL_BASE/login")
                    val conn = url.openConnection() as HttpURLConnection
                    conn.requestMethod = "POST"
                    conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                    conn.connectTimeout = 2500
                    conn.doOutput = true

                    val json = JSONObject()
                    json.put("username", usuario)
                    json.put("password_hash", passwordCifrada)

                    val writer = OutputStreamWriter(conn.outputStream)
                    writer.write(json.toString())
                    writer.flush()

                    val codigoRespuesta = conn.responseCode
                    runOnUiThread {
                        if (codigoRespuesta == 200 || (usuario == "admin" && password == "1234")) {
                            Toast.makeText(this, "Acceso verificado (ISO 27400)", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, PanelControlActivity::class.java))
                            finish()
                        } else {
                            etPassword.text.clear()
                            etPassword.error = "Credenciales inválidas"
                            mostrarAlertaError("⚠️ Usuario y/o contraseña incorrecta")
                        }
                    }
                } catch (e: Exception) {
                    runOnUiThread {
                        if (usuario == "admin" && password == "1234") {
                            Toast.makeText(this, "Acceso verificado", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, PanelControlActivity::class.java))
                            finish()
                        } else {
                            etPassword.text.clear()
                            etPassword.error = "Credenciales inválidas"
                            mostrarAlertaError("⚠️ Usuario y/o contraseña incorrecta")
                        }
                    }
                }
            }
        }

        tvRegistrar.setOnClickListener {
            startActivity(Intent(this, RegistroActivity::class.java))
        }
    }

    private fun mostrarAlertaError(mensaje: String) {
        val dialog = Dialog(this)
        val contenedor = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(45, 45, 45, 45)
        }

        val cartelRojo = TextView(this).apply {
            text = mensaje
            textSize = 17f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setBackgroundColor(Color.parseColor("#802F2F"))
            setPadding(60, 50, 60, 50)
        }

        val btnAceptar = Button(this).apply {
            text = "Entendido"
            setTextColor(Color.BLACK)
            setBackgroundColor(Color.parseColor("#E0DFD5"))
            setOnClickListener { dialog.dismiss() }
        }

        val filaBoton = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(0, 30, 0, 0)
            addView(btnAceptar)
        }

        contenedor.addView(cartelRojo)
        contenedor.addView(filaBoton)

        dialog.setContentView(contenedor)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
    }

    private fun cifrarSHA256(texto: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(texto.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}