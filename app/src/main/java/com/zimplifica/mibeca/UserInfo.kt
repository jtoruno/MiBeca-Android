package com.zimplifica.mibeca

import android.app.ActivityOptions
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.zimplifica.mibeca.Utils.MaskEditText
import com.zimplifica.mibeca.idCardReader.Person

class UserInfo : AppCompatActivity() {
    lateinit var info : Person
    lateinit var datePicker : EditText
    lateinit var name: EditText
    lateinit var lastName: EditText
    lateinit var cedIdentifier: MaskEditText
    lateinit var nextBtn : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_info)

        val toolbar : Toolbar = findViewById(R.id.user_info_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
        nextBtn = findViewById(R.id.button3)

        val dataActivity: Intent = intent
        info = dataActivity.getSerializableExtra("person") as Person

        datePicker = findViewById(R.id.dateEtxt)
        name = findViewById(R.id.editText2)
        lastName = findViewById(R.id.editText3)
        cedIdentifier = findViewById(R.id.idEtxt)

        if(info!=null){
            name.setText(info.nombre, TextView.BufferType.EDITABLE)
            cedIdentifier.setText(info.cedula, TextView.BufferType.EDITABLE)
            lastName.setText(info.apellido1 + " "+ info.apellido2, TextView.BufferType.EDITABLE)
            datePicker.setText(info.fechaNacimiento, TextView.BufferType.EDITABLE)
        }

        nextBtn.setOnClickListener {
            val intent = Intent(this, FinishSignUp::class.java)
            intent.putExtra("person",info)
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
}
