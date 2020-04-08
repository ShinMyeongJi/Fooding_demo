package com.dev.eatit

import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.view.View
import android.widget.Toast
import com.dev.eatit.common.Common
import com.dev.eatit.model.User
import com.facebook.FacebookSdk
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import io.paperdb.Paper
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

class MainActivity : AppCompatActivity() {

    lateinit var signUp : Button
    lateinit var signIn : Button


    lateinit var slogan : TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        FacebookSdk.sdkInitialize(applicationContext)
        signUp = findViewById(R.id.btnSignUp)
        signIn = findViewById(R.id.btnSignIn)

        Paper.init(this@MainActivity)

        getKeyHash()

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

        var user = Paper.book().read<String>(Common.USER_KEY)
        var pwd = Paper.book().read<String>(Common.PWD_KEY)

        if(user != null && pwd != null){
            if(!user.isEmpty() && !pwd.isEmpty()){
                login(user, pwd)
            }
        }
    }


    private fun getKeyHash(){
        try{
            var info = packageManager.getPackageInfo("com.dev.eatit", PackageManager.GET_SIGNATURES)
            for(signature in info.signatures){
                var md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray())
                Log.d("key hash = ", Base64.encodeToString(md.digest(), Base64.DEFAULT))
            }
        }catch (e : PackageManager.NameNotFoundException){
            e.printStackTrace()
        }catch (e : NoSuchAlgorithmException){
            e.printStackTrace()
        }
    }
    private fun login(phone : String, pwd : String){
        val database = FirebaseDatabase.getInstance()
        val table_user = database.getReference("User")

        var mDialog = ProgressDialog(this@MainActivity)
        mDialog.setMessage("Please waiting...")
        mDialog.show()

        table_user.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if(p0.child(phone).exists()) {
                    mDialog.dismiss()
                    var user = p0.child(phone).getValue(User::class.java)
                    user?.phone = phone
                    if (user!!.password.equals(pwd))
                    {
                        var homeIntent = Intent(this@MainActivity, Home::class.java)
                        Common.currentUser = user
                        startActivity(homeIntent)
                        finish()
                    }
                    else
                        Toast.makeText(
                            this@MainActivity,"Login failed!!", Toast.LENGTH_LONG).show()

                }else{
                    mDialog.dismiss()
                    Toast.makeText(this@MainActivity, "User not exist in Database", Toast.LENGTH_LONG).show()
                }
            }

            override fun onCancelled(p0: DatabaseError) {
            }
        })
    }
}
