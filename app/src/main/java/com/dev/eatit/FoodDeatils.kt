package com.dev.eatit

import android.media.Image
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton
import com.dev.eatit.model.Food
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.*
import com.squareup.picasso.Picasso

class FoodDeatils : AppCompatActivity() {

    lateinit var food_name : TextView
    lateinit var food_price : TextView
    lateinit var food_description : TextView
    lateinit var food_image : ImageView
    lateinit var collapsingToolbarLayout : CollapsingToolbarLayout
    lateinit var btnCart : FloatingActionButton
    lateinit var numberButton : ElegantNumberButton

    var foodId : String = ""

    lateinit var database : FirebaseDatabase
    lateinit var food : DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_food_deatils)

        database = FirebaseDatabase.getInstance()
        food = database.getReference("Food")

        numberButton = findViewById(R.id.number_button)
        btnCart = findViewById(R.id.btnCart)

        food_name = findViewById(R.id.food_detail_name)
        food_price = findViewById(R.id.food_detail_price)
        food_description = findViewById(R.id.food_detail_description)
        food_image = findViewById(R.id.food_detail_img)
        collapsingToolbarLayout = findViewById(R.id.collapsing)

        collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.ExpandedAppbar)
        collapsingToolbarLayout.setCollapsedTitleTextAppearance(R.style.CollapsedAppbar)

        if(intent != null){
            foodId = intent.getStringExtra("foodId")
        }
        if(!foodId.isEmpty()){
            getDetailFood(foodId)
        }
    }

    private fun getDetailFood(foodId : String){
        food.child(foodId).addValueEventListener(object : ValueEventListener{
            override fun onDataChange(dataSnapShot: DataSnapshot) {
                var food = dataSnapShot.getValue(Food::class.java)
                Picasso.get().load(food?.image).into(food_image)
                collapsingToolbarLayout.title = food?.name
                food_price.setText(food?.price)
                food_name.setText(food?.name)
                food_description.setText(food?.description)

            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }
}
