package com.zimplifica.mibeca

import android.app.ActivityOptions
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView

class SignScreen : AppCompatActivity() {
    lateinit var signUpBtn : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_screen)
        signUpBtn = findViewById(R.id.signUpTxt)
        signUpBtn.setOnClickListener {
            val intent = Intent(this, IdIndications::class.java)
            val option : ActivityOptions = ActivityOptions.makeCustomAnimation(this, R.anim.abc_slide_in_bottom, R.anim.abc_slide_out_bottom)
            startActivity(intent, option.toBundle())
        }
    }
}
