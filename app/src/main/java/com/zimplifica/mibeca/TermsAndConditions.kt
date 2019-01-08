package com.zimplifica.mibeca

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.widget.TextView
import android.widget.Toast
import java.io.IOException


class TermsAndConditions : AppCompatActivity() {

    lateinit var textTermns : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_terms_and_conditions)

        val toolbar : Toolbar = findViewById(R.id.toolbar_terms_and_conditions)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        textTermns = findViewById(R.id.termstextView)

        var reader : String? = null

        try {

            val stringToUse = application.assets.open("termsAndConditions.txt").bufferedReader().use {
                it.readText()
            }
            reader = stringToUse

        }catch (e : IOException) {
            Toast.makeText(applicationContext,"Error reading file!",Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }

        textTermns.text = reader


    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onBackPressed() {
        finish()
    }
}
