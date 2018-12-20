package com.zimplifica.mibeca

import android.app.ActivityOptions
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import com.amazonaws.mobile.client.AWSMobileClient
import com.amazonaws.mobile.client.Callback
import com.amazonaws.mobile.client.results.SignInResult
import com.amazonaws.mobile.client.results.SignInState
import com.amazonaws.mobile.client.results.SignUpResult
import java.lang.Exception

class VerifyCode : AppCompatActivity() {

    lateinit var code : EditText
    lateinit var reSendCode : Button
    lateinit var nextBtn : Button
    lateinit var progressBar : ProgressBar
    var userName = ""
    var password = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verify_code)

        val toolbar : Toolbar = findViewById(R.id.code_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)

        val dataActivity : Intent = intent
        userName = dataActivity.getStringExtra("userName")
        password = dataActivity.getStringExtra("password")

        code = findViewById(R.id.CodeEditText)
        reSendCode = findViewById(R.id.button6)
        nextBtn = findViewById(R.id.button7)
        progressBar = findViewById(R.id.progressBar)
        progressBar.visibility = View.GONE

        nextBtn.setOnClickListener {
            nextBtn.isEnabled = false
            progressBar.visibility = View.VISIBLE
            confirmSignUp(code.text.toString(),userName)
        }

        reSendCode.setOnClickListener {
            reSendCode(userName)
        }

    }

    fun confirmSignUp(code : String, user : String){
        AWSMobileClient.getInstance().confirmSignUp(user,code, object : Callback<SignUpResult>{
            override fun onResult(result: SignUpResult?) {
                runOnUiThread {
                    if(!result!!.confirmationState){
                        progressBar.visibility = View.GONE
                        nextBtn.isEnabled = true
                        val details = result?.userCodeDeliveryDetails
                        Log.e("VerifyCode", details.destination)
                        Toast.makeText(this@VerifyCode,"Confirmación Incorrecta, intente de nuevo",Toast.LENGTH_SHORT)
                    }
                    else{
                        signIn(userName, password)
                    }
                }
            }

            override fun onError(e: Exception?) {
                Log.e("Verify Code", "Confirm sign-up error", e)
                runOnUiThread {
                    progressBar.visibility = View.GONE
                    nextBtn.isEnabled = true
                }
            }

        })
    }

    fun signIn(user : String, password : String){
        AWSMobileClient.getInstance().signIn(user, password, null, object : Callback<SignInResult>{
            override fun onResult(result: SignInResult?) {
                runOnUiThread {
                    when(result?.signInState){
                        SignInState.DONE -> {
                            val intent = Intent(this@VerifyCode, Home::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                            val option : ActivityOptions = ActivityOptions.makeCustomAnimation(this@VerifyCode, R.anim.abc_slide_in_bottom, R.anim.abc_slide_out_bottom)
                            startActivity(intent, option.toBundle())
                        }
                        else -> {
                            progressBar.visibility = View.GONE
                            nextBtn.isEnabled = true
                            Log.e("SignIn",result?.signInState.toString())
                            //Toast.makeText(this@VerifyCode,"Error al iniciar Sesión", Toast.LENGTH_SHORT).show()
                            val intent = Intent(applicationContext, SignScreen::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(intent)

                        }
                    }
                }
            }

            override fun onError(e: Exception?) {
                Log.e("SignInScreen", "Sign-in error", e)
                Log.e("SignInScreen", e?.message.toString())

                runOnUiThread {
                    //Toast.makeText(this@VerifyCode,"Error al iniciar Sesión", Toast.LENGTH_SHORT).show()
                    progressBar.visibility = View.GONE
                    nextBtn.isEnabled = true
                    val intent = Intent(applicationContext, SignScreen::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)

                }
            }

        })
    }

    fun reSendCode(userName : String){
        AWSMobileClient.getInstance().resendSignUp(userName, object : Callback<SignUpResult>{
            override fun onResult(result: SignUpResult?) {
                Log.e("VerifyCode", "CodeSend")
                runOnUiThread {
                    Toast.makeText(this@VerifyCode,"Código Reenviado", Toast.LENGTH_SHORT).show()
                }

            }

            override fun onError(e: Exception?) {
                runOnUiThread {
                    Log.e("VerifyCode", e.toString())
                    Toast.makeText(this@VerifyCode,"Error al re-enviar código", Toast.LENGTH_SHORT).show()
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
