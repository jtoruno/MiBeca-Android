package com.zimplifica.mibeca

import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import android.content.Intent
import android.util.Log
import com.amazonaws.mobileconnectors.pinpoint.targeting.notification.NotificationClient
import com.amazonaws.mobileconnectors.pinpoint.targeting.notification.NotificationDetails
import com.google.firebase.messaging.RemoteMessage
import com.google.firebase.messaging.FirebaseMessagingService


class PushListenerService : FirebaseMessagingService() {

    override fun onNewToken(token: String?) {
        super.onNewToken(token)

        Log.d(TAG, "Registering push notifications token: " + token!!)
        MainActivity.getPinpointManager(applicationContext).notificationClient.registerDeviceToken(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage?) {
        super.onMessageReceived(remoteMessage)
        Log.e(TAG, "Message: " + remoteMessage!!.data)

        //val intent = Intent(applicationContext, MainActivity::class.java)
        //intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)

        val notificationClient = MainActivity.getPinpointManager(applicationContext).notificationClient

        val notificationDetails = NotificationDetails.builder()
                .from(remoteMessage.from)
                .mapData(remoteMessage.data)
                .intentAction(NotificationClient.FCM_INTENT_ACTION)
                //.intent(intent)
                .build()

        val pushResult = notificationClient.handleCampaignPush(notificationDetails)

        if (NotificationClient.CampaignPushResult.NOT_HANDLED != pushResult) {
            /**
             * The push message was due to a Pinpoint campaign.
             * If the app was in the background, a local notification was added
             * in the notification center. If the app was in the foreground, an
             * event was recorded indicating the app was in the foreground,
             * for the demo, we will broadcast the notification to let the main
             * activity display it in a dialog.
             */
            if (NotificationClient.CampaignPushResult.APP_IN_FOREGROUND == pushResult) {
                /* Create a message that will display the raw data of the campaign push in a dialog. */
                val dataMap = HashMap(remoteMessage.data)
                broadcast(remoteMessage.from, dataMap)
            }
            return
        }
    }

    private fun broadcast(from: String?, dataMap: HashMap<String, String>) {
        val intent = Intent(ACTION_PUSH_NOTIFICATION)
        intent.putExtra(INTENT_SNS_NOTIFICATION_FROM, from)
        intent.putExtra(INTENT_SNS_NOTIFICATION_DATA, dataMap)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    companion object {
        val TAG = PushListenerService::class.java.simpleName!!

        // Intent action used in local broadcast
        const val ACTION_PUSH_NOTIFICATION = "push-notification"
        // Intent keys
        const val INTENT_SNS_NOTIFICATION_FROM = "from"
        const val INTENT_SNS_NOTIFICATION_DATA = "data"

        /**
         * Helper method to extract push message from bundle.
         *
         * @param data bundle
         * @return message string from push notification
         */
        fun getMessage(data: Bundle): String {
            return (data.get("data") as HashMap<*, *>).toString()
        }
    }
}