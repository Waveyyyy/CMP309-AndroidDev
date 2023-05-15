package uk.ac.abertay.songoftheday.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import uk.ac.abertay.songoftheday.R
import uk.ac.abertay.songoftheday.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding

    private lateinit var homeViewModel: HomeViewModel

    private lateinit var auth: FirebaseAuth

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

        checkLoggedIn()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.root.findViewById<View>(R.id.fab_home).setOnClickListener { view ->
            Snackbar.make(view, "CUMMMMMMM", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }
    }


    private fun checkLoggedIn(): Boolean {
        if (auth.currentUser  == null) {
            binding.fabHome.hide()
            return false
        }
        binding.fabHome.show()
        return true
    }


}