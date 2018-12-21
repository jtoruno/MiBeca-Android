package com.zimplifica.mibeca

import android.app.ActivityOptions
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.amazonaws.mobile.client.AWSMobileClient
import com.amazonaws.mobile.client.Callback
import com.amazonaws.mobile.client.results.ForgotPasswordResult
import com.amazonaws.mobile.client.results.ForgotPasswordState
import com.zimplifica.mibeca.Utils.MaskEditText
import java.lang.Exception

class ForgotPass1 : AppCompatActivity() {

    lateinit var idNumber : EditText
    lateinit var nextBtn : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_pass1)

        val toolbar : Toolbar = findViewById(R.id.ForgotPass1_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)

        idNumber = findViewById(R.id.idNumberForgotpass)
        nextBtn = findViewById(R.id.button8)
        nextBtn.isEnabled = false

        idNumber.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                nextBtn.isEnabled = Patterns.EMAIL_ADDRESS.matcher(s).matches()
            }
        })

        nextBtn.setOnClickListener {
            nextBtn.isEnabled = false
            forgotPass(idNumber.text.toString())
        }

    }

    fun forgotPass(userName : String){
        AWSMobileClient.getInstance().forgotPassword(userName, object : Callback<ForgotPasswordResult>{
            override fun onResult(result: ForgotPasswordResult?) {
                when(result?.state){
                    ForgotPasswordState.CONFIRMATION_CODE -> {
                        runOnUiThread {
                            val intent = Intent(this@ForgotPass1, ForgotPass2::class.java)
                            val option : ActivityOptions = ActivityOptions.makeCustomAnimation(this@ForgotPass1, R.anim.abc_slide_in_bottom, R.anim.abc_slide_out_bottom)
                            startActivity(intent, option.toBundle())
                            finish()
                        }
                    }
                    else -> {
                        runOnUiThread {
                            nextBtn.isEnabled = true
                            Toast.makeText(this@ForgotPass1,"Error con la generación del código", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }

            override fun onError(e: Exception?) {
                runOnUiThread {
                    nextBtn.isEnabled = true
                    Toast.makeText(this@ForgotPass1,"Error con la generación del código", Toast.LENGTH_SHORT).show()
                }
                Log.e("ForgotPass1", "forgot password error", e)
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
