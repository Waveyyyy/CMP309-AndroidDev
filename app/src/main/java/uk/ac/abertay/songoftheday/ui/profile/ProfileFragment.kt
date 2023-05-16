package uk.ac.abertay.songoftheday.ui.profile

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentContainerView
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import uk.ac.abertay.songoftheday.MainActivity
import uk.ac.abertay.songoftheday.R
import uk.ac.abertay.songoftheday.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding

    private lateinit var profileViewModel: ProfileViewModel

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    {
        profileViewModel = ViewModelProvider(this)[ProfileViewModel::class.java]

        binding = FragmentProfileBinding.inflate(inflater, container, false)

        auth = Firebase.auth
        if (auth.currentUser != null) {
            changeDisplayValues()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.logoutFab.setOnClickListener {
            if (auth.currentUser != null) {
                logUserOut(view)
            }
        }
    }

    private fun changeDisplayValues() {
        val displayName = auth.currentUser?.displayName.toString()
        if (displayName == ""){
            binding.nameValue.text = auth.currentUser?.email.toString().substringBefore("@")
        }else {
            binding.nameValue.text = displayName
        }
        binding.emailValue.text = auth.currentUser?.email.toString()
    }

    private fun logUserOut(view: View) {
        if (profileViewModel.logOut(auth))
        {
            view.findNavController().navigate(R.id.action_nav_profile_to_nav_auth)
        } else {
            Snackbar.make(binding.root, "Logout Failed", Snackbar.LENGTH_LONG).setAction("Action", null).show()
        }
    }

}