package com.zimplifica.mibeca.idCardReader

import kotlin.experimental.xor

object CedulaCR {
    private val keysArray = byteArrayOf(0x27.toByte(), 0x30.toByte(), 0x04.toByte(), 0xA0.toByte(), 0x00.toByte(), 0x0F.toByte(), 0x93.toByte(), 0x12.toByte(), 0xA0.toByte(), 0xD1.toByte(), 0x33.toByte(), 0xE0.toByte(), 0x03.toByte(), 0xD0.toByte(), 0x00.toByte(), 0xDf.toByte(), 0x00.toByte())

    fun parse(raw: ByteArray): Person? {
        var d = ""
        var j = 0
        for (i in raw.indices) {
            if (j == 17) {
                j = 0
            }
            val c = (keysArray[j] xor raw[i].toChar().toByte()).toChar()
            //System.out.println("Char "+c +"  "+ "Code " + (int)c );
            if ((c + "").matches("^[a-zA-Z0-9]*$".toRegex())) {
                d += c
            } else if (c.toInt() == 65489 || c.toInt() == 165) {
                d += 'Ñ'.toString()

            } else {
                d += ' '.toString()
            }
            j++
        }
        var p: Person? = Person()
        try {
            p?.cedula =(d.substring(0, 9).trim { it <= ' ' })
            p?.apellido1= (d.substring(9, 35).trim { it <= ' ' })
            p?.apellido2= (d.substring(35, 61).trim { it <= ' ' })
            p?.nombre= (d.substring(61, 91).trim { it <= ' ' })
            p?.genero= (d[91])
            p?.fechaNacimiento = (d.substring(98, 100)  + "/" + d.substring(96, 98) + "/" +d.substring(92, 96) )
            p?.fechaVencimiento = (d.substring(106, 108)  + "/" + d.substring(104, 106) + "/" + d.substring(100, 104))
        } catch (e: Exception) {
            p = null
        }

        return p
    }
}