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
import android.widget.*
import com.amazonaws.mobile.client.AWSMobileClient
import com.amazonaws.mobile.client.Callback
import com.amazonaws.mobile.client.results.SignUpResult
import com.zimplifica.mibeca.idCardReader.Person
import java.lang.Exception
import java.util.regex.Pattern

class FinishSignUp : AppCompatActivity() {
    lateinit var termnsTxt : TextView
    lateinit var userInfo : Person
    lateinit var phoneNumber : String
    lateinit var signUpBtn : Button
    lateinit var password : EditText
    lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_finish_sign_up)

        val toolbar : Toolbar = findViewById(R.id.finish_sign_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        progressBar = findViewById(R.id.progressBar)
        progressBar.visibility = View.GONE
        val dataActivity: Intent = intent
        userInfo = dataActivity.getSerializableExtra("person") as Person
        phoneNumber = dataActivity.getStringExtra("phone")
        termnsTxt = findViewById(R.id.textView10)
        password = findViewById(R.id.passwordFinishSignUp)
        signUpBtn = findViewById(R.id.button4)
        signUpBtn.setOnClickListener {
            if(validatePassword(password.text.toString())){
                progressBar.visibility = View.VISIBLE
                signUpBtn.isEnabled = false
                val citizen =  hashMapOf<String,String>()
                citizen["name"] = userInfo.nombre!!
                citizen["family_name"] = userInfo.apellido1 + " " + userInfo.apellido2
                citizen["birthdate"] = userInfo.fechaNacimiento!!
                citizen["phone_number"] = "+506$phoneNumber"
                Log.e("SignUp", userInfo.cedula + password.text.toString())
                signUp(userInfo.cedula!!,password.text.toString(),citizen)

            }
            else {
                Toast.makeText(this@FinishSignUp,"Ingrese una contraseña correcta", Toast.LENGTH_SHORT).show()
            }
        }


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
                        progressBar.visibility = View.GONE
                        signUpBtn.isEnabled = true
                        Log.e("SignUp", "Requiere confirmación")
                        Toast.makeText(this@FinishSignUp,"Error al crear la cuenta, intente de nuevo", Toast.LENGTH_SHORT).show()
                    }
                    else{
                        progressBar.visibility = View.GONE
                        signUpBtn.isEnabled = true
                        Log.e("SignUp", "Cuenta Creada correctamente")
                        Toast.makeText(this@FinishSignUp,"Cuenta creada correctamente", Toast.LENGTH_LONG).show()
                        val intent = Intent(this@FinishSignUp, SignScreen::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                        val option : ActivityOptions = ActivityOptions.makeCustomAnimation(this@FinishSignUp, R.anim.abc_slide_in_bottom, R.anim.abc_slide_out_bottom)
                        startActivity(intent, option.toBundle())
                        finish()
                    }
                }
            }

            override fun onError(e: Exception?) {
                progressBar.visibility = View.GONE
                signUpBtn.isEnabled = true
                Log.e("SignUp", e.toString())
                Toast.makeText(this@FinishSignUp,"Error al crear la cuenta, intente de nuevo", Toast.LENGTH_SHORT).show()
            }

        })
    }

    fun validatePassword(password: String):Boolean{
        val passPattern : String = "((?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[!$#@_.+-]).{8,20})"
        val pattern = Pattern.compile(passPattern)
        val matcher = pattern.matcher(password)
        return matcher.matches()

    }

}
