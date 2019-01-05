package com.zimplifica.mibeca

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.FragmentManager
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.LayoutInflater
import android.view.Window
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import com.amazonaws.GetDepositsByBeneficiaryQuery
import com.amazonaws.UnsubscribeBeneficiaryMutation
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
import com.zimplifica.mibeca.Adapters.NoAdapterDeposit
import com.zimplifica.mibeca.NewArq.Beneficiary
import com.zimplifica.mibeca.NewArq.BeneficiaryDatabase
import com.zimplifica.mibeca.NewArq.DbWorkerThread
import com.zimplifica.mibeca.Utils.Deposit
import java.text.SimpleDateFormat
import java.util.*

class DepositsByUser : AppCompatActivity() {

    private lateinit var mDbWorkerThread: DbWorkerThread
    private var mDb : BeneficiaryDatabase? = null

    lateinit var listView : ListView
    lateinit var appSyncClient : AWSAppSyncClient
    lateinit var swipeRefresh : SwipeRefreshLayout
    lateinit var spinnerDialog : Dialog
    lateinit var deleteBtn : Button
    var userId = ""
    val fm = supportFragmentManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_deposits_by_user)

        val toolbar : Toolbar = findViewById(R.id.toolbarDepositsByUser)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        listView = findViewById(R.id.deposit_list_view2)
        listView.isEnabled = false
        swipeRefresh = findViewById(R.id.swipeRefreshDeposits)
        deleteBtn = findViewById(R.id.deleteBtnDeposits)


        spinnerDialog = Dialog(this,R.style.ThemeOverlay_AppCompat_Dark)
        progressBarShow(spinnerDialog)
        spinnerDialog.show()


        val dataActivity: Intent = intent
        userId = dataActivity.getStringExtra("idUser")
        //toolbar.title = "Depósitos de $userId"
        //toolbar.setTitle("Depósitos de $userId")
        supportActionBar!!.title = "Depósitos de $userId"


        appSyncClient = AWSAppSyncClient.builder()
                .context(this)
                .awsConfiguration(AWSConfiguration(this))
                .credentialsProvider(AWSMobileClient.getInstance())
                .build()

        swipeRefresh.setOnRefreshListener {
            depositsByUser(userId)
        }
        depositsByUser(userId)

        deleteBtn.setOnClickListener {
            val dialog = AlertDialog.Builder(this)
            dialog.setTitle("Eliminando subscripción")
            dialog.setMessage("¿Desea realmente cancelar la subscripción de este beneficiario?")
            dialog.setPositiveButton("Eliminar"){
                dialog, which ->
                spinnerDialog.show()
                DeleteSubscription(userId)
            }
            dialog.setNegativeButton("Cancelar"){
                dialog, which ->
                dialog.cancel()
            }
            dialog.show()
        }

        mDbWorkerThread = DbWorkerThread("dbWorkerThread")
        mDbWorkerThread.start()
        mDb = BeneficiaryDatabase.getInstance(this)
    }

    fun DeleteSubscription(userNameId : String){
        val mutation = UnsubscribeBeneficiaryMutation.builder()
                .citizenId(userNameId)
                .build()
        appSyncClient.mutate(mutation).enqueue(object : GraphQLCall.Callback<UnsubscribeBeneficiaryMutation.Data>(){
            override fun onFailure(e: ApolloException) {
                Log.e("DepositsByUser ", "Error", e)
                runOnUiThread {
                    spinnerDialog.dismiss()
                    Toast.makeText(this@DepositsByUser, "Error al eliminar subscripción",Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(response: Response<UnsubscribeBeneficiaryMutation.Data>) {
                Log.e("DepositsByUser",response.data().toString())
                spinnerDialog.dismiss()
                runOnUiThread {
                    saveStringInSp(this@DepositsByUser, "refreshFragmentCode", "200")
                }
                val oldValue = response?.data()?.unsubscribeBeneficiary()
                if (oldValue!=null){
                    val task = Runnable { mDb?.beneficiaryDao()?.deleteBeneficiary(Beneficiary(oldValue.pk(), oldValue.citizenId(), oldValue.createdAt())) }
                    mDbWorkerThread.postTask(task)
                }
                onBackPressed()


                /*
                if (response.data() != null){
                    onBackPressed()
                }
                else{
                    runOnUiThread {
                        spinnerDialog.dismiss()
                        Toast.makeText(this@DepositsByUser, "Error al eliminar subscripción",Toast.LENGTH_SHORT).show()
                    }

                }*/
            }

        })
    }

    fun saveStringInSp(ctx: Context, key: String, value: String) {
        val editor = ctx.getSharedPreferences("SP", Activity.MODE_PRIVATE).edit()
        editor.putString(key, value)
        editor.apply()
    }

    fun depositsByUser(id : String){
        val query = GetDepositsByBeneficiaryQuery.builder()
                .citizenId(id)
                .build()
        appSyncClient.query(query).responseFetcher(AppSyncResponseFetchers.NETWORK_ONLY).enqueue(object : GraphQLCall.Callback<GetDepositsByBeneficiaryQuery.Data>(){
            override fun onFailure(e: ApolloException) {
                Log.e("DepositsByUser ", "Error", e)
                runOnUiThread {
                    spinnerDialog.dismiss()
                    swipeRefresh.isRefreshing = false
                    listView.divider = null
                    listView.adapter = NoAdapterDeposit(applicationContext,userId)
                }
            }

            override fun onResponse(response: Response<GetDepositsByBeneficiaryQuery.Data>) {
                println(response.data().toString())
                runOnUiThread {
                    val listToPrint = mutableListOf<Deposit>()
                    val dateList = LinkedHashSet<String>()
                    val list = response.data()?.depositsByBeneficiary?.items()
                    if(list!!.isEmpty()){
                        listView.divider = null
                        listView.adapter = NoAdapterDeposit(applicationContext,userId)
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
                        val adapter = DepositAdapter(applicationContext)
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
                    spinnerDialog.dismiss()
                }
            }

        })

    }

    fun progressBarShow(pd : Dialog){
        val view = LayoutInflater.from(this).inflate(R.layout.remove_border, null)
        pd.requestWindowFeature(Window.FEATURE_NO_TITLE)
        pd.window.setBackgroundDrawableResource(R.color.backGroundTransparent)
        pd.setContentView(view)

    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onBackPressed() {
        finish()
    }
}
