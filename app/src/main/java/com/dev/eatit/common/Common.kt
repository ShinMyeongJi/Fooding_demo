package com.dev.eatit.common

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.os.Build
import com.dev.eatit.model.User
import com.dev.eatit.remote.ApiService
import com.dev.eatit.remote.RetrofitClient
import com.facebook.AccessToken
import retrofit2.Retrofit

class Common {
    companion object{
        var topic = "News"
        lateinit var currentUser : User
        val FOOD_ID = "FoodId"
        var restaurantSelected = ""
        private val BASE_URL = "https://fcm.googleapis.com/"

        fun getFCMService(): ApiService? {
            return RetrofitClient.Companion.getClient(BASE_URL)?.create(ApiService::class.java)
        }

        val DELETE = "삭제"
        val USER_KEY = "User"
        val PWD_KEY = "Password"

       @JvmStatic fun convertCodeToStatus(code: String): String? {
            if (code == "0") {
               return  "준비 중"
            } else if (code == "1") {
               return  "배송 중"
            } else {
               return  "배송 완료"
            }
        }

        @JvmStatic fun isConnectedToInternet(context : Context) : Boolean{
            var connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                val network = connectivityManager.activeNetwork ?: return false // null 이면 false 반환
                val actNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false

                return when{
                    actNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                    actNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                    else -> false
                }
            } else {
                val networkInfo = connectivityManager.activeNetworkInfo ?: return false
                return networkInfo.isConnected
            }

           /* if(connectivityManager != null){
                var info = connectivityManager.allNetworkInfo as Array
                if(info != null){
                    for(i in 0..info.size-1){
                        if(info[i].state == NetworkInfo.State.CONNECTED){
                            return true
                        }
                    }
                }
            }*/
        }

        @JvmStatic fun checkLoginFacebook() : Boolean{
            var accessToken = AccessToken.getCurrentAccessToken()
            if(accessToken != null) return true
            else return false
        }
    }


}