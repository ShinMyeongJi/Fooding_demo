package com.dev.eatit

import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dev.eatit.ViewHolder.CartAdapter
import com.dev.eatit.common.Common
import com.dev.eatit.database.Database
import com.dev.eatit.model.Order
import com.dev.eatit.model.Request
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_food_deatils.view.*
import java.lang.NumberFormatException
import java.text.NumberFormat
import java.util.*

class Cart : AppCompatActivity() {

    lateinit var recyclerView : RecyclerView
    lateinit var layoutManager : RecyclerView.LayoutManager

    lateinit var database : FirebaseDatabase
    lateinit var requests : DatabaseReference

    lateinit var txtTotal : TextView
    lateinit var btnPlace : Button

    lateinit var cart : ArrayList<Order>
    lateinit var adapter : CartAdapter

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

        val edtAddress = EditText(this@Cart)
        var lp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )

        edtAddress.layoutParams = lp
        alertDialog.setView(edtAddress)
        alertDialog.setIcon(R.drawable.ic_shopping_cart_black_24dp)

        alertDialog.setPositiveButton("Yes", object : DialogInterface.OnClickListener{
            override fun onClick(dialog: DialogInterface?, which: Int) {
                var request = Request(
                    Common.currentUser.phone,
                    Common.currentUser.name,
                    edtAddress.text.toString(),
                    txtTotal.text.toString(),
                    cart
                )

                //현재 시간을 key 값으로 input
                requests.child(System.currentTimeMillis().toString()).setValue(request)
                Database(baseContext).cleanCart()
                Toast.makeText(this@Cart, "주문이 완료되었습니다.", Toast.LENGTH_LONG).show()
                finish()
            }
        })

        alertDialog.setNegativeButton("No", object : DialogInterface.OnClickListener{
            override fun onClick(dialog: DialogInterface?, which: Int) {
                dialog?.dismiss()
            }
        })
        alertDialog.show()
    }

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
