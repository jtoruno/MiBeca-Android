package com.zimplifica.mibeca.NewArq

import android.arch.persistence.room.*
import com.amazonaws.type.NetworkStatus
import java.util.*

@Entity
data class Beneficiary( @PrimaryKey val id : String , val pk : String, @ColumnInfo(name = "citizenId") val citizenId : String, @ColumnInfo(name = "createdAt")  val createdAt : String, val hasNewDeposits : Boolean, val networkStatus : String) {

    /*
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val beneficiary = other as Beneficiary
        return Objects.equals(citizenId, beneficiary.citizenId)
    }
    */
}