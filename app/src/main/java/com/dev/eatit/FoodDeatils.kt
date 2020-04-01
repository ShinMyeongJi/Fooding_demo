package com.dev.eatit

import android.media.Image
import android.media.MicrophoneInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton
import com.dev.eatit.common.Common
import com.dev.eatit.database.Database
import com.dev.eatit.model.Food
import com.dev.eatit.model.Order
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import com.stepstone.apprating.AppRatingDialog
import kotlinx.android.synthetic.main.activity_food_deatils.*

class FoodDeatils : AppCompatActivity() {

    lateinit var food_name : TextView
    lateinit var food_price : TextView
    lateinit var food_description : TextView
    lateinit var food_image : ImageView
    lateinit var collapsingToolbarLayout : CollapsingToolbarLayout
    lateinit var btnCart : FloatingActionButton
    lateinit var btnRating : FloatingActionButton
    lateinit var numberButton : ElegantNumberButton

    lateinit var ratingBar : RatingBar

    lateinit var detailsLayout : CoordinatorLayout

    var foodId : String = ""

    lateinit var database : FirebaseDatabase
    lateinit var food : DatabaseReference
    lateinit var currentFood : Food
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_food_deatils)

        database = FirebaseDatabase.getInstance()
        food = database.getReference("Food")

        numberButton = findViewById(R.id.number_button)
        btnCart = findViewById(R.id.btnCart)
        btnRating = findViewById(R.id.btnRating)

        food_name = findViewById(R.id.food_detail_name)
        food_price = findViewById(R.id.food_detail_price)
        food_description = findViewById(R.id.food_detail_description)
        food_image = findViewById(R.id.food_detail_img)
        collapsingToolbarLayout = findViewById(R.id.collapsing)

        ratingBar = findViewById(R.id.ratingBar)

        detailsLayout = findViewById(R.id.detailsLayout)

        collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.ExpandedAppbar)
        collapsingToolbarLayout.setCollapsedTitleTextAppearance(R.style.CollapsedAppbar)

        if(intent != null){
            foodId = intent.getStringExtra("foodId")
        }
        if(!foodId.isEmpty()){
            if(Common.isConnectedToInternet(this@FoodDeatils)) {
                getDetailFood(foodId)
            }else{
                Snackbar.make(detailsLayout, "네트워크 연결을 확인해주세요.", Snackbar.LENGTH_SHORT).show()
                return
            }
        }

        btnRating.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v: View?) {
                showRatingDialog()
            }
        })

        btnCart.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v: View?) {
                Database(baseContext).addCart(
                    Order(
                        foodId,
                        currentFood.name,
                        numberButton.number,
                        currentFood.price,
                        currentFood.discount
                    )
                )
                Toast.makeText(this@FoodDeatils, "Added to Cart", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun getDetailFood(foodId : String){
        food.child(foodId).addValueEventListener(object : ValueEventListener{
            override fun onDataChange(dataSnapShot: DataSnapshot) {
                currentFood = dataSnapShot.getValue(Food::class.java)!!
                Picasso.get().load(currentFood?.image).into(food_image)
                collapsingToolbarLayout.title = currentFood?.name
                food_price.setText(currentFood?.price)
                food_name.setText(currentFood?.name)
                food_description.setText(currentFood?.description)

            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }

    private fun showRatingDialog(){
        var ratingDialogBuilder = AppRatingDialog.Builder()
        ratingDialogBuilder.setPositiveButtonText("확인")
            .setNegativeButtonText("취소")
    }
}
