from flask import Flask, request, jsonify
import mysql.connector

app = Flask(__name__)

# Configuración de la conexión local al Workbench
def conectar_bd():
    return mysql.connector.connect(
        host="localhost",
        user="root",
        password="123456",
        database="seguridad_tienda_iot",
        port=3306
    )

# 1. Ruta para Registrar Usuarios 
@app.route('/registrar', methods=['POST'])
def registrar_usuario():
    datos = request.json
    username = datos.get('username')
    password_hash = datos.get('password_hash')
    
    try:
        conexion = conectar_bd()
        cursor = conexion.cursor()
        cursor.execute(
            "INSERT INTO usuarios (username, password_hash) VALUES (%s, %s)",
            (username, password_hash)
        )
        conexion.commit()
        cursor.close()
        conexion.close()
        return jsonify({"estado": "ok", "mensaje": "Usuario guardado en MySQL"}), 200
    except Exception as e:
        return jsonify({"estado": "error", "mensaje": str(e)}), 400

# 2. Ruta para Iniciar Sesión 
@app.route('/login', methods=['POST'])
def login_usuario():
    datos = request.json
    username = datos.get('username')
    password_hash = datos.get('password_hash')
    
    conexion = conectar_bd()
    cursor = conexion.cursor(dictionary=True)
    cursor.execute(
        "SELECT * FROM usuarios WHERE username = %s AND password_hash = %s",
        (username, password_hash)
    )
    usuario = cursor.fetchone()
    cursor.close()
    conexion.close()
    
    if usuario:
        return jsonify({"estado": "ok", "mensaje": "Login correcto"}), 200
    else:
        return jsonify({"estado": "error", "mensaje": "Credenciales incorrectas"}), 401

# 3. Ruta para Consultar el Historial de Alertas (Pantalla 3)
@app.route('/historial', methods=['GET'])
def obtener_historial():
    conexion = conectar_bd()
    cursor = conexion.cursor(dictionary=True)
    cursor.execute("SELECT * FROM historial_alertas ORDER BY id_alerta DESC")
    alertas = cursor.fetchall()
    cursor.close()
    conexion.close()
    return jsonify(alertas), 200

# 4. Ruta para Insertar una Nueva Alerta desde los Sensores (Pantalla 2)
@app.route('/alerta', methods=['POST'])
def registrar_alerta():
    datos = request.json
    conexion = conectar_bd()
    cursor = conexion.cursor()
    cursor.execute(
        "INSERT INTO historial_alertas (fecha_hora, tipo_sensor, detalle_evento, nivel_alerta) VALUES (%s, %s, %s, %s)",
        (datos.get('fecha_hora'), datos.get('tipo_sensor'), datos.get('detalle_evento'), datos.get('nivel_alerta'))
    )
    conexion.commit()
    cursor.close()
    conexion.close()
    return jsonify({"estado": "ok"}), 200

# 5. Ruta para Borrar el Historial (Botón Rojo Pantalla 3)
@app.route('/borrar_historial', methods=['POST'])
def borrar_historial():
    conexion = conectar_bd()
    cursor = conexion.cursor()
    cursor.execute("TRUNCATE TABLE historial_alertas")
    conexion.commit()
    cursor.close()
    conexion.close()
    return jsonify({"estado": "ok", "mensaje": "Historial borrado de MySQL"}), 200

if __name__ == '__main__':
    # host='0.0.0.0' permite que el dispositivo se conecte al Notebook
    app.run(host='0.0.0.0', port=5000, debug=True)