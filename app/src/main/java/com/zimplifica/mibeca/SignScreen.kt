package com.zimplifica.mibeca

import android.app.ActivityOptions
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.amazonaws.mobile.client.AWSMobileClient
import com.amazonaws.mobile.client.Callback
import com.amazonaws.mobile.client.results.SignInResult
import com.amazonaws.mobile.client.results.SignInState
import java.lang.Exception
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoMfaSettings.SMS_MFA



class SignScreen : AppCompatActivity() {
    lateinit var signUpBtn : TextView
    lateinit var signInBtn : Button
    lateinit var user : EditText
    lateinit var password : EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_screen)
        signInBtn = findViewById(R.id.button)
        signUpBtn = findViewById(R.id.signUpTxt)
        user = findViewById(R.id.editText)
        password = findViewById(R.id.passwordEtxt)
        signUpBtn.setOnClickListener {
            val intent = Intent(this, IdIndications::class.java)
            val option : ActivityOptions = ActivityOptions.makeCustomAnimation(this, R.anim.abc_slide_in_bottom, R.anim.abc_slide_out_bottom)
            startActivity(intent, option.toBundle())
        }
        signInBtn.setOnClickListener {
            if(user.text.toString()=="" || password.text.toString()==""){
                val toastError: Toast = Toast.makeText(applicationContext,"Llene los campos requeridos", Toast.LENGTH_SHORT)
                toastError.show()
            }
            else{
                signIn(user.text.toString(),password.text.toString())
            }
        }
    }

    fun signIn(userName : String, password : String){
        AWSMobileClient.getInstance().signIn(userName,password, null, object : Callback<SignInResult>{
            override fun onResult(result: SignInResult?) {
                runOnUiThread {
                    when(result?.signInState){
                        SignInState.DONE -> {
                            val intent = Intent(this@SignScreen, Home::class.java)
                            val option : ActivityOptions = ActivityOptions.makeCustomAnimation(this@SignScreen, R.anim.abc_slide_in_bottom, R.anim.abc_slide_out_bottom)
                            startActivity(intent, option.toBundle())
                        }
                        else -> {
                            Toast.makeText(this@SignScreen,"Error al iniciar Sesión",Toast.LENGTH_SHORT).show()
                        }

                    }
                }
            }

            override fun onError(e: Exception?) {
                Log.e("SignInScreen", "Sign-in error", e)
            }

        })
    }
}
