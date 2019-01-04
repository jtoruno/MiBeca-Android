package com.zimplifica.mibeca.Adapters

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.support.constraint.ConstraintLayout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import com.zimplifica.mibeca.DepositsByUser
import com.zimplifica.mibeca.R
import com.zimplifica.mibeca.VerifyCode

class IdAdapter(var mCtx : Context, var resource : Int, var items : List<UserData>) : ArrayAdapter<UserData>(mCtx, resource,items) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val layoutInflater : LayoutInflater = LayoutInflater.from(mCtx)

        val view : View = layoutInflater.inflate(resource , null )

        val layout : ConstraintLayout = view.findViewById(R.id.id_row_constrain_layout)
        val textView : TextView = view.findViewById(R.id.textView17)
        var mItems = items[position]

        textView.text = "Beneficiario - "+ mItems.id
        layout.setOnClickListener {
            //Toast.makeText(mCtx,"Hello",Toast.LENGTH_SHORT).show()
            val intent = Intent(mCtx, DepositsByUser::class.java)
            intent.putExtra("idUser",mItems.id)
            //val option : ActivityOptions = ActivityOptions.makeCustomAnimation(mCtx, R.anim.abc_slide_in_bottom, R.anim.abc_slide_out_bottom)
            mCtx.startActivity(intent)

        }


        return view
    }
}