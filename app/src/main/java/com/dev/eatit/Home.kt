package com.dev.eatit

import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.navigation.NavigationView
import androidx.navigation.ui.AppBarConfiguration
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dev.eatit.ViewHolder.MenuViewHolder
import com.dev.eatit.common.Common
import com.dev.eatit.model.Category
import com.dev.eatit.model.Food
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso

class Home : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration

    lateinit var database : FirebaseDatabase
    lateinit var category: DatabaseReference

    lateinit var txtFullName : TextView

    lateinit var recycler_menu : RecyclerView
    lateinit var layoutManager : RecyclerView.LayoutManager
    var adapter: FirebaseRecyclerAdapter<Category, MenuViewHolder>? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        toolbar.setTitle("Menu")
        setSupportActionBar(toolbar)


        //Firebase 초기화
        database = FirebaseDatabase.getInstance()
        category = database.getReference("Category")


        val fab: FloatingActionButton = findViewById(R.id.fab)
        fab.setOnClickListener { view ->
            //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show()
            var cartIntent = Intent(this@Home, Cart::class.java)
            startActivity(cartIntent)
        }
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        // val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow
            ), drawerLayout
        )
        //setupActionBarWithNavController(navController, appBarConfiguration)
        //navView.setupWithNavController(navController)

        //User에 대한 이름 설정
        var headerView = navView.getHeaderView(0)
        txtFullName = headerView.findViewById(R.id.txtFullName)
        txtFullName.setText(Common.currentUser.name)

        //Load Menu
        recycler_menu = findViewById(R.id.recycle_menu) as RecyclerView
        recycler_menu.setHasFixedSize(true)
        layoutManager = LinearLayoutManager(this)
        recycler_menu.layoutManager = layoutManager

        loadMenu()




        navView.setNavigationItemSelectedListener(object : NavigationView.OnNavigationItemSelectedListener{
            override fun onNavigationItemSelected(item: MenuItem): Boolean {
                var id = item.itemId

                if(id == R.id.nav_menu){

                }else if(id == R.id.nav_cart){
                    var cartIntent = Intent(this@Home, Cart::class.java)
                    startActivity(cartIntent)
                }else if(id == R.id.nav_orders){
                    var orderIntent = Intent(this@Home, OrderStatus::class.java)
                    startActivity(orderIntent)
                }else if(id == R.id.logout){
                    var signIn = Intent(this@Home, SignIn::class.java)
                    signIn.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(signIn)
                }

                var drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
                drawer.closeDrawer(GravityCompat.START)
                return true
            }
        })

        /*supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_menu_black_24dp)
        supportActionBar?.setDisplayHomeAsUpEnabled(true);
        supportActionBar?.setHomeButtonEnabled(true);*/

    }

    fun loadMenu(){


        adapter = object : FirebaseRecyclerAdapter<Category, MenuViewHolder>(Category::class.java, R.layout.menu_item, MenuViewHolder::class.java, category) {
            override fun populateViewHolder(viewHolder: MenuViewHolder?, model: Category?, position: Int) {
                viewHolder?.txtMenuName?.setText(model?.name)
                Picasso.get().load(model?.image).into(viewHolder?.imageView)
                var clickItem = model as Category

                viewHolder?.setItemClickListener(object :
                    ItemClickListener {
                    override fun onClick(view: View, position: Int, isLongClick: Boolean) {
                        //클릭 시 새 activity에 menuId를 보내 줌
                        var menuIdIntent = Intent(this@Home, FoodList::class.java)
                        menuIdIntent.putExtra("CategoryId", adapter?.getRef(position)?.key)
                        startActivity(menuIdIntent);
                        //Toast.makeText(this@Home, "" + clickItem.name, Toast.LENGTH_LONG).show()
                    }
                })
            }
        }
        recycler_menu.adapter = adapter
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.home, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        //val navController = findNavController(R.id.nav_host_fragment)
        //return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
        return false
    }



}
