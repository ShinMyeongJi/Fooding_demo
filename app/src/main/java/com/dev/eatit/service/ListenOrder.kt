package com.dev.eatit.service

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.dev.eatit.OrderStatus
import com.dev.eatit.model.Request
import com.google.android.gms.common.internal.service.Common
import com.google.firebase.database.*

class ListenOrder : Service(), ChildEventListener{

    lateinit var database : FirebaseDatabase
    lateinit var requests : DatabaseReference


    override fun onCreate() {
        super.onCreate()
        database = FirebaseDatabase.getInstance()
        requests = database.getReference("Requests")
    }

    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        requests.addChildEventListener(this)
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onCancelled(p0: DatabaseError) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onChildMoved(p0: DataSnapshot, p1: String?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
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

        var builder = NotificationCompat.Builder(baseContext)
        builder.setAutoCancel(true)
            .setDefaults(Notification.DEFAULT_ALL)
            .setWhen(System.currentTimeMillis())
            .setTicker("EDMTDev")
            .setContentInfo("주문이 수정되었습니다.")
            .setContentText("주문 #" + key + " 가 " + convertCodeToStatus(request.status))
            .setContentIntent(contentIntent)
            .setContentInfo("Info")

        var notificationManager = baseContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1, builder.build())
    }

    override fun onChildAdded(p0: DataSnapshot, p1: String?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onChildRemoved(p0: DataSnapshot) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun convertCodeToStatus(code: String): String? {
        return if (code == "0") {
            "준비 중"
        } else if (code == "1") {
            "배송 중"
        } else {
            "배송 완료"
        }
    }
}
