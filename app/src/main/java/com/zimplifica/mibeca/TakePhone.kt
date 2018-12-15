package com.zimplifica.mibeca

import android.app.ActivityOptions
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.zimplifica.mibeca.Utils.MaskEditText
import com.zimplifica.mibeca.idCardReader.Person

class TakePhone : AppCompatActivity() {

    lateinit var nextBtn : Button
    lateinit var info : Person
    lateinit var phone : MaskEditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_take_phone)
        phone = findViewById(R.id.phoneSignUpEtxt)

        val dataActivity: Intent = intent
        info = dataActivity.getSerializableExtra("person") as Person

        val toolbar : Toolbar = findViewById(R.id.toolbar_take_phone)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        nextBtn = findViewById(R.id.button5)
        nextBtn.setOnClickListener {
            if(!validatePhone(phone.rawText.toString())){
                Toast.makeText(this,"Ingrese un número válido", Toast.LENGTH_SHORT).show()
            }
            else {
                val intent = Intent(this, FinishSignUp::class.java)
                intent.putExtra("person",info)
                intent.putExtra("phone",phone.rawText.toString())
                val option : ActivityOptions = ActivityOptions.makeCustomAnimation(this, R.anim.abc_slide_in_bottom, R.anim.abc_slide_out_bottom)
                startActivity(intent, option.toBundle())
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onBackPressed() {
        finish()
    }
    //Fun for validate a valid phone with a specific pattern
    fun validatePhone(phone: String):Boolean{
        val first = phone.substring(0,1)
        println(first)
        return !(phone.length!=8 || first=="0" || first=="1" || first=="2" || first=="3"
                || first=="4" || first=="9")
    }
}
