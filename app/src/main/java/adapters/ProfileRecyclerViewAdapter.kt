package adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import models.HomeModel
import project.social.whisper.IndividualPostActivity
import project.social.whisper.R

class ProfileRecyclerViewAdapter(private val postList:ArrayList<HomeModel>, private val context: Context) :
    RecyclerView.Adapter<ProfileRecyclerViewAdapter.ProfileViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.profile_recycler_view_layout, parent, false)

        return ProfileViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProfileViewHolder, position: Int) {
        Glide.with(context).load(postList[position].post).into(holder.img)

        val m = postList[position]

        holder.img.setOnClickListener {
            val i = Intent(context, IndividualPostActivity::class.java)
            i.putExtra("position", position)
            i.putExtra("key", m.key)
            context.startActivity(i)
        }
    }

    override fun getItemCount(): Int {
        return postList.size
    }

    class ProfileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val img: ImageView = itemView.findViewById(R.id.iv_post_profile_rv)
    }
}