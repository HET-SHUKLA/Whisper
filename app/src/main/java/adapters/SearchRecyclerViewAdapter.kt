package adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import models.SearchModel
import project.social.whisper.R
import project.social.whisper.SearchActivity

class SearchRecyclerViewAdapter(private val searchResults:ArrayList<SearchModel>) :
    RecyclerView.Adapter<SearchRecyclerViewAdapter.SearchViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.search_recycler_view, parent, false)

        return SearchViewHolder(view)
    }

    override fun getItemCount(): Int {
        return searchResults.size
    }

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {

        holder.userImg.setImageResource(searchResults[position].userImg)
        holder.userName.text = searchResults[position].userName

        holder.container.setOnClickListener {
            Log.d("RESULT",holder.userName.text.toString())
        }

    }

    class SearchViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userImg: ImageView = itemView.findViewById(R.id.img_search_act)
        val userName:TextView = itemView.findViewById(R.id.tv_search_act)
        val container:CardView = itemView.findViewById(R.id.cv_search_act)
    }
}