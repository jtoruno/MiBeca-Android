package com.zimplifica.mibeca

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.amazonaws.mobile.client.AWSMobileClient
import com.amazonaws.mobile.client.Callback
import com.amazonaws.mobile.client.results.ForgotPasswordResult
import com.amazonaws.mobile.client.results.ForgotPasswordState
import java.lang.Exception
import java.util.regex.Pattern

class ForgotPass2 : AppCompatActivity() {

    lateinit var nextBtn : Button
    lateinit var password : EditText
    lateinit var code : EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_pass2)

        val toolbar : Toolbar = findViewById(R.id.forgotpass2toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)

        nextBtn = findViewById(R.id.button9)
        password = findViewById(R.id.passForgotEdtxt)
        code = findViewById(R.id.codeForgotTxt)
        nextBtn.isEnabled = false

        nextBtn.setOnClickListener {
            if (!validatePassword(password.text.toString())){
                Toast.makeText(this@ForgotPass2,"Ingrese una contraseña correcta.", Toast.LENGTH_LONG).show()
            }
            else {
                nextBtn.isEnabled = false
                forgotPassContinue(password.text.toString(), code.text.toString())
            }
        }

        code.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                nextBtn.isEnabled = s?.length == 6
            }

        })


    }

    fun forgotPassContinue( newPass : String, code : String ){
        AWSMobileClient.getInstance().confirmForgotPassword(newPass,code, object : Callback<ForgotPasswordResult>{
            override fun onResult(result: ForgotPasswordResult?) {
                runOnUiThread {
                    Log.d("Forgotpass2", "forgot password state: " + result?.state)
                    when(result?.state){
                        ForgotPasswordState.DONE -> {
                            Toast.makeText(this@ForgotPass2,"Contraseña cambiada correctamente.", Toast.LENGTH_LONG).show()
                            val intent = Intent(this@ForgotPass2, SignScreen::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(intent)
                        }
                        else -> {
                            nextBtn.isEnabled = true
                            Toast.makeText(this@ForgotPass2,"No se pudo cambiar la ontraseña. Intente nuevamente.", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }

            override fun onError(e: Exception?) {
                Log.e("ForgotPass2", "forgot password error", e)
                runOnUiThread {
                    nextBtn.isEnabled = true
                    Toast.makeText(this@ForgotPass2,"No se pudo cambiar la ontraseña. Intente nuevamente.", Toast.LENGTH_LONG).show()
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

    fun validatePassword(password: String):Boolean{
        val passPattern : String = "((?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[!$#@_.+-]).{8,20})"
        val pattern = Pattern.compile(passPattern)
        val matcher = pattern.matcher(password)
        return matcher.matches()

    }
}
