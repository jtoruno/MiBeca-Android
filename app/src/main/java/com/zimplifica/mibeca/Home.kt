package com.zimplifica.mibeca

import android.app.Activity
import android.app.Application
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v7.app.ActionBarDrawerToggle
import android.util.Log
import android.view.MenuItem
import com.amazonaws.UpdateUserEndpointMutation
import com.amazonaws.mobile.client.AWSMobileClient
import com.amazonaws.mobile.config.AWSConfiguration
import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient
import com.amazonaws.type.NotificationsChannel
import com.apollographql.apollo.GraphQLCall
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.app_bar_home.*

class Home : AppCompatActivity() , NavigationView.OnNavigationItemSelectedListener{

    val fm = supportFragmentManager
    lateinit var appSyncClient : AWSAppSyncClient

    override fun onNavigationItemSelected(p0: MenuItem): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val homeFragment = HomeFragment()
        fm.beginTransaction().add(R.id.home_frame,homeFragment, "1").commit()
        init()
        /*
        appSyncClient = AWSAppSyncClient.builder()
                .context(applicationContext)
                .awsConfiguration(AWSConfiguration(applicationContext))
                .build()
                */

    }

    fun init(){
        val toggle = ActionBarDrawerToggle(Activity(),home_layout, toolbarHome,R.string.nav_open,R.string.nav_close)
        home_layout.addDrawerListener(toggle)
        toggle.syncState()
        navigation_view.setNavigationItemSelectedListener(this)
    }

    fun endPoint(endPoint: String){
        val mutation = UpdateUserEndpointMutation.builder()
                .user(AWSMobileClient.getInstance().username)
                .endpoint(endPoint)
                .channelType(NotificationsChannel.GCM)
                .build()
        appSyncClient.mutate(mutation).enqueue( object : GraphQLCall.Callback<UpdateUserEndpointMutation.Data>(){
            override fun onFailure(e: ApolloException) {
                 Log.e("Home", e.toString())
            }

            override fun onResponse(response: Response<UpdateUserEndpointMutation.Data>) {
                 Log.i("Home",response.toString())
            }

        })

    }
}
