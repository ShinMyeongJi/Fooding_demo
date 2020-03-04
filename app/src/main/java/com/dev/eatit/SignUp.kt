package com.dev.eatit

import android.app.ProgressDialog
import android.os.Bundle
import android.view.View
import android.os.PersistableBundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dev.eatit.model.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.rengwuxian.materialedittext.MaterialEditText

class SignUp : AppCompatActivity() {

    lateinit var edtPhone : MaterialEditText
    lateinit var edtName : MaterialEditText
    lateinit var edtPassword : MaterialEditText

    lateinit var btnSignUp : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)


        edtPhone = findViewById(R.id.edtPhone)
        edtName = findViewById(R.id.edtName)
        edtPassword = findViewById(R.id.edtPassword)

        btnSignUp = findViewById(R.id.btnSignUp)

        //Init Firebase
        var database = FirebaseDatabase.getInstance()
        var table_user = database.getReference("User")

        btnSignUp.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v: View?) {
                var mDialog = ProgressDialog(this@SignUp)
                mDialog.setMessage("Please waiting...")
                mDialog.show()


                table_user.addValueEventListener(object : ValueEventListener{
                    override fun onDataChange(p0: DataSnapshot) {
                        if(p0.child(edtPhone.text.toString()).exists()){
                            mDialog.dismiss()
                            Toast.makeText(this@SignUp, "Phone Number already exist", Toast.LENGTH_LONG).show()
                        }else{
                            mDialog.dismiss()
                            var user = User(edtName.text.toString(), edtPassword.text.toString())
                            table_user.child(edtPhone.text.toString()).setValue(user)
                            Toast.makeText(this@SignUp, "Sign Up Successfully", Toast.LENGTH_LONG).show()
                            finish()
                        }
                    }

                    override fun onCancelled(p0: DatabaseError) {

                    }
                })
            }
        })

    }
}