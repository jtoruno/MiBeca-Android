package com.zimplifica.mibeca.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.zimplifica.mibeca.R
import com.zimplifica.mibeca.Utils.Deposit
import java.util.*

class DepositAdapter(context: Context): BaseAdapter() {

    private val TYPE_ITEM = 0
    private val TYPE_HEADER = 1
    private val mInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    var mData = mutableListOf<Deposit>()
    var sectionHeader = TreeSet<Int>()

    fun addItem(item : Deposit){
        mData.add(item)
        notifyDataSetChanged()
    }

    fun addHeader(item : Deposit){
        mData.add(item)
        sectionHeader.add(mData.size-1)
        notifyDataSetChanged()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var view  = convertView
        var holder: ViewHolder?
        val rowType = getItemViewType(position)

        if(view == null){
            holder = ViewHolder()
            when(rowType){
                TYPE_ITEM -> {
                    view = mInflater.inflate(R.layout.deposit_row, null)
                    holder.textDescription = view.findViewById(R.id.textView15)
                    holder.textDate = view.findViewById(R.id.textView14)
                }
                TYPE_HEADER -> {
                    view = mInflater.inflate(R.layout.header_item_mov, null)
                    holder.textDate = view.findViewById(R.id.textSeparator)
                }
            }
            view?.tag = holder

        }
        else {
            holder = view.tag as ViewHolder
        }

        if(rowType == TYPE_ITEM){
            holder.textDate?.text = mData[position].date
            holder.textDescription?.text = mData[position].description
        }
        else if(rowType == TYPE_HEADER){
            holder.textDate?.text = mData[position].date
        }
        return view!!
    }

    override fun getItem(position: Int): Any {
        return mData[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return mData.size
    }

    override fun getViewTypeCount(): Int {
        return 2
    }

    override fun getItemViewType(position: Int): Int {
        return if (sectionHeader.contains(position)) TYPE_HEADER else TYPE_ITEM
    }

    class ViewHolder {
        var textDate : TextView? = null
        var textDescription : TextView? = null
    }
}