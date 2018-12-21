package com.zimplifica.mibeca

import android.app.ActivityOptions
import android.content.Intent
import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.text.*
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.*
import com.amazonaws.AmazonServiceException
import com.amazonaws.mobile.client.AWSMobileClient
import com.amazonaws.mobile.client.Callback
import com.amazonaws.mobile.client.results.SignUpResult
import com.zimplifica.mibeca.Utils.MaskEditText
import com.zimplifica.mibeca.idCardReader.Person
import java.lang.Exception

class TakePhone : AppCompatActivity() {

    lateinit var nextBtn : Button
    lateinit var info : Person
    lateinit var email : EditText
    //lateinit var progressBar: ProgressBar
    private var password = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_take_phone)
        email = findViewById(R.id.emailEtxtSignUp)



        //val dataActivity: Intent = intent
        //info = dataActivity.getSerializableExtra("person") as Person
        //password = dataActivity.getStringExtra("password")

        //progressBar = findViewById(R.id.progressBar3)
        //progressBar.visibility = View.GONE

        val toolbar : Toolbar = findViewById(R.id.toolbar_take_phone)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        nextBtn = findViewById(R.id.button5)
        nextBtn.isEnabled = false

        email.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                nextBtn.isEnabled = Patterns.EMAIL_ADDRESS.matcher(s).matches()
            }

        })

        nextBtn.setOnClickListener {
            //progressBar.visibility = View.VISIBLE
            //nextBtn.isEnabled = false
            /*
            val citizen =  hashMapOf<String,String>()
            citizen["name"] = info.nombre!!
            citizen["family_name"] = info.apellido1 + " " + info.apellido2
            citizen["birthdate"] = info.fechaNacimiento!!
            citizen["email"] = email.text.toString()
            Log.e("SignUp", info.cedula + password)
            //signUp(info.cedula!!,password,citizen)
            */

            val intent = Intent(this, FinishSignUp::class.java)
            intent.putExtra("email",email.text.toString())
            //intent.putExtra("password", password.text.toString())
            //intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            val option : ActivityOptions = ActivityOptions.makeCustomAnimation(this, R.anim.abc_slide_in_bottom, R.anim.abc_slide_out_bottom)
            startActivity(intent, option.toBundle())
        }

    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onBackPressed() {
        finish()
    }

    /*

    fun signUp(userName : String, password : String,attributes : HashMap<String, String> ){
        AWSMobileClient.getInstance().signUp(userName,password,attributes, null, object : Callback<SignUpResult> {
            override fun onResult(result: SignUpResult?) {
                runOnUiThread {
                    Log.e("SignUp", result?.confirmationState.toString())
                    if(!result?.confirmationState!!){
                        progressBar.visibility = View.GONE
                        nextBtn.isEnabled = true
                        Log.e("SignUp", "Requiere confirmación")
                        val intent = Intent(this@TakePhone, VerifyCode::class.java)
                        intent.putExtra("userName",info.cedula!!)
                        intent.putExtra("password",password)
                        val option : ActivityOptions = ActivityOptions.makeCustomAnimation(this@TakePhone, R.anim.abc_slide_in_bottom, R.anim.abc_slide_out_bottom)
                        startActivity(intent, option.toBundle())
                    }
                    else{
                        progressBar.visibility = View.GONE
                        nextBtn.isEnabled = true
                        Log.e("SignUp", "Cuenta Creada correctamente")
                        Toast.makeText(this@TakePhone,"Cuenta creada correctamente", Toast.LENGTH_LONG).show()
                        val intent = Intent(this@TakePhone, SignScreen::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                        val option : ActivityOptions = ActivityOptions.makeCustomAnimation(this@TakePhone, R.anim.abc_slide_in_bottom, R.anim.abc_slide_out_bottom)
                        startActivity(intent, option.toBundle())
                        finish()
                    }
                }
            }

            override fun onError(e: Exception?) {
                runOnUiThread {
                    progressBar.visibility = View.GONE
                    nextBtn.isEnabled = true
                    Log.e("SignUp", e.toString())
                    //Toast.makeText(this@TakePhone,"Error al crear la cuenta, intente de nuevo", Toast.LENGTH_SHORT).show()
                    val exception = e as AmazonServiceException
                    when(exception.errorCode){
                        "UsernameExistsException" -> {
                            errorTxt.visibility = View.VISIBLE
                            errorTxt.postDelayed(Runnable { errorTxt.setVisibility(View.GONE) }, 5000)
                        }
                    }

                }

            }

        })
    }
    */


    //Fun for validate a valid phone with a specific pattern
    fun validatePhone(phone: String):Boolean{
        val first = phone.substring(0,1)
        println(first)
        return !(phone.length!=8 || first=="0" || first=="1" || first=="2" || first=="3"
                || first=="4" || first=="9")
    }


}
