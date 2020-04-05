package com.dev.eatit

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.renderscript.Sampler
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.widget.*
import com.dev.eatit.common.Common
import com.dev.eatit.model.User
import com.google.android.material.datepicker.MaterialCalendar
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.*
import com.rengwuxian.materialedittext.MaterialEditText
import io.paperdb.Paper

class SignIn : AppCompatActivity(){

    lateinit var edtPhone : EditText
    lateinit var edtPassword : EditText
    lateinit var btnSignIn : Button
    lateinit var txtForgotPwd : TextView

    lateinit var autoLoginCheck : CheckBox
    lateinit var signUpView : RelativeLayout

    lateinit var database : FirebaseDatabase
    lateinit var table_user : DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        edtPhone = findViewById(R.id.edtPhone)
        edtPassword = findViewById(R.id.edtPassword)
        btnSignIn = findViewById(R.id.btnSignIn)
        autoLoginCheck = findViewById(R.id.autoLoginCheck)
        txtForgotPwd = findViewById(R.id.txtForgotPwd)

        signUpView = findViewById(R.id.signView)
        Paper.init(this@SignIn)


        database = FirebaseDatabase.getInstance()
        table_user = database.getReference("User")

        txtForgotPwd.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v: View?) {
                showForgotPwdDialog()
            }
        })

        btnSignIn.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {

                if (Common.isConnectedToInternet(baseContext)) {

                    if (autoLoginCheck.isChecked()) {
                        Paper.book().write(Common.USER_KEY, edtPhone.text.toString())
                        Paper.book().write(Common.PWD_KEY, edtPassword.text.toString())
                    }

                    var mDialog = ProgressDialog(this@SignIn)
                    mDialog.setMessage("Please waiting...")
                    mDialog.show()

                    table_user.addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(p0: DataSnapshot) {
                            if (p0.child(edtPhone.text.toString()).exists()) {
                                mDialog.dismiss()
                                var user = p0.child(edtPhone.text.toString()).getValue(User::class.java)
                                user?.phone = edtPhone.text.toString()
                                if (user!!.password.equals(edtPassword.text.toString())) {
                                    mDialog.dismiss()
                                    var homeIntent = Intent(this@SignIn, Home::class.java)
                                    Log.e("Tlqkf", user.phone)
                                    Common.currentUser = user
                                    startActivity(homeIntent)
                                    finish()
                                } else
                                    Toast.makeText(
                                        this@SignIn, "Login failed!!", Toast.LENGTH_LONG
                                    ).show()

                            } else {
                                mDialog.dismiss()
                                Toast.makeText(
                                    this@SignIn,
                                    "User not exist in Database",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }

                        override fun onCancelled(p0: DatabaseError) {
                        }
                    })
                }else{
                    Snackbar.make(signUpView, "네트워크 연결을 확인해주세요.", Snackbar.LENGTH_SHORT).show()
                    return
                }
            }
        })
    }

    private fun showForgotPwdDialog(){
        var builder = AlertDialog.Builder(this@SignIn)
        builder.setTitle("비밀번호 찾기")
        builder.setMessage("secure code를 입력하세요.")

        var inflater = this.layoutInflater
        var forgot_view = inflater.inflate(R.layout.forgot_password_layout, null)

        builder.setView(forgot_view)
        builder.setIcon(R.drawable.ic_security_black_24dp)

        var edtPhone = forgot_view.findViewById<MaterialEditText>(R.id.edtPhone)
        var secureCode = forgot_view.findViewById<MaterialEditText>(R.id.secureCode)

        builder.setPositiveButton("확인", object : DialogInterface.OnClickListener{
            override fun onClick(dialog: DialogInterface?, which: Int) {
               table_user.addValueEventListener(object : ValueEventListener{
                   override fun onDataChange(p0: DataSnapshot) {
                       var user = p0.child(edtPhone.text.toString()).getValue(User::class.java)
                       if(user?.secureCode.equals(secureCode.text.toString())){
                           Toast.makeText(this@SignIn, "비밀번호는 " + user?.password + "입니다.", Toast.LENGTH_LONG).show()
                       }else{
                           Toast.makeText(this@SignIn, "secure code가 옳바르지 않습니다.", Toast.LENGTH_LONG).show()

                       }
                   }

                   override fun onCancelled(p0: DatabaseError) {

                   }
               })
            }
        })

        builder.setNegativeButton("취소", object : DialogInterface.OnClickListener{
            override fun onClick(dialog: DialogInterface?, which: Int) {

            }
        })

        builder.show()
    }
}