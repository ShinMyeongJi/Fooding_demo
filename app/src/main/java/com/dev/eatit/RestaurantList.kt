package com.dev.eatit

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.dev.eatit.ViewHolder.MenuViewHolder
import com.dev.eatit.ViewHolder.RestaurantViewHolder
import com.dev.eatit.common.Common
import com.dev.eatit.model.Category
import com.dev.eatit.model.Restaurant
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso

class RestaurantList : AppCompatActivity() {

    lateinit var recyclerView : RecyclerView
    lateinit var mSwipeRefreshLayout : SwipeRefreshLayout

    lateinit var database : FirebaseDatabase

    var adapter: FirebaseRecyclerAdapter<Restaurant, RestaurantViewHolder>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_restaurant_list)
        recyclerView = findViewById(R.id.recycle_restaurants)
        recyclerView.layoutManager= LinearLayoutManager(this)

        mSwipeRefreshLayout = findViewById(R.id.swipeRestaurantLayout)
        mSwipeRefreshLayout.setColorSchemeResources(
            R.color.colorPrimary,
            android.R.color.holo_green_dark,
            android.R.color.holo_orange_dark,
            android.R.color.holo_blue_dark)

        mSwipeRefreshLayout.setOnRefreshListener(object : SwipeRefreshLayout.OnRefreshListener {
            override fun onRefresh() {
                if (Common.isConnectedToInternet(baseContext)) {
                    loadRestaurants()
                }else{
                    Snackbar.make(recyclerView, "네트워크 연결을 확인해주세요.", Snackbar.LENGTH_SHORT).show()
                    return
                }
            }
        })

        mSwipeRefreshLayout.post(object : Runnable{
            override fun run() {
                if (Common.isConnectedToInternet(baseContext)) {
                    loadRestaurants()
                }else{
                    Snackbar.make(recyclerView, "네트워크 연결을 확인해주세요.", Snackbar.LENGTH_SHORT).show()
                    return
                }
            }
        })


        var options = FirebaseRecyclerOptions.Builder<Restaurant>()
            .setQuery(
                FirebaseDatabase.getInstance()
                    .getReference()
                    .child("Restaurants")
                , Restaurant::class.java
            )
            .build()

        adapter = object : FirebaseRecyclerAdapter<Restaurant, RestaurantViewHolder>(options) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RestaurantViewHolder {
                var itemView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.restaurant_item, parent, false)
                var viewHolder = RestaurantViewHolder(itemView)
                return viewHolder
            }

            override fun onBindViewHolder(viewHolder: RestaurantViewHolder, position: Int, model: Restaurant) {
                Log.d("=====", model?.name + ",,," + model?.image)
                viewHolder.txtRestaurantName?.setText(model?.name)
                Picasso.get().load(model?.image).into(viewHolder?.restaurantImage)
                var clickItem = model

                viewHolder?.setItemClickListener(object : ItemClickListener {
                    override fun onClick(
                        view: android.view.View,
                        position: Int,
                        isLongClick: Boolean
                    ) {
                        var menuIdIntent = Intent(this@RestaurantList, Home::class.java)
                        Common.restaurantSelected = adapter?.getRef(position)?.key!!
                        startActivity(menuIdIntent);
                    }
                })
            }
        }



    }

    private fun loadRestaurants(){
        adapter?.startListening()
        recyclerView.adapter = adapter
        mSwipeRefreshLayout.isRefreshing = false

        recyclerView.adapter?.notifyDataSetChanged()
        recyclerView.scheduleLayoutAnimation()
    }

    override fun onStop() {
        super.onStop()
        adapter?.stopListening()
    }

    override fun onResume() {
        super.onResume()
        loadRestaurants()
    }
}
