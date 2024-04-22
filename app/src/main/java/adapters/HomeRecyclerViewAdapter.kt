package adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import models.HomeModel
import project.social.whisper.R

class HomeRecyclerViewAdapter (private val postList:ArrayList<HomeModel>, private val context:Context) :
    RecyclerView.Adapter<HomeRecyclerViewAdapter.HomeViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.home_recycler_view, parent, false)

        return HomeViewHolder(view)
    }

    override fun getItemCount(): Int {
        return postList.size
    }

    override fun onBindViewHolder(holder: HomeViewHolder, position: Int) {

        var isUpClickable = true
        var isDownClickable = true

        val dbPath = DatabaseAdapter.scoreTable.child(postList[position].key)
            .child(postList[position].timeStamp).child(GlobalStaticAdapter.key)

        dbPath.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists())
                {
                    val isUpVoted = snapshot.child("UPVOTED").getValue(Boolean::class.java)!!

                    if(isUpVoted)
                    {
                        isUpClickable = false
                        isDownClickable = true
                    }
                    else
                    {
                        isUpClickable = true
                        isDownClickable = false
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })

        Glide.with(context).load(postList[position].img).into(holder.img)
        holder.title.text = postList[position].title
        holder.subTitle.text = postList[position].subTitle
        holder.score.text = postList[position].score.toString()
        Glide.with(context).load(postList[position].post).into(holder.post)

        holder.upVote.setOnClickListener {
            //When user clicks upvote
            if(isUpClickable) {
                val score = holder.score.text.toString().toInt() + 1
                holder.score.text = (score).toString()
                isUpClickable = false
                isDownClickable = true

                DatabaseAdapter.postTable.child(postList[position].key)
                    .child(postList[position].timeStamp).child("SCORE")
                    .setValue(score)

                dbPath.child("UPVOTED").setValue(true)
            }
        }

        holder.downVote.setOnClickListener {
            //When user clicks down vote
            if(isDownClickable) {
                val score = holder.score.text.toString().toInt() - 1
                holder.score.text = (score).toString()
                isDownClickable = false
                isUpClickable = true

                DatabaseAdapter.postTable.child(postList[position].key)
                    .child(postList[position].timeStamp).child("SCORE")
                    .setValue(score)

                dbPath.child("UPVOTED").setValue(false)
            }
        }
    }

    class HomeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val img: ImageView = itemView.findViewById(R.id.home_frag_img)
        val title: TextView = itemView.findViewById(R.id.home_frag_title)
        val subTitle: TextView = itemView.findViewById(R.id.home_frag_sub_title)
        val post: ImageView = itemView.findViewById(R.id.home_frag_post)
        val upVote: ImageButton = itemView.findViewById(R.id.home_frag_post_up)
        val downVote: ImageButton = itemView.findViewById(R.id.home_frag_post_down)
        val score: TextView = itemView.findViewById(R.id.home_frag_post_score)
        //val comment: ImageButton = itemView.findViewById(R.id.home_frag_post_comment)
    }
}