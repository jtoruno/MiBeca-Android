package com.zimplifica.mibeca.NewArq

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.ViewModelProvider
import android.content.Context
import android.util.Log
import javax.inject.Inject

class BeneficiaryViewModel(val context: Context) : ViewModel() {
    private var beneficiary: LiveData<Beneficiary>? = null
    private var beneficiaryRepo: BeneficiaryRepository? = null


    @Inject
    fun BeneficiaryViewModel(){
        this.beneficiaryRepo = BeneficiaryRepository()
    }

    fun init() {
        this.beneficiaryRepo = BeneficiaryRepository()
        beneficiaryRepo?.getInstance(context)
        if (this.beneficiary != null) {
            return
        }
        beneficiary = beneficiaryRepo?.getBeneficiary()
    }

    fun getBeneficiary(): LiveData<List<Beneficiary>> {
        this.beneficiaryRepo = BeneficiaryRepository()
        beneficiaryRepo?.getInstance(context)
        return beneficiaryRepo!!.getBeneficiaries()
    }

}

class MyViewModelFactory(val context: Context) : ViewModelProvider.NewInstanceFactory(){
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return BeneficiaryViewModel(context) as T
    }
}

