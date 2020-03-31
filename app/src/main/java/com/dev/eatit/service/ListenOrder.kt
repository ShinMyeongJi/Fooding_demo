package com.dev.eatit.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.dev.eatit.OrderStatus
import com.dev.eatit.common.Common
import com.dev.eatit.model.Request
import com.google.firebase.database.*


class ListenOrder : Service(), ChildEventListener{

    lateinit var database : FirebaseDatabase
    lateinit var requests : DatabaseReference


    override fun onCreate() {
        super.onCreate()
        database = FirebaseDatabase.getInstance()
        requests = database.getReference("Requests")
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        requests.addChildEventListener(this)
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onCancelled(p0: DatabaseError) {

    }

    override fun onChildMoved(p0: DataSnapshot, p1: String?) {

    }

    override fun onChildChanged(p0: DataSnapshot, p1: String?) {
        //Trigger
        var request = p0.getValue(Request::class.java)
        showNotification(p0.key!!, request!!)
    }

    private fun showNotification(key : String, request : Request){
        var intent = Intent(baseContext, OrderStatus::class.java)
        intent.putExtra("userPhone", request.phone)
        var contentIntent = PendingIntent.getActivity(baseContext, 0,
            intent, PendingIntent.FLAG_UPDATE_CURRENT)


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "foodStatus",
                "foodStatus",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val notificationManager = getSystemService(
                NotificationManager::class.java
            )
            notificationManager.createNotificationChannel(channel)
        }


        var builder = NotificationCompat.Builder(baseContext, "foodStatus")
        builder.setAutoCancel(true)
            .setDefaults(Notification.DEFAULT_ALL)
            .setWhen(System.currentTimeMillis())
            .setTicker("EDMTDev")
            .setSmallIcon(R.drawable.notification_template_icon_bg)
            .setContentInfo("주문이 수정되었습니다.")
            .setContentText("주문 #" + key + " 가 " + Common.convertCodeToStatus(request.status))
            .setContentIntent(contentIntent)
            .setContentInfo("Info")

        var notificationManager = baseContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1, builder.build())
    }

    override fun onChildAdded(p0: DataSnapshot, p1: String?) {

    }

    override fun onChildRemoved(p0: DataSnapshot) {

    }
}
