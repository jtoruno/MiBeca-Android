package com.zimplifica.mibeca

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.widget.TextView
import android.widget.Toast
import java.io.IOException

class PrivacyPolicy : AppCompatActivity() {

    lateinit var textPrivacy : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_privacy_policy)
        textPrivacy = findViewById(R.id.text_privacy_policy)
        val toolbar : Toolbar = findViewById(R.id.toolbar_privacy_policy)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)

        var reader : String? = null

        try {

            val stringToUse = application.assets.open("privacyPolicy.txt").bufferedReader().use {
                it.readText()
            }
            reader = stringToUse

        }catch (e : IOException) {
            Toast.makeText(applicationContext,"Error reading file!", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
        textPrivacy.text = reader

    }
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onBackPressed() {
        finish()
    }
}
