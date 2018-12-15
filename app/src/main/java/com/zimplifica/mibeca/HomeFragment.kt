package com.zimplifica.mibeca

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ListView
import com.amazonaws.mobile.client.AWSMobileClient
import com.zimplifica.mibeca.Adapters.DepositAdapter
import com.zimplifica.mibeca.Utils.Deposit

class HomeFragment : Fragment() {

    lateinit var SignOutBtn : Button
    lateinit var listView : ListView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        listView = view.findViewById(R.id.deposit_list_view)

        val adapter = DepositAdapter(activity!!)
        adapter.addHeader(Deposit("14/12/2018",""))
        adapter.addItem(Deposit("14/12/2018","FONABE"))
        adapter.addItem(Deposit("14/12/2018","FONABE"))
        adapter.addItem(Deposit("14/12/2018","FONABE"))
        adapter.addHeader(Deposit("10/12/2018",""))
        adapter.addItem(Deposit("10/12/2018","FONABE"))
        adapter.addItem(Deposit("10/12/2018","IMAS"))
        adapter.addItem(Deposit("10/12/2018","FONABE"))
        adapter.addHeader(Deposit("06/11/2018",""))
        adapter.addItem(Deposit("06/11/2018","FONABE"))
        adapter.addItem(Deposit("06/11/2018","IMAS"))

        listView.adapter = adapter

        SignOutBtn = view.findViewById(R.id.button6)
        SignOutBtn.setOnClickListener {
            AWSMobileClient.getInstance().signOut()
            val intent = Intent(activity, SignScreen::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
        }

        return view
    }

}
