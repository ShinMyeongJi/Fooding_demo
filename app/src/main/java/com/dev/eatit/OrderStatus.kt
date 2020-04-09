package com.dev.eatit

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dev.eatit.ViewHolder.FoodViewHolder
import com.dev.eatit.ViewHolder.OrderViewHolder
import com.dev.eatit.common.Common
import com.dev.eatit.model.Food
import com.dev.eatit.model.Order
import com.dev.eatit.model.Request
import com.dev.eatit.model.Status
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
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

        var getOrderByUser = requests.orderByChild("phone").equalTo(phone)


        var options = FirebaseRecyclerOptions.Builder<Request>()
            .setQuery(getOrderByUser, Request::class.java)
            .build()


        adapter = object : FirebaseRecyclerAdapter<Request, OrderViewHolder>(options){
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
                var itemView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.order_layout, parent, false)
                var viewHolder = OrderViewHolder(itemView)
                return viewHolder
            }

            override fun onBindViewHolder(viewHolder: OrderViewHolder, position: Int, model: Request) {
                viewHolder?.order_id?.setText(adapter.getRef(position).key)
                viewHolder?.order_status?.setText(Common.convertCodeToStatus(model?.status!!))
                viewHolder?.order_address?.setText(model?.address)
                viewHolder?.order_phone?.setText(model?.phone)
            }

        }
        adapter?.startListening()
        recyclerView.adapter = adapter
    }

    override fun onStop() {
        super.onStop()
        adapter?.stopListening()
    }
}
