package com.dev.eatit

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dev.eatit.ViewHolder.FoodViewHolder
import com.dev.eatit.ViewHolder.MenuViewHolder
import com.dev.eatit.model.Category
import com.dev.eatit.model.Food
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.squareup.picasso.Picasso

class FoodList : AppCompatActivity() {

    lateinit var database : FirebaseDatabase
    lateinit var foodList: DatabaseReference

    lateinit var recycler_food : RecyclerView
    lateinit var layoutManager : RecyclerView.LayoutManager

    var categoryId : String = ""
    var adapter: FirebaseRecyclerAdapter<Food, FoodViewHolder>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_food_list)

        database = FirebaseDatabase.getInstance()
        foodList = database.getReference("Food")

        Log.d("menuId", foodList.child("01/menuId").toString())
        recycler_food = findViewById(R.id.recycle_food) as RecyclerView
        recycler_food.setHasFixedSize(true)
        layoutManager = LinearLayoutManager(this)
        recycler_food.layoutManager = layoutManager

        if(intent != null) {
           categoryId = intent.getStringExtra("CategoryId")
           Log.d("CategoryId = ", categoryId)
        }

        if(!categoryId.isEmpty() && categoryId != null){
            loadListFood(categoryId)
        }
    }

    private fun loadListFood(categoryId : String){
        adapter = object :  FirebaseRecyclerAdapter<Food, FoodViewHolder>(
            Food::class.java,
            R.layout.food_item,
            FoodViewHolder::class.java,
            foodList.orderByChild("menuId").equalTo(categoryId) // like : Select * From Food where menuId = categoryId
        ) {
            override fun populateViewHolder(foodViewHolder: FoodViewHolder?, model: Food?, position: Int) {
                foodViewHolder?.food_name?.setText(model?.name)
                Log.d("foodName : ", model?.name)
                Picasso.get().load(model?.image).into(foodViewHolder?.food_image)

                val food = model

                foodViewHolder?.setItemClickListener(object : ItemClickListener{
                    override fun onClick(view: View, position: Int, isLongClick: Boolean) {
                        Toast.makeText(this@FoodList, "" + food?.name, Toast.LENGTH_LONG).show()
                    }
                })
            }
        }

        Log.d("itemCount : " , adapter?.itemCount.toString())
        recycler_food.adapter = adapter
    }
}
