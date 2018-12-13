package com.zimplifica.mibeca

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.amazonaws.mobile.client.AWSMobileClient

class MainActivity : AppCompatActivity() {
    //https://docs.aws.amazon.com/es_es/aws-mobile/latest/developerguide/getting-started.html

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        /*
        AWSMobileClient.getInstance().initialize(this){
            Log.e("Main", "AWSMobileClient is initialized")
        }.execute()
        */
    }
}
