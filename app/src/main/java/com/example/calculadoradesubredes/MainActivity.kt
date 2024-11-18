package com.example.calculadoradesubredes

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    // Inicializa la instancia de Base de Datos
    lateinit var dbHelper: DBHelper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Referencia a la base de datos
        dbHelper = DBHelper(this)

        // Referencias a los Octetos
        val editTextOcteto1 = findViewById<EditText>(R.id.Octeto1)
        val editTextOcteto2 = findViewById<EditText>(R.id.Octeto2)
        val editTextOcteto3 = findViewById<EditText>(R.id.Octeto3)
        val editTextOcteto4 = findViewById<EditText>(R.id.Octeto4)

        // Referencias a los elementos del layout
        val editTextMask = findViewById<EditText>(R.id.editTextMask)
        val buttonCalculate = findViewById<Button>(R.id.buttonCalculate)
        val buttonClear = findViewById<Button>(R.id.buttonClear)
        val textViewResult = findViewById<TextView>(R.id.textViewResult)
        // Referencia al Spinner
        val spinnerDivisiones = findViewById<Spinner>(R.id.spinnerDivisiones)
        // Referencia al RecyclerView
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        // Incializa una instancia subredAdapter con una lista vacia
        val subredAdapter = SubredAdapter(listOf())
        // Configura el RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        // Configura el adaptador, con la instancia de subredAdapter
        recyclerView.adapter = subredAdapter

        // Configura el botón para realizar el cálculo
        buttonCalculate.setOnClickListener {
        // array con los 4 cotectos transformados a enteros
            val direccionIP = IntArray(4) {
                // Condiciona que deban ser enteros para introducirlos al array
                when (it) {
                    0 -> editTextOcteto1.text.toString().toIntOrNull() ?: 0
                    1 -> editTextOcteto2.text.toString().toIntOrNull() ?: 0
                    2 -> editTextOcteto3.text.toString().toIntOrNull() ?: 0
                    3 -> editTextOcteto4.text.toString().toIntOrNull() ?: 0
                    else -> 0
                }
            }
            // almacena cada octeto introducido, en cuatro variables, para convertir a binario
            var BinarioOcteto1 = DecimalToBinary(direccionIP[0])
            var BinarioOcteto2 = DecimalToBinary(direccionIP[1])
            var BinarioOcteto3 = DecimalToBinary(direccionIP[2])
            var BinarioOcteto4 = DecimalToBinary(direccionIP[3])
            // Introduce los octetos en una lista, y los une con puntos
            val DireccionIPBinaria = listOf(BinarioOcteto1, BinarioOcteto2, BinarioOcteto3, BinarioOcteto4).joinToString(".")
            // Convierte el prefijo, a la mascara de subred en forma binaria
            // la funcion replace(), elimina el / del prefijo y deja solo el entero
            var mascara = generateSubnetMask(editTextMask.text.toString().replace("/", "").toInt())
            // Llama a la funcion calculateSubnet, para calcular la direccion de red y broadcast
            var (DireccionRed, DireccionBroadcast) = calcularRedBroadcast(DireccionIPBinaria, mascara)

            // Muestra en el Textview, las direcciones de Red y Broadcast
            textViewResult.text = """
            Dirección de Red: ${formatIp(DireccionRed)}
            Dirección de Broadcast: ${formatIp(DireccionBroadcast)}
        """.trimIndent()

            // Calcula las divisiones posibles para subtenear, para introducirlas en un spinner
            val divisiones = calcularDivisiones(editTextMask.text.toString().replace("/", "").toInt())
            // Crear y asignar un adaptador para el Spinner
            val adapter = ArrayAdapter(
                this,
                android.R.layout.simple_spinner_item, // Diseño básico para las opciones
                divisiones
            )
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerDivisiones.adapter = adapter

            // Ya con las pisibles divisiones calculadas, se configura el spinner
            spinnerDivisiones.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val seleccion = divisiones[position] // Obtiene la opción seleccionada
                    // Imprime un pequeño mensaje de aviso de las cantidad de subredes que se elegio en el spinner
                    Toast.makeText(
                        this@MainActivity,
                        "Dividir la red en $seleccion subredes",
                        Toast.LENGTH_SHORT
                    ).show()
                    // Calcula las subredes solicitadas, y guarda en la base de datos
                    calcularSubredes(DireccionRed, editTextMask.text.toString().replace("/", "").toInt(), seleccion)
                    //  Solicita la lista de subredes desde la base de datos
                    val listaSubredes = mostrarSubredes()
                    // Configura el RecyclerView
                    subredAdapter.actualizarDatos(listaSubredes)
                }
                // Opcion en caso de que no se seleccione nada (Aun no hay acciones configuradas par aeste caso)
                override fun onNothingSelected(parent: AdapterView<*>) {
                    // Acción cuando no se selecciona nada (opcional)
                }
            })
        }
        // Configura el botón para vaciar la tabla
        buttonClear.setOnClickListener {
            // Borra la tabla de subredes
            VaciarSubredes()
        }
    }

    // Función para convertir decimal a binario
    private fun DecimalToBinary(DecimalOcteto: Int): String {
        // Recibira el numero decimal en entero, y lo convertira en a binario de tipo string
        val binario : String = Integer.toBinaryString(DecimalOcteto)
        // Retorna el binario rellenando los ceros a la izquierda hasta tener 8 bits
        return String.format("%8s", binario).replace(" ", "0")
    }
    // Funcion para generar la mascara de subred a partir del prefijo introducido
    private fun generateSubnetMask(prefix: Int): Int {
        // Verificar que el prefijo esté entre 0 y 32
        require(!(prefix < 0 || prefix > 32)) { "El prefijo debe estar entre 0 y 32." }
        // Generar la máscara usando desplazamiento
        val subnetMask = (0xFFFFFFFFL shl (32 - prefix)).toInt()
        return subnetMask
    }

    // Método para convertir la máscara en binario para mostrarla como String
    private fun toBinaryString(mask: Int): String {
        return String.format("%32s", Integer.toBinaryString(mask)).replace(' ', '0')
    }

    // Función para calcular la direccion de red y broadcast
    private fun calcularRedBroadcast(ip: String, mask: Int): Pair<Int, Int> {
        // Recibira la direccion IP en formato binario tipo string punteado
        // y la mascara en formato binario tipo entero sin puntear
        // Transforma la direccion IP de tipo string a entero, y lo almacena de manera segmentda en un array
        val ipSegments = ip.split(".").map { it.toInt(2) }
        // Se operara con AND para obtener la direccion de red, comparando la ip segmentada en el array
        // con la mascara en formato binario entero sin puntear
        val networkAddress = ipSegments.foldIndexed(0) { i, acc, segment ->
            acc or (segment shl (24 - i * 8))
        } and mask

        // Se operara con OR para obtener la direccion de broadcast, comparando la direccion de red
        // con la mascara, invirtiendo sus 1 y 0
        val broadcastAddress = networkAddress or mask.inv()
        // Retorna la direccion de red y broadcast mediante la funcion Pair(), que permite devolver mas de un solo valor
        return Pair(networkAddress, broadcastAddress)
    }

    // Función auxiliar para convertir una direccion de binario sin puntear, a decimal puntado
    private fun formatIp(address: Int): String {
        return listOf(
            (address shr 24) and 0xff,
            (address shr 16) and 0xff,
            (address shr 8) and 0xff,
            address and 0xff
        ).joinToString(".")
    }

    // Función para calcular las divisiones posibles para subtenear
    fun calcularDivisiones(prefijo: Int): List<Int> {
        // Validar que el prefijo esté en un rango válido (1-30)
        if (prefijo < 1 || prefijo > 30) {
            throw IllegalArgumentException("El prefijo debe estar entre 1 y 30.")
        }
        // Número de bits disponibles para subredes
        val bitsDisponibles = 32 - prefijo
        // Generar una lista mutable con las divisiones posibles (2^1, 2^2, ..., 2^bitsDisponibles)
        val divisiones = mutableListOf<Int>()
        // Recorrer los bits disponibles con un for, calculando las divisiones
        for (i in 1..bitsDisponibles) {
            divisiones.add(2.0.pow(i).toInt()) // Elevar 2 a la potencia de i
        }
        // Retorna una lista con las divisiones posibles 2, ..., hasta cantidad maxima de subredes posibles
        return divisiones
    }

    // Función de extensión para calcular potencias fácilmente
    fun Double.pow(exp: Int): Double = Math.pow(this, exp.toDouble())

    // Funcion para calcular las subredes a partir de la direccion de red, el prefijo y la cantidad de subredes deseadas
    fun calcularSubredes(
        direccionRed: Int, // Dirección de red inicial en formato binario entero
        prefijo: Int,      // Prefijo de la red original
        subredes: Int      // Número de subredes deseadas
    ): List<Subred> {
        // Calcula el nuevo prefijo mediante operaciones matematicas
        val bitsAdicionales = Math.ceil(Math.log(subredes.toDouble()) / Math.log(2.0)).toInt()
        val nuevoPrefijo = prefijo + bitsAdicionales
        // Obtiene el nuevo tamaño de las subredes
        val tamanioSubred = 1 shl (32 - nuevoPrefijo)
        // Lista para almacenar las subredes
        val listaSubredes = mutableListOf<Subred>()

        // Guarda la direccion de red Actual
        var direccionActual = direccionRed
        // Se entra al ciclo for, para que calcule cada una de las subredes, hasta llegar al limite de subredes
        for (i in 0 until subredes) {
            // Se calcula la direccion de broadcast de la subred actual
            val direccionBroadcast = direccionActual + tamanioSubred - 1
            // Se calcula el rango de la subred, sumando 1 a la direccion de red
            // y restando 1 a la direccion de broadcast
            val rango = "${intToIp(direccionActual + 1)} - ${intToIp(direccionBroadcast - 1)}"

            // Agrega a la tabla subredes en la base de datos. Transformando antes la direccion
            // actual y la direccion broadcast a formato IP
            agregarSubred(intToIp(direccionActual), rango, intToIp(direccionBroadcast))

            // Avanza a la siguiente subred a calcular
            direccionActual += tamanioSubred
        }
        // Retorna la lista resultante
        return listaSubredes
    }

    // Convierte un entero binario a formato IP punteada
    fun intToIp(entero: Int): String {
        return "${(entero shr 24) and 0xFF}.${(entero shr 16) and 0xFF}.${(entero shr 8) and 0xFF}.${entero and 0xFF}"
    }

    // Funcion de operacion para agregar una subred a la base de datos
    private fun agregarSubred(_direccionRed: String, _rango: String, _direccionBroadcast: String) {
        val direccionRed = _direccionRed
        val rango = _rango
        val direccionBroadcast = _direccionBroadcast
        val id = dbHelper.insertarSubred(direccionRed, rango, direccionBroadcast)
    }

    // Funcion de operacion para mostrar la tabla de subredes de la base de datos
    private fun mostrarSubredes(): List<Subred> {
        val listaSubredes = dbHelper.obtenerSubredes()
        return listaSubredes
    }

    // Funcion de operacion para vaciar la tabla de subredes de la base de datos
    private fun VaciarSubredes() {
        val filasAfectadas = dbHelper.vaciarTablaSubredes()
    }
}

