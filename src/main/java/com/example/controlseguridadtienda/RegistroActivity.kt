package com.example.controlseguridadtienda

import android.app.Dialog
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

class RegistroActivity : AppCompatActivity() {

    private val URL_BASE = "http://192.168.1.103:5000"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro)

        val etRegUsuario = findViewById<EditText>(R.id.etRegUsuario)
        val etRegPassword = findViewById<EditText>(R.id.etRegPassword)
        val etRegConfirmar = findViewById<EditText>(R.id.etRegConfirmar)
        val btnRegistrarCuenta = findViewById<Button>(R.id.btnRegistrarCuenta)
        val tvVolverLogin = findViewById<TextView>(R.id.tvVolverLogin)

        btnRegistrarCuenta.setOnClickListener {
            val usuario = etRegUsuario.text.toString().trim()
            val pass = etRegPassword.text.toString().trim()
            val confirmar = etRegConfirmar.text.toString().trim()

            // 1. Alerta si algún campo está vacío
            if (usuario.isEmpty() || pass.isEmpty() || confirmar.isEmpty()) {
                if (usuario.isEmpty()) etRegUsuario.error = "Campo obligatorio"
                if (pass.isEmpty()) etRegPassword.error = "Campo obligatorio"
                if (confirmar.isEmpty()) etRegConfirmar.error = "Campo obligatorio"
                mostrarAlerta("⚠️ Por favor complete todos los campos", false)
                return@setOnClickListener
            }

            // 2. Alerta si tiene menos de 6 caracteres
            if (pass.length < 6) {
                etRegPassword.error = "Mínimo 6 caracteres (actual: ${pass.length})"
                mostrarAlerta("⚠️ La contraseña es muy corta.\nDebe tener al menos 6 caracteres.", false)
                return@setOnClickListener
            }

            // 3. Alerta si puso más de 20 caracteres
            if (pass.length > 20) {
                etRegPassword.error = "Máximo 20 caracteres"
                mostrarAlerta("⚠️ La contraseña no puede superar los 20 caracteres.", false)
                return@setOnClickListener
            }

            // 4. Alerta si las contraseñas no coinciden
            if (pass != confirmar) {
                etRegConfirmar.error = "Las contraseñas no coinciden"
                etRegConfirmar.text.clear()
                mostrarAlerta("⚠️ Las contraseñas ingresadas no coinciden.\nPor favor verifíquelas.", false)
                return@setOnClickListener
            }

            val hashGenerado = cifrarSHA256(pass)

            // Envío directo a MySQL mediante la API en Python
            thread {
                try {
                    val url = URL("$URL_BASE/registrar")
                    val conn = url.openConnection() as HttpURLConnection
                    conn.requestMethod = "POST"
                    conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                    conn.connectTimeout = 2500
                    conn.doOutput = true

                    val json = JSONObject()
                    json.put("username", usuario)
                    json.put("password_hash", hashGenerado)

                    val writer = OutputStreamWriter(conn.outputStream)
                    writer.write(json.toString())
                    writer.flush()

                    val codigo = conn.responseCode
                    runOnUiThread {
                        if (codigo == 200) {
                            mostrarAlerta("✅ ¡Usuario '$usuario' registrado con éxito!\n(Cifrado SHA-256 en MySQL)", true)
                        } else {
                            etRegUsuario.error = "Usuario ya registrado"
                            mostrarAlerta("⚠️ El nombre de usuario '$usuario' ya existe en la Base de Datos.", false)
                        }
                    }
                } catch (e: Exception) {
                    runOnUiThread {
                        mostrarAlerta("⚠️ Error de conexión con el servidor MySQL.", false)
                    }
                }
            }
        }

        tvVolverLogin.setOnClickListener {
            finish()
        }
    }

    private fun mostrarAlerta(mensaje: String, cerrarAlAceptar: Boolean) {
        val dialog = Dialog(this)
        val contenedor = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(45, 45, 45, 45)
        }

        val colorFondo = if (cerrarAlAceptar) "#435739" else "#802F2F"

        val cartel = TextView(this).apply {
            text = mensaje
            textSize = 16f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setBackgroundColor(Color.parseColor(colorFondo))
            setPadding(55, 50, 55, 50)
        }

        val btnAceptar = Button(this).apply {
            text = "Aceptar"
            setTextColor(Color.BLACK)
            setBackgroundColor(Color.parseColor("#E0DFD5"))
            setOnClickListener {
                dialog.dismiss()
                if (cerrarAlAceptar) {
                    Toast.makeText(this@RegistroActivity, "Ahora puede iniciar sesión", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }

        val filaBoton = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(0, 30, 0, 0)
            addView(btnAceptar)
        }

        contenedor.addView(cartel)
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