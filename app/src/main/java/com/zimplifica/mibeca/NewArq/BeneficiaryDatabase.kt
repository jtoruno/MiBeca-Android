package com.zimplifica.mibeca.NewArq

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.content.Context

@Database(entities = arrayOf(Beneficiary::class), version = 2)
abstract class BeneficiaryDatabase : RoomDatabase() {
    abstract fun beneficiaryDao() : BeneficiaryDao

    companion object {
        private var INSTANCE : BeneficiaryDatabase? = null

        fun getInstance(context: Context) : BeneficiaryDatabase?{
            if(INSTANCE==null){
                synchronized(BeneficiaryDatabase::class){
                    INSTANCE = Room.databaseBuilder(context.applicationContext, BeneficiaryDatabase::class.java, "beneficiary2.db").build()
                }
            }
            return INSTANCE
        }

        fun destroyInstance(){
            INSTANCE = null
        }
    }
}