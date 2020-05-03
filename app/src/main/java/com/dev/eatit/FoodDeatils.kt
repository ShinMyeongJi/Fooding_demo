package com.dev.eatit

import android.content.Intent
import android.media.Image
import android.media.MicrophoneInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract
import android.view.View
import android.widget.*
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton
import com.dev.eatit.common.Common
import com.dev.eatit.database.Database
import com.dev.eatit.model.Food
import com.dev.eatit.model.Order
import com.dev.eatit.model.Rating
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import com.stepstone.apprating.AppRatingDialog
import com.stepstone.apprating.listener.RatingDialogListener
import kotlinx.android.synthetic.main.activity_food_deatils.*
import java.util.*

class FoodDeatils : AppCompatActivity(), RatingDialogListener {

    lateinit var food_name : TextView
    lateinit var food_price : TextView
    lateinit var food_description : TextView
    lateinit var food_image : ImageView
    lateinit var collapsingToolbarLayout : CollapsingToolbarLayout
    lateinit var btnCart : FloatingActionButton
    lateinit var btnRating : FloatingActionButton
    lateinit var numberButton : ElegantNumberButton
    lateinit var more_comment_btn : Button

    lateinit var ratingBar : RatingBar

    lateinit var detailsLayout : CoordinatorLayout

    var foodId : String = ""

    lateinit var database : FirebaseDatabase
    lateinit var food : DatabaseReference
    lateinit var ratingTbl : DatabaseReference

    lateinit var currentFood : Food
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_food_deatils)

        database = FirebaseDatabase.getInstance()
        food = database.getReference("Restaurants").child(Common.restaurantSelected).child("detail").child("Food")
        ratingTbl = database.getReference("Rating")

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

        more_comment_btn = findViewById(R.id.more_comment_btn)

        collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.ExpandedAppbar)
        collapsingToolbarLayout.setCollapsedTitleTextAppearance(R.style.CollapsedAppbar)

        if(intent != null){
            foodId = intent.getStringExtra("foodId")
        }
        if(!foodId.isEmpty()){
            if(Common.isConnectedToInternet(this@FoodDeatils)) {
                getDetailFood(foodId)
                getRatingFood(foodId)
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
                var isExist = Database(baseContext).checkExistFood(Common.currentUser.phone, foodId)
                if(!isExist) {
                    Database(baseContext).addCart(
                        Order(
                            Common.currentUser.phone,
                            foodId,
                            currentFood.name,
                            numberButton.number,
                            currentFood.price,
                            currentFood.discount,
                            currentFood.image
                        )
                    )

                }else{
                    var newDB = Database(baseContext)
                    newDB.increaseCart(Common.currentUser.phone, foodId)
                }

                Toast.makeText(this@FoodDeatils, "Added to Cart", Toast.LENGTH_LONG).show()

            }
        })

        more_comment_btn.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v: View?) {
                var intent = Intent(this@FoodDeatils, ShowComment::class.java)
                intent.putExtra(Common.FOOD_ID, foodId)
                startActivity(intent)
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
            .setNoteDescriptions(Arrays.asList("매우 만족", "만족", "보통", "불만족", "매우 불만족"))
            .setDefaultRating(1)
            .setTitle("평점")
            .setDescription("별점을 입력해주세요.")
            .setTitleTextColor(R.color.colorPrimary)
            .setHint("리뷰를 작성해주세요.")
            .setHintTextColor(R.color.colorAccent)
            .setCommentTextColor(R.color.white)
            .setCommentBackgroundColor(R.color.colorPrimaryDark)
            .setWindowAnimation(R.style.RatingDialogFadeAnim)
            .create(this@FoodDeatils)
            .show()
    }

    private fun getRatingFood(foodId : String){
        var foodRating = ratingTbl.orderByChild("foodId").equalTo(foodId)
        foodRating.addValueEventListener(object : ValueEventListener{
            var count = 0
            var sum = 0

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for(postSnapShot in dataSnapshot.children){
                    var item = postSnapShot.getValue(Rating::class.java)
                    sum += Integer.parseInt(item?.rateVlaue!!)
                    count++
                }

                if(count != 0){
                    var average = (sum/count).toFloat()
                    ratingBar.rating = average
                }
            }

            override fun onCancelled(p0: DatabaseError) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
        })

    }

    override fun onNegativeButtonClicked() {

    }

    override fun onNeutralButtonClicked() {

    }

    override fun onPositiveButtonClicked(rate: Int, comment: String) {
        var rate = Rating(Common.currentUser.phone, foodId, rate.toString(), comment)

        ratingTbl.push()
            .setValue(rate)
            .addOnCompleteListener(object : OnCompleteListener<Void>{
                override fun onComplete(task: Task<Void>) {
                    Toast.makeText(this@FoodDeatils, "리뷰가 등록되었습니다.", Toast.LENGTH_LONG).show()
                }
            })


        /*ratingTbl.child(Common.currentUser.phone).addValueEventListener(object : ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
                if(p0.child(Common.currentUser.phone).exists()){
                    ratingTbl.child(Common.currentUser.phone).removeValue()
                    ratingTbl.child(Common.currentUser.phone).setValue(rate)
                }else{
                    ratingTbl.child(Common.currentUser.phone).setValue(rate)
                }
            }

            override fun onCancelled(p0: DatabaseError) {
            }
        })*/
    }
}
