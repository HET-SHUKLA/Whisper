package project.social.whisper

import adapters.ChatAdapter
import adapters.DatabaseAdapter
import adapters.GlobalStaticAdapter
import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import fragments.ContactFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import models.ChatModel
import project.social.whisper.databinding.ActivityChatBinding
import services.NotificationService
import java.math.BigInteger
import java.nio.charset.StandardCharsets
import java.util.Date
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class ChatActivity : AppCompatActivity() {

    private lateinit var b:ActivityChatBinding

    private lateinit var key:String
    private lateinit var uid:String

    private lateinit var senderKey:String
    private lateinit var receiverKey:String

    private lateinit var senderRoom:String
    private lateinit var receiverRoom:String

    private var chats:ArrayList<ChatModel> = ArrayList()
    private var chatsTemp:ArrayList<ChatModel> = ArrayList()


    private var chatAdapter:ChatAdapter = ChatAdapter(this, chats)

    //Activity Result Launcher
    private lateinit var readContacts: ActivityResultLauncher<Intent>

    private lateinit var callback: OnBackPressedCallback

    private lateinit var imageCapture: ActivityResultLauncher<Intent>

    private lateinit var sharedSecret:ByteArray

    private lateinit var userName:String
    private lateinit var fcmToken:String

    private lateinit var selfUserName:String
    private lateinit var selfImgUrl:String

    companion object {
        const val MY_PERMISSIONS_REQUEST_LOCATION = 1
    }

    //Permission callback
    private val permissionsResultCallback = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        when (it) {
            true -> {
                Toast.makeText(this, "Granted", Toast.LENGTH_LONG).show()
            }

            false -> {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        b = ActivityChatBinding.inflate(layoutInflater)
        setContentView(b.root)

        //Activity Results
        readContacts = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == Activity.RESULT_OK) {
                Toast.makeText(this, "Contact selected",Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Contact cancel",Toast.LENGTH_LONG).show()
            }
        }

        callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (b.flChatActCont.visibility == View.VISIBLE) {
                    Log.d("ASDASD","a")
                    b.flChatActCont.visibility = View.GONE
                    supportFragmentManager.popBackStack() // Pop the back stack when fragment is visible
                    // Consume the event
                } else {
                    Log.d("ASDASD","ab")
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()// Delegate to default navigation behavior
                    isEnabled = true
                }
            }
        }

        imageCapture = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == Activity.RESULT_OK) {
                val data = it.data
                val uri: Uri? = data?.data

                val encryptedUri = DatabaseAdapter.encryptImage(uri!!, sharedSecret, applicationContext)

                uploadImage(encryptedUri)
            } else {
                Toast.makeText(this, "Image selection canceled", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        onBackPressedDispatcher.addCallback(this, callback)

        key = GlobalStaticAdapter.key2
        uid = GlobalStaticAdapter.uid2
        userName = GlobalStaticAdapter.userName2
        val imgUrl = GlobalStaticAdapter.imageUrl2
        fcmToken = GlobalStaticAdapter.fcmToken2

        senderKey = GlobalStaticAdapter.key
        receiverKey = key

        senderRoom = senderKey + receiverKey
        receiverRoom = receiverKey + senderKey

        DatabaseAdapter.keysTable.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                var publicKeyForShared = ""
                var privateKeyForShared = ""

                if(!snapshot.child(senderRoom).exists()) {
                    DatabaseAdapter.generateEncryptionKey(
                        DatabaseAdapter.returnUser()?.email!!,
                        DatabaseAdapter.generateRandomKey(),
                        senderRoom
                    )

                    DatabaseAdapter.usersTable.child(uid).child("EMAIL").addListenerForSingleValueEvent(object:ValueEventListener{
                        override fun onDataChange(s: DataSnapshot) {
                            DatabaseAdapter.generateEncryptionKey(
                                s.getValue(String::class.java)!!,
                                DatabaseAdapter.generateRandomKey(),
                                receiverRoom
                            )

                            //while(!snapshot.child("PUBLIC_KEY").exists()) {
                            if (snapshot.child(receiverRoom).child("PUBLIC_KEY").exists()) {
                                publicKeyForShared =
                                    snapshot.child("PUBLIC_KEY").getValue(String::class.java)!!
                            }

                            if (snapshot.child(senderRoom).child("KEY").exists()) {
                                privateKeyForShared =
                                    snapshot.child("KEY").getValue(String::class.java)!!
                            }
                            //}
                        }
                        override fun onCancelled(error: DatabaseError) {
                        }
                    })

                }
                else
                {
                    publicKeyForShared = snapshot.child(receiverRoom).child("PUBLIC_KEY").getValue(String::class.java)!!
                    privateKeyForShared = snapshot.child(senderRoom).child("KEY").getValue(String::class.java)!!
                }

                if(publicKeyForShared!="")
                {

                    val num = DatabaseAdapter.decryptPrivateKey(privateKeyForShared, senderRoom, DatabaseAdapter.returnUser()?.email!!)

                    sharedSecret = BigInteger(publicKeyForShared)
                        .modPow(BigInteger(num), DatabaseAdapter.p)
                        .toString().toByteArray(StandardCharsets.ISO_8859_1).toHashSet().toByteArray()

                }

                receiveData()
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })

        selfUserName = GlobalStaticAdapter.userName
        selfImgUrl = GlobalStaticAdapter.imageUrl

        val lManager = LinearLayoutManager(this)
        lManager.stackFromEnd = true
        b.rvChatAct.layoutManager = lManager
        b.rvChatAct.adapter = chatAdapter

//        val itemTouchHelper = ItemTouchHelper(simpleCallback)
//        itemTouchHelper.attachToRecyclerView(b.rvChatAct)

//        b.rvChatAct.addOnLayoutChangeListener { _, _, _, _, bottom, _, _, _, oldBottom ->
//            if (bottom < oldBottom) {
//                b.rvChatAct.scrollBy(0, oldBottom - bottom);
//            }
//        }
        b.imgChatActSend.setOnClickListener {
            sendData()
        }

        b.tvChatActUserName.text = userName
        Glide.with(this).load(imgUrl).into(b.imgChatActUserImage)

        //Accept Requests buttons
        b.btnActMsgReq.setOnClickListener {
            try {
                DatabaseAdapter.chatRooms.child(receiverRoom).child("IS_ACCEPTED").setValue(true)
                    .addOnCompleteListener {
                        b.llChatActMsgReq.visibility = View.GONE
                    }
            }catch(e:Exception)
            {
                Log.d("DB_ERROR",e.toString())
            }
        }

        b.ibChatActAttach.setOnClickListener {
            val popupMenu = PopupMenu(this, b.ibChatActAttach)

            // Inflating popup menu from popup_menu.xml file
            popupMenu.menuInflater.inflate(R.menu.chat_pop_up_menu, popupMenu.menu)

            popupMenu.setOnMenuItemClickListener {
                when(it.itemId) {

                    R.id.menu_contact -> {
                        requestContactPermission()

                        if (hasContactPermission()) {
                            val fragment = ContactFragment()

                            supportFragmentManager.beginTransaction()
                                .replace(R.id.fl_chat_act_cont, fragment)
                                .commit()

                            b.flChatActCont.visibility = View.VISIBLE
                        }
                    }

                    R.id.menu_image -> {
                        sendImage()
                    }

                    R.id.menu_location -> {
                        sendLocation()
                    }

                    R.id.menu_camera -> {
                        sendWithCamera()
                    }
                }
                true
            }
            // Showing the popup menu
            popupMenu.show()
        }

        b.tvChatActUserName.setOnClickListener {
            val i = Intent(this, UserProfileActivity::class.java)
            startActivity(i)
        }

        //Decline Requests buttons
        b.btnDecMsgReq.setOnClickListener {
            Toast.makeText(this,"Request declined!",Toast.LENGTH_LONG).show()
            b.llChatActMsgReq.visibility = View.GONE
        }
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            applicationContext,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun sendLocation() {
        requestLocationPermission()
        requestLocationCorsePermission()

        if(isGpsEnabled()) {
            if (hasLocationPermission() && hasLocationCorsePermission()) {
                getLocation()
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_LONG).show()
            }
        }
        else{
            showEnableLocationDialog()
        }
    }

    private fun showEnableLocationDialog() {
        // Show an alert dialog to prompt the user to enable location services
        AlertDialog.Builder(this)
            .setTitle("Location Services")
            .setMessage("Please enable location services to use this feature.")
            .setPositiveButton("OK") { _, _ ->
                // Open location settings screen to allow the user to enable location services
                val settingsIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(settingsIntent)
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
                Toast.makeText(this, "Unable to access location",Toast.LENGTH_LONG).show()
            }
            .show()

    }

    private fun isGpsEnabled(): Boolean {
        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        Log.d("Location", "Is : ${locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)}")
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    private fun hasLocationCorsePermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            applicationContext,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationCorsePermission() {
        val permission = ContextCompat.checkSelfPermission(
            applicationContext, android.Manifest.permission.ACCESS_COARSE_LOCATION
        )

        if (permission != PackageManager.PERMISSION_GRANTED) {
            permissionsResultCallback.launch(android.Manifest.permission.ACCESS_COARSE_LOCATION)
        } else {
            Toast.makeText(applicationContext, "Location granted", Toast.LENGTH_LONG).show()
        }
    }

    private fun getLocation() {
        Log.d("Location", "Here1")

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Create a location request
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
            .setWaitForAccurateLocation(true)
            .build()


        // Check location settings
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val client = LocationServices.getSettingsClient(this)
        val task = client.checkLocationSettings(builder.build())

        // Request location updates
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.d("Location", "Here")

            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                MY_PERMISSIONS_REQUEST_LOCATION
            )

            return
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val lastLocation = locationResult.lastLocation
                // Handle the updated location
                val latitude = lastLocation?.latitude
                val longitude = lastLocation?.longitude
                Log.d("Location", "Latitude: $latitude, Longitude: $longitude")
            }
        }, Looper.getMainLooper())
    }

    private fun requestLocationPermission() {
        val permission = ContextCompat.checkSelfPermission(
            applicationContext, android.Manifest.permission.ACCESS_FINE_LOCATION
        )

        if (permission != PackageManager.PERMISSION_GRANTED) {
            permissionsResultCallback.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            Toast.makeText(applicationContext, "Location granted", Toast.LENGTH_LONG).show()
        }
    }

    private fun uploadImage(uri: Uri?) {
        try {
            if (uri != null) {
                val curTime = Date().time.toString()
                DatabaseAdapter.chatImage.child(senderRoom).child(curTime).putFile(uri).addOnSuccessListener {

                    DatabaseAdapter.deleteTempFiles(cacheDir)

                    DatabaseAdapter.chatImage.child(senderRoom).child(curTime).downloadUrl.addOnSuccessListener { img ->

                        val chatMap = HashMap<String, Any>()
                        chatMap["SENDER_KEY"] = senderKey
                        chatMap["SENDER_UID"] = DatabaseAdapter.returnUser()?.uid!!
                        chatMap["MESSAGE"] = img.toString()
                        chatMap["TIMESTAMP"] = Date().time

                        try {
                            DatabaseAdapter.chatTable.child(senderRoom).push().setValue(chatMap)
                            DatabaseAdapter.chatTable.child(receiverRoom).push().setValue(chatMap)

                            runBlocking {
                                launch(Dispatchers.IO) {
                                    NotificationService.sendNotification(
                                        "Image",
                                        fcmToken,
                                        selfUserName
                                    )
                                }
                            }

                            lifecycleScope.launch {
                                receiveLastMessage()
                                populateRecyclerView()
                                isRequesting(img.toString())
                            }
                        }catch(e:Exception)
                        {
                            Log.d("DB_ERROR",e.toString())
                        }
                    }
                }
            }
            else
            {
                Toast.makeText(
                    applicationContext,
                    "Something went wrong, try again",
                    Toast.LENGTH_LONG
                ).show()
            }
        }catch(e:Exception)
        {
            Log.d("DB_ERROR",e.toString())
        }
    }
    private fun sendWithCamera() {
        requestCameraPermission()

        if (hasCameraPermission()) {
            openCamera()
        } else {
            Toast.makeText(
                applicationContext,
                "Please give permission of camera",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun openCamera() {
        ImagePicker.with(this)
            .cameraOnly()
            .crop() //Crop image(Optional), Check Customization for more option
            .compress(1024) //Final image size will be less than 1 MB(Optional)
            .maxResultSize(
                1080,
                1080
            ) //Final image resolution will be less than 1080 x 1080(Optional)
            .createIntent { intent ->
                imageCapture.launch(intent)
            }
    }

    private fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            applicationContext,
            android.Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        val permission = ContextCompat.checkSelfPermission(
            applicationContext, android.Manifest.permission.CAMERA
        )

        if (permission != PackageManager.PERMISSION_GRANTED) {
            permissionsResultCallback.launch(android.Manifest.permission.CAMERA)
        } else {
            Toast.makeText(applicationContext, "CAMERA granted", Toast.LENGTH_LONG).show()
        }
    }

    private fun sendImage() {

    }

    override fun onDestroy() {
        //Remove cache files
        DatabaseAdapter.deleteTempFiles(cacheDir)
        // Remove the callback when the activity is destroyed
        callback.remove()
        super.onDestroy()
    }

    private fun hasContactPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestContactPermission() {
        val permission = ContextCompat.checkSelfPermission(
            this, android.Manifest.permission.READ_CONTACTS
        )

        if (permission != PackageManager.PERMISSION_GRANTED) {
            permissionsResultCallback.launch(android.Manifest.permission.READ_CONTACTS)
        } else {
            Toast.makeText(this, "Contact granted", Toast.LENGTH_LONG).show()
        }
    }

    private fun receiveData() {
        lifecycleScope.launch {
            try {
                receivingData()
                Log.d("IMG_ERROR","0.2")
                isVisible()
                Log.d("IMG_ERROR","0.3")
                populateRecyclerView()

                Log.d("IMG_ERROR","0.4")
                // The next task will only be executed after receivingData() is completed
            } catch (e: Exception) {
                Log.d("DB_ERROR", e.toString())
            }
        }

    }

    private suspend fun populateRecyclerView() {

        for(data in chatsTemp) {
            Log.d("IMG_ERROR","0")
            if (data.MESSAGE?.contains("https://firebasestorage.googleapis.com")!!) {
                Log.d("IMG_ERROR","INSIDE IF : ${chats.size}")
                val uri = DatabaseAdapter.downloadImageAndConvertToUri(applicationContext, data.MESSAGE!!, (Date().time).toString())
                val decryptedUri = DatabaseAdapter.decryptImage(uri, sharedSecret, applicationContext)
                data.MESSAGE = decryptedUri.toString()
                Log.d("IMG_ERROR", "1")
                chats.add(data)
                Log.d("IMG_ERROR","Chat size : ${chats.size}")
                Log.d("IMG_ERROR", "2")
            } else {
                data.MESSAGE =
                    DatabaseAdapter.decryptMessage(data.MESSAGE!!, sharedSecret)
                Log.d("IMG_ERROR", "3")
                chats.add(data)
                Log.d("IMG_ERROR","Chat size : ${chats.size}")
            }

            chatAdapter.notifyItemInserted(chats.size)
            b.rvChatAct.scrollToPosition(chatAdapter.itemCount - 1)
        }
    }

    private fun isVisible(){

        var isAccepted:Boolean

        try{
            DatabaseAdapter.chatRooms.addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    if(snapshot.exists())
                    {
                        for(s in snapshot.children)
                        {
                            val key = s.key!!

                            if(key == receiverRoom)
                            {
                                isAccepted = s.child("IS_ACCEPTED").getValue(Boolean::class.java)!!

                                if(!isAccepted)
                                {
                                    b.llChatActMsgReq.visibility = View.VISIBLE
                                }
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("DB_ERROR",error.toString())
                }
            })
        }catch(e:Exception)
        {
            Log.d("DB_ERROR",e.toString())
        }
    }

    private suspend fun receivingData() = withContext(Dispatchers.IO) {
        val snapshot = suspendCoroutine { continuation ->
            DatabaseAdapter.chatTable.child(receiverRoom).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    continuation.resume(snapshot)
                }

                override fun onCancelled(error: DatabaseError) {
                    continuation.resumeWithException(Exception(error.toException()))
                }
            })
        }

        chatsTemp.clear()

        if (snapshot.exists()) {
            Log.d("IMG_ERROR","0.0")
            for (s in snapshot.children) {
                val data: ChatModel = s.getValue(ChatModel::class.java)!!
                chatsTemp.add(data)
            }
            Log.d("IMG_ERROR","0.1")
        }
    }

    private fun sendData() {
        sendingMessage()
    }

    private fun sendingMessage() {
        if(b.edtChatActMessage.text.toString().isNotEmpty())
        {
            val msg = b.edtChatActMessage.text.toString()
            val encMsg = DatabaseAdapter.encryptMessage(msg, sharedSecret)
            val chatMap = HashMap<String, Any>()
            chatMap["SENDER_KEY"] = senderKey
            chatMap["SENDER_UID"] = DatabaseAdapter.returnUser()?.uid!!
            chatMap["MESSAGE"] = encMsg
            chatMap["TIMESTAMP"] = Date().time

            try {
                DatabaseAdapter.chatTable.child(senderRoom).push().setValue(chatMap)
                DatabaseAdapter.chatTable.child(receiverRoom).push().setValue(chatMap)

                runBlocking {
                    launch(Dispatchers.IO) {
                        NotificationService.sendNotification(msg, fcmToken, selfUserName)
                    }
                }

                lifecycleScope.launch {
                    receiveLastMessage()
                    populateRecyclerView()
                    isRequesting(encMsg)
                }
            }catch(e:Exception)
            {
                Log.d("DB_ERROR",e.toString())
            }

            b.edtChatActMessage.text.clear()
        }
    }

    private suspend fun receiveLastMessage() = withContext(Dispatchers.IO) {
        val snapshot = suspendCoroutine { continuation ->
            DatabaseAdapter.chatTable.child(receiverRoom).orderByKey().limitToLast(1).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    continuation.resume(snapshot)
                }

                override fun onCancelled(error: DatabaseError) {
                    continuation.resumeWithException(Exception(error.toException()))
                }
            })
        }

        if (snapshot.exists()) {
            chatsTemp.clear()
            for (s in snapshot.children) {
                val data: ChatModel = s.getValue(ChatModel::class.java)!!
                Log.d("FETCHING_DATA",chatsTemp.size.toString())
                Log.d("FETCHING_DATA",data.MESSAGE.toString())
                chatsTemp.add(data)
            }
        }
    }

    private fun isRequesting(msg:String) {

        var request = true
        var isSender = true

        try {
            DatabaseAdapter.chatRooms.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    if (snapshot.exists()) {
                        for (s in snapshot.children) {
                            val key = s.key!!

                            if (key == senderRoom || key == receiverRoom) {
                                request = false

                                val user = s.child("USER_1").getValue(String::class.java)!!

                                isSender = user == senderKey
                            }
                        }
                    }

                        if (request) {
                            DatabaseAdapter.chatRooms.child(senderRoom).child("USER_1")
                                .setValue(senderKey)

                            DatabaseAdapter.chatRooms.child(senderRoom).child("USER_1_UID")
                                .setValue(DatabaseAdapter.returnUser()?.uid!!)

                            DatabaseAdapter.chatRooms.child(senderRoom).child("USER_2")
                                .setValue(receiverKey)

                            DatabaseAdapter.chatRooms.child(senderRoom).child("USER_2_UID")
                                .setValue(uid)

                            DatabaseAdapter.chatRooms.child(senderRoom).child("IS_ACCEPTED")
                                .setValue(false)

                            DatabaseAdapter.chatRooms.child(senderRoom).child("LAST_MESSAGE")
                                .setValue(msg)
                        }
                        else
                        {
                            if(isSender) {
                                DatabaseAdapter.chatRooms.child(senderRoom).child("LAST_MESSAGE")
                                    .setValue(msg)
                            }else
                            {
                                DatabaseAdapter.chatRooms.child(receiverRoom).child("LAST_MESSAGE")
                                    .setValue(msg)
                            }
                        }

                }

                override fun onCancelled(e: DatabaseError) {
                    Log.d("DB_ERROR", e.toString())
                }
            })
        }catch(e:Exception)
        {
            Log.d("DB_ERROR",e.toString())
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_LOCATION -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    Toast.makeText(this, "Granted", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, "Denied", Toast.LENGTH_LONG).show()
                }
                return
            }
            else -> {
                // Ignore all other requests.
            }
        }
    }

}


//    private var simpleCallback: ItemTouchHelper.SimpleCallback = object : ItemTouchHelper.SimpleCallback(
//        ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.START or ItemTouchHelper.END,
//        0
//    ) {
//        override fun onMove(
//            recyclerView: RecyclerView,
//            viewHolder: RecyclerView.ViewHolder,
//            target: RecyclerView.ViewHolder
//        ): Boolean {
//            return false
//        }
//
//        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}
//
//        override fun onChildDraw(
//            c: Canvas,
//            recyclerView: RecyclerView,
//            viewHolder: RecyclerView.ViewHolder,
//            dX: Float,
//            dY: Float,
//            actionState: Int,
//            isCurrentlyActive: Boolean
//        ) {
//            // Get the ConstraintLayout within the item view
//            val constraintLayout = viewHolder.itemView.findViewById<ConstraintLayout>(R.id.cl_sender_layout)
//
//            // Get the RelativeLayout bounds
//            val relativeLayout = viewHolder.itemView.findViewById<RelativeLayout>(R.id.ll_sender_chat)
//            val parentWidth = relativeLayout.width
//            val parentHeight = relativeLayout.height
//
//            // Calculate the bounds of the ConstraintLayout within the RelativeLayout
//            val layoutParams = constraintLayout.layoutParams as RelativeLayout.LayoutParams
//            val childLeft = layoutParams.leftMargin
//            val childTop = layoutParams.topMargin
//            val childRight = parentWidth - layoutParams.rightMargin - constraintLayout.width
//            val childBottom = parentHeight - layoutParams.bottomMargin - constraintLayout.height
//
//            // Restrict horizontal dragging within the bounds of the RelativeLayout
//            val clampedDx = dX.coerceIn(childLeft.toFloat(), childRight.toFloat())
//
//            // Ensure vertical dragging is disabled
//            super.onChildDraw(c, recyclerView, viewHolder, clampedDx, 0f, actionState, isCurrentlyActive)
//        }
//    }