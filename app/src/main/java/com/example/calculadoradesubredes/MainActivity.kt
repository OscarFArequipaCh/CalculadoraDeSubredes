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
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Referencias a los Octetos
        val editTextOcteto1 = findViewById<EditText>(R.id.Octeto1)
        val editTextOcteto2 = findViewById<EditText>(R.id.Octeto2)
        val editTextOcteto3 = findViewById<EditText>(R.id.Octeto3)
        val editTextOcteto4 = findViewById<EditText>(R.id.Octeto4)

        // Referencias a los elementos del layout
        val editTextMask = findViewById<EditText>(R.id.editTextMask)
        val buttonCalculate = findViewById<Button>(R.id.buttonCalculate)
        val textViewResult = findViewById<TextView>(R.id.textViewResult)
        // Referencia al Spinner
        val spinnerDivisiones = findViewById<Spinner>(R.id.spinnerDivisiones)



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
            // val DecimalOcteto1 = editTextOcteto1.text.toString().toInt()
            var BinarioOcteto1 = DecimalToBinary(direccionIP[0])
            var BinarioOcteto2 = DecimalToBinary(direccionIP[1])
            var BinarioOcteto3 = DecimalToBinary(direccionIP[2])
            var BinarioOcteto4 = DecimalToBinary(direccionIP[3])

            val DireccionIPBinaria = listOf(BinarioOcteto1, BinarioOcteto2, BinarioOcteto3, BinarioOcteto4).joinToString(".")
            // Convierte el prefijo, a la mascara de subred en forma bianria
            // la funcion replace(), elimina el / del prefijo y deja solo el entero
            var mascara = generateSubnetMask(editTextMask.text.toString().replace("/", "").toInt())
            // Rellena los ceros a la izquierda de la mascara
            var mascaraBinaria = toBinaryString(mascara)

            var DireccionRed = calculateSubnet(DireccionIPBinaria, mascara)

            // Muestra el resultado de la conversion a binario
            //textViewResult.text = "$BinarioOcteto1\n$BinarioOcteto2\n$BinarioOcteto3\n$BinarioOcteto4\n$mascaraBinaria"
            //textViewResult.text = "$DireccionIPBinaria\n$mascaraBinaria"
            textViewResult.text = DireccionRed


            // Calcula las divisiones posibles para subtenear
            val divisiones = calcularDivisiones(editTextMask.text.toString().replace("/", "").toInt())
            // Crear y asignar un adaptador para el Spinner
            val adapter = ArrayAdapter(
                this,
                android.R.layout.simple_spinner_item, // Diseño básico para las opciones
                divisiones
            )
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerDivisiones.adapter = adapter

            // Manejar la selección de opciones
            spinnerDivisiones.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val seleccion = divisiones[position] // Obtiene la opción seleccionada
                    Toast.makeText(
                        this@MainActivity,
                        "Dividir la red en $seleccion subredes",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                    // Acción cuando no se selecciona nada (opcional)
                }
            })
        }

    }

    // Función para convertir decimal a binario
    private fun DecimalToBinary(DecimalOcteto: Int): String {
        // Recibira el numero decimal en entero, y lo convertira en a binario de tipo string
        val binario : String = Integer.toBinaryString(DecimalOcteto)
        // Retorna el binario rellenando los ceros a la izquierda hasta tener 8 bits
        return String.format("%8s", binario).replace(" ", "0")
    }

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
    private fun calculateSubnet(ip: String, mask: Int): String {
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

        // Se devolvera un string, que tendra la direccion de red y broadcast, pero llamando antes a
        // la funcion formatoIp, para que las transforme a decimal punteado
        return """
            Dirección de Red: ${formatIp(networkAddress)}
            Dirección de Broadcast: ${formatIp(broadcastAddress)}
        """.trimIndent()
        /*return """
            Dirección de Red: ${toBinaryString(networkAddress)}
            Dirección de Broadcast: ${toBinaryString(broadcastAddress)}
            Dirección IP: ${ipSegments[0]}.${ipSegments[1]}.${ipSegments[2]}.${ipSegments[3]}
            Dirección IP Input: ${ip}
            Mascara Input: ${toBinaryString(mask)}
        """.trimIndent()*/
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


    fun calcularDivisiones(prefijo: Int): List<Int> {
        // Validar que el prefijo esté en un rango válido (1-30)
        if (prefijo < 1 || prefijo > 30) {
            throw IllegalArgumentException("El prefijo debe estar entre 1 y 30.")
        }

        // Número de bits disponibles para subredes
        val bitsDisponibles = 32 - prefijo

        // Generar una lista con las divisiones posibles (2^1, 2^2, ..., 2^bitsDisponibles)
        val divisiones = mutableListOf<Int>()
        for (i in 1..bitsDisponibles) {
            divisiones.add(2.0.pow(i).toInt()) // Elevar 2 a la potencia de i
        }

        return divisiones
    }

    // Función de extensión para calcular potencias fácilmente
    fun Double.pow(exp: Int): Double = Math.pow(this, exp.toDouble())





    /*fun ipDecimalToBinary(ip: String): String {
        // Divide la IP en sus octetos
        val octets = ip.split(".")
        if (octets.size != 4) {
            throw IllegalArgumentException("Dirección IP no válida")
        }

        // Convierte cada octeto a binario y completa con ceros a la izquierda hasta tener 8 bits
        val binaryOctets = octets.map { octet ->
            val decimal = octet.toInt()
            String.format("%8s", Integer.toBinaryString(decimal)).replace(" ", "0")
        }

        // Une los octetos en formato binario separados por puntos
        return binaryOctets.joinToString(".")
    }*/

}

