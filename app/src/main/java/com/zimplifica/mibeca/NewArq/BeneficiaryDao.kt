package com.zimplifica.mibeca.NewArq

import android.arch.persistence.room.OnConflictStrategy.REPLACE
import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*


@Dao
interface BeneficiaryDao {
    @Insert(onConflict = REPLACE)
    fun save(beneficiary: Beneficiary)
    @Insert(onConflict = REPLACE)
    fun saveList(list : List<Beneficiary>)
    @Query("SELECT * FROM Beneficiary")
    fun load() : LiveData<Beneficiary>
    @Query("SELECT * FROM Beneficiary")
    fun list(): LiveData<List<Beneficiary>>
    @Query("DELETE from Beneficiary")
    fun deleteAll()
    @Delete
    fun deleteBeneficiary(beneficiary: Beneficiary)
    @Query("DELETE FROM Beneficiary WHERE id = :citizenIdDelete")
    fun deleteById(citizenIdDelete : String)
    @Update
    fun update(beneficiary: Beneficiary)
    @Query("UPDATE Beneficiary SET hasNewDeposits = :status WHERE id = :citId")
    fun updateDepositStatus(citId : String, status : Boolean)
}