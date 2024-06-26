package fragments

import adapters.ChatRecyclerViewAdapter
import adapters.DatabaseAdapter
import adapters.GlobalStaticAdapter
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import models.ChatRecyclerModel
import models.ChatUserModel
import project.social.whisper.R
import project.social.whisper.databinding.FragmentChatRequestBinding

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class ChatRequestFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    private var senderKey = GlobalStaticAdapter.key

    private lateinit var users:ArrayList<ChatRecyclerModel>
    private lateinit var usersKey:ArrayList<ChatUserModel>

    private lateinit var b:FragmentChatRequestBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        b = FragmentChatRequestBinding.inflate(inflater, container, false)

        //Initializing array
        users = ArrayList()
        usersKey = ArrayList()

        b.chatRequestFragRecyclerView.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL ,false)

        fetchingData()

        return b.root
    }

    private fun fetchingData() {
        var isSender: Boolean
        var count = 0

        try {
            DatabaseAdapter.chatRooms.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d("IDK","onDataChange")
                    if (snapshot.exists()) {
                        for(s in snapshot.children)
                        {
                            val key = s.key!!

                            if (key.contains(senderKey)) {

                                val user1 = s.child("USER_1").getValue(String::class.java)!!
                                val user1Uid = s.child("USER_1_UID").getValue(String::class.java)!!

                                val isAccepted = s.child("IS_ACCEPTED").getValue(Boolean::class.java)!!
                                val lastMessage = s.child("LAST_MESSAGE").getValue(String::class.java)?:"HI"

                                isSender = user1 == senderKey

                                if(!isSender)
                                {
                                    if(!isAccepted) {
                                        usersKey.add(ChatUserModel(user1Uid, user1, lastMessage))
                                    }
                                }
                            }
                            count++
                        }

                        fetchingDetails()

                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("DB_ERROR",error.toString())
                }
            })
        }catch (e:Exception)
        {
            Log.d("DB_ERROR",e.toString())
        }
    }

    private fun fetchingDetails()
    {
        Log.d("IDK","OK")
        try {
            for(k in usersKey) {
                DatabaseAdapter.userDetailsTable.child(k.uid).child(k.key)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {

                            if (snapshot.exists()) {
                                val userName =
                                    snapshot.child("USER_NAME").getValue(String::class.java)!!

                                val imgUrl = snapshot.child("IMAGE").getValue(String::class.java)
                                    ?:getString(R.string.image_not_found)

                                val fcm = snapshot.child("FCM_TOKEN").getValue(String::class.java)?:""

                                val type = snapshot.child("ACCOUNT_TYPE")
                                    .getValue(String::class.java) ?: "PUBLIC"

                                val about = snapshot.child("ABOUT")
                                    .getValue(String::class.java) ?: ""

                                users.add(ChatRecyclerModel(userName,about, imgUrl, k.lastMessage, k.key, k.uid, fcm, type))

                                Log.d("IDK","ADDED")
                                if(users.size == usersKey.size)
                                {
                                    if(isAdded) {
                                        val adapter =
                                            ChatRecyclerViewAdapter(requireContext(), users)
                                        b.chatRequestFragRecyclerView.adapter = adapter
                                    }
                                }

                            } else {
                                if(isAdded) {
                                    Toast.makeText(
                                        requireContext(),
                                        "Something went wrong",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Log.d("DB_ERROR", error.toString())
                        }
                    })
                Log.d("IDK","First round")
            }
        }catch(e:Exception)
        {
            Log.d("DB_ERROR",e.toString())
        }
        Log.d("IDK","user size"+users.size.toString())
    }

    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ChatRequestFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}