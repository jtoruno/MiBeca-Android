package com.zimplifica.mibeca.NewArq

import android.arch.lifecycle.LiveData
import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient
import android.arch.persistence.room.Room
import android.content.Context
import android.util.Log
import com.amazonaws.GetSubscriptionsQuery
import com.amazonaws.mobile.client.AWSMobileClient
import com.amazonaws.mobile.config.AWSConfiguration
import com.amazonaws.mobileconnectors.appsync.fetcher.AppSyncResponseFetchers
import com.amazonaws.mobileconnectors.appsync.sigv4.CognitoUserPoolsAuthProvider
import com.amazonaws.type.NetworkStatus
import com.apollographql.apollo.GraphQLCall
import com.apollographql.apollo.api.Operation
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.api.ResponseField
import com.apollographql.apollo.cache.normalized.CacheKey
import com.apollographql.apollo.cache.normalized.CacheKeyResolver
import com.apollographql.apollo.exception.ApolloException
import java.text.ParseException
import javax.inject.Singleton

@Singleton
class BeneficiaryRepository  {


    private val TAG : String = BeneficiaryRepository::class.java.simpleName
    private var beneficiaryDao: BeneficiaryDao? = null
    private var client : AWSAppSyncClient? = null

    fun getInstance(context: Context) {
        //beneficiaryDao = Room.databaseBuilder(context.applicationContext,BeneficiaryDatabase::class.java, "beneficiary-database").build().beneficiaryDao()
        beneficiaryDao = BeneficiaryDatabase.getInstance(context)?.beneficiaryDao()
        client = AWSAppSyncClient.builder()
                .context(context)
                .awsConfiguration(AWSConfiguration(context))
                .credentialsProvider(AWSMobileClient.getInstance())
                .resolver(object : CacheKeyResolver(){

                    private fun formatCacheKey(id : String?): CacheKey{
                        return if (id == null || id.isEmpty()){
                            CacheKey.NO_KEY
                        } else{
                            CacheKey.from(id)
                        }
                    }

                    override fun fromFieldRecordSet(field: ResponseField, recordSet: MutableMap<String, Any>): CacheKey {

                        val id =  recordSet["id"] as? String
                        return formatCacheKey(id)
                    }

                    override fun fromFieldArguments(field: ResponseField, variables: Operation.Variables): CacheKey {
                        val id =  field.resolveArgument("id", variables) as String
                        return formatCacheKey(id)
                    }

                })
                .build()
    }



    fun getBeneficiaries() : LiveData<List<Beneficiary>> {
        //refreshBeneficiary()
        checkIfOffline()
        return beneficiaryDao!!.list()
    }

    fun getBeneficiary() : LiveData<Beneficiary>{

        refreshBeneficiary()
        return beneficiaryDao!!.load()
    }

    private fun checkIfOffline() {
        val query = GetSubscriptionsQuery.builder()
                .build()
        client!!.query(query).responseFetcher(AppSyncResponseFetchers.CACHE_ONLY).enqueue(object : GraphQLCall.Callback<GetSubscriptionsQuery.Data>(){
            override fun onFailure(e: ApolloException) {
                Log.e("Home", "Failed to cache items ", e)
            }

            override fun onResponse(response: Response<GetSubscriptionsQuery.Data>) {
                Log.e("RepositoryCache",response.data().toString())
                if(response.hasErrors()){
                    Log.e(TAG, "onResponse: errors:" + response.errors())
                    return
                }
                var checkOffline = false
                val list = mutableListOf<Beneficiary>()
                val items = arrayListOf<GetSubscriptionsQuery.Item>()
                if(response.data()!=null){
                    items.addAll(response.data()!!.subscriptions.items())
                }
                val iterator = items.iterator()
                while (iterator.hasNext()){
                    val oldValue = iterator.next()
                    list.add(Beneficiary(oldValue.id(),oldValue.pk(), oldValue.citizenId(), oldValue.createdAt(), oldValue.hasNewDeposits(), oldValue.networkStatus().toString()))
                    if(oldValue.networkStatus()== NetworkStatus.offline || oldValue.networkStatus()==NetworkStatus.offlineDeleted){
                        checkOffline = true
                    }
                }
                beneficiaryDao!!.deleteAll()
                beneficiaryDao!!.saveList(list)
                if(!checkOffline){
                    refreshBeneficiary()
                }
                else{
                    Log.e("BeneficiaryRepo", "Offline things to do")
                }

            }
        })
    }

    private fun refreshBeneficiary(){
        val query = GetSubscriptionsQuery.builder().build()
        client!!.query(query).responseFetcher(AppSyncResponseFetchers.NETWORK_ONLY).enqueue(object : GraphQLCall.Callback<GetSubscriptionsQuery.Data>(){
            override fun onFailure(e: ApolloException) {
                Log.e(TAG, "Failed to refresh news item", e)
            }

            override fun onResponse(response: Response<GetSubscriptionsQuery.Data>) {
                if(response.hasErrors()){
                    Log.e(TAG, "onResponse: errors:" + response.errors())
                    return
                }
                Log.d(TAG, "onResponse: accessing data")
                val list = mutableListOf<Beneficiary>()
                Log.e("BeneficiaryRepo", response.data().toString())
                val items = response.data()!!.subscriptions.items()
                val iterate = items.iterator()
                while (iterate?.hasNext()!!){
                    val oldValue = iterate.next()
                    list.add(Beneficiary(oldValue.id(),oldValue.pk(), oldValue.citizenId(), oldValue.createdAt(), oldValue.hasNewDeposits(), oldValue.networkStatus().toString()))
                }
                /*
                try {

                }catch (e : ParseException){
                    e.printStackTrace()
                }
                */
                val it2 = list.iterator()
                while (it2.hasNext()){
                    val oldValue = it2.next()
                    Log.e("List to show", oldValue.citizenId)
                }
                beneficiaryDao!!.deleteAll()
                beneficiaryDao!!.saveList(list)

            }

        })
    }

}

