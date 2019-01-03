package com.zimplifica.mibeca

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ListView
import com.amazonaws.mobile.auth.core.internal.util.ThreadUtils.runOnUiThread
import com.amazonaws.mobile.client.AWSMobileClient
import com.amazonaws.mobile.config.AWSConfiguration
import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient
import com.amazonaws.mobileconnectors.appsync.fetcher.AppSyncResponseFetchers
import com.amazonaws.mobileconnectors.appsync.sigv4.CognitoUserPoolsAuthProvider
import com.apollographql.apollo.GraphQLCall
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException
import com.zimplifica.mibeca.Adapters.DepositAdapter
import com.zimplifica.mibeca.Adapters.NoAdapter
import com.zimplifica.mibeca.Utils.Deposit
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {

    lateinit var listView : ListView
    lateinit var appSyncClient : AWSAppSyncClient
    lateinit var swipeRefresh : SwipeRefreshLayout


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

        //listView.adapter = adapter
        listView.adapter = NoAdapter(activity!!)


        appSyncClient = AWSAppSyncClient.builder()
                .context(activity)
                .awsConfiguration(AWSConfiguration(activity))
                .cognitoUserPoolsAuthProvider(object : CognitoUserPoolsAuthProvider{
                    override fun getLatestAuthToken(): String {
                        return try {
                            AWSMobileClient.getInstance().tokens.idToken.tokenString
                        } catch (e: Exception) {
                            Log.e("APPSYNC_ERROR", e.localizedMessage)
                            e.localizedMessage
                        }
                    }
                })
                .build()

        swipeRefresh = view.findViewById(R.id.swipeRefresh)

        swipeRefresh.setOnRefreshListener {
            println("refresh")
            //queryDeposits()
        }

        //queryDeposits()

        return view
    }
    /*
    fun queryDeposits(){
        val query = GetDepositsByUserQuery.builder()
                .user(AWSMobileClient.getInstance().username)
                .build()

        appSyncClient.query(query).responseFetcher(AppSyncResponseFetchers.CACHE_AND_NETWORK).enqueue(object : GraphQLCall.Callback<GetDepositsByUserQuery.Data>(){
            override fun onFailure(e: ApolloException) {
                Log.e("HomeFragment ", "Error", e)
                runOnUiThread {
                    swipeRefresh.isRefreshing = false
                    listView.adapter = NoAdapter(activity!!)
                }
            }

            override fun onResponse(response: Response<GetDepositsByUserQuery.Data>) {
                println(response.data().toString())
                runOnUiThread {
                    val listToPrint = mutableListOf<Deposit>()
                    val dateList = LinkedHashSet<String>()
                    val list = response.data()?.depositsByUser?.items()
                    if(list!!.isEmpty()){
                        listView.adapter = NoAdapter(activity!!)
                    }
                    else{
                        val iterate = list.iterator()
                        while (iterate.hasNext()){
                            val oldValue = iterate.next()
                            val date = Date(oldValue.createdAt().toLong())
                            val dateFormat = SimpleDateFormat("dd/MM/yyyy").format(date)
                            val deposit = Deposit(dateFormat,oldValue.description() + " " + oldValue.depositId())
                            dateList.add(dateFormat)
                            listToPrint.add(deposit)
                        }

                        val returnList = mutableListOf<Deposit>()
                        val adapter = DepositAdapter(activity!!)
                        val iteratorDate = dateList.iterator()

                        while (iteratorDate.hasNext()){
                            val oldVale = iteratorDate.next()
                            val header = Deposit(oldVale, "")
                            adapter.addHeader(header)
                            returnList.add(header)

                            val itDeposit = listToPrint.iterator()
                            while (itDeposit.hasNext()){
                                val value = itDeposit.next()
                                val compareDate = value.date
                                if(compareDate == oldVale){
                                    adapter.addItem(value)
                                    returnList.add(value)
                                }
                            }

                            Log.d("Deposit Dates", oldVale)
                        }

                        listView.adapter = adapter
                    }
                    swipeRefresh.isRefreshing = false
                }
            }

        })
    }
    */

}
