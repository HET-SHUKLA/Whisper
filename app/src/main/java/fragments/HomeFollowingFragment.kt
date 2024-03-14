package fragments

import adapters.HomeRecyclerViewAdapter
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import models.HomeModel
import project.social.whisper.R
import project.social.whisper.databinding.FragmentHomeFollowingBinding

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class HomeFollowingFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

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
        val b = FragmentHomeFollowingBinding.inflate(inflater,container,false)
        val posts = ArrayList<HomeModel>()

        b.homeFollowingFragRecyclerView.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL ,false)

        //TODO("Will need to change using fetching data from firebase")
        posts.add(HomeModel("Username",R.mipmap.ic_launcher,"caption",R.mipmap.ic_launcher,1))
        posts.add(HomeModel("Username",R.mipmap.ic_launcher,"caption",R.mipmap.ic_launcher,1))
        posts.add(HomeModel("Username",R.mipmap.ic_launcher,"caption",R.mipmap.ic_launcher,1))
        posts.add(HomeModel("Username",R.mipmap.ic_launcher,"caption",R.mipmap.ic_launcher,1))
        posts.add(HomeModel("Username",R.mipmap.ic_launcher,"caption",R.mipmap.ic_launcher,1))
        posts.add(HomeModel("Username",R.mipmap.ic_launcher,"caption",R.mipmap.ic_launcher,1))
        posts.add(HomeModel("Username",R.mipmap.ic_launcher,"caption",R.mipmap.ic_launcher,1))
        posts.add(HomeModel("Username",R.mipmap.ic_launcher,"caption",R.mipmap.ic_launcher,1))
        posts.add(HomeModel("Username",R.mipmap.ic_launcher,"caption",R.mipmap.ic_launcher,1))
        posts.add(HomeModel("Username",R.mipmap.ic_launcher,"caption",R.mipmap.ic_launcher,1))
        posts.add(HomeModel("Username",R.mipmap.ic_launcher,"caption",R.mipmap.ic_launcher,1))
        posts.add(HomeModel("Username",R.mipmap.ic_launcher,"caption",R.mipmap.ic_launcher,1))
        posts.add(HomeModel("Username",R.mipmap.ic_launcher,"caption",R.mipmap.ic_launcher,1))
        posts.add(HomeModel("Username",R.mipmap.ic_launcher,"caption",R.mipmap.ic_launcher,1))

        val adapter = HomeRecyclerViewAdapter(posts)
        b.homeFollowingFragRecyclerView.adapter = adapter

        return b.root
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment HomeFollowingFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HomeFollowingFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}