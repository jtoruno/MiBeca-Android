package com.zimplifica.mibeca

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.util.Log
import android.widget.TextView
import com.amazonaws.GetUserInfoQuery
import com.amazonaws.mobile.client.AWSMobileClient
import com.amazonaws.mobile.config.AWSConfiguration
import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient
import com.amazonaws.mobileconnectors.appsync.fetcher.AppSyncResponseFetchers
import com.apollographql.apollo.GraphQLCall
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException

class AccountInfo : AppCompatActivity() {

    lateinit var appSyncClient : AWSAppSyncClient
    lateinit var name : TextView
    lateinit var lastName : TextView
    lateinit var idNumber : TextView
    lateinit var birthDate : TextView
    lateinit var phoneNumber : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_info)

        name = findViewById(R.id.nameAccountInfo)
        lastName = findViewById(R.id.LastnameAccountInfo)
        idNumber = findViewById(R.id.idNumberAccountInfo)
        birthDate = findViewById(R.id.birthDateAccountInfo)
        phoneNumber = findViewById(R.id.phoneNumberAccountInfo)

        val toolbar : Toolbar = findViewById(R.id.account_info_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)

        appSyncClient = AWSAppSyncClient.builder()
                .context(applicationContext)
                .awsConfiguration(AWSConfiguration(applicationContext))
                .credentialsProvider(AWSMobileClient.getInstance())
                .build()

        userData()
    }

    fun userData(){

        val query = GetUserInfoQuery.builder()
                .username(AWSMobileClient.getInstance().username)
                .build()
        appSyncClient.query(query).responseFetcher(AppSyncResponseFetchers.CACHE_ONLY).enqueue( object : GraphQLCall.Callback<GetUserInfoQuery.Data>(){
            override fun onFailure(e: ApolloException) {
                Log.e("ERROR", e.toString())
            }

            override fun onResponse(response: Response<GetUserInfoQuery.Data>) {
                Log.i("Home", response.data().toString())
                runOnUiThread {
                    if(response.data()!=null){
                        name.text = response.data()!!.userInfo.name()
                        lastName.text = response.data()!!.userInfo.family_name()
                        idNumber.text = response.data()!!.userInfo.username()
                        birthDate.text = response.data()!!.userInfo.birthdate()
                        phoneNumber.text = response.data()!!.userInfo.phone_number()
                    }
                }

            }

        })
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onBackPressed() {
        finish()
    }
}
