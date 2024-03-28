package project.social.whisper

import adapters.DatabaseAdapter
import adapters.GlobalStaticAdapter
import adapters.HomeRecyclerViewAdapter
import android.app.Dialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import models.HomeModel
import project.social.whisper.databinding.ActivityUserProfileBinding

class UserProfileActivity : BaseActivity() {

    private lateinit var b:ActivityUserProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        b = ActivityUserProfileBinding.inflate(layoutInflater)
        setContentView(b.root)

        val posts = ArrayList<HomeModel>()

        b.txtProfileActUserName.text = GlobalStaticAdapter.userName2
        Glide.with(applicationContext).load(GlobalStaticAdapter.imageUrl2).into(b.imgProfileActUserImage)
        b.txtProfileActAbout.text = GlobalStaticAdapter.about2

        b.btnProfileActMessage.setOnClickListener {
            val i = Intent(this, ChatActivity::class.java)
            startActivity(i)
        }

        b.imgBtnProfileActSetting.setOnClickListener {
            val d = Dialog(this)
            d.setContentView(R.layout.user_profile_setting_alert)
            d.setCancelable(false)

            val btnNo = d.findViewById<Button>(R.id.btn_no_block_acc)
            val btnYes = d.findViewById<Button>(R.id.btn_yes_block_acc)
            val tv = d.findViewById<TextView>(R.id.tv_block_alert)

            DatabaseAdapter.blockTable.child(GlobalStaticAdapter.key)
                .addListenerForSingleValueEvent(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists())
                    {
                        for(s in snapshot.children)
                        {
                            val k = s.key!!

                            if(k == GlobalStaticAdapter.key2)
                            {
                                tv.text = "Do you wanted to unblock this user?"
                                return
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })

            btnNo.setOnClickListener {
                d.dismiss()
            }

            btnYes.setOnClickListener {

                if(tv.text.toString().contains("Do you wanted to unblock this user?"))
                {
                    DatabaseAdapter.blockTable.child(GlobalStaticAdapter.key)
                        .child(GlobalStaticAdapter.key2)
                        .removeValue()
                        .addOnCompleteListener {
                            Toast.makeText(this, "User unblocked", Toast.LENGTH_LONG)
                                .show()
                        }.addOnFailureListener {
                            Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG)
                                .show()
                        }
                }
                else {

                    DatabaseAdapter.blockTable.child(GlobalStaticAdapter.key)
                        .child(GlobalStaticAdapter.key2)
                        .child("IS_BLOCKED")
                        .setValue(true)
                        .addOnCompleteListener {
                            Toast.makeText(this, "User blocked", Toast.LENGTH_LONG)
                                .show()
                        }.addOnFailureListener {
                            Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG)
                                .show()
                        }
                }
                d.dismiss()
            }

            d.show()
        }

        b.imgBtnProfileAlias.setOnClickListener {

            val alias = Dialog(this)
            alias.setCancelable(true)
            alias.setContentView(R.layout.alias_alert_dialog)

            var isSender = false

            val senderRoom = GlobalStaticAdapter.key+GlobalStaticAdapter.key2
            val receiverRoom = GlobalStaticAdapter.key2+GlobalStaticAdapter.key

            val btnDone = alias.findViewById<Button>(R.id.btn_done_alias_alert)
            val btnRemove = alias.findViewById<Button>(R.id.btn_rm_alias_alert)
            val edt = alias.findViewById<EditText>(R.id.edt_alias_alert)

            DatabaseAdapter.chatRooms.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists()) {
                        if (snapshot.hasChild(senderRoom)) {

                            GlobalStaticAdapter.alias2 =
                                snapshot.child(senderRoom).child("ALIAS_2")
                                    .getValue(String::class.java)
                                    ?: GlobalStaticAdapter.userName2

                            isSender = true

                        }
                        if (snapshot.hasChild(receiverRoom)) {

                            GlobalStaticAdapter.alias2 =
                                snapshot.child(receiverRoom).child("ALIAS_1")
                                    .getValue(String::class.java)
                                    ?: GlobalStaticAdapter.userName2

                        }

                        edt.setText(GlobalStaticAdapter.alias2)
                        alias.show()
                    }else
                        Toast.makeText(applicationContext, "First you need to follow",
                            Toast.LENGTH_LONG).show()
                }

                override fun onCancelled(error: DatabaseError) {
                }

            })

            btnDone.setOnClickListener {
                if(edt.text.trim().toString().isNotEmpty())
                {
                    if(isSender){
                        DatabaseAdapter.chatRooms.child(senderRoom)
                            .child("ALIAS_2")
                            .setValue(edt.text.toString())
                    }
                    else
                    {
                        DatabaseAdapter.chatRooms.child(receiverRoom)
                            .child("ALIAS_1")
                            .setValue(edt.text.toString())
                    }
                }
                else
                    Toast.makeText(this, "Enter alias first", Toast.LENGTH_LONG).show()

                alias.cancel()
            }

            btnRemove.setOnClickListener {
                if(isSender)
                {
                    DatabaseAdapter.chatRooms.child(senderRoom)
                        .child("ALIAS_2")
                        .removeValue()
                }
                else
                {
                    DatabaseAdapter.chatRooms.child(receiverRoom)
                        .child("ALIAS_1")
                        .removeValue()
                }
                alias.cancel()
            }
        }

        b.rvProfileActRecentPosts.layoutManager = GridLayoutManager(this, 2)
        val adapter = HomeRecyclerViewAdapter(posts, applicationContext)
        b.rvProfileActRecentPosts.adapter = adapter

//        DatabaseAdapter.postTable.child(GlobalStaticAdapter.uid)
//            .child(GlobalStaticAdapter.key)
//            .addListenerForSingleValueEvent(object: ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                Log.d("DB_ERROR", GlobalStaticAdapter.uid)
//                if(snapshot.exists())
//                {
//                    Log.d("DB_ERROR", GlobalStaticAdapter.key)
//                     for(sn in snapshot.children) {
//                         val title = sn.child("USERNAME").getValue(String::class.java)!!
//
//                         val image = sn.child("IMAGE").getValue(String::class.java)
//                             ?: getString(R.string.image_not_found)
//
//                         val score = sn.child("SCORE").getValue(Int::class.java) ?: 0
//
//                         val userImage = sn.child("USER_IMAGE").getValue(String::class.java)
//                             ?: getString(R.string.image_not_found)
//
//                         posts.add(HomeModel(title, userImage, "Caption", image, score))
//                         adapter.notifyItemInserted(posts.size)
//
//                     }
//                }
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//
//            }
//
//        })

    }

    override fun getSelectedTheme(): String {
        val sharedPreferences = getSharedPreferences("app_theme", MODE_PRIVATE)
        return sharedPreferences.getString("theme", "primary1")?: "primary1"
    }

    override fun getWhiteOrBlackTheme(): String {
        val sharedPreferences = getSharedPreferences("app_theme_wb", MODE_PRIVATE)
        return sharedPreferences.getString("theme_wb", "system")?: "system"
    }
}