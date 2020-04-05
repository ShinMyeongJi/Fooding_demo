package com.dev.eatit.remote

import com.dev.eatit.model.MyPushResponse
import com.dev.eatit.model.Sender
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

interface ApiService {
    @Headers(
        "Content-Type:application/json",
        "Authorization:key=AAAAEoDIED4:APA91bHT-XIW5D0biXgMpBs-9o83lmH8LxxopbzJ2xCjPOmKleuo2jWcebbYJwL0WPXFL7sDHpEQPQWkxcSoG_4F0ad5Ijjd-NMjTUb-aRt0I0WIGT0eG03VPM1t4NOif7S2JVgsqZ5q"
    )

    @POST("fcm/send")
    fun sendNotification(@Body body : Sender) : Call<MyPushResponse>
}