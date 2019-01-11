package com.zimplifica.mibeca.NewArq

import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.zimplifica.mibeca.R

class BeneficiaryListAdapter(val beneficiary : List<Beneficiary>,val callback: (Beneficiary) -> Unit) : RecyclerView.Adapter<BeneficiaryListAdapter.BeneficiaryViewHolder>() {

    private var mBeneficiaries = beneficiary

    fun setBeneficiaries(beneficiary : List<Beneficiary>){
        mBeneficiaries = beneficiary
        for (n in beneficiary) {
            Log.e("NewsListAdapter", "setNews: " + n.citizenId)
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): BeneficiaryViewHolder {
        val v = LayoutInflater.from(p0.context)
                .inflate(R.layout.layout_id_row, p0, false)
        return BeneficiaryViewHolder(v)
    }

    override fun getItemCount(): Int {
        return mBeneficiaries.size
    }

    override fun onBindViewHolder(p0: BeneficiaryViewHolder, p1: Int) {
        val data = mBeneficiaries[p1]
        p0.text.text = data.citizenId
        if(!mBeneficiaries[p1].hasNewDeposits){
            p0.newText.visibility = View.GONE
        }
        else{
            p0.newText.visibility = View.VISIBLE
        }
        p0.itemView.setOnClickListener {
            Log.e("PBA","CLICKED "+itemCount)
            callback(mBeneficiaries[p1])
        }
    }

    class BeneficiaryViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
        var text: TextView = itemView.findViewById(R.id.textView17)
        var newText = itemView.findViewById<TextView>(R.id.textView24)
    }
}