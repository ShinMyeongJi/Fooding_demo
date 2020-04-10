package com.dev.eatit

import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dev.eatit.ViewHolder.CartAdapter
import com.dev.eatit.common.Common
import com.dev.eatit.database.Database
import com.dev.eatit.model.*
import com.dev.eatit.remote.ApiService
import com.google.firebase.database.*
import com.rengwuxian.materialedittext.MaterialEditText
import kotlinx.android.synthetic.main.activity_food_deatils.view.*
import retrofit2.Call
import retrofit2.Response
import uk.co.chrisjenx.calligraphy.CalligraphyConfig
import java.lang.NumberFormatException
import java.text.NumberFormat
import java.util.*
import javax.security.auth.callback.Callback

class Cart : AppCompatActivity() {

    lateinit var recyclerView : RecyclerView
    lateinit var layoutManager : RecyclerView.LayoutManager

    lateinit var database : FirebaseDatabase
    lateinit var requests : DatabaseReference

    lateinit var txtTotal : TextView
    lateinit var btnPlace : Button

    lateinit var cart : ArrayList<Order>
    lateinit var adapter : CartAdapter

    lateinit var mService : ApiService //푸시 서비스
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_cart)

        database = FirebaseDatabase.getInstance()
        requests = database.getReference("Requests")

        recyclerView = findViewById(R.id.listCart)
        recyclerView.setHasFixedSize(true)
        layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager

        txtTotal = findViewById(R.id.totalPrice)
        btnPlace = findViewById(R.id.btnPlaceOrder)

        mService = Common.getFCMService()!!

        loadListFood()

        btnPlace.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v: View?) {
                if(cart.size > 0) {
                    showAlertDialog()
                }else{

                }

            }
        })
    }

    private fun loadListFood(){
        cart = Database(this).cart as ArrayList<Order>
        adapter = CartAdapter(cart, this)
        adapter.notifyDataSetChanged()
        recyclerView.adapter = adapter

        var total = 0
        for(order in cart){
            total += (Integer.parseInt(order.price) * Integer.parseInt(order.quantity))
        }

        var fmt = NumberFormat.getCurrencyInstance(Locale.KOREA)

        txtTotal.setText(fmt.format(total))
    }

    private fun showAlertDialog(){
        var alertDialog = AlertDialog.Builder(this@Cart)
        alertDialog.setTitle("주소입력")
        alertDialog.setMessage("주소를 입력하세요 : ")

        /*val edtAddress = EditText(this@Cart)
        var lp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        edtAddress.layoutParams = lp*/

        var inflater = this.layoutInflater
        var order_address_comment = inflater.inflate(R.layout.order_address_comment, null)

        var edtAddress = order_address_comment.findViewById<MaterialEditText>(R.id.edtAddress)
        var edtComment = order_address_comment.findViewById<MaterialEditText>(R.id.edtComment)

        alertDialog.setView(order_address_comment)
        alertDialog.setIcon(R.drawable.ic_shopping_cart_black_24dp)

        alertDialog.setPositiveButton("Yes", object : DialogInterface.OnClickListener{
            override fun onClick(dialog: DialogInterface?, which: Int) {
                var request = Request(
                    Common.currentUser.phone,
                    Common.currentUser.name,
                    edtAddress.text.toString(),
                    txtTotal.text.toString(),
                    "0",
                    edtComment.text.toString(),
                    cart
                )

                var order_number = System.currentTimeMillis().toString()

                //현재 시간을 key 값으로 input
                requests.child(order_number).setValue(request)
                Database(baseContext).cleanCart()

                sendNotificationOrder(order_number)


            }
        })

        alertDialog.setNegativeButton("No", object : DialogInterface.OnClickListener{
            override fun onClick(dialog: DialogInterface?, which: Int) {
                dialog?.dismiss()
            }
        })
        alertDialog.show()
    }

    private fun sendNotificationOrder(order_number : String){
        var tokens = database.getReference("Tokens")
        var query = tokens.orderByChild("serverToken").equalTo(true)

        query.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot:DataSnapshot) {
                for(postSnapShots in snapshot.children){
                    var serverToken = postSnapShots.getValue(Token::class.java)

                    //보낼 raw payload 생성
                    var notification = Notification("푸딩", "새로운 주문이 들어왔습니다." + order_number)
                    var content = Sender(serverToken?.token, notification)

                    mService.sendNotification(content)
                        .enqueue(object : retrofit2.Callback<MyPushResponse>{
                            override fun onResponse(
                                call: Call<MyPushResponse>,
                                response: Response<MyPushResponse>
                            ) {

                                if(response.body()?.success == 1){
                                    Toast.makeText(this@Cart, "주문이 완료되었습니다.", Toast.LENGTH_LONG).show()
                                    finish()
                                }else{
                                    Toast.makeText(this@Cart, "주문 실패", Toast.LENGTH_LONG).show()
                                }
                            }

                            override fun onFailure(call: Call<MyPushResponse>, t: Throwable) {
                                Log.e("ERROR", t.message)
                            }
                        })
                }
            }

            override fun onCancelled(p0: DatabaseError) {
            }
        })


    }

    //Firebase Cloud Messaging 시스템은 http post 요청을 따른다.
    //header 내용은 두 가지,
    // content-type = application/json
    // Authorization : key=<Server key>
    // raw data structure
    //{
    //"to:<Token receiver>"
    //{
    // "notification" :
    //      {
    //          "title" : "Hello",
    //          "body" : "Hello this is body of message"
    //      }
    //}
    //}
    //

    override fun onContextItemSelected(item: MenuItem): Boolean {
        if(item.title.equals(Common.DELETE)){
            deleteCart(item.order)
        }
        return true
    }

    private fun deleteCart(order : Int){
        cart.removeAt(order)
        var deleteDb = Database(this@Cart)
        deleteDb.cleanCart()

        for(item in cart){
            var newDB = Database(this@Cart)
            newDB.addCart(item)
        }

        loadListFood()
    }
}
