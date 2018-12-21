package com.zimplifica.mibeca

import android.app.ActivityOptions
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.*
import com.amazonaws.AmazonServiceException
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
    lateinit var progressBar: ProgressBar
    lateinit var forgotPassBtn : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_screen)
        signInBtn = findViewById(R.id.button)
        signUpBtn = findViewById(R.id.signUpTxt)
        progressBar = findViewById(R.id.progressBar2)
        forgotPassBtn = findViewById(R.id.textView4)
        progressBar.visibility = View.GONE
        user = findViewById(R.id.editText)
        password = findViewById(R.id.passwordEtxt)
        signUpBtn.setOnClickListener {
            val intent = Intent(this, TakePhone::class.java)
            val option : ActivityOptions = ActivityOptions.makeCustomAnimation(this, R.anim.abc_slide_in_bottom, R.anim.abc_slide_out_bottom)
            startActivity(intent, option.toBundle())
        }
        signInBtn.setOnClickListener {
            user.onEditorAction(EditorInfo.IME_ACTION_DONE)
            password.onEditorAction(EditorInfo.IME_ACTION_DONE)
            if(user.text.toString()=="" || password.text.toString()==""){
                val toastError: Toast = Toast.makeText(applicationContext,"Llene los campos requeridos", Toast.LENGTH_SHORT)
                toastError.show()
            }
            else{
                signInBtn.isEnabled = false
                progressBar.visibility = View.VISIBLE
                signIn(user.text.toString(),password.text.toString())
            }
        }

        forgotPassBtn.setOnClickListener {
            val intent = Intent(this@SignScreen, ForgotPass1::class.java)
            val option : ActivityOptions = ActivityOptions.makeCustomAnimation(this@SignScreen, R.anim.abc_slide_in_bottom, R.anim.abc_slide_out_bottom)
            startActivity(intent, option.toBundle())
        }
    }

    fun signIn(userName : String, password : String){
        AWSMobileClient.getInstance().signIn(userName,password, null, object : Callback<SignInResult>{
            override fun onResult(result: SignInResult?) {
                runOnUiThread {
                    when(result?.signInState){
                        SignInState.DONE -> {
                            signInBtn.isEnabled = true
                            progressBar.visibility = View.GONE
                            val intent = Intent(this@SignScreen, Home::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                            val option : ActivityOptions = ActivityOptions.makeCustomAnimation(this@SignScreen, R.anim.abc_slide_in_bottom, R.anim.abc_slide_out_bottom)
                            startActivity(intent, option.toBundle())
                        }
                        else -> {
                            Log.e("SignIn",result?.signInState.toString())
                            signInBtn.isEnabled = true
                            progressBar.visibility = View.GONE
                            Toast.makeText(this@SignScreen,"Error al iniciar Sesión",Toast.LENGTH_SHORT).show()
                        }

                    }
                }
            }

            override fun onError(e: Exception?) {
                runOnUiThread {
                    progressBar.visibility = View.GONE
                    signInBtn.isEnabled = true
                    //Toast.makeText(this@SignScreen,"Error al iniciar Sesión",Toast.LENGTH_SHORT).show()

                    val exception = e as AmazonServiceException
                    when(exception.errorCode){
                        "UserNotConfirmedException" ->{
                            val intent = Intent(this@SignScreen, VerifyCode::class.java)
                            intent.putExtra("userName",userName)
                            intent.putExtra("password",password)
                            val option : ActivityOptions = ActivityOptions.makeCustomAnimation(this@SignScreen, R.anim.abc_slide_in_bottom, R.anim.abc_slide_out_bottom)
                            startActivity(intent, option.toBundle())
                        }
                        else -> {
                            Toast.makeText(this@SignScreen,"Error al iniciar Sesión",Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                Log.e("SignInScreen", "Sign-in error", e)
                Log.e("SignInScreen", e?.message.toString())


            }

        })
    }
}
