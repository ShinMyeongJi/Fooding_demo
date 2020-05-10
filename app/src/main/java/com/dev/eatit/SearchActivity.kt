package com.dev.eatit

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.dev.eatit.ViewHolder.FoodViewHolder
import com.dev.eatit.common.Common
import com.dev.eatit.database.Database
import com.dev.eatit.model.Food
import com.facebook.CallbackManager
import com.facebook.share.model.ShareLinkContent
import com.facebook.share.widget.ShareDialog
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.*
import com.mancj.materialsearchbar.MaterialSearchBar
import com.squareup.picasso.Picasso

class SearchActivity : AppCompatActivity() {


    lateinit var recycler_search : RecyclerView
    lateinit var layoutManager : RecyclerView.LayoutManager

    lateinit var database : FirebaseDatabase
    lateinit var foodList : DatabaseReference

    lateinit var adapter : FirebaseRecyclerAdapter<Food, FoodViewHolder>

    var searchAdapter : FirebaseRecyclerAdapter<Food, FoodViewHolder>? = null
    lateinit var materialSearchBar : MaterialSearchBar
    var suggestionList : ArrayList<String> = ArrayList()

    lateinit var localDB : Database

    lateinit var callbackManager : CallbackManager
    lateinit var shareDialog : ShareDialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        callbackManager = CallbackManager.Factory.create()
        shareDialog = ShareDialog(this)

        database = FirebaseDatabase.getInstance()
        foodList = database.getReference("Restaurants").child(Common.restaurantSelected).child("detail").child("Food")

        recycler_search = findViewById(R.id.recycler_search) as RecyclerView
        recycler_search.setHasFixedSize(true)
        layoutManager = LinearLayoutManager(this)
        recycler_search.layoutManager = layoutManager

        localDB = Database(this@SearchActivity)


        materialSearchBar = findViewById(R.id.searchBar)
        loadRes()

        materialSearchBar.addTextChangeListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                var suggestion = ArrayList<String>();
                for(search in suggestionList){
                    if(search.contains(materialSearchBar.text.toLowerCase())){
                        suggestion.add(search)

                    }
                }
                materialSearchBar.lastSuggestions = suggestion
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })

        materialSearchBar.setOnSearchActionListener(object : MaterialSearchBar.OnSearchActionListener{
            override fun onButtonClicked(buttonCode: Int) {

            }

            override fun onSearchConfirmed(text: CharSequence?) {
                if (text != null) {
                    startSearch(text)
                }
            }

            override fun onSearchStateChanged(enabled: Boolean) {
                if(!enabled){
                    recycler_search.adapter = searchAdapter
                }
            }
        })

        loadAllFoods()



    }

    private fun loadAllFoods(){
        var queryById = foodList


        var options = FirebaseRecyclerOptions.Builder<Food>()
            .setQuery(queryById, Food::class.java)
            .build()

        adapter = object : FirebaseRecyclerAdapter<Food, FoodViewHolder>(options){
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodViewHolder {
                var itemView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.food_item, parent, false)
                var viewHolder = FoodViewHolder(itemView)
                return viewHolder
            }

            override fun onBindViewHolder(foodViewHolder: FoodViewHolder, position: Int, model: Food) {
                foodViewHolder?.food_name?.setText(model?.name)
                foodViewHolder?.food_price?.setText(String.format("%s원", model?.price))
                Picasso.get().load(model?.image).into(foodViewHolder?.food_image)

                //favorites
                if(localDB.isFavorites(adapter?.getRef(position)?.key, Common.currentUser.phone)){
                    foodViewHolder?.favorites?.setImageResource(R.drawable.ic_favorite_black_24dp)
                }

                foodViewHolder?.favorites?.setOnClickListener(object : View.OnClickListener{
                    override fun onClick(v: View?) {
                        if(!localDB.isFavorites(adapter?.getRef(position)?.key, Common.currentUser.phone)) {
                            localDB.addToFavorites(adapter?.getRef(position)?.key, Common.currentUser.phone)
                            foodViewHolder?.favorites?.setImageResource(R.drawable.ic_favorite_black_24dp)
                            Snackbar.make(
                                recycler_search,
                                model?.name + "가 즐겨찾기에 추가 되었습니다.",
                                Snackbar.LENGTH_SHORT
                            ).show()
                        }else{
                            localDB.removeFavorites(adapter?.getRef(position)?.key, Common.currentUser.phone)
                            foodViewHolder?.favorites?.setImageResource(R.drawable.ic_favorite_border_black_24dp)
                        }
                    }
                })

                //facebook share
                foodViewHolder?.share?.setOnClickListener(object : View.OnClickListener{
                    override fun onClick(v: View?) {
                        //Picasso.get().load(model?.image).into(target)
                        var content = ShareLinkContent.Builder()
                            .setContentUrl(Uri.parse("https://developers.facebook.com"))
                            .setImageUrl(Uri.parse(model?.image))
                            .build()


                        shareDialog.show(content)


                    }
                })

                val food = model
                foodViewHolder?.setItemClickListener(object : ItemClickListener{
                    override fun onClick(view: View, position: Int, isLongClick: Boolean) {
                        var foodDetail = Intent(this@SearchActivity, FoodDeatils::class.java)
                        foodDetail.putExtra("foodId", adapter?.getRef(position)?.key)
                        startActivity(foodDetail)
                    }
                })

            }
        }

        adapter?.startListening()

        Log.d("itemCount : " , adapter?.itemCount.toString())
        recycler_search.adapter = adapter

        recycler_search?.adapter?.notifyDataSetChanged()

    }

    private fun loadRes(){
        foodList.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(dataSnapShot : DataSnapshot) {
                for(snapShots in dataSnapShot.children){
                    var food = snapShots.getValue(Food::class.java)
                    suggestionList.add(food?.name.toString())
                }

                materialSearchBar.lastSuggestions = suggestionList
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }

    private fun startSearch(text : CharSequence){
        var searchByName = foodList.orderByChild("name").equalTo(text.toString()) as Query

        var options = FirebaseRecyclerOptions.Builder<Food>()
            .setQuery(searchByName, Food::class.java)
            .build()

        searchAdapter = object :FirebaseRecyclerAdapter<Food, FoodViewHolder>(options){
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodViewHolder {
                var itemView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.food_item, parent, false)
                var viewHolder = FoodViewHolder(itemView)
                return viewHolder
            }

            override fun onBindViewHolder(foodViewHolder: FoodViewHolder, position: Int, model: Food) {
                foodViewHolder?.food_name?.setText(model?.name)
                Log.d("foodName : ", model?.name)
                Picasso.get().load(model?.image).into(foodViewHolder?.food_image)

                val food = model

                foodViewHolder?.setItemClickListener(object : ItemClickListener{
                    override fun onClick(view: View, position: Int, isLongClick: Boolean) {
                        Toast.makeText(this@SearchActivity, ""+position , Toast.LENGTH_LONG).show()

                        var foodDetail = Intent(this@SearchActivity, FoodDeatils::class.java)
                        foodDetail.putExtra("foodId", searchAdapter?.getRef(position)?.key)
                        startActivity(foodDetail)
                    }
                })
            }
        }



        searchAdapter?.startListening()
        recycler_search.adapter = searchAdapter
    }

    override fun onStop() {
        if(adapter != null)
            adapter.stopListening()
        if(searchAdapter != null)
            searchAdapter?.stopListening()

        super.onStop()

    }
}
