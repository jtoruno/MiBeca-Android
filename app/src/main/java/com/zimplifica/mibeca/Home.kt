package com.zimplifica.mibeca

import android.app.Activity
import android.app.AlertDialog
import android.app.Application
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.app.FragmentManager
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.app.ActionBarDrawerToggle
import android.text.InputType
import android.text.Layout
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.*
import com.amazonaws.*

import com.amazonaws.mobile.client.AWSMobileClient
import com.amazonaws.mobile.config.AWSConfiguration
import com.amazonaws.mobileconnectors.appsync.*
import com.amazonaws.mobileconnectors.appsync.fetcher.AppSyncResponseFetchers
import com.amazonaws.mobileconnectors.appsync.sigv4.CognitoUserPoolsAuthProvider
import com.amazonaws.mobileconnectors.pinpoint.PinpointManager
import com.amazonaws.mobileconnectors.pinpoint.targeting.endpointProfile.EndpointProfileUser
import com.amazonaws.type.DeltaAction
import com.amazonaws.type.EndpointAction
import com.apollographql.apollo.GraphQLCall
import com.apollographql.apollo.api.*
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.cache.normalized.CacheKey
import com.apollographql.apollo.cache.normalized.CacheKeyResolver
import com.apollographql.apollo.exception.ApolloException
import com.apollographql.apollo.internal.util.Cancelable
import com.zimplifica.mibeca.NewArq.Beneficiary
import com.zimplifica.mibeca.NewArq.BeneficiaryDatabase
import com.zimplifica.mibeca.NewArq.DbWorkerThread
import com.zimplifica.mibeca.WalkThrough.WalkThrough
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.app_bar_home.*
import com.apollographql.apollo.api.Query
import com.zimplifica.mibeca.Utils.CustomSync
import java.util.*

class Home : AppCompatActivity() , NavigationView.OnNavigationItemSelectedListener{

    private lateinit var mDbWorkerThread: DbWorkerThread
    private var mDb : BeneficiaryDatabase? = null

    val fm = supportFragmentManager
    lateinit var appSyncClient : AWSAppSyncClient
    //lateinit var userTxt : TextView
    lateinit var signOut : TextView
    lateinit var accountInfo : TextView
    var uuidUserName : String? = ""
    var endPointId : String ? = ""
    lateinit var pinPointManager : PinpointManager
    lateinit var changePassword : TextView
    lateinit var addBtn : ImageButton
    lateinit var termsAndConditions : TextView
    lateinit var privacyPolicy : TextView
    private var deltaWatcher : Cancelable? = null

    enum class ModelState {
        ADD, DELETE, UPDATE
    }


    override fun onNavigationItemSelected(p0: MenuItem): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        //userTxt = findViewById(R.id.userTextView)

        //userTxt.text = AWSMobileClient.getInstance().username

        termsAndConditions = findViewById(R.id.terms_and_conditions_txt)
        privacyPolicy = findViewById(R.id.privacy_policy_txt)

        pinPointManager = MainActivity.getPinpointManager(applicationContext)
        changePassword = findViewById(R.id.changepasswordHomeTxt)
        termsAndConditions.setOnClickListener {
            val intent = Intent(this, TermsAndConditions::class.java)
            startActivity(intent)
        }
        privacyPolicy.setOnClickListener {
            val intent = Intent(this, PrivacyPolicy::class.java)
            startActivity(intent)
        }
        changePassword.setOnClickListener {

            /*
            val intent = Intent(PushListenerService.ACTION_PUSH_NOTIFICATION)
            intent.putExtra(PushListenerService.INTENT_SNS_NOTIFICATION_FROM, "PBA Service")
            //intent.putExtra(PushListenerService.INTENT_SNS_NOTIFICATION_DATA, dataMap)
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
            */
        }
        signOut = findViewById(R.id.signOutTxt)
        addBtn = findViewById(R.id.addImgBtn)
        accountInfo = findViewById(R.id.accountInfotextView)
        accountInfo.text = AWSMobileClient.getInstance().username
        accountInfo.setOnClickListener {
            //val intent = Intent(this, AccountInfo::class.java)
            //startActivity(intent)
        }
        signOut.setOnClickListener {
            updateEndPointAttri(endPointId, EndpointAction.signOut)
            println("Click on SignOut")
            AWSMobileClient.getInstance().signOut()
            val intent = Intent(this, WalkThrough::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
        }

        addBtn.setOnClickListener {
            //Toast.makeText(this,"Agregar",Toast.LENGTH_SHORT).show()
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Suscribir Beneficiario")
            builder.setMessage("Ingrese el número de cédula del beneficiario")
            /*
            val input = EditText(this)
            input.setEms(10)
            input.hint = "Ejemplo: 123456789"
            input.inputType = InputType.TYPE_CLASS_NUMBER
            builder.setView(input)
            */
            val view = LayoutInflater.from(this).inflate(R.layout.input_dialog_id,null)
            val input : EditText = view.findViewById(R.id.citizenIdTxt)
            builder.setView(view)
            builder.setPositiveButton("Suscribir"){
                dialog, which ->
                println(input.text.toString())
                if(input.text.toString().length==9){
                    //Toast.makeText(this,"Agregar",Toast.LENGTH_SHORT).show()
                    //addCitizenId(input.text.toString())
                    addBeneficiary(input.text.toString())

                }
                else{
                    Toast.makeText(this, "Ingrese datos correctos", Toast.LENGTH_SHORT).show()
                }
            }
            builder.setNegativeButton("Cancelar"){
                dialog, which ->
                dialog.cancel()
            }
            builder.show()
        }

        val homeFragment = HomeFragment2()
        fm.beginTransaction().add(R.id.home_frame,homeFragment, "1").commit()
        init()

        appSyncClient = AWSAppSyncClient.builder()
                .context(this)
                .awsConfiguration(AWSConfiguration(this))
                .credentialsProvider(AWSMobileClient.getInstance())
                /*
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
                        Log.e("iD Table", formatCacheKey(id).toString())
                        return formatCacheKey(id)
                    }

                    override fun fromFieldArguments(field: ResponseField, variables: Operation.Variables): CacheKey {
                        val id =  field.resolveArgument("id", variables) as String
                        Log.e("iD Table", formatCacheKey(id).toString())
                        return formatCacheKey(id)
                    }
                })
                */
                .build()

        userData()
        loadBeneficiariesWithSyncFeature()






        mDbWorkerThread = DbWorkerThread("dbWorkerThread")
        mDbWorkerThread.start()
        mDb = BeneficiaryDatabase.getInstance(this)


    }

    fun init(){
        val toggle = ActionBarDrawerToggle(Activity(),home_layout, toolbarHome,R.string.nav_open,R.string.nav_close)
        home_layout.addDrawerListener(toggle)
        toggle.syncState()
        navigation_view.setNavigationItemSelectedListener(this)
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
                        uuidUserName = response.data()?.userInfo?.username()
                        signUserIdEndPoint()
                        //onSubscribeToAdd(uuidUserName!!)
                        //onSubscribeToDelete(uuidUserName!!)
                        //onSubscribeToDeposits(uuidUserName!!)
                    }
                }

            }

        })
    }

    override fun onResume() {
        super.onResume()
        val code = getStringFromSp(this,"refreshFragmentCode")
        val citizenId = getStringFromSp(this,"citizenId")
        if(code!= null){
            if(code=="200"){
                //optimisticWriteDelete(citizenId!!)
                //DeleteSubscription(citizenId!!)
                deleteBeneficiary(citizenId!!)
                val settings = this.getSharedPreferences("SP", Activity.MODE_PRIVATE)
                settings.edit().remove("refreshFragmentCode").apply()
                settings.edit().remove("citizenId").apply()
                //reloadFragment()

            }
        }
    }

    fun processCacheData(expected : GetSubscriptionsQuery.Item, condition : ModelState){
        val query = GetSubscriptionsQuery.builder()
                .build()
        appSyncClient.query(query).responseFetcher(AppSyncResponseFetchers.CACHE_ONLY).enqueue(object :GraphQLCall.Callback<GetSubscriptionsQuery.Data>(){
            override fun onFailure(e: ApolloException) {
                Log.e("Home", "Failed to edit item ", e)
            }

            override fun onResponse(response: Response<GetSubscriptionsQuery.Data>) {
                val items = mutableListOf<GetSubscriptionsQuery.Item>()
                if(response.data() != null){
                    items.addAll(response.data()!!.subscriptions.items())
                }
                when(condition){
                    ModelState.ADD->{
                        items.add(expected)
                        //Add to DAO
                        val task = Runnable { mDb?.beneficiaryDao()?.save(Beneficiary(expected.id(), expected.pk(), expected.citizenId(), expected.createdAt(), expected.hasNewDeposits())) }
                        mDbWorkerThread.postTask(task)
                    }
                    ModelState.DELETE->{
                        val iterator = items.iterator()
                        while (iterator.hasNext()){
                            val oldValue = iterator.next()
                            if(oldValue.id() == expected.citizenId()){
                                items.remove(oldValue)
                            }
                        }
                        //Add to DAO
                        val task = Runnable { mDb?.beneficiaryDao()?.deleteById(expected.citizenId()) }
                        mDbWorkerThread.postTask(task)
                    }
                    ModelState.UPDATE->{
                        val iterator = items.iterator()
                        while (iterator.hasNext()){
                            val oldValue = iterator.next()
                            if(oldValue.id() == expected.citizenId()){
                                val beneficiary = GetSubscriptionsQuery.Item("CitizenSubscription",
                                        oldValue.id(),oldValue.pk(), oldValue.citizenId(), oldValue.createdAt(), oldValue.hasNewDeposits(), oldValue.aws_ds())
                                items.remove(oldValue)
                                items.add(beneficiary)
                                //Add to DAO
                                //////////////////
                                val task = Runnable { mDb?.beneficiaryDao()?.update(Beneficiary(oldValue.id(), oldValue.pk(), oldValue.citizenId(), oldValue.createdAt(), oldValue.hasNewDeposits())) }
                                mDbWorkerThread.postTask(task)
                            }
                        }
                    }
                }
                val data = GetSubscriptionsQuery.Data(GetSubscriptionsQuery.GetSubscriptions("PaginatedSubscriptions",items,null))
                appSyncClient.store.write(query, data).enqueue(null)
            }

        })
    }

    fun optimisticWrite(citizenId: String){
        val query = GetSubscriptionsQuery.builder()
                .build()
        val expected = SubscribeBeneficiaryMutation.SubscribeBeneficiary("CitizenSubscription",
                citizenId,uuidUserName!!,citizenId,Date().time.toString(),false, DeltaAction.update)
        appSyncClient.query(query).responseFetcher(AppSyncResponseFetchers.CACHE_ONLY).enqueue(object : GraphQLCall.Callback<GetSubscriptionsQuery.Data>(){
            override fun onFailure(e: ApolloException) {
                Log.e("Home", "Failed to add item ", e)
            }
            override fun onResponse(response: Response<GetSubscriptionsQuery.Data>) {
                val items = mutableListOf<GetSubscriptionsQuery.Item>()
                if(response.data() != null){
                    items.addAll(response.data()!!.subscriptions.items())
                }
                items.add(GetSubscriptionsQuery.Item(expected.__typename(),expected.id(),expected.pk(),expected.citizenId(),
                        expected.createdAt(),expected.hasNewDeposits(), expected.aws_ds()))
                //Add to DAO
                val task = Runnable { mDb?.beneficiaryDao()?.save(Beneficiary(expected.id(), expected.pk(), expected.citizenId(), expected.createdAt(), expected.hasNewDeposits())) }
                mDbWorkerThread.postTask(task)
                //////////////////
                //Overwrite the cache with the new results
                val data = GetSubscriptionsQuery.Data(GetSubscriptionsQuery.GetSubscriptions("PaginatedSubscriptions",items,null))
                appSyncClient.store.write(query, data).enqueue(null)
            }
        })
    }


    fun addCitizenId(citizenId : String){


        /*
        val mutation = SubscribeBeneficiaryMutation.builder()
                .citizenId(citizenId)
                .build()
        appSyncClient.mutate(mutation).enqueue(object : GraphQLCall.Callback<SubscribeBeneficiaryMutation.Data>(){
            override fun onFailure(e: ApolloException) {
                Log.e("ERROR ADD BENEFICIARY ", e.toString())
                runOnUiThread {
                    Toast.makeText(this@Home, "Ha ocurrido un error agregando Beneficiario, intente de nuevo.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(response: Response<SubscribeBeneficiaryMutation.Data>) {
                println(response.data().toString())
                val data = response.data()!!.subscribeBeneficiary()
                val expected = GetSubscriptionsQuery.Item(data.__typename(), data.id(),data.pk(),
                        data.citizenId(),data.createdAt(),data.hasNewDeposits(), data.aws_ds())
                processCacheData(expected, ModelState.ADD)
                runOnUiThread {
                    /*
                    fm.popBackStackImmediate(null,FragmentManager.POP_BACK_STACK_INCLUSIVE)
                    val homeFragment = HomeFragment2()
                    fm.beginTransaction().replace(R.id.home_frame,homeFragment, "1").commit()
                    */
                    /*
                    val oldValue = response?.data()?.subscribeBeneficiary()
                    if (oldValue!=null){
                        val task = Runnable { mDb?.beneficiaryDao()?.save(Beneficiary(oldValue.id(), oldValue.pk(), oldValue.citizenId(), oldValue.createdAt(), oldValue.hasNewDeposits())) }
                        mDbWorkerThread.postTask(task)
                    }
                    */

                }

            }

        })
        optimisticWrite(citizenId)
        */
        addBeneficiaryInQuery(citizenId)

    }

    fun saveStringInSp(ctx: Context, key: String, value: String) {
        val editor = ctx.getSharedPreferences("SP", Activity.MODE_PRIVATE).edit()
        editor.putString(key, value)
        editor.apply()
    }

    fun getStringFromSp(ctx: Context, key: String): String? {
        val sharedPreferences = ctx.getSharedPreferences("SP", Activity.MODE_PRIVATE)
        return sharedPreferences.getString(key, null)
    }

    fun reloadFragment(){
        fm.popBackStackImmediate(null,FragmentManager.POP_BACK_STACK_INCLUSIVE)
        val homeFragment = HomeFragment2()
        fm.beginTransaction().replace(R.id.home_frame,homeFragment, "1").commit()
    }

    fun signUserIdEndPoint(){
        val targetingClient = pinPointManager.targetingClient
        //val interests = mutableListOf<String>(fullName)
        //targetingClient.addAttribute("name",interests )
        //targetingClient.updateEndpointProfile()

        val endPointProfile = targetingClient.currentEndpoint()
        val endPointProfileUser = EndpointProfileUser()
        endPointProfileUser.userId = uuidUserName
        endPointProfile.user = endPointProfileUser
        targetingClient.updateEndpointProfile(endPointProfile)
        Log.d("Home","Asigned user ID " + endPointProfileUser.userId + " to end point "+ endPointProfile.endpointId )
        endPointId = endPointProfile.endpointId

        updateEndPointAttri(endPointId,EndpointAction.signIn)
    }

    fun updateEndPointAttri(endPointId : String?, action : EndpointAction){
        val mutation = UpdateEndpointAttributesMutation.builder()
                .endpointId(endPointId!!)
                .action(action)
                .build()

        appSyncClient.mutate(mutation).enqueue(object : GraphQLCall.Callback<UpdateEndpointAttributesMutation.Data>(){
            override fun onFailure(e: ApolloException) {
                Log.e("Home UpdateEndPoint", e.toString())
            }

            override fun onResponse(response: Response<UpdateEndpointAttributesMutation.Data>) {
                Log.d("Home UpdateEndPoint", response.data()?.updateEndpointAttributes().toString())
            }

        })
    }
    /*

    fun onSubscribeToAdd(username : String){
        val subscribe = SubscribeToAddBeneficiarySubscription.builder()
                .pk(username)
                .build()
        appSyncClient.subscribe(subscribe).execute(object : AppSyncSubscriptionCall.Callback<SubscribeToAddBeneficiarySubscription.Data>{
            override fun onFailure(e: ApolloException) {
                Log.e("Error", e.toString())
            }

            override fun onResponse(response: Response<SubscribeToAddBeneficiarySubscription.Data>) {
                val oldValue = response?.data()?.subscribeToAddBeneficiary()
                if (oldValue!=null){
                    val task = Runnable { mDb?.beneficiaryDao()?.save(Beneficiary(oldValue.id(), oldValue.pk(), oldValue.citizenId(), oldValue.createdAt(), oldValue.hasNewDeposits())) }
                    mDbWorkerThread.postTask(task)
                }
            }

            override fun onCompleted() {
                Log.i("Completed", "Subscription completed")
            }

        })
    }
    */
    /*
    fun onSubscribeToDelete(username: String){
        val subscribe = SubscribeToDeleteBeneficiarySubscription.builder()
                .pk(username)
                .build()
        appSyncClient.subscribe(subscribe).execute(object : AppSyncSubscriptionCall.Callback<SubscribeToDeleteBeneficiarySubscription.Data>{
            override fun onFailure(e: ApolloException) {
                Log.e("Error", e.toString())
            }

            override fun onResponse(response: Response<SubscribeToDeleteBeneficiarySubscription.Data>) {
                val oldValue = response?.data()?.subscribeToDeleteBeneficiary()
                if (oldValue!=null){
                    val task = Runnable { mDb?.beneficiaryDao()?.deleteById(oldValue.citizenId()) }
                    mDbWorkerThread.postTask(task)
                }
            }

            override fun onCompleted() {
                Log.i("Completed", "Subscription completed")
            }

        })
    }
    */
    /*
    fun onSubscribeToDeposits(username: String){
        val subscribe = SubscribeToNewDepositsSubscription.builder()
                .pk(username)
                .build()
        appSyncClient.subscribe(subscribe).execute(object : AppSyncSubscriptionCall.Callback<SubscribeToNewDepositsSubscription.Data>{
            override fun onFailure(e: ApolloException) {
                Log.e("Error", e.toString())
            }

            override fun onResponse(response: Response<SubscribeToNewDepositsSubscription.Data>) {
                val oldValue = response?.data()?.subscribeToNewDeposits()
                if (oldValue!=null){
                    Log.e("Home", "SubscribeDepositupdate")
                    val task = Runnable { mDb?.beneficiaryDao()?.update(Beneficiary(oldValue.id(), oldValue.pk(), oldValue.citizenId(), oldValue.createdAt(), oldValue.hasNewDeposits(),oldValue.networkStatus().toString())) }
                    mDbWorkerThread.postTask(task)
                }
            }

            override fun onCompleted() {
                Log.i("Completed", "Subscription completed")
            }

        })
    }
    */



    fun optimisticWriteDelete(citizenId: String){
        val query = GetSubscriptionsQuery.builder()
                .build()
        appSyncClient.query(query).responseFetcher(AppSyncResponseFetchers.CACHE_ONLY).enqueue(object : GraphQLCall.Callback<GetSubscriptionsQuery.Data>(){
            override fun onFailure(e: ApolloException) {
                Log.e("DepositsByUser", "Failed to delete item ", e)
            }
            override fun onResponse(response: Response<GetSubscriptionsQuery.Data>) {
                val items = mutableListOf<GetSubscriptionsQuery.Item>()
                if(response.data() != null){
                    items.addAll(response.data()!!.subscriptions.items())
                }
                val iterator = items.iterator()
                while (iterator.hasNext()){
                    val oldValue = iterator.next()
                    if(oldValue.id() == citizenId){
                        items.remove(oldValue)
                        val beneficiary = GetSubscriptionsQuery.Item("CitizenSubscription",
                                oldValue.id(),oldValue.pk(), oldValue.citizenId(), oldValue.createdAt(), oldValue.hasNewDeposits(),  oldValue.aws_ds())
                        items.remove(oldValue)
                        items.add(beneficiary)
                        //Add to DAO
                        //////////////////
                        val task = Runnable { mDb?.beneficiaryDao()?.update(Beneficiary(oldValue.id(), oldValue.pk(), oldValue.citizenId(), oldValue.createdAt(), oldValue.hasNewDeposits())) }
                        mDbWorkerThread.postTask(task)
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


    fun DeleteSubscription(citizenId: String){
        /*
        */

        deleteBeneficiaryInQuery(citizenId)
    }

    fun loadBeneficiariesWithSyncFeature(){

        val baseQuery = GetSubscriptionsQuery.builder().build()

        val baseQueryCallBack = object : GraphQLCall.Callback<GetSubscriptionsQuery.Data>(){
            override fun onFailure(e: ApolloException) {
               //print error
            }

            override fun onResponse(response: Response<GetSubscriptionsQuery.Data>) {
                val query = GetSubscriptionsQuery.builder().build()
                appSyncClient.query(query).responseFetcher(AppSyncResponseFetchers.CACHE_ONLY).enqueue(object : GraphQLCall.Callback<GetSubscriptionsQuery.Data>(){
                    override fun onFailure(e: ApolloException) {
                        Log.e("Base Query", "Failed to refresh news item", e)
                    }

                    override fun onResponse(response: Response<GetSubscriptionsQuery.Data>) {
                        if(response.hasErrors()){
                            //Log.e(TAG, "onResponse: errors:" + response.errors())
                            return
                        }
                        //Log.d(TAG, "onResponse: accessing data")
                        val list = mutableListOf<Beneficiary>()
                        Log.e("Base Query data", response.data().toString())
                        response.data()?.subscriptions?.let {
                            val iterate = it.items().iterator()
                            while (iterate.hasNext()){
                                val oldValue = iterate.next()
                                list.add(Beneficiary(oldValue.id(),oldValue.pk(), oldValue.citizenId(), oldValue.createdAt(), oldValue.hasNewDeposits()))
                            }

                        } ?: run {
                            //init cache db
                            var items = mutableListOf<GetSubscriptionsQuery.Item>()
                            //update cache db
                            val data = GetSubscriptionsQuery.Data(GetSubscriptionsQuery.GetSubscriptions("PaginatedSubscriptions",items, null))
                            appSyncClient.store.write(query,data)?.enqueue(null)
                        }

                        val task = Runnable {
                            mDb?.beneficiaryDao()?.deleteAll()
                            mDb?.beneficiaryDao()?.saveList(list)
                        }

                        mDbWorkerThread.postTask(task)

                    }

                })
            }


        }
        val subscription = OnDeltaUpdateSubscription.builder().build()

            val subscriptionCallBack = object : AppSyncSubscriptionCall.Callback<OnDeltaUpdateSubscription.Data>{
            override fun onFailure(e: ApolloException) {
                Log.e("Subscription", "", e)
            }

            override fun onResponse(response: Response<OnDeltaUpdateSubscription.Data>) {

                val filter = response.data()?.onDeltaUpdate()?.aws_ds()
                when(filter){
                    DeltaAction.add ->{
                        addBeneficiaryInQuery(response.data()?.onDeltaUpdate()?.citizenId()!!)
                    }
                    DeltaAction.delete ->{
                        deleteBeneficiaryInQuery(response.data()?.onDeltaUpdate()?.citizenId()!!)
                    }
                    DeltaAction.update ->{
                        updateBeneficiaryInQuery(response.data()?.onDeltaUpdate()?.citizenId()!!)

                    }
                }
            }

            override fun onCompleted() {
                Log.e("Complete","Load Beneficiary Completed")
            }


        }


        val deltaQuery = GetSubscriptionsQuery.builder().build()

        val deltaQueyCallback = object : GraphQLCall.Callback<GetSubscriptionsQuery.Data>(){
            override fun onFailure(e: ApolloException) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onResponse(response: Response<GetSubscriptionsQuery.Data>) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }


        }


        val customSync = CustomSync(appSyncClient)

        customSync.sync(baseQuery,baseQueryCallBack,subscription,subscriptionCallBack, deltaQuery, deltaQueyCallback, 30)
    }


    fun addBeneficiaryInQuery(citizenId : String){
        val expected = GetSubscriptionsQuery.Item("CitizenSubscription",
                citizenId,uuidUserName!!,citizenId,Date().time.toString(),false, DeltaAction.add)
        val query = GetSubscriptionsQuery.builder()
                .build()
        appSyncClient.query(query).responseFetcher(AppSyncResponseFetchers.CACHE_ONLY).enqueue(object : GraphQLCall.Callback<GetSubscriptionsQuery.Data>(){
            override fun onFailure(e: ApolloException) {
                Log.e("DepositsByUser", "Failed to add item ", e)
            }

            override fun onResponse(response: Response<GetSubscriptionsQuery.Data>) {
                var items = mutableListOf<GetSubscriptionsQuery.Item>()
                if(response.data()!= null){
                    items.addAll(response.data()!!.subscriptions.items())
                }
                items.add(expected)

                //update cache db
                val data = GetSubscriptionsQuery.Data(GetSubscriptionsQuery.GetSubscriptions("PaginatedSubscriptions",items, null))
                appSyncClient.store.write(query,data).enqueue(null)

                //update model
                val task = Runnable { mDb?.beneficiaryDao()?.save(Beneficiary(expected.id(), expected.pk(), expected.citizenId(), expected.createdAt(), expected.hasNewDeposits())) }
                mDbWorkerThread.postTask(task)
            }

        })

    }
    fun updateBeneficiaryInQuery(citizenId : String){
        val query = GetSubscriptionsQuery.builder()
                .build()
        appSyncClient.query(query).responseFetcher(AppSyncResponseFetchers.CACHE_ONLY).enqueue(object : GraphQLCall.Callback<GetSubscriptionsQuery.Data>(){
            override fun onFailure(e: ApolloException) {
                Log.e("DepositsByUser", "Failed to add item ", e)
            }

            override fun onResponse(response: Response<GetSubscriptionsQuery.Data>) {
                var items = mutableListOf<GetSubscriptionsQuery.Item>()
                if(response.data()!= null){
                    items.addAll(response.data()!!.subscriptions.items())
                }
                val iterator = items.iterator()
                while (iterator.hasNext()){
                    val oldValue = iterator.next()
                    if(oldValue.id()==citizenId){
                        val beneficiary = GetSubscriptionsQuery.Item("CitizenSubscription",
                                oldValue.id(),oldValue.pk(), oldValue.citizenId(), oldValue.createdAt(),true, oldValue.aws_ds())
                        items.remove(oldValue)
                        items.add(beneficiary)
                        //Update to DAO
                        //////////////////
                        val task = Runnable { mDb?.beneficiaryDao()?.update(Beneficiary(beneficiary.id(), beneficiary.pk(), beneficiary.citizenId(), beneficiary.createdAt(), beneficiary.hasNewDeposits())) }
                        mDbWorkerThread.postTask(task)
                        //update cache db
                        val data = GetSubscriptionsQuery.Data(GetSubscriptionsQuery.GetSubscriptions("PaginatedSubscriptions",items, null))
                        appSyncClient.store.write(query,data).enqueue(null)
                    }
                }
            }

        })

    }
    fun deleteBeneficiaryInQuery(citizenId : String){
        val query = GetSubscriptionsQuery.builder()
                .build()
        appSyncClient.query(query).responseFetcher(AppSyncResponseFetchers.CACHE_ONLY).enqueue(object : GraphQLCall.Callback<GetSubscriptionsQuery.Data>(){
            override fun onFailure(e: ApolloException) {
                Log.e("DepositsByUser", "Failed to add item ", e)
            }

            override fun onResponse(response: Response<GetSubscriptionsQuery.Data>) {
                var items = mutableListOf<GetSubscriptionsQuery.Item>()
                if(response.data()!= null){
                    items.addAll(response.data()!!.subscriptions.items())
                }
                val iterator = items.iterator()
                while (iterator.hasNext()){
                    val oldValue = iterator.next()
                    if(oldValue.id()==citizenId){
                        items.remove(oldValue)
                        //update model
                        val task = Runnable { mDb?.beneficiaryDao()?.deleteById(citizenId) }
                        mDbWorkerThread.postTask(task)
                        //update cache db
                        val data = GetSubscriptionsQuery.Data(GetSubscriptionsQuery.GetSubscriptions("PaginatedSubscriptions",items, null))
                        appSyncClient.store.write(query,data).enqueue(null)
                    }
                }
            }

        })

    }
    fun addBeneficiary(citizenId : String){

        val mutation = SubscribeBeneficiaryMutation.builder()
                .citizenId(citizenId)
                .build()
        appSyncClient.mutate(mutation).enqueue(object : GraphQLCall.Callback<SubscribeBeneficiaryMutation.Data>(){
            override fun onFailure(e: ApolloException) {
                Log.e("ERROR ADD BENEFICIARY ", e.toString())
                runOnUiThread {
                    Toast.makeText(this@Home, "Ha ocurrido un error agregando Beneficiario, intente de nuevo.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(response: Response<SubscribeBeneficiaryMutation.Data>) {
                println(response.data().toString())
                val data = response.data()!!.subscribeBeneficiary()
                val expected = GetSubscriptionsQuery.Item(data.__typename(), data.id(),data.pk(),
                        data.citizenId(),data.createdAt(),data.hasNewDeposits(), data.aws_ds())
                processCacheData(expected, ModelState.ADD)
                runOnUiThread {
                    /*
                    fm.popBackStackImmediate(null,FragmentManager.POP_BACK_STACK_INCLUSIVE)
                    val homeFragment = HomeFragment2()
                    fm.beginTransaction().replace(R.id.home_frame,homeFragment, "1").commit()
                    */
                    /*
                    val oldValue = response?.data()?.subscribeBeneficiary()
                    if (oldValue!=null){
                        val task = Runnable { mDb?.beneficiaryDao()?.save(Beneficiary(oldValue.id(), oldValue.pk(), oldValue.citizenId(), oldValue.createdAt(), oldValue.hasNewDeposits())) }
                        mDbWorkerThread.postTask(task)
                    }
                    */

                }

            }

        })

        addBeneficiaryInQuery(citizenId)
    }
    fun updateBeneficiary(citizenId : String){
    }
    fun deleteBeneficiary(citizenId : String){

        val mutation = UnsubscribeBeneficiaryMutation.builder()
                .citizenId(citizenId)
                .build()
        appSyncClient.mutate(mutation).enqueue(object : GraphQLCall.Callback<UnsubscribeBeneficiaryMutation.Data>(){
            override fun onFailure(e: ApolloException) {
                Log.e("DepositsByUser ", "Error", e)
            }

            override fun onResponse(response: Response<UnsubscribeBeneficiaryMutation.Data>) {
                Log.e("DepositsByUser",response.data().toString())
            }

        })

        deleteBeneficiaryInQuery(citizenId)
    }

}



