package uk.ac.abertay.songoftheday.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import uk.ac.abertay.songoftheday.R
import uk.ac.abertay.songoftheday.SpotifyActivity
import uk.ac.abertay.songoftheday.databinding.FragmentHomeBinding


class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding

    private lateinit var homeViewModel: HomeViewModel

    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        homeViewModel =
            ViewModelProvider(this)[HomeViewModel::class.java]

        binding = FragmentHomeBinding.inflate(inflater, container, false)

        /*TODO: check if user is logged in, if not dont show fab*/

        auth = Firebase.auth
        storage = Firebase.storage

        checkLoggedIn()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkLoggedIn()
        binding.root.findViewById<View>(R.id.fab_home).setOnClickListener { view ->
            startActivity(Intent(requireActivity(), SpotifyActivity::class.java))
            Snackbar.make(view, "CUMMMMMMM", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }
    }


/*
    private fun dynamicallyAddItems() {
        for (s in dataSnapshot.getChildren()) {
            // for each value in review db, create textview and add to list
            val reviews: Reviews = s.getValue(Reviews::class.java)!!
            val tv = TextView(this@HomeFragment.context)
            val layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            )
            layoutParams.setMargins(10, 10, 10, 10)
            tv.layoutParams = layoutParams
            tv.text = HtmlCompat.fromHtml(
                "<b>" + reviews.pubName + " : " +
                        reviews.rating + "</b>" + "<br />" + reviews.description + "<b>" +
                        "<br />Review By: " + "</b>" + reviews.user,
                HtmlCompat.FROM_HTML_MODE_COMPACT
            )
            tv.setTextColor(-0x1000000)
            tv.setBackgroundColor(-0x19191a) // hex color 0xAARRGGBB
            tv.gravity = Gravity.CENTER
            tv.setPadding(0, 20, 0, 20) // in pixels (left, top, right, bottom
            linearLayout.addView(tv, 0)
            reviewsList.add(tv)
        }
    }
*/

    private fun checkLoggedIn(): Boolean {
        if (auth.currentUser  == null) {
            binding.root.getViewById(R.id.fab_home).visibility = View.GONE
            return false
        }
        binding.root.getViewById(R.id.fab_home).visibility = View.VISIBLE
        return true
    }


}