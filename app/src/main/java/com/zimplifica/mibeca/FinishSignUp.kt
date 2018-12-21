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
import android.view.View
import android.widget.*
import com.amazonaws.AmazonServiceException
import com.amazonaws.mobile.client.AWSMobileClient
import com.amazonaws.mobile.client.Callback
import com.amazonaws.mobile.client.results.SignUpResult
import com.zimplifica.mibeca.idCardReader.Person
import java.lang.Exception
import java.util.regex.Pattern

class FinishSignUp : AppCompatActivity() {
    //lateinit var termnsTxt : TextView
    //lateinit var userInfo : Person
    //lateinit var phoneNumber : String
    lateinit var errorTxt : TextView
    lateinit var signUpBtn : Button
    lateinit var password : EditText
    lateinit var termnsTxt : TextView
    lateinit var progressBar: ProgressBar
    lateinit var img1 : ImageView
    lateinit var img2 : ImageView
    lateinit var img3 : ImageView
    lateinit var img4 : ImageView
    var emailAdress = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_finish_sign_up)

        val toolbar : Toolbar = findViewById(R.id.finish_sign_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        progressBar = findViewById(R.id.progressBar3)
        errorTxt = findViewById(R.id.textView22)
        errorTxt.visibility = View.GONE
        progressBar.visibility = View.GONE
        val dataActivity: Intent = intent
        emailAdress = dataActivity.getStringExtra("email")
        //userInfo = dataActivity.getSerializableExtra("person") as Person
        //phoneNumber = dataActivity.getStringExtra("phone")
        //termnsTxt = findViewById(R.id.textView10)
        password = findViewById(R.id.passwordFinishSignUp)
        termnsTxt = findViewById(R.id.textView21)
        img1 = findViewById(R.id.imageView4)
        img2 = findViewById(R.id.imageView5)
        img3 = findViewById(R.id.imageView6)
        img4 = findViewById(R.id.imageView7)
        signUpBtn = findViewById(R.id.button4)
        signUpBtn.setOnClickListener {
            if(validatePassword(password.text.toString())){
                signUpBtn.isEnabled = false
                signUp(emailAdress,password.text.toString())
            }
            else {
                Toast.makeText(this@FinishSignUp,"Ingrese una contraseña correcta", Toast.LENGTH_SHORT).show()
            }
        }

        password.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val pattern1 =Pattern.compile(".*[A-Z].*")
                val pattern2 = Pattern.compile(".*\\d.*")
                val pattern3 = Pattern.compile(".*[!\$#@_.+-].*")
                val pattern4 = Pattern.compile(".{8,20}")

                if(pattern1.matcher(s).matches()){
                    img1.setImageResource(R.drawable.check_image)
                }
                else{
                    img1.setImageResource(R.drawable.icon_close)
                }

                if(pattern2.matcher(s).matches()){
                    img2.setImageResource(R.drawable.check_image)
                }
                else{img2.setImageResource(R.drawable.icon_close)}

                if(pattern3.matcher(s).matches()){
                    img3.setImageResource(R.drawable.check_image)
                }
                else{img3.setImageResource(R.drawable.icon_close)}

                if(pattern4.matcher(s).matches()){
                    img4.setImageResource(R.drawable.check_image)
                }
                else{img4.setImageResource(R.drawable.icon_close)}

            }

        })


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

    fun validatePassword(password: String):Boolean{
        val passPattern : String = "((?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[!$#@_.+-]).{8,20})"
        val pattern = Pattern.compile(passPattern)
        val matcher = pattern.matcher(password)
        return matcher.matches()

    }

    fun signUp(userName : String, password : String ){
        AWSMobileClient.getInstance().signUp(userName,password,hashMapOf<String,String>(), null, object : Callback<SignUpResult> {
            override fun onResult(result: SignUpResult?) {
                runOnUiThread {
                    Log.e("SignUp", result?.confirmationState.toString())
                    if(!result?.confirmationState!!){
                        progressBar.visibility = View.GONE
                        signUpBtn.isEnabled = true
                        Log.e("SignUp", "Requiere confirmación")
                        val intent = Intent(this@FinishSignUp, VerifyCode::class.java)
                        intent.putExtra("userName",emailAdress)
                        intent.putExtra("password",password)
                        val option : ActivityOptions = ActivityOptions.makeCustomAnimation(this@FinishSignUp, R.anim.abc_slide_in_bottom, R.anim.abc_slide_out_bottom)
                        startActivity(intent, option.toBundle())
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
                runOnUiThread {
                    progressBar.visibility = View.GONE
                    signUpBtn.isEnabled = true
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

}
