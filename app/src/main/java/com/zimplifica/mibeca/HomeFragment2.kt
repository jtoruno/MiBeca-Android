package com.zimplifica.mibeca

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import com.zimplifica.mibeca.Adapters.IdAdapter
import com.zimplifica.mibeca.Adapters.UserData


class HomeFragment2 : Fragment() {
    lateinit var recyclerView: ListView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        val view = inflater.inflate(R.layout.fragment_home_fragment2, container, false)
        recyclerView = view.findViewById(R.id.RecyclerViewFragment)

        val list = mutableListOf<UserData>()
        list.add(UserData("1 1650 0454"))
        list.add(UserData("1 1652 1234"))
        list.add(UserData("1 1657 9999"))
        val adapter = IdAdapter(activity!!,R.layout.layout_id_row,list)
        recyclerView.adapter = adapter
        return view
    }

}
