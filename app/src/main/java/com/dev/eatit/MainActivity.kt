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
import com.dev.eatit.model.Token
import com.dev.eatit.model.User
import com.facebook.*
import com.facebook.appevents.AppEventsLogger
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FacebookAuthCredential
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
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

    lateinit var loginButton : LoginButton

    lateinit var mCallbackManager : CallbackManager
    lateinit var mFirebaseaAuth : FirebaseAuth

    lateinit var authStateListener : FirebaseAuth.AuthStateListener

    lateinit var accessTokenTracker : AccessTokenTracker
    lateinit var slogan : TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        FacebookSdk.sdkInitialize(applicationContext)
        signUp = findViewById(R.id.btnSignUp)
        signIn = findViewById(R.id.btnSignIn)
        loginButton = findViewById(R.id.login_button)
        loginButton.setPermissions("email", "public_profile")

        mFirebaseaAuth = FirebaseAuth.getInstance()
        //FacebookSdk.sdkInitialize(getApplicationContext())

        mCallbackManager = CallbackManager.Factory.create()
        loginButton.registerCallback(mCallbackManager, object : FacebookCallback<LoginResult>{
            override fun onSuccess(result: LoginResult?) {
                Toast.makeText(this@MainActivity, "Login Success", Toast.LENGTH_LONG).show()
                handleFacebookToken(result?.accessToken!!)
            }

            override fun onCancel() {

            }

            override fun onError(error: FacebookException?) {

            }
        })

        authStateListener = object : FirebaseAuth.AuthStateListener{
            override fun onAuthStateChanged(p0: FirebaseAuth) {
                var user = mFirebaseaAuth.currentUser

            }
        }

        accessTokenTracker = object : AccessTokenTracker(){
            override fun onCurrentAccessTokenChanged(
                oldAccessToken: AccessToken?,
                currentAccessToken: AccessToken?
            ) {
                if(currentAccessToken == null){
                    Toast.makeText(this@MainActivity, "logout", Toast.LENGTH_SHORT).show()
                    mFirebaseaAuth.signOut()
                    LoginManager.getInstance().logOut();
                }
            }
        }

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

    private fun handleFacebookToken(token : AccessToken){
        var credential = FacebookAuthProvider.getCredential(token.token)
        mFirebaseaAuth.signInWithCredential(credential)
            .addOnCompleteListener(this, object : OnCompleteListener<AuthResult>{
                override fun onComplete(p0: Task<AuthResult>) {
                    if(p0.isSuccessful){

                        var user = mFirebaseaAuth.currentUser
                       // if(p0.result?.additionalUserInfo?.isNewUser!!){

                            //Toast.makeText(this@MainActivity, user?.uid, Toast.LENGTH_SHORT).show()
                            //Init Firebase
                            var database = FirebaseDatabase.getInstance()
                            var table_user = database.getReference("User")
                            var token = Token(null, false)
                            database.getReference("Tokens").child(user?.uid!!).setValue(token)

                            table_user.addValueEventListener(object : ValueEventListener{
                                override fun onDataChange(p0: DataSnapshot) {
                                    var buildUser = User(user?.displayName, null, null)
                                    buildUser.phone = user?.uid!!
                                    if(!p0.child(user?.uid!!).exists()){
                                        table_user.child(user?.uid!!).setValue(buildUser)
                                    }
                                    Common.currentUser = buildUser
                                    var homeIntent = Intent(this@MainActivity, Home::class.java)
                                    startActivity(homeIntent)
                                    finish()
                                }

                                override fun onCancelled(p0: DatabaseError) {

                                }
                            })
                        //}


                    }else{
                        Log.d("으이구", p0.exception.toString())
                        Toast.makeText(this@MainActivity, "failed", Toast.LENGTH_SHORT).show()
                    }
                }
            })
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        mCallbackManager.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onStart() {
        super.onStart()
        mFirebaseaAuth.addAuthStateListener(authStateListener)
    }

    override fun onStop() {
        super.onStop()
        if(authStateListener != null){
            mFirebaseaAuth.removeAuthStateListener(authStateListener)
        }
    }
}
