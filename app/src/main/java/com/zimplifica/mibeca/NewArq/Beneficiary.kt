package com.zimplifica.mibeca.NewArq

import android.arch.persistence.room.*
import java.util.*

@Entity
data class Beneficiary( val pk : String, @ColumnInfo(name = "citizenId") val citizenId : String, @ColumnInfo(name = "createdAt") @PrimaryKey val createdAt : String) {

    /*
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val beneficiary = other as Beneficiary
        return Objects.equals(citizenId, beneficiary.citizenId)
    }
    */
}