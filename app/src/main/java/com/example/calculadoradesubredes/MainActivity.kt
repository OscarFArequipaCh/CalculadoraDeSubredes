package com.example.calculadoradesubredes

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
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

            // Convierte el prefijo, a la mascara de subred en forma bianria
            // la funcion replace(), elimina el / del prefijo y deja solo el entero
            var mascara = generateSubnetMask(editTextMask.text.toString().replace("/", "").toInt())
            // Rellena los ceros a la izquierda de la mascara
            var mascaraBinaria = toBinaryString(mascara)

            // Muestra el resultado de la conversion a binario
            textViewResult.text = "$BinarioOcteto1\n$BinarioOcteto2\n$BinarioOcteto3\n$BinarioOcteto4\n$mascaraBinaria"
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



    // Función para calcular la subred
    private fun calculateSubnet(ip: String, cidr: Int): String {
        // Lógica de conversión básica (simplificada para ilustración)
        val ipSegments = ip.split(".").map { it.toInt() }
        val mask = (0xffffffff shl (32 - cidr)).toInt()

        val networkAddress = ipSegments.foldIndexed(0) { i, acc, segment ->
            acc or (segment shl (24 - i * 8))
        } and mask

        val broadcastAddress = networkAddress or mask.inv()

        // Resultado en formato humano
        return """
            Dirección de Red: ${formatIp(networkAddress)}
            Dirección de Broadcast: ${formatIp(broadcastAddress)}
        """.trimIndent()
    }

    // Función auxiliar para convertir int a IP en formato string
    private fun formatIp(address: Int): String {
        return listOf(
            (address shr 24) and 0xff,
            (address shr 16) and 0xff,
            (address shr 8) and 0xff,
            address and 0xff
        ).joinToString(".")
    }
    fun ipDecimalToBinary(ip: String): String {
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
    }

}

