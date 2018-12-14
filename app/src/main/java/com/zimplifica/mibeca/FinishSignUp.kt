package com.zimplifica.mibeca

import android.app.ActivityOptions
import android.content.Intent
import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.View
import android.widget.TextView
import com.amazonaws.mobile.client.AWSMobileClient
import com.amazonaws.mobile.client.Callback
import com.amazonaws.mobile.client.results.SignUpResult
import java.lang.Exception

class FinishSignUp : AppCompatActivity() {
    lateinit var termnsTxt : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_finish_sign_up)

        val toolbar : Toolbar = findViewById(R.id.finish_sign_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)

        termnsTxt = findViewById(R.id.textView10)

        //Clickeable text
        val ss = SpannableString(resources.getString(R.string.signUpTermsText))
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(textView: View) {
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = true
            }
        }

        val clickableSpanPrivacity = object : ClickableSpan() {
            override fun onClick(textView: View) {
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = true
            }
        }
        ss.setSpan(clickableSpan,40,62, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        ss.setSpan(clickableSpanPrivacity,69,92, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        termnsTxt.setText(ss)
        termnsTxt.movementMethod = LinkMovementMethod.getInstance()
        termnsTxt.highlightColor = Color.TRANSPARENT
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onBackPressed() {
        finish()
    }

    fun signUp(userName : String, password : String,attributes : HashMap<String, String> ){
        AWSMobileClient.getInstance().signUp(userName,password,attributes, null, object : Callback<SignUpResult>{
            override fun onResult(result: SignUpResult?) {
                runOnUiThread {
                    if(!result?.confirmationState!!){
                        Log.e("SignUp", "Requiere confirmación")
                    }
                    else{
                        Log.e("SignUp", "Cuenta Creada correctamente")
                    }
                }
            }

            override fun onError(e: Exception?) {
                Log.e("SignUp", e.toString())
            }

        })
    }

}
