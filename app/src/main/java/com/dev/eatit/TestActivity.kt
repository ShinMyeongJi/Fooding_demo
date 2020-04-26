package com.dev.eatit

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.facebook.common.Common

class TestActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)
        Toast.makeText(this@TestActivity, com.dev.eatit.common.Common.currentUser.phone, Toast.LENGTH_SHORT).show()
    }
}
