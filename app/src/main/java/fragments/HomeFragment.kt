package fragments

import adapters.HomeViewPagerAdapter
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import project.social.whisper.R
import project.social.whisper.databinding.FragmentHomeBinding

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [HomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeFragment : Fragment() {
    // TODO: Rename and change types of parameters
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

//        View binding
        val b = FragmentHomeBinding.inflate(inflater, container, false)

        val tab = b.homeFragTabLayout
        val vp = b.homeFragViewPager

        val fa = HomeViewPagerAdapter(this)
        vp.adapter = fa

        val labels = arrayOf("Explore new", "Following")

        tab.setTabTextColors(
            ContextCompat.getColor(requireContext(), R.color.unselected_tab), // Text color for unselected tabs
            ContextCompat.getColor(requireContext(), R.color.selected_tab)   // Text color for selected tab
        )

        tab.setSelectedTabIndicatorColor(
            ContextCompat.getColor(requireContext(), R.color.selected_tab)
        )

//        Assigning title to the tabs
        TabLayoutMediator(tab, vp
        ) { tab1, position ->
            tab1.text = labels[position]
        }.attach()

        return b.root

    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId)
        {
            R.id.home_tool_search -> {
                Toast.makeText(activity, "Search", Toast.LENGTH_SHORT).show()
            }
            else -> {
                Toast.makeText(activity, "Create New", Toast.LENGTH_SHORT).show()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment HomeFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HomeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}