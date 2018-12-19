package com.zimplifica.mibeca

import android.app.Activity
import android.os.Bundle
import android.R.string.cancel
import android.app.AlertDialog
import android.content.*
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.content.DialogInterface




class NotifyReceived :  Activity() {

    val ACTION = "push-notification"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.e("Not","INIT")

        displayAlert()

        val Builder = AlertDialog.Builder(this)
                .setMessage("Do You Want continue ?")
                .setTitle("exit")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setNegativeButton("No"){
                    dialog, which ->
                    //dialog.dismiss()
                    this.finish()
                }
                .setPositiveButton("yes", null)
        val alertDialog = Builder.create()
        //alertDialog.show()

        //val filter = IntentFilter(ACTION)
        //this.registerReceiver(mReceivedSMSReceiver , filter)
        //LocalBroadcastManager.getInstance(this).registerReceiver(mReceivedSMSReceiver, filter)
    }

    private fun displayAlert() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Are you sure you want to exit?").setCancelable(
                false).setPositiveButton("Yes"
        ) { dialog, id -> dialog.cancel() }.setNegativeButton("No"
        ) { dialog, id -> dialog.cancel() }
        val alert = builder.create()
        alert.show()
    }



    val mReceivedSMSReceiver = object : BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent?.action
            Log.e("Re", action)
            if(ACTION == action){
                displayAlert()
            }
        }

    }
}