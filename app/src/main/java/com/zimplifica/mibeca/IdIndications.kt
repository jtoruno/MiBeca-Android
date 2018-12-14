package com.zimplifica.mibeca

import android.app.ActivityOptions
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import com.google.zxing.integration.android.IntentIntegrator
import com.zimplifica.mibeca.idCardReader.CedulaCR
import com.zimplifica.mibeca.idCardReader.Person
import com.zimplifica.mibeca.idCardReader.ScanerCedulasCR
import java.util.*

class IdIndications : AppCompatActivity() {

    lateinit var scanBtn : Button
    internal var TYPES: Collection<String> = Arrays.asList("PDF417")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_id_indications)
        scanBtn = findViewById(R.id.button2)

        val toolbar : Toolbar = findViewById(R.id.id_indications_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
    }
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onBackPressed() {
        finish()
    }

    fun scanBarcode(view: View) {
        IntentIntegrator(this)
                .setOrientationLocked(false)
                //.addExtra("", Intents.Scan.INVERTED_SCAN)
                .setCaptureActivity(ScanerCedulasCR::class.java)
                .setDesiredBarcodeFormats(TYPES)
                .initiateScan()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents == null) {
                Log.d("MainActivity", "Scaneo cancelado")
                //Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show()
            } else {
                try {
                    val p: Person

                    //Workarround debido a que result.getRawBytes() devuelve un array nulo
                    val d = ByteArray(result.contents.length)
                    for (i in d.indices) {
                        d[i] = result.contents.codePointAt(i).toByte()
                    }

                    p = CedulaCR.parse(d)!!


                    val intent = Intent(this, UserInfo::class.java)
                    intent.putExtra("person",p)
                    val option : ActivityOptions = ActivityOptions.makeCustomAnimation(this, R.anim.abc_slide_in_bottom, R.anim.abc_slide_out_bottom)
                    startActivity(intent, option.toBundle())
                    finish()


                    //Toast.makeText(applicationContext, p.toString(), Toast.LENGTH_SHORT).show()
                    Log.d("MainActivity", "Scaneado")
                    //Toast.makeText(this, p.toString(), Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    //Toast.makeText(this, "Error: No se pudo hacer el parse"+e.toString(), Toast.LENGTH_LONG).show();
                }

            }
        } else {
            // This is important, otherwise the result will not be passed to the fragment
            super.onActivityResult(requestCode, resultCode, data)
        }
    }
}
