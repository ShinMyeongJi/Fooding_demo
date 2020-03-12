package com.dev.eatit

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.renderscript.Sampler
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase
import android.view.View
import android.widget.Toast
import com.dev.eatit.common.Common
import com.dev.eatit.model.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class SignIn : AppCompatActivity(){

    lateinit var edtPhone : EditText
    lateinit var edtPassword : EditText
    lateinit var btnSignIn : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        edtPhone = findViewById(R.id.edtPhone)
        edtPassword = findViewById(R.id.edtPassword)
        btnSignIn = findViewById(R.id.btnSignIn)

        var database = FirebaseDatabase.getInstance()
        var table_user = database.getReference("User")

        btnSignIn.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v: View?) {
                var mDialog = ProgressDialog(this@SignIn)
                mDialog.setMessage("Please waiting...")
                mDialog.show()

                table_user.addValueEventListener(object : ValueEventListener{
                    override fun onDataChange(p0: DataSnapshot) {
                        if(p0.child(edtPhone.text.toString()).exists()) {
                            mDialog.dismiss()
                            var user = p0.child(edtPhone.text.toString()).getValue(User::class.java)

                            if (user!!.password.equals(edtPassword.text.toString()))
                            {
                                var homeIntent = Intent(this@SignIn, Home::class.java)
                                Common.currentUser = user
                                startActivity(homeIntent)
                                finish()
                            }
                            else
                                Toast.makeText(
                                    this@SignIn,"Login failed!!", Toast.LENGTH_LONG).show()

                        }else{
                            mDialog.dismiss()
                            Toast.makeText(this@SignIn, "User not exist in Database", Toast.LENGTH_LONG).show()
                        }
                    }

                    override fun onCancelled(p0: DatabaseError) {
                    }
                })
            }
        })
    }
}