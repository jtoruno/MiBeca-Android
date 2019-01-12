package com.zimplifica.mibeca


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
import com.amazonaws.GetSubscriptionsQuery
import com.amazonaws.GetUserInfoQuery
import com.amazonaws.UpdateNewDepositsStateMutation
import com.amazonaws.mobile.auth.core.internal.util.ThreadUtils.runOnUiThread
import com.amazonaws.mobile.client.AWSMobileClient
import com.amazonaws.mobile.config.AWSConfiguration
import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient
import com.amazonaws.mobileconnectors.appsync.fetcher.AppSyncResponseFetchers
import com.apollographql.apollo.GraphQLCall
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException
import com.zimplifica.mibeca.NewArq.*



class HomeFragment2 : Fragment() {
    private lateinit var mDbWorkerThread: DbWorkerThread
    lateinit var recyclerView: RecyclerView
    lateinit var appSyncClient : AWSAppSyncClient
    lateinit var swipeRefresh : SwipeRefreshLayout

    lateinit var mAdapter : BeneficiaryListAdapter
    lateinit var viewModel : BeneficiaryViewModel
    private var mDb : BeneficiaryDatabase? = null

    private var uuidUser = ""

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

        appSyncClient = AWSAppSyncClient.builder()
                .context(activity)
                .awsConfiguration(AWSConfiguration(activity))
                .credentialsProvider(AWSMobileClient.getInstance())
                .build()

        userData()

        //getSubscriptions()
        swipeRefresh.setOnRefreshListener {

            //getSubscriptions()
            refreshModel()
        }

        mDb = BeneficiaryDatabase.getInstance(activity!!)

        viewModel = ViewModelProviders.of(this, MyViewModelFactory(activity!!)).get(BeneficiaryViewModel::class.java)
        viewModel.getBeneficiary().observe(this, Observer {
            Log.e("Size ", it?.size.toString())
            mAdapter.setBeneficiaries(it?: emptyList())
        })
        recyclerView.setHasFixedSize(true)
        recyclerView.isNestedScrollingEnabled = false
        val layoutManager = LinearLayoutManager(activity)
        recyclerView.layoutManager = layoutManager
        val list = mutableListOf<Beneficiary>()
        mAdapter = BeneficiaryListAdapter(list){
            if(it.hasNewDeposits){
                updateBeneficiary(it.citizenId)
                //updateDepositState(it.citizenId, false)
                //optimisticWrite(it.citizenId, false)
            }
            val intent = Intent(activity, DepositsByUser::class.java)
            intent.putExtra("idUser",it.citizenId)
            intent.putExtra("uuidUser", uuidUser)
            startActivity(intent)
        }
        val divider = DividerItemDecoration(recyclerView.context,layoutManager.orientation)
        recyclerView.addItemDecoration(divider)
        recyclerView.adapter = mAdapter


        return view
    }

    fun userData(){
        val query = GetUserInfoQuery.builder()
                .build()
        appSyncClient.query(query).responseFetcher(AppSyncResponseFetchers.CACHE_FIRST).enqueue( object : GraphQLCall.Callback<GetUserInfoQuery.Data>(){
            override fun onFailure(e: ApolloException) {
                Log.e("ERROR", e.toString())

            }

            override fun onResponse(response: Response<GetUserInfoQuery.Data>) {
                Log.i("Home", response.data().toString())
                runOnUiThread {
                    if(response.data()!=null){
                        uuidUser = response.data()!!.userInfo.username()
                    }
                }

            }

        })
    }

    fun updateBeneficiaryInQuery(citizenId : String){
        val task = Runnable { mDb?.beneficiaryDao()?.updateDepositStatus(citizenId, false) }
        mDbWorkerThread.postTask(task)
    }

    fun updateBeneficiary(citizenId : String){
        val mutation = UpdateNewDepositsStateMutation.builder()
                .citizenId(citizenId)
                .hasNewDeposits(false)
                .build()
        appSyncClient.mutate(mutation).enqueue(object: GraphQLCall.Callback<UpdateNewDepositsStateMutation.Data>(){
            override fun onFailure(e: ApolloException) {
                Log.e("HomeFragment", e.toString())
            }

            override fun onResponse(response: Response<UpdateNewDepositsStateMutation.Data>) {
                Log.i("HomeFragment",response.data().toString())
            }

        })
        updateBeneficiaryInQuery(citizenId)
    }

    override fun onDestroy() {
        mDbWorkerThread.quit()
        BeneficiaryDatabase.destroyInstance()
        super.onDestroy()
    }

    fun refreshModel(){
        val query = GetSubscriptionsQuery.builder().build()
        appSyncClient.query(query).responseFetcher(AppSyncResponseFetchers.NETWORK_ONLY).enqueue(object : GraphQLCall.Callback<GetSubscriptionsQuery.Data>(){
            override fun onFailure(e: ApolloException) {
                Log.e("HomeFragment", "Failed to refresh model", e)
                runOnUiThread {
                    swipeRefresh.isRefreshing= false
                }
            }

            override fun onResponse(response: Response<GetSubscriptionsQuery.Data>) {
                if(response.hasErrors()){
                    Log.e("HOME fragment", "onResponse: errors:" + response.errors())
                    runOnUiThread {
                        swipeRefresh.isRefreshing= false
                    }
                    return
                }
                if (response.fromCache()){
                    Log.e("home fragment", "Data from Cache")
                    runOnUiThread {
                        swipeRefresh.isRefreshing= false
                    }
                    return
                }
                val list = mutableListOf<Beneficiary>()
                Log.e("Refresh Data", response.data().toString())
                response.data()?.subscriptions?.let {
                    val iterate = it.items().iterator()
                    while (iterate.hasNext()){
                        val oldValue = iterate.next()
                        list.add(Beneficiary(oldValue.id(),oldValue.pk(), oldValue.citizenId(), oldValue.createdAt(), oldValue.hasNewDeposits()))
                    }
                }
                val task = Runnable {
                    mDb?.beneficiaryDao()?.deleteAll()
                    mDb?.beneficiaryDao()?.saveList(list)
                }
                mDbWorkerThread.postTask(task)
                runOnUiThread {
                    swipeRefresh.isRefreshing= false
                }
            }

        })
    }

}
