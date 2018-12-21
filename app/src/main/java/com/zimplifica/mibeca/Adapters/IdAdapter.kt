package com.zimplifica.mibeca.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.zimplifica.mibeca.R

class IdAdapter(var mCtx : Context, var resource : Int, var items : List<UserData>) : ArrayAdapter<UserData>(mCtx, resource,items) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val layoutInflater : LayoutInflater = LayoutInflater.from(mCtx)

        val view : View = layoutInflater.inflate(resource , null )

        val textView : TextView = view.findViewById(R.id.textView17)
        var mItems = items[position]

        textView.text = mItems.id

        return view
    }
}