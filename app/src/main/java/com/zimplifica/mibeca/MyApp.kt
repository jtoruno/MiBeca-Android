package com.zimplifica.mibeca

import android.app.ActivityOptions
import android.app.Application
import android.content.Intent
import android.util.Log
import com.amazonaws.mobile.client.AWSMobileClient
import android.widget.TextView
import com.amazonaws.mobile.auth.core.internal.util.ThreadUtils.runOnUiThread
import com.amazonaws.mobile.client.Callback
import com.amazonaws.mobile.client.UserState
import com.amazonaws.mobile.client.UserStateDetails
import java.lang.Exception


class MyApp: Application() {
    override fun onCreate() {
        super.onCreate()
        AWSMobileClient.getInstance().initialize(applicationContext, object : Callback<UserStateDetails> {
            override fun onResult(result: UserStateDetails?) {
                Log.e("App", result?.userState.toString())
                when(result?.userState){
                    UserState.SIGNED_IN-> runOnUiThread{
                        val intent = Intent(applicationContext, Home::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        startActivity(intent)

                    }
                    UserState.SIGNED_OUT -> runOnUiThread {
                        val intent = Intent(applicationContext, SignScreen::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        startActivity(intent)
                    }


                    else -> AWSMobileClient.getInstance().signOut()
                }
            }

            override fun onError(e: Exception?) {
                Log.e("INIT", e.toString());
            }

        })

    }
}