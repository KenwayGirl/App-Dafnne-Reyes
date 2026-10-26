package com.example.controlseguridadtienda

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

class HistorialActivity : AppCompatActivity() {

    private val URL_BASE = "http://192.168.1.103:5000"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_historial)

        val btnActualizar = findViewById<Button>(R.id.btnActualizar)
        val btnBorrar = findViewById<Button>(R.id.btnBorrar)
        val navPanel = findViewById<LinearLayout>(R.id.navPanel)
        val navSalir = findViewById<LinearLayout>(R.id.navSalir)

        val tvAlerta1 = findViewById<TextView>(R.id.tvAlerta1)
        val tvAlerta2 = findViewById<TextView>(R.id.tvAlerta2)
        val tvAlerta3 = findViewById<TextView>(R.id.tvAlerta3)
        val cajasTexto = listOf(tvAlerta1, tvAlerta2, tvAlerta3)

        cargarHistorialDesdeMySQL(cajasTexto)

        btnActualizar.setOnClickListener {
            cargarHistorialDesdeMySQL(cajasTexto)
        }

        btnBorrar.setOnClickListener {
            thread {
                try {
                    val url = URL("$URL_BASE/borrar_historial")
                    val conn = url.openConnection() as HttpURLConnection
                    conn.requestMethod = "POST"
                    conn.connectTimeout = 2500

                    if (conn.responseCode == 200) {
                        runOnUiThread {
                            tvAlerta1.text = "Sin registros en la base de datos"
                            tvAlerta2.text = "---"
                            tvAlerta3.text = "---"
                            Toast.makeText(this, "Historial eliminado de MySQL", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    runOnUiThread {
                        tvAlerta1.text = "Sin registros locales"
                        tvAlerta2.text = "---"
                        tvAlerta3.text = "---"
                        Toast.makeText(this, "Historial limpiado en vista local", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        // Navegación Inferior -> Regresar al Panel de Control
        navPanel.setOnClickListener {
            startActivity(Intent(this, PanelControlActivity::class.java))
            overridePendingTransition(0, 0)
            finish()
        }

        // Navegación Inferior -> Mostrar ventana "¿CERRAR SESIÓN?"
        navSalir.setOnClickListener {
            mostrarDialogoCerrarSesion()
        }
    }

    private fun mostrarDialogoCerrarSesion() {
        val dialog = Dialog(this)
        val contenedor = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(40, 40, 40, 40)
        }

        val cartelRojo = TextView(this).apply {
            text = "¿CERRAR SESIÓN?"
            textSize = 20f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setBackgroundColor(Color.parseColor("#802F2F"))
            setPadding(70, 55, 70, 55)
        }

        val filaBotones = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(0, 35, 0, 0)
        }

        val btnSi = Button(this).apply {
            text = "Si"
            setTextColor(Color.BLACK)
            setBackgroundColor(Color.parseColor("#E0DFD5"))
            setOnClickListener {
                dialog.dismiss()
                startActivity(Intent(this@HistorialActivity, MainActivity::class.java))
                finish()
            }
        }

        val espacio = TextView(this).apply { text = "      " }

        val btnNo = Button(this).apply {
            text = "No"
            setTextColor(Color.BLACK)
            setBackgroundColor(Color.parseColor("#E0DFD5"))
            setOnClickListener { dialog.dismiss() }
        }

        filaBotones.addView(btnSi)
        filaBotones.addView(espacio)
        filaBotones.addView(btnNo)
        contenedor.addView(cartelRojo)
        contenedor.addView(filaBotones)

        dialog.setContentView(contenedor)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
    }

    private fun cargarHistorialDesdeMySQL(cajas: List<TextView>) {
        thread {
            try {
                val conn = URL("$URL_BASE/historial").openConnection() as HttpURLConnection
                conn.connectTimeout = 2500
                val respuesta = conn.inputStream.bufferedReader().readText()
                val jsonArray = JSONArray(respuesta)

                runOnUiThread {
                    if (jsonArray.length() == 0) {
                        cajas[0].text = "Sin alertas registradas en MySQL"
                        cajas[1].text = "---"
                        cajas[2].text = "---"
                    } else {
                        for (i in 0 until 3) {
                            if (i < jsonArray.length()) {
                                val item = jsonArray.getJSONObject(i)
                                val fecha = item.getString("fecha_hora")
                                val sensor = item.getString("tipo_sensor")
                                val detalle = item.getString("detalle_evento")
                                val nivel = item.getString("nivel_alerta")
                                val icono = if (nivel == "ROJO") "🔴" else "🟢"

                                cajas[i].text = "$icono $fecha\n$sensor: $detalle"
                            } else {
                                cajas[i].text = "---"
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                // Si el PC está apagado mantiene las 3 tarjetas de muestra
            }
        }
    }
}