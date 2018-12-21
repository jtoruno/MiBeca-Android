package com.zimplifica.mibeca

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.app.ActionBarDrawerToggle
import android.util.Log
import android.view.MenuItem
import android.widget.TextView

import com.amazonaws.mobile.client.AWSMobileClient
import com.amazonaws.mobile.config.AWSConfiguration
import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient
import com.amazonaws.mobileconnectors.appsync.fetcher.AppSyncResponseFetchers
import com.amazonaws.mobileconnectors.pinpoint.PinpointManager
import com.amazonaws.mobileconnectors.pinpoint.targeting.endpointProfile.EndpointProfileUser
import com.apollographql.apollo.GraphQLCall
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.app_bar_home.*
import java.util.*

class Home : AppCompatActivity() , NavigationView.OnNavigationItemSelectedListener{

    val fm = supportFragmentManager
    lateinit var appSyncClient : AWSAppSyncClient
    lateinit var userTxt : TextView
    lateinit var signOut : TextView
    lateinit var accountInfo : TextView
    var fullName = ""
    lateinit var pinPointManager : PinpointManager
    lateinit var changePassword : TextView

    override fun onNavigationItemSelected(p0: MenuItem): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        userTxt = findViewById(R.id.userTextView)

        userTxt.text = AWSMobileClient.getInstance().username

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
        accountInfo = findViewById(R.id.accountInfotextView)
        accountInfo.setOnClickListener {
            val intent = Intent(this, AccountInfo::class.java)
            startActivity(intent)
        }
        signOut.setOnClickListener {
            println("Click on SignOut")
            AWSMobileClient.getInstance().signOut()
            val intent = Intent(this, SignScreen::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
        }

        val homeFragment = HomeFragment2()
        fm.beginTransaction().add(R.id.home_frame,homeFragment, "1").commit()
        init()

        appSyncClient = AWSAppSyncClient.builder()
                .context(applicationContext)
                .awsConfiguration(AWSConfiguration(applicationContext))
                .credentialsProvider(AWSMobileClient.getInstance())
                .build()

        //userData()

    }

    fun init(){
        val toggle = ActionBarDrawerToggle(Activity(),home_layout, toolbarHome,R.string.nav_open,R.string.nav_close)
        home_layout.addDrawerListener(toggle)
        toggle.syncState()
        navigation_view.setNavigationItemSelectedListener(this)
    }
    /*
    fun userData(){

        val query = GetUserInfoQuery.builder()
                .username(AWSMobileClient.getInstance().username)
                .build()
        appSyncClient.query(query).responseFetcher(AppSyncResponseFetchers.CACHE_FIRST).enqueue( object : GraphQLCall.Callback<GetUserInfoQuery.Data>(){
            override fun onFailure(e: ApolloException) {
                Log.e("ERROR", e.toString())
            }

            override fun onResponse(response: Response<GetUserInfoQuery.Data>) {
                Log.i("Home", response.data().toString())
                runOnUiThread {
                    if(response.data()!=null){
                        val name = response.data()?.userInfo?.name() + " " + response.data()?.userInfo?.family_name()
                        fullName = response.data()?.userInfo?.name()!!
                        userTxt.text = name
                        ssignUserIdEndPoint()
                    }
                }

            }

        })
    }
    */



    fun ssignUserIdEndPoint(){
        val targetingClient = pinPointManager.targetingClient
        val interests = mutableListOf<String>(fullName)
        targetingClient.addAttribute("name",interests )
        targetingClient.updateEndpointProfile()

        val endPointProfile = targetingClient.currentEndpoint()
        val endPointProfileUser = EndpointProfileUser()
        endPointProfileUser.userId = AWSMobileClient.getInstance().username
        endPointProfile.user = endPointProfileUser
        targetingClient.updateEndpointProfile(endPointProfile)
        Log.d("Home","Asigned user ID " + endPointProfileUser.userId + " to end point "+ endPointProfile.endpointId )


    }

}
