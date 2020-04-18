package com.dev.eatit

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.dev.eatit.ViewHolder.ShowCommentViewHolder
import com.dev.eatit.common.Common
import com.dev.eatit.model.Rating
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import uk.co.chrisjenx.calligraphy.CalligraphyConfig

class ShowComment : AppCompatActivity() {

    lateinit var recyclerView : RecyclerView
    lateinit var layoutManager : RecyclerView.LayoutManager

    lateinit var database : FirebaseDatabase
    lateinit var ratingTbl : DatabaseReference

    lateinit var mSwipeRefreshLayout: SwipeRefreshLayout

    lateinit var adapter : FirebaseRecyclerAdapter<Rating, ShowCommentViewHolder>

    var foodId = ""
    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(newBase)
    }

    override fun onStop() {
        super.onStop()
        if(adapter != null)
            adapter.stopListening()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_comment)
        database = FirebaseDatabase.getInstance()
        ratingTbl = database.getReference("Rating")

        recyclerView = findViewById(R.id.recycler_comment)
        layoutManager = LinearLayoutManager(this@ShowComment)
        recyclerView.layoutManager = layoutManager

        mSwipeRefreshLayout = findViewById(R.id.swipe_layout)
        mSwipeRefreshLayout.setOnRefreshListener(object : SwipeRefreshLayout.OnRefreshListener{
            override fun onRefresh() {
                setComments()
            }
        })
        mSwipeRefreshLayout.post(object : Runnable{
            override fun run() {
                mSwipeRefreshLayout.isRefreshing = true

                setComments()
            }
        })
    }


    private fun setComments(){
        if(intent != null)
            foodId = intent.getStringExtra(Common.FOOD_ID)
        if(!foodId.isEmpty() && foodId != null){
            var query = ratingTbl.orderByChild("foodId").equalTo(foodId)

            var options = FirebaseRecyclerOptions.Builder<Rating>()
                .setQuery(query, Rating::class.java)
                .build()

            adapter = object : FirebaseRecyclerAdapter<Rating, ShowCommentViewHolder>(options){

                override fun onCreateViewHolder(
                    parent: ViewGroup,
                    viewType: Int
                ): ShowCommentViewHolder {
                    var view = LayoutInflater.from(parent.context).inflate(R.layout.comment_layout, parent,false)
                    return ShowCommentViewHolder(view)
                }

                override fun onBindViewHolder(
                    holder: ShowCommentViewHolder,
                    position: Int,
                    model: Rating
                ) {
                    holder.ratingBar.rating = model.rateVlaue.toFloat()
                    holder.txtComment.setText(model.comment)
                    holder.txtUserPhone.setText(model.userPhone)
                }


            }

            loadComment(foodId)
        }
    }

    private fun loadComment(foodId : String){
        adapter.startListening()

        recyclerView.adapter = adapter
        mSwipeRefreshLayout.isRefreshing = false
    }
}
