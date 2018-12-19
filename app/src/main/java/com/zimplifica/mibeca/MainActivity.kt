package com.zimplifica.mibeca

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.amazonaws.mobile.client.AWSMobileClient
import com.amazonaws.mobile.client.Callback
import com.amazonaws.mobile.client.UserState
import com.amazonaws.mobile.client.UserStateDetails
import com.amazonaws.mobileconnectors.pinpoint.PinpointManager
import com.google.firebase.iid.InstanceIdResult
import com.google.android.gms.tasks.Task
import android.support.annotation.NonNull
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId
import com.amazonaws.mobileconnectors.pinpoint.PinpointConfiguration





class MainActivity : AppCompatActivity() {
    //https://docs.aws.amazon.com/es_es/aws-mobile/latest/developerguide/getting-started.html

    val TAG = MainActivity::class.java.simpleName
    lateinit var pinpointManager: PinpointManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        /*
        AWSMobileClient.getInstance().initialize(this){
            Log.e("Main", "AWSMobileClient is initialized")
        }.execute()
        */

        AWSMobileClient.getInstance().initialize(applicationContext, object : Callback<UserStateDetails> {
            override fun onResult(result: UserStateDetails?) {
                Log.e("App", result?.userState.toString())
                when(result?.userState){
                    UserState.SIGNED_IN-> runOnUiThread{
                        val intent = Intent(applicationContext, Home::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)

                    }
                    UserState.SIGNED_OUT , UserState.GUEST-> runOnUiThread {
                        val intent = Intent(applicationContext, SignScreen::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                    }


                    else -> AWSMobileClient.getInstance().signOut()
                }
            }

            override fun onError(e: Exception?) {
                Log.e("INIT", e.toString());
            }

        })

        //GetPinPointmanager
        pinpointManager = getPinpointManager(applicationContext)

    }


    companion object {
        fun getPinpointManager(applicationContext: Context): PinpointManager {


                AWSMobileClient.getInstance().initialize(applicationContext, object : Callback<UserStateDetails> {
                    override fun onResult(userStateDetails: UserStateDetails) {
                        Log.i("INIT", userStateDetails.userState.toString())
                    }

                    override fun onError(e: Exception) {
                        Log.e("INIT", "Initialization error.", e)
                    }
                })

                val pinpointConfig = PinpointConfiguration(
                        applicationContext,
                        AWSMobileClient.getInstance(),
                        AWSMobileClient.getInstance().configuration)

                val pinpointManager = PinpointManager(pinpointConfig)

                FirebaseInstanceId.getInstance().instanceId
                        .addOnCompleteListener { task ->
                            val token = task.result!!.token
                            Log.d("MainActivity", "Registering push notifications token: $token")
                            pinpointManager!!.notificationClient.registerDeviceToken(token)
                        }

            return pinpointManager
        }
    }




}
