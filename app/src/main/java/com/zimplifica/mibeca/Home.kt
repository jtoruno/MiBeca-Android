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
import com.amazonaws.GetUserInfoQuery
import com.amazonaws.SubscribeBeneficiaryMutation
import com.amazonaws.UpdateEndpointAttributesMutation

import com.amazonaws.mobile.client.AWSMobileClient
import com.amazonaws.mobile.config.AWSConfiguration
import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient
import com.amazonaws.mobileconnectors.appsync.fetcher.AppSyncResponseFetchers
import com.amazonaws.mobileconnectors.appsync.sigv4.CognitoUserPoolsAuthProvider
import com.amazonaws.mobileconnectors.pinpoint.PinpointManager
import com.amazonaws.mobileconnectors.pinpoint.targeting.endpointProfile.EndpointProfileUser
import com.amazonaws.type.EndpointAction
import com.apollographql.apollo.GraphQLCall
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException
import com.zimplifica.mibeca.NewArq.Beneficiary
import com.zimplifica.mibeca.NewArq.BeneficiaryDatabase
import com.zimplifica.mibeca.NewArq.DbWorkerThread
import com.zimplifica.mibeca.WalkThrough.WalkThrough
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.app_bar_home.*
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


    override fun onNavigationItemSelected(p0: MenuItem): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        //userTxt = findViewById(R.id.userTextView)

        //userTxt.text = AWSMobileClient.getInstance().username

        pinPointManager = MainActivity.getPinpointManager(applicationContext)
        changePassword = findViewById(R.id.changepasswordHomeTxt)
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
                    addCitizenId(input.text.toString())

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
                .build()

        userData()
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
        appSyncClient.query(query).responseFetcher(AppSyncResponseFetchers.NETWORK_ONLY).enqueue( object : GraphQLCall.Callback<GetUserInfoQuery.Data>(){
            override fun onFailure(e: ApolloException) {
                Log.e("ERROR", e.toString())

            }

            override fun onResponse(response: Response<GetUserInfoQuery.Data>) {
                Log.i("Home", response.data().toString())
                runOnUiThread {
                    if(response.data()!=null){
                        uuidUserName = response.data()?.userInfo?.username()
                        signUserIdEndPoint()

                    }
                }

            }

        })
    }

    override fun onResume() {
        super.onResume()
        val code = getStringFromSp(this,"refreshFragmentCode")
        if(code!= null){
            if(code=="200"){
                val settings = this.getSharedPreferences("SP", Activity.MODE_PRIVATE)
                settings.edit().remove("refreshFragmentCode").apply()
                //reloadFragment()

            }
        }
    }


    fun addCitizenId(citizenId : String){
        val mutation = SubscribeBeneficiaryMutation.builder()
                .citizenId(citizenId)
                .build()
        appSyncClient.mutate(mutation).enqueue(object : GraphQLCall.Callback<SubscribeBeneficiaryMutation.Data>(){
            override fun onFailure(e: ApolloException) {
                Log.e("ERROR", e.toString())
                runOnUiThread {
                    Toast.makeText(this@Home, "Ha ocurrido un error agregando Beneficiario, intente de nuevo.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(response: Response<SubscribeBeneficiaryMutation.Data>) {
                println(response.data().toString())
                runOnUiThread {
                    /*
                    fm.popBackStackImmediate(null,FragmentManager.POP_BACK_STACK_INCLUSIVE)
                    val homeFragment = HomeFragment2()
                    fm.beginTransaction().replace(R.id.home_frame,homeFragment, "1").commit()
                    */
                    val oldValue = response?.data()?.subscribeBeneficiary()
                    if (oldValue!=null){
                        val task = Runnable { mDb?.beneficiaryDao()?.save(Beneficiary(oldValue.pk(), oldValue.citizenId(), oldValue.createdAt())) }
                        mDbWorkerThread.postTask(task)
                    }

                }

            }

        })
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

}
