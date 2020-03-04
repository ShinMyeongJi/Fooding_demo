package com.dev.eatit

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.view.View

class MainActivity : AppCompatActivity() {

    lateinit var signUp : Button
    lateinit var signIn : Button


    lateinit var slogan : TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        signUp = findViewById(R.id.btnSignUp)
        signIn = findViewById(R.id.btnSignIn)

        signUp.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v: View?) {
                var intent = Intent(this@MainActivity, SignUp::class.java)
                startActivity(intent)
            }
        })


        signIn.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v: View?) {
                var intent = Intent(this@MainActivity, SignIn::class.java)
                startActivity(intent)
            }
        })


    }
}
