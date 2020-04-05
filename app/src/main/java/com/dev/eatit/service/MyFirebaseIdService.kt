package com.dev.eatit.service

import com.dev.eatit.common.Common
import com.dev.eatit.model.Token
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessagingService

class MyFirebaseIdService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        if(Common.currentUser != null)
            updateTokenToFirebase(token)
    }

    private fun updateTokenToFirebase(tokenRefreshed : String){
        var db = FirebaseDatabase.getInstance()
        var tokens = db.getReference("Tokens")
        var token = Token(tokenRefreshed, false)
        tokens.child(Common.currentUser.phone).setValue(token)
    }
}