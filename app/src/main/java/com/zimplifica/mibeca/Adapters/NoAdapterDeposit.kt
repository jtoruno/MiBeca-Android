package com.zimplifica.mibeca.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.zimplifica.mibeca.R
import com.zimplifica.mibeca.Utils.Deposit

class NoAdapterDeposit(context: Context, val id : String): BaseAdapter() {
    var mData = mutableListOf<Deposit>()
    private val mInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var view = convertView
        var holder : ViewHolder?
        if (view == null){
            holder = ViewHolder()
            view = mInflater.inflate(R.layout.no_deposits_message, null)
            holder.text = view!!.findViewById(R.id.textView18)
            view?.tag = holder
        }
        else{
            holder = view.tag as ViewHolder
        }

        holder?.text?.text = "El beneficiario con cédula '$id' aún no tiene depósitos registrados"

        return view
    }

    override fun getItem(position: Int): Any {
        return mData[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return 1
    }

    class ViewHolder{
        var text : TextView? = null
    }
}