package com.dev.eatit

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.dev.eatit.ViewHolder.FoodViewHolder
import com.dev.eatit.ViewHolder.MenuViewHolder
import com.dev.eatit.common.Common
import com.dev.eatit.database.Database
import com.dev.eatit.model.Category
import com.dev.eatit.model.Food
import com.facebook.CallbackManager
import com.facebook.share.model.ShareLinkContent
import com.facebook.share.model.SharePhoto
import com.facebook.share.model.SharePhotoContent
import com.facebook.share.widget.ShareDialog
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.*
import com.mancj.materialsearchbar.MaterialSearchBar
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import java.lang.Exception


class FoodList : AppCompatActivity() {

    lateinit var database : FirebaseDatabase
    lateinit var foodList: DatabaseReference

    lateinit var recycler_food : RecyclerView
    lateinit var layoutManager : RecyclerView.LayoutManager

    lateinit var swipeLayout : SwipeRefreshLayout

    var categoryId : String = ""
    var adapter: FirebaseRecyclerAdapter<Food, FoodViewHolder>? = null

    lateinit var searchBar : MaterialSearchBar
    var searchAdapter : FirebaseRecyclerAdapter<Food, FoodViewHolder>? = null
    var suggestionList: ArrayList<String> = ArrayList()

    lateinit var localDB : Database

    lateinit var callbackManager : CallbackManager
    lateinit var shareDialog : ShareDialog

    var target : Target = object : Target{
        override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
        }

        override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
        }

        override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {

            var photo = SharePhoto.Builder()
                .setBitmap(bitmap)
                .build()
            //if(ShareDialog.canShow(SharePhotoContent::class.java)){
                var content = SharePhotoContent.Builder()
                    .addPhoto(photo)
                    .build()
                shareDialog.show(content)
            //}
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_food_list)

        callbackManager = CallbackManager.Factory.create()
        shareDialog = ShareDialog(this@FoodList)

        database = FirebaseDatabase.getInstance()
        foodList = database.getReference("Food")

        swipeLayout = findViewById(R.id.swipe_layout)

        recycler_food = findViewById(R.id.recycle_food) as RecyclerView
        recycler_food.setHasFixedSize(true)
        layoutManager = LinearLayoutManager(this)
        recycler_food.layoutManager = layoutManager

        swipeLayout.setColorSchemeResources(
            R.color.colorPrimary,
            android.R.color.holo_green_dark,
            android.R.color.holo_orange_dark,
            android.R.color.holo_blue_dark
        )

        swipeLayout.setOnRefreshListener(object : SwipeRefreshLayout.OnRefreshListener {
            override fun onRefresh() {
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
            }
        })

        swipeLayout.post(object : Runnable{
            override fun run() {
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
            }
        })

        localDB = Database(this@FoodList)


        searchBar = findViewById(R.id.searchBar)
        loadRes()
        searchBar.lastSuggestions = suggestionList

        searchBar.addTextChangeListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                var suggestion = ArrayList<String>();
                for(search in suggestionList){
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
        var queryById = foodList.orderByChild("menuId").equalTo(categoryId)


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

                //facebook share
                foodViewHolder?.share?.setOnClickListener(object : View.OnClickListener{
                    override fun onClick(v: View?) {
                        Picasso.get().load(model?.image).into(target)
                        var bitmap = BitmapFactory.decodeResource(resources, R.drawable.ic_favorite_border_black_24dp) as Bitmap
                        var photo = SharePhoto.Builder()
                            .setBitmap(bitmap)
                            .build()

                        var content = SharePhotoContent.Builder()
                            .addPhoto(photo)
                            .build()
                        shareDialog.show(content)

                        /*var shareDialog = ShareDialog(this@FoodList)


                        var content = ShareLinkContent.Builder()
                            .setContentUrl(Uri.parse("https://developers.facebook.com"))
                            .build();
                        shareDialog.show(content)*/

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

        adapter?.startListening()

        Log.d("itemCount : " , adapter?.itemCount.toString())
        recycler_food.adapter = adapter
        swipeLayout.isRefreshing = false
    }

    override fun onStop() {
        super.onStop()
        adapter?.stopListening()
        searchAdapter?.stopListening()
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

        var searchByName = foodList.orderByChild("Name").equalTo(text.toString()) as Query

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
                        Toast.makeText(this@FoodList, ""+position , Toast.LENGTH_LONG).show()

                        var foodDetail = Intent(this@FoodList, FoodDeatils::class.java)
                        foodDetail.putExtra("foodId", searchAdapter?.getRef(position)?.key)
                        startActivity(foodDetail)
                    }
                })
            }
        }



        searchAdapter?.startListening()
        recycler_food.adapter = searchAdapter
    }
}
