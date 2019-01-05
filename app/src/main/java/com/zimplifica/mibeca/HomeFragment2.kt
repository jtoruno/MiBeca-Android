package com.zimplifica.mibeca

import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
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
import com.zimplifica.mibeca.NewArq.*
import java.util.*


class HomeFragment2 : Fragment() {
    private lateinit var mDbWorkerThread: DbWorkerThread
    lateinit var recyclerView: RecyclerView
    lateinit var appSyncClient : AWSAppSyncClient
    lateinit var swipeRefresh : SwipeRefreshLayout

    lateinit var mAdapter : BeneficiaryListAdapter
    lateinit var viewModel : BeneficiaryViewModel
    private var mDb : BeneficiaryDatabase? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        val view = inflater.inflate(R.layout.fragment_home_fragment2, container, false)
        recyclerView = view.findViewById(R.id.RecyclerViewFragment)
        swipeRefresh = view.findViewById(R.id.swiperefreshlayoutHomeFragment)

        mDbWorkerThread = DbWorkerThread("dbWorkerThread")
        mDbWorkerThread.start()

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
                .credentialsProvider(AWSMobileClient.getInstance())
                .build()

        //getSubscriptions()
        swipeRefresh.setOnRefreshListener {
            //getSubscriptions()
        }

        mDb = BeneficiaryDatabase.getInstance(activity!!)
        /*
        val task = Runnable { mDb?.beneficiaryDao()?.deleteAll() }
        mDbWorkerThread.postTask(task)
        */



        viewModel = ViewModelProviders.of(this, MyViewModelFactory(activity!!)).get(BeneficiaryViewModel::class.java)
        viewModel.getBeneficiary().observe(this, Observer {
            Log.e("Size ", it?.size.toString())
            mAdapter.setBeneficiaries(it?: emptyList())
        })
        recyclerView.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(activity)
        recyclerView.layoutManager = layoutManager
        val list = mutableListOf<Beneficiary>()
        mAdapter = BeneficiaryListAdapter(list){
            val intent = Intent(activity, DepositsByUser::class.java)
            intent.putExtra("idUser",it.citizenId)
            //val option : ActivityOptions = ActivityOptions.makeCustomAnimation(mCtx, R.anim.abc_slide_in_bottom, R.anim.abc_slide_out_bottom)
            startActivity(intent)
            //val task = Runnable { mDb?.beneficiaryDao()?.save(Beneficiary(UUID.randomUUID().toString(), "123456789","123")) }
            //mDbWorkerThread.postTask(task)

        }
        val divider = DividerItemDecoration(recyclerView.context,layoutManager.orientation)
        recyclerView.addItemDecoration(divider)
        recyclerView.adapter = mAdapter


        return view
    }

    override fun onDestroy() {
        mDbWorkerThread.quit()
        BeneficiaryDatabase.destroyInstance()
        super.onDestroy()
    }


    /*
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
    */


}
