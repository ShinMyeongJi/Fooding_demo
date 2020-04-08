package com.dev.eatit

import android.content.DialogInterface
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.dev.eatit.ViewHolder.MenuViewHolder
import com.dev.eatit.common.Common
import com.dev.eatit.model.Category
import com.dev.eatit.model.Token
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.Task
import com.google.android.material.dialog.InsetDialogOnTouchListener
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.iid.FirebaseInstanceId
import com.rengwuxian.materialedittext.MaterialEditText
import com.squareup.picasso.Picasso
import io.paperdb.Paper
import java.lang.Exception

class Home : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration

    lateinit var database : FirebaseDatabase
    lateinit var category: DatabaseReference

    lateinit var txtFullName : TextView

    lateinit var recycler_menu : RecyclerView
    lateinit var layoutManager : RecyclerView.LayoutManager

    lateinit var swipeLayout : SwipeRefreshLayout

    var adapter: FirebaseRecyclerAdapter<Category, MenuViewHolder>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        //Firebase 초기화
        database = FirebaseDatabase.getInstance()
        category = database.getReference("Category")

        swipeLayout = findViewById(R.id.swipeLayout)
        swipeLayout.setColorSchemeResources(
            R.color.colorPrimary,
            android.R.color.holo_green_dark,
            android.R.color.holo_orange_dark,
            android.R.color.holo_blue_dark)

        swipeLayout.setOnRefreshListener(object : SwipeRefreshLayout.OnRefreshListener {
            override fun onRefresh() {
                if (Common.isConnectedToInternet(baseContext)) {
                    loadMenu()
                }else{
                    Snackbar.make(recycler_menu, "네트워크 연결을 확인해주세요.", Snackbar.LENGTH_SHORT).show()
                    return
                }
            }
        })

        swipeLayout.post(object : Runnable{
            override fun run() {
                if (Common.isConnectedToInternet(baseContext)) {
                    loadMenu()
                }else{
                    Snackbar.make(recycler_menu, "네트워크 연결을 확인해주세요.", Snackbar.LENGTH_SHORT).show()
                    return
                }
            }
        })

        Paper.init(this@Home)

        val fab: FloatingActionButton = findViewById(R.id.fab)
        fab.setOnClickListener { view ->
            var cartIntent = Intent(this@Home, Cart::class.java)
            startActivity(cartIntent)
        }
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        //User에 대한 이름 설정
        var headerView = navView.getHeaderView(0)
        txtFullName = headerView.findViewById(R.id.txtFullName)
        txtFullName.setText(Common.currentUser.name)

        //Load Menu
        recycler_menu = findViewById(R.id.recycle_menu) as RecyclerView
        recycler_menu.setHasFixedSize(true)
        layoutManager = LinearLayoutManager(this)
        recycler_menu.layoutManager = layoutManager

        updateToken(FirebaseInstanceId.getInstance().token!!)

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
                }else if(id == R.id.nav_change_pwd) {
                    showChangePasswordDialog()
                }else if(id == R.id.logout){
                    //자동 로그인 해제
                    Paper.book().destroy()


                    var signIn = Intent(this@Home, SignIn::class.java)
                    signIn.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(signIn)
                }

                var drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
                drawer.closeDrawer(GravityCompat.START)
                return true
            }
        })

    }

    private fun showChangePasswordDialog(){
        var dialogBuilder = AlertDialog.Builder(this@Home)
        dialogBuilder.setTitle("비밀번호 변경")

        var inflater = LayoutInflater.from(this)
        var layout_pwd = inflater.inflate(R.layout.change_password_layout, null)

        var edtPassword = layout_pwd.findViewById<MaterialEditText>(R.id.edtPassword)
        var edtNewPassword = layout_pwd.findViewById<MaterialEditText>(R.id.edtNewPassword)
        var edtRepeatPassword = layout_pwd.findViewById<MaterialEditText>(R.id.edtRepeatPassword)

        dialogBuilder.setView(layout_pwd)

        dialogBuilder.setPositiveButton("확인", object : DialogInterface.OnClickListener{
            override fun onClick(dialog: DialogInterface?, which: Int) {
                var wating_dialog_builder = AlertDialog.Builder(this@Home)
                var wating_dialog = wating_dialog_builder.create()
                wating_dialog.show()


                if(edtPassword.text.toString().equals(Common.currentUser.password)){
                    if(edtNewPassword.text.toString().equals(edtRepeatPassword.text.toString())){
                        var passwordUpdate = HashMap<String, String>()
                        passwordUpdate.put("password", edtNewPassword.text.toString())

                        var user = FirebaseDatabase.getInstance().getReference("User")
                        user.child(Common.currentUser.phone)
                            .updateChildren(passwordUpdate as Map<String, String>)
                            .addOnCompleteListener(object : OnCompleteListener<Void>{
                                override fun onComplete(p0: Task<Void>) {
                                    wating_dialog.dismiss()
                                    Toast.makeText(this@Home, "비밀번호가 변경되었습니다.", Toast.LENGTH_LONG).show()
                                }
                            })
                            .addOnFailureListener(object : OnFailureListener{
                                override fun onFailure(p0: Exception) {
                                    Toast.makeText(this@Home, p0.message, Toast.LENGTH_LONG).show()
                                }
                            })
                    }else{
                        wating_dialog.dismiss()
                        Toast.makeText(this@Home, "새 비밀번호를 확인해주세요.", Toast.LENGTH_LONG).show()

                    }
                }
            }
        })

        dialogBuilder.setNegativeButton("취소", object : DialogInterface.OnClickListener{
            override fun onClick(dialog: DialogInterface?, which: Int) {

            }
        })
        dialogBuilder.show()
    }

    private fun updateToken(tokenRefreshed : String){
        var db = FirebaseDatabase.getInstance()
        var tokens = db.getReference("Tokens")
        var token = Token(tokenRefreshed, false)
        tokens.child(Common.currentUser.phone).setValue(token)
    }

    fun loadMenu(){
        adapter = object : FirebaseRecyclerAdapter<Category, MenuViewHolder>(Category::class.java, R.layout.menu_item, MenuViewHolder::class.java, category) {
            override fun populateViewHolder(viewHolder: MenuViewHolder?, model: Category?, position: Int) {
                viewHolder?.txtMenuName?.setText(model?.name)
                Picasso.get().load(model?.image).into(viewHolder?.imageView)
                var clickItem = model as Category

                viewHolder?.setItemClickListener(object : ItemClickListener {
                   override fun onClick(
                        view: android.view.View,
                        position: Int,
                        isLongClick: Boolean
                    ) {
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
        swipeLayout.isRefreshing = false
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.refresh){
            loadMenu()
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.home, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}
