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
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.concurrent.thread

class PanelControlActivity : AppCompatActivity() {

    private var conectadoBluetooth = false
    private val URL_BASE = "http://192.168.1.103:5000"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_panel_control)

        val tvEstadoConexion = findViewById<TextView>(R.id.tvEstadoConexion)
        val btnVincular = findViewById<Button>(R.id.btnVincular)
        val tvSensorPir = findViewById<TextView>(R.id.tvSensorPir)
        val tvSensorSonido = findViewById<TextView>(R.id.tvSensorSonido)
        val btnSubirPersiana = findViewById<Button>(R.id.btnSubirPersiana)
        val btnBajarPersiana = findViewById<Button>(R.id.btnBajarPersiana)
        val btnSilenciarAlarma = findViewById<Button>(R.id.btnSilenciarAlarma)

        val navHistorial = findViewById<LinearLayout>(R.id.navHistorial)
        val navSalir = findViewById<LinearLayout>(R.id.navSalir)

        btnVincular.setOnClickListener {
            conectadoBluetooth = !conectadoBluetooth
            if (conectadoBluetooth) {
                tvEstadoConexion.text = "Estado: Conectado (Bluetooth) 🟢"
                Toast.makeText(this, "Conectado a módulo Bluetooth Arduino", Toast.LENGTH_SHORT).show()
                guardarAlertaEnMySQL("Sistema", "Bluetooth vinculado", "VERDE")
            } else {
                tvEstadoConexion.text = "Estado: Desconectado (Bluetooth) 🔴"
                Toast.makeText(this, "Arduino desconectado", Toast.LENGTH_SHORT).show()
            }
        }

        tvSensorPir.setOnClickListener {
            tvSensorPir.text = "Sensor PIR\n¡MOVIMIENTO!"
            guardarAlertaEnMySQL("Sensor PIR", "Movimiento detectado", "ROJO")
        }

        tvSensorSonido.setOnClickListener {
            tvSensorSonido.text = "Sensor Sonido\n¡RUIDO FUERTE!"
            guardarAlertaEnMySQL("Sensor Sonido", "Ruido fuerte (Persiana Bajada)", "ROJO")
        }

        btnSubirPersiana.setOnClickListener {
            Toast.makeText(this, "Subiendo Persiana (Servo 0°)", Toast.LENGTH_SHORT).show()
            guardarAlertaEnMySQL("Control Persiana", "Persiana subida (0°)", "VERDE")
        }

        btnBajarPersiana.setOnClickListener {
            Toast.makeText(this, "Bajando Persiana (Servo 90°)", Toast.LENGTH_SHORT).show()
            guardarAlertaEnMySQL("Control Persiana", "Persiana bajada (90°)", "ROJO")
        }

        btnSilenciarAlarma.setOnClickListener {
            tvSensorPir.text = "Sensor PIR\nSIN MOVIMIENTO"
            tvSensorSonido.text = "Sensor Sonido\nNORMAL (Silencio)"
            Toast.makeText(this, "Sirena apagada y sensores reiniciados", Toast.LENGTH_SHORT).show()
        }

        // Navegación Inferior -> Ir a Historial
        navHistorial.setOnClickListener {
            startActivity(Intent(this, HistorialActivity::class.java))
            overridePendingTransition(0, 0)
            finish()
        }

        // Navegación Inferior -> Mostrar ventana "¿CERRAR SESIÓN?" del Mockup
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
                startActivity(Intent(this@PanelControlActivity, MainActivity::class.java))
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

    private fun guardarAlertaEnMySQL(sensor: String, detalle: String, nivel: String) {
        val fechaActual = SimpleDateFormat("dd/MM/yyyy - HH:mm 'hrs'", Locale.getDefault()).format(Date())
        thread {
            try {
                val url = URL("$URL_BASE/alerta")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                conn.connectTimeout = 2500
                conn.doOutput = true

                val json = JSONObject()
                json.put("fecha_hora", fechaActual)
                json.put("tipo_sensor", sensor)
                json.put("detalle_evento", detalle)
                json.put("nivel_alerta", nivel)

                val writer = OutputStreamWriter(conn.outputStream)
                writer.write(json.toString())
                writer.flush()

                if (conn.responseCode == 200) {
                    runOnUiThread {
                        Toast.makeText(this, "Alerta guardada en MySQL", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this, "Alerta registrada en modo local", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}