package com.zimplifica.mibeca

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import com.amazonaws.GetSubscriptionsQuery
import com.amazonaws.mobile.auth.core.internal.util.ThreadUtils.runOnUiThread
import com.amazonaws.mobile.client.AWSMobileClient
import com.amazonaws.mobile.config.AWSConfiguration
import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient
import com.amazonaws.mobileconnectors.appsync.fetcher.AppSyncResponseFetchers
import com.amazonaws.mobileconnectors.appsync.sigv4.CognitoUserPoolsAuthProvider
import com.apollographql.apollo.GraphQLCall
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException
import com.zimplifica.mibeca.Adapters.IdAdapter
import com.zimplifica.mibeca.Adapters.NoAdapter
import com.zimplifica.mibeca.Adapters.UserData


class HomeFragment2 : Fragment() {
    lateinit var recyclerView: ListView
    lateinit var appSyncClient : AWSAppSyncClient
    lateinit var swipeRefresh : SwipeRefreshLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        val view = inflater.inflate(R.layout.fragment_home_fragment2, container, false)
        recyclerView = view.findViewById(R.id.RecyclerViewFragment)
        swipeRefresh = view.findViewById(R.id.swiperefreshlayoutHomeFragment)

        /*
        val list = mutableListOf<UserData>()
        list.add(UserData("116500454"))
        list.add(UserData("116521234"))
        list.add(UserData("115650399"))
        list.add(UserData("116579999"))
        val adapter = IdAdapter(activity!!,R.layout.layout_id_row,list)
        recyclerView.adapter = adapter
        */
        appSyncClient = AWSAppSyncClient.builder()
                .context(activity)
                .awsConfiguration(AWSConfiguration(activity))
                .cognitoUserPoolsAuthProvider(object : CognitoUserPoolsAuthProvider {
                    override fun getLatestAuthToken(): String {
                        return try {
                            AWSMobileClient.getInstance().tokens.idToken.tokenString
                        } catch (e: Exception) {
                            Log.e("APPSYNC_ERROR", e.localizedMessage)
                            e.localizedMessage
                        }
                    }
                }).build()

        getSubscriptions()
        swipeRefresh.setOnRefreshListener {
            getSubscriptions()
        }
        return view
    }

    fun getSubscriptions(){
        val query = GetSubscriptionsQuery.builder().build()
        appSyncClient.query(query).responseFetcher(AppSyncResponseFetchers.NETWORK_ONLY).enqueue(object : GraphQLCall.Callback<GetSubscriptionsQuery.Data>(){
            override fun onFailure(e: ApolloException) {
                Log.e("HomeFragment", e.toString())
                runOnUiThread{
                    swipeRefresh.isRefreshing = false
                    recyclerView.adapter = NoAdapter(activity!!)
                }
            }

            override fun onResponse(response: Response<GetSubscriptionsQuery.Data>) {
                if(response.data()?.subscriptions==null){
                    runOnUiThread{
                        swipeRefresh.isRefreshing = false
                        recyclerView.adapter = NoAdapter(activity!!)
                    }
                }
                else{
                    if(response.data()!!.subscriptions.items()?.isEmpty()){
                        runOnUiThread{
                            swipeRefresh.isRefreshing = false
                            recyclerView.adapter = NoAdapter(activity!!)
                        }
                    }
                    else{
                        val list = mutableListOf<UserData>()
                        val items = response.data()!!.subscriptions.items()
                        val iterate = items.iterator()
                        while (iterate.hasNext()){
                            val oldValue = iterate.next()
                            list.add(UserData(oldValue.citizenId()))
                        }
                        runOnUiThread {
                            swipeRefresh.isRefreshing = false
                            val adapter = IdAdapter(activity!!,R.layout.layout_id_row,list)
                            recyclerView.adapter = adapter
                        }


                    }
                }
            }

        })

    }

}
