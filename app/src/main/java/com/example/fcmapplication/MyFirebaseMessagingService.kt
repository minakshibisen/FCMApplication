package com.example.fcmapplication

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

const val channelId = "notification_channel"
const val channelName ="com.example.fcmapplication"
@SuppressLint("MissingFirebaseInstanceTokenRefresh")
class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.e("TAG", "onMessageReceived() called with: remoteMessage = ${remoteMessage.notification?.title}")
      if (remoteMessage.getNotification()!= null){
          remoteMessage.notification?.title?.let { remoteMessage.notification!!.body?.let { it1 ->
              generateNotification(it,
                  it1
              )
          } }
      }
    }

@SuppressLint("RemoteViewLayout")
    fun getRemoteView(title: String,message: String): RemoteViews? {
        val remoteView = RemoteViews("com.example.fcmapplication",R.layout.notification_layout)
        remoteView.setTextViewText(R.id.text_title,title)
        remoteView.setTextViewText(R.id.text_sub_title,message)
    remoteView.setImageViewResource(R.id.img_notify,R.drawable.ic_notifications)
    return remoteView
    }

    fun generateNotification(title:String,message:String) {

        val intent = Intent(this,MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val pendingIntent = PendingIntent.getActivity(this,0,intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE)

        var builder : NotificationCompat.Builder=NotificationCompat.Builder(applicationContext,channelId)
            .setSmallIcon(R.drawable.ic_notifications)
            .setContentTitle(title)
            .setContentText(message)
            .setContentIntent(pendingIntent)
            .setSubText(message)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(1000,1000,1000,1000))
            .setOnlyAlertOnce(true)


        builder = builder.setContent(getRemoteView(title,message))

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val notificationChannel = NotificationChannel(channelId, channelName,NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(notificationChannel)

        }
        notificationManager.notify(0,builder.build())
    }

}