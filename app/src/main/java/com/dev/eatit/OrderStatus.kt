package com.dev.eatit

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dev.eatit.ViewHolder.OrderViewHolder
import com.dev.eatit.common.Common
import com.dev.eatit.model.Order
import com.dev.eatit.model.Request
import com.dev.eatit.model.Status
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class OrderStatus : AppCompatActivity() {

    lateinit var recyclerView : RecyclerView
    lateinit var layoutManager : RecyclerView.LayoutManager

    lateinit var database : FirebaseDatabase
    lateinit var requests : DatabaseReference

    lateinit var adapter : FirebaseRecyclerAdapter<Request, OrderViewHolder>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_status)

        database = FirebaseDatabase.getInstance()
        requests = database.getReference("Requests")

        recyclerView = findViewById(R.id.listOrders)
        recyclerView.setHasFixedSize(true)

        layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager

        if(intent == null) {
            loadOrders(Common.currentUser.phone)
        }else{
            loadOrders(intent.getStringExtra("userPhone"))
        }
        //.d("phone ====", requests.orderByChild("Phone").equalTo(Common.currentUser.phone).)
    }

    private fun loadOrders(phone : String){


        adapter = object : FirebaseRecyclerAdapter<Request, OrderViewHolder>(
            Request::class.java,
            R.layout.order_layout,
            OrderViewHolder::class.java,
            requests.orderByChild("phone").equalTo(phone)
        ){
            override fun populateViewHolder(viewHolder: OrderViewHolder?, p1: Request?, p2: Int) {
                viewHolder?.order_id?.setText(adapter.getRef(p2).key)
                viewHolder?.order_status?.setText(Common.convertCodeToStatus(p1?.status!!))
                viewHolder?.order_address?.setText(p1?.address)
                viewHolder?.order_phone?.setText(p1?.phone)
            }
        }

        recyclerView.adapter = adapter
    }


}
