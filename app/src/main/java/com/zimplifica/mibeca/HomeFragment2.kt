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
import com.amazonaws.GetUserInfoQuery
import com.amazonaws.UpdateNewDepositsStateMutation
import com.amazonaws.mobile.auth.core.internal.util.ThreadUtils.runOnUiThread
import com.amazonaws.mobile.client.AWSMobileClient
import com.amazonaws.mobile.config.AWSConfiguration
import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient
import com.amazonaws.mobileconnectors.appsync.fetcher.AppSyncResponseFetchers
import com.amazonaws.mobileconnectors.appsync.sigv4.CognitoUserPoolsAuthProvider
import com.apollographql.apollo.GraphQLCall
import com.apollographql.apollo.api.Operation
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.api.ResponseField
import com.apollographql.apollo.cache.normalized.CacheKey
import com.apollographql.apollo.cache.normalized.CacheKeyResolver
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
                .resolver(object : CacheKeyResolver(){

                    private fun formatCacheKey(id : String?): CacheKey{
                        return if (id == null || id.isEmpty()){
                            CacheKey.NO_KEY
                        } else{
                            CacheKey.from(id)
                        }
                    }

                    override fun fromFieldRecordSet(field: ResponseField, recordSet: MutableMap<String, Any>): CacheKey {

                        val id =  recordSet["id"] as? String
                        return formatCacheKey(id)
                    }

                    override fun fromFieldArguments(field: ResponseField, variables: Operation.Variables): CacheKey {
                        val id =  field.resolveArgument("id", variables) as String
                        return formatCacheKey(id)
                    }

                })
                .build()

        userData()

        //getSubscriptions()
        swipeRefresh.setOnRefreshListener {
            //getSubscriptions()

            swipeRefresh.isRefreshing= false
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
            if(it.hasNewDeposits){
                updateDepositState(it.citizenId, false)
            }
            val intent = Intent(activity, DepositsByUser::class.java)
            intent.putExtra("idUser",it.citizenId)
            intent.putExtra("uuidUser", uuidUser)
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
                        uuidUser = response.data()!!.userInfo?.username()
                    }
                }

            }

        })
    }

    override fun onDestroy() {
        mDbWorkerThread.quit()
        BeneficiaryDatabase.destroyInstance()
        super.onDestroy()
    }

    fun optimisticWrite(citizenId: String, state: Boolean){
        val query = GetSubscriptionsQuery.builder()
                .build()

        appSyncClient.query(query).responseFetcher(AppSyncResponseFetchers.CACHE_ONLY).enqueue(object : GraphQLCall.Callback<GetSubscriptionsQuery.Data>(){
            override fun onFailure(e: ApolloException) {
                Log.e("DepositsByUser", "Failed to update item ", e)
            }
            override fun onResponse(response: Response<GetSubscriptionsQuery.Data>) {
                val items = arrayListOf<GetSubscriptionsQuery.Item>()
                if(response.data() != null){
                    items.addAll(response.data()!!.subscriptions.items())
                }
                val iterator = items.iterator()
                while (iterator.hasNext()){
                    val oldValue = iterator.next()
                    if(oldValue.id() == citizenId){

                    }
                }

                //Add to DAO
                val task = Runnable { mDb?.beneficiaryDao()?.deleteById(citizenId) }
                mDbWorkerThread.postTask(task)
                //////////////////
                //Overwrite the cache with the new results
                val data = GetSubscriptionsQuery.Data(GetSubscriptionsQuery.GetSubscriptions("PaginatedSubscriptions",items,null))
                appSyncClient.store.write(query, data).enqueue(null)

            }
        })
    }

    fun updateDepositState(citizenId : String, state : Boolean){
        val mutation = UpdateNewDepositsStateMutation.builder()
                .citizenId(citizenId)
                .hasNewDeposits(state)
                .build()
        appSyncClient.mutate(mutation).enqueue(object: GraphQLCall.Callback<UpdateNewDepositsStateMutation.Data>(){
            override fun onFailure(e: ApolloException) {
                Log.e("HomeFragment", e.toString())
            }

            override fun onResponse(response: Response<UpdateNewDepositsStateMutation.Data>) {
                val oldValue = response?.data()?.updateNewDepositsState()
                if (oldValue!=null){
                    val task = Runnable { mDb?.beneficiaryDao()?.update(Beneficiary(oldValue.id(), oldValue.pk(), oldValue.citizenId(), oldValue.createdAt(), oldValue.hasNewDeposits())) }
                    mDbWorkerThread.postTask(task)
                }
            }

        })
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
