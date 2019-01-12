package com.zimplifica.mibeca

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import com.amazonaws.mobile.client.AWSMobileClient
import android.widget.TextView
import com.amazonaws.mobile.auth.core.internal.util.ThreadUtils.runOnUiThread
import com.amazonaws.mobile.client.Callback
import com.amazonaws.mobile.client.UserState
import com.amazonaws.mobile.client.UserStateDetails
import java.lang.Exception
import android.view.WindowManager
import android.app.Activity
import android.arch.lifecycle.ProcessLifecycleOwner


class MyApp: Application() {

    private val lifecycleListener : SampleLifecycleListener by lazy {
        SampleLifecycleListener()
    }



    override fun onCreate() {
        super.onCreate()
        setupLifecycleListener()


        val nMessageReceiver = object  : BroadcastReceiver(){
            override fun onReceive(context: Context?, intent: Intent?) {
                /*
                val array = intent?.getSerializableExtra("data") as HashMap<String,String>
                val iterator = array.iterator()
                while (iterator.hasNext()){
                    val value = iterator.next()
                    Log.e("App", value.toString())
                }
                */
                Log.e("App",intent?.getStringExtra("from") )
                Log.e("App", intent?.extras.toString())

                val am = context!!.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                val cn = am.getRunningTasks(1)[0].topActivity
                println(cn)

                val m = Intent(this@MyApp, FragmentTryActivity::class.java)
                m.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(m)

                //showDialog(baseContext)
            }

        }
        //LocalBroadcastManager.getInstance(this).registerReceiver(nMessageReceiver,  IntentFilter("push-notification"))


    }

    private fun setupLifecycleListener(){
        ProcessLifecycleOwner.get().lifecycle
                .addObserver(lifecycleListener)
    }


    fun showDialog(context: Context){
        val builder = AlertDialog.Builder(context)
                .setCancelable(true)
                .setMessage("Example")
                .setTitle("Hello")
                .setPositiveButton("Cerrar"){
                    dialog, which ->
                    dialog.dismiss()
                }
        val dialog = builder.create()
        //dialog.window.setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT)
        dialog.show()


    }

}