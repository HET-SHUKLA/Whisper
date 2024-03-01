package fragments

import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.FrameLayout
import androidx.activity.addCallback
import models.ContactModel
import project.social.whisper.R
import project.social.whisper.databinding.FragmentContactBinding

// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ContactFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ContactFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    private var contact:ArrayList<ContactModel> = ArrayList()

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

        val b = FragmentContactBinding.inflate(inflater, container, false)

//        val dataBundle = arguments
//
//        val array = dataBundle?.getStringArrayList("contact")!!
//
//        Log.d("CONTACT",array.size.toString())
//        Log.d("CONTACT",array[0])

        readContact()
        if(isAdded) {

        }

        return b.root
    }

    private fun readContact() {
        if(isAdded) {
            val contentResolver = requireActivity().contentResolver;
            val cursor =
                contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null)
            if (cursor!!.moveToFirst()) {
                if (cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME) >= 0) {
                    do {
                        val name =
                            cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                        val number =
                            cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))

                        if (name != null && number != null) {
                            contact.add(ContactModel(name, number))
                        }

                    } while (cursor.moveToNext())
                }
            }
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ContactFragment.
         */

        const val CONTACTS = "contacts"
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ContactFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}