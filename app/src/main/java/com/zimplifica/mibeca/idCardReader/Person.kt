package com.zimplifica.mibeca.idCardReader

import java.io.Serializable

class Person : Serializable {
    var cedula: String? = null
    var nombre: String? = null
    var apellido1: String? = null
    var apellido2: String? = null
    var genero: Char = ' '
    var fechaNacimiento: String? = null
    var fechaVencimiento: String? = null

    internal constructor() {

    }

    internal constructor(_ced: String, _nom: String, _ape1: String, _ape2: String, _gen: Char, _fecN: String, _fecV: String) {
        this.cedula = _ced
        this.nombre = _nom
        this.apellido1 = _ape1
        this.apellido2 = _ape2
        this.genero = _gen
        this.fechaNacimiento = _fecN
        this.fechaVencimiento = _fecV
    }

    override fun toString(): String {
        return this.cedula + " " +
                this.apellido1 + " " +
                this.apellido2 + " " +
                this.nombre + " " +
                this.fechaNacimiento + " " +
                this.fechaVencimiento
    }
}