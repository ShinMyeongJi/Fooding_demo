package com.dev.eatit

import android.Manifest
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dev.eatit.Interface.RecyclerItemTouchHelper
import com.dev.eatit.Interface.RecyclerItemTouchHelperListener
import com.dev.eatit.ViewHolder.CartAdapter
import com.dev.eatit.ViewHolder.CartViewHolder
import com.dev.eatit.common.Common
import com.dev.eatit.database.Database
import com.dev.eatit.model.*
import com.dev.eatit.remote.ApiService
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.GooglePlayServicesUtil
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.*
import com.rengwuxian.materialedittext.MaterialEditText
import retrofit2.Call
import retrofit2.Response
import java.text.NumberFormat
import java.util.*

class Cart : AppCompatActivity() ,
    GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener, RecyclerItemTouchHelperListener {

    lateinit var recyclerView : RecyclerView
    lateinit var layoutManager : RecyclerView.LayoutManager

    lateinit var database : FirebaseDatabase
    lateinit var requests : DatabaseReference

    lateinit var txtTotal : TextView
    lateinit var btnPlace : Button

    lateinit var cart : ArrayList<Order>
    lateinit var adapter : CartAdapter

    lateinit var mService : ApiService //푸시 서비스
    lateinit var placeClient : PlacesClient;

    lateinit var rootLayout : RelativeLayout
    var shipAddress : Place? = null
    var placeFields = Arrays.asList(
        Place.Field.ID,
        Place.Field.NAME,
        Place.Field.ADDRESS,
        Place.Field.LAT_LNG
    )

    lateinit var mLocationRequest : LocationRequest
    lateinit var mGoogleApiClient : GoogleApiClient
    lateinit var mLastLocation : Location
    lateinit var mFusedClient : FusedLocationProviderClient

    lateinit var locationCallback : LocationCallback
    companion object{
        val UPDATE_INTERVAL = 5000
        val FATEST_INTERVAL = 3000
        val DISPLACEMENT = 10
        val LOCATION_REQUEST_CODE = 9999
        val PLAY_SERVICES_REQUEST = 9997
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        //권한 체크
        if(
            ActivityCompat.checkSelfPermission(this@Cart, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this@Cart, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ){
            ActivityCompat.requestPermissions(this@Cart, arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ), LOCATION_REQUEST_CODE)
        }
        else
        {
            if(checkPlayServices()){
                buildGoogleApiClient()
                //createLocationRequest()
            }
        }


        initPlaces()
        database = FirebaseDatabase.getInstance()
        requests = database.getReference("Requests")

        recyclerView = findViewById(R.id.listCart)
        recyclerView.setHasFixedSize(true)
        layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager

        txtTotal = findViewById(R.id.totalPrice)
        btnPlace = findViewById(R.id.btnPlaceOrder)

        rootLayout = findViewById(R.id.rootLayout)

        //삭제
        var itemTouchHelperCallback = RecyclerItemTouchHelper(0, ItemTouchHelper.LEFT, this)
        var res = ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView)

        mService = Common.getFCMService()!!
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getString(R.string.place_api_key), Locale.KOREA);
        }
        loadListFood()

        btnPlace.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v: View?) {
                if(cart.size > 0) {
                    showAlertDialog()
                }else{

                }

            }
        })

        locationCallback = object : LocationCallback(){
            override fun onLocationResult(locationResult: LocationResult?) {
                super.onLocationResult(locationResult)

                var locationList = locationResult?.locations

                if(locationList?.size!! > 0) {
                    mLastLocation = locationList?.get(locationList?.size - 1)
                }
            }
        }
    }

    private fun checkPlayServices(): Boolean {
        var resultCode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this)
        //var resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this)
        if(resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode))
                GooglePlayServicesUtil.getErrorDialog(
                    resultCode,
                    this,
                    PLAY_SERVICES_REQUEST
                ).show()
            else {
                Toast.makeText(this, "지원되지 않는 기기입니다.", Toast.LENGTH_SHORT)
                finish()
            }
            return false
        }
        return true
    }

    @Synchronized
    private fun buildGoogleApiClient(){
        mGoogleApiClient = GoogleApiClient.Builder(this)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .addApi(LocationServices.API)
            .build()
    }

    private fun initPlaces(){
        Places.initialize(this, getString(R.string.place_api_key))
        placeClient = Places.createClient(this)
    }

    private fun loadListFood(){
        cart = Database(this).getCart(Common.currentUser.phone) as ArrayList<Order>
        adapter = CartAdapter(cart, this)
        adapter.notifyDataSetChanged()
        recyclerView.adapter = adapter

        var total = 0
        for(order in cart){
            total += (Integer.parseInt(order.price) * Integer.parseInt(order.quantity))
        }

        var fmt = NumberFormat.getCurrencyInstance(Locale.KOREA)

        txtTotal.setText(fmt.format(total))
    }

    private fun showAlertDialog(){
        var alertDialog = AlertDialog.Builder(this@Cart)
        alertDialog.setTitle("주소입력")
        alertDialog.setMessage("주소를 입력하세요 : ")

        var inflater = this.layoutInflater
        var order_address_comment = inflater.inflate(R.layout.order_address_comment, null)

        //var edtAddress = order_address_comment.findViewById<MaterialEditText>(R.id.edtAddress)
        var edtAddress = supportFragmentManager.findFragmentById(R.id.place_autocomplete_fragment) as AutocompleteSupportFragment
        edtAddress.setCountry("KR")

        edtAddress.setPlaceFields(placeFields)
        edtAddress.view?.findViewById<ImageButton>(R.id.places_autocomplete_search_button)?.visibility = View.GONE
        edtAddress.view?.findViewById<EditText>(R.id.places_autocomplete_search_input)?.setHint("주소 입력")
        edtAddress.view?.findViewById<EditText>(R.id.places_autocomplete_search_input)?.setTextSize(14F)

        edtAddress.setOnPlaceSelectedListener(object : PlaceSelectionListener{
            override fun onPlaceSelected(place : Place) {
                shipAddress = place
            }
            override fun onError(p0: Status) {

            }
        })

        var edtComment = order_address_comment.findViewById<MaterialEditText>(R.id.edtComment)
        var get_home_address = order_address_comment.findViewById<RadioButton>(R.id.get_home_address)

        get_home_address.setOnCheckedChangeListener(object : CompoundButton.OnCheckedChangeListener{
            override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
                if(isChecked){
                    if(Common.currentUser.homeAddress != null)
                        edtAddress.view?.findViewById<EditText>(R.id.places_autocomplete_search_input)?.setText(Common.currentUser.homeAddress)
                }
            }
        })

        alertDialog.setView(order_address_comment)
        alertDialog.setIcon(R.drawable.ic_shopping_cart_black_24dp)

        alertDialog.setPositiveButton("Yes", object : DialogInterface.OnClickListener{
            override fun onClick(dialog: DialogInterface?, which: Int) {
                var request = Request(
                    Common.currentUser.phone,
                    Common.currentUser.name,
                    shipAddress?.address.toString(),
                    txtTotal.text.toString(),
                    "0",
                    edtComment.text.toString(),
                    String.format("%s, %s", shipAddress?.latLng?.latitude, shipAddress?.latLng?.longitude),
                    cart
                )
                Toast.makeText(this@Cart, shipAddress?.latLng?.latitude.toString() + "," + shipAddress?.latLng?.longitude.toString(), Toast.LENGTH_LONG).show()

                var order_number = System.currentTimeMillis().toString()

                //현재 시간을 key 값으로 input
                requests.child(order_number).setValue(request)
                Database(baseContext).cleanCart(Common.currentUser.phone)

                sendNotificationOrder(order_number)


            }
        })

        alertDialog.setNegativeButton("No", object : DialogInterface.OnClickListener{
            override fun onClick(dialog: DialogInterface?, which: Int) {
                dialog?.dismiss()
                supportFragmentManager.beginTransaction()
                    .remove(supportFragmentManager.findFragmentById(R.id.place_autocomplete_fragment) as AutocompleteSupportFragment)
                    .commit()
            }
        })


        alertDialog.setOnDismissListener(object : DialogInterface.OnDismissListener{
            override fun onDismiss(dialog: DialogInterface?) {
                dialog?.dismiss()
                supportFragmentManager.beginTransaction()
                    .remove(supportFragmentManager.findFragmentById(R.id.place_autocomplete_fragment) as AutocompleteSupportFragment)
                    .commit()
            }
        })


        alertDialog.show()
    }

    private fun sendNotificationOrder(order_number : String){
        var tokens = database.getReference("Tokens")
        var query = tokens.orderByChild("serverToken").equalTo(true)

        query.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot:DataSnapshot) {
                for(postSnapShots in snapshot.children){
                    var serverToken = postSnapShots.getValue(Token::class.java)

                    //보낼 raw payload 생성
                    var notification = Notification("푸딩", "새로운 주문이 들어왔습니다." + order_number)
                    var content = Sender(serverToken?.token, notification)

                    mService.sendNotification(content)
                        .enqueue(object : retrofit2.Callback<MyPushResponse>{
                            override fun onResponse(
                                call: Call<MyPushResponse>,
                                response: Response<MyPushResponse>
                            ) {

                                if(response.body()?.success == 1){
                                    Toast.makeText(this@Cart, "주문이 완료되었습니다.", Toast.LENGTH_LONG).show()
                                    finish()
                                }else{
                                    Toast.makeText(this@Cart, "주문 실패", Toast.LENGTH_LONG).show()
                                }
                            }

                            override fun onFailure(call: Call<MyPushResponse>, t: Throwable) {
                                Log.e("ERROR", t.message)
                            }
                        })
                }
            }

            override fun onCancelled(p0: DatabaseError) {
            }
        })


    }

    //Firebase Cloud Messaging 시스템은 http post 요청을 따른다.
    //header 내용은 두 가지,
    // content-type = application/json
    // Authorization : key=<Server key>
    // raw data structure
    //{
    //"to:<Token receiver>"
    //{
    // "notification" :
    //      {
    //          "title" : "Hello",
    //          "body" : "Hello this is body of message"
    //      }
    //}
    //}
    //

    override fun onContextItemSelected(item: MenuItem): Boolean {
        if(item.title.equals(Common.DELETE)){
            deleteCart(item.order)
        }
        return true
    }

    private fun deleteCart(order : Int){
        cart.removeAt(order)
        var deleteDb = Database(this@Cart)
        deleteDb.cleanCart(Common.currentUser.phone)

        for(item in cart){
            var newDB = Database(this@Cart)
            newDB.addCart(item)
        }

        loadListFood()
    }

    override fun onConnected(p0: Bundle?) {
        displayLocation()
        startLocationUpdates()
    }

    override fun onConnectionSuspended(p0: Int) {
        mGoogleApiClient.connect()
    }

    override fun onConnectionFailed(p0: ConnectionResult) {

    }

    private fun displayLocation(){

    }

    private fun startLocationUpdates(){
        if(
            ActivityCompat.checkSelfPermission(this@Cart, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this@Cart, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ){
            return
        }
        //LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this)
        mFusedClient.requestLocationUpdates(mLocationRequest, locationCallback, Looper.myLooper())
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int, position: Int) {
        if(viewHolder is CartViewHolder){
            var name = (recyclerView.adapter as CartAdapter).getItem(viewHolder.adapterPosition).getProductName()

            var deleteItem = (recyclerView.adapter as CartAdapter).getItem(viewHolder.adapterPosition)
            var deleteIndex = viewHolder.adapterPosition

            adapter.removeItem(deleteIndex)
            var newDB = Database(baseContext).removeFromCart(deleteItem.productId, Common.currentUser.phone)

            var total = 0
            val orders =
                Database(baseContext).getCart(Common.currentUser.phone)
            for (orderItem in orders) {
                total += orderItem.price.toInt() * orderItem.quantity.toInt()
            }

            val fmt = NumberFormat.getCurrencyInstance(Locale.KOREA)

            txtTotal.setText(fmt.format(total.toLong()))

            var snackbar = Snackbar.make(rootLayout, name + "가 삭제되었습니다.", Snackbar.LENGTH_SHORT)
            snackbar.setAction("실행 취소", object : View.OnClickListener{
                override fun onClick(v: View?) {
                    adapter.restoreItem(deleteItem, deleteIndex)

                    var newDB = Database(baseContext).addCart(deleteItem)
                    var total = 0
                    val orders =
                        Database(baseContext).getCart(Common.currentUser.phone)
                    for (orderItem in orders) {
                        total += orderItem.price.toInt() * orderItem.quantity.toInt()
                    }

                    val fmt = NumberFormat.getCurrencyInstance(Locale.KOREA)

                    txtTotal.setText(fmt.format(total.toLong()))
                }
            })
            snackbar.setActionTextColor(Color.BLUE)
            snackbar.show()
        }
    }


}
