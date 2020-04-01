package com.dev.eatit

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dev.eatit.ViewHolder.FoodViewHolder
import com.dev.eatit.common.Common
import com.dev.eatit.database.Database
import com.dev.eatit.model.Food
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.*
import com.mancj.materialsearchbar.MaterialSearchBar
import com.squareup.picasso.Picasso
import java.text.FieldPosition


class FoodList : AppCompatActivity() {

    lateinit var database : FirebaseDatabase
    lateinit var foodList: DatabaseReference

    lateinit var recycler_food : RecyclerView
    lateinit var layoutManager : RecyclerView.LayoutManager

    var categoryId : String = ""
    var adapter: FirebaseRecyclerAdapter<Food, FoodViewHolder>? = null

    lateinit var searchBar : MaterialSearchBar
    var searchAdapter : FirebaseRecyclerAdapter<Food, FoodViewHolder>? = null
    var suggestionList: ArrayList<String> = ArrayList()

    lateinit var localDB : Database

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_food_list)

        database = FirebaseDatabase.getInstance()
        foodList = database.getReference("Food")

        recycler_food = findViewById(R.id.recycle_food) as RecyclerView
        recycler_food.setHasFixedSize(true)
        layoutManager = LinearLayoutManager(this)
        recycler_food.layoutManager = layoutManager

        localDB = Database(this@FoodList)
        if(intent != null) {
           categoryId = intent.getStringExtra("CategoryId")
           Log.d("CategoryId = ", categoryId)
        }

        if(!categoryId.isEmpty() && categoryId != null){
            if(Common.isConnectedToInternet(this@FoodList)){
                loadListFood(categoryId)
            }else{
                Snackbar.make(recycler_food, "네트워크 연결을 확인해주세요.", Snackbar.LENGTH_SHORT).show()
                return
            }
        }

        searchBar = findViewById(R.id.searchBar)
        loadRes()
        searchBar.lastSuggestions = suggestionList

        searchBar.addTextChangeListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                var suggestion = ArrayList<String>();
                for(search in suggestionList){
                    Log.d("search===== : ", search)
                    if(search.contains(searchBar.text.toLowerCase())){
                        suggestion.add(search)

                    }
                }
                searchBar.lastSuggestions = suggestion
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })

        searchBar.setOnSearchActionListener(object : MaterialSearchBar.OnSearchActionListener{
            override fun onButtonClicked(buttonCode: Int) {

            }

            override fun onSearchConfirmed(text: CharSequence?) {
                startSearch(text)
            }

            override fun onSearchStateChanged(enabled: Boolean) {
                if(!enabled){
                    recycler_food.adapter = searchAdapter
                }
            }
        })

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
                Picasso.get().load(model?.image).into(foodViewHolder?.food_image)

                //favorites
                if(localDB.isFavorites(adapter?.getRef(position)?.key)){
                    foodViewHolder?.favorites?.setImageResource(R.drawable.ic_favorite_black_24dp)
                }

                foodViewHolder?.favorites?.setOnClickListener(object : View.OnClickListener{
                    override fun onClick(v: View?) {
                        if(!localDB.isFavorites(adapter?.getRef(position)?.key)) {
                            localDB.addToFavorites(adapter?.getRef(position)?.key)
                            foodViewHolder?.favorites?.setImageResource(R.drawable.ic_favorite_black_24dp)
                            Snackbar.make(
                                recycler_food,
                                model?.name + "가 즐겨찾기에 추가 되었습니다.",
                                Snackbar.LENGTH_SHORT
                            ).show()
                        }else{
                            localDB.removeFavorites(adapter?.getRef(position)?.key)
                            foodViewHolder?.favorites?.setImageResource(R.drawable.ic_favorite_border_black_24dp)
                        }
                    }
                })

                val food = model
                foodViewHolder?.setItemClickListener(object : ItemClickListener{
                    override fun onClick(view: View, position: Int, isLongClick: Boolean) {
                        var foodDetail = Intent(this@FoodList, FoodDeatils::class.java)
                        foodDetail.putExtra("foodId", adapter?.getRef(position)?.key)
                        startActivity(foodDetail)
                    }
                })
            }
        }

        Log.d("itemCount : " , adapter?.itemCount.toString())
        recycler_food.adapter = adapter
    }

    private fun loadRes(){
        foodList.orderByChild("menuId").equalTo(categoryId)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(dataSnapShot : DataSnapshot) {
                   for(snapShots in dataSnapShot.children){
                       var food = snapShots.getValue(Food::class.java)
                       suggestionList.add(food?.name.toString())
                    }
                }

                override fun onCancelled(p0: DatabaseError) {

                }
            })

    }

    private fun startSearch(text: CharSequence?) {
        searchAdapter = object :FirebaseRecyclerAdapter<Food, FoodViewHolder>(
            Food::class.java,
            R.layout.food_item,
            FoodViewHolder::class.java,
            foodList.orderByChild("name").equalTo(text.toString())
        ){
            override fun populateViewHolder(foodViewHolder: FoodViewHolder?, model: Food?, position: Int) {
                foodViewHolder?.food_name?.setText(model?.name)
                Log.d("foodName : ", model?.name)
                Picasso.get().load(model?.image).into(foodViewHolder?.food_image)

                val food = model

                foodViewHolder?.setItemClickListener(object : ItemClickListener{
                    override fun onClick(view: View, position: Int, isLongClick: Boolean) {
                        Toast.makeText(this@FoodList, ""+position , Toast.LENGTH_LONG).show()

                        var foodDetail = Intent(this@FoodList, FoodDeatils::class.java)
                        foodDetail.putExtra("foodId", searchAdapter?.getRef(position)?.key)
                        startActivity(foodDetail)
                    }
                })
            }
        }
        recycler_food.adapter = searchAdapter
    }
}
