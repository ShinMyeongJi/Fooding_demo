package com.dev.eatit.common

import com.dev.eatit.model.User

class Common {
    companion object{
        lateinit var currentUser : User

       /* @JvmStatic fun convertCodeToStatus(code: String): String? {
            return if (code == "0") {
                "준비 중"
            } else if (code == "1") {
                "배송 중"
            } else {
                "배송 완료"
            }
        }*/
    }


}