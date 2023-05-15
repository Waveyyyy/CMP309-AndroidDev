package uk.ac.abertay.songoftheday.ui.auth

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import uk.ac.abertay.songoftheday.R
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import uk.ac.abertay.songoftheday.databinding.FragmentAuthBinding

class AuthFragment : Fragment() {

    private lateinit var binding: FragmentAuthBinding

    private lateinit var authViewModel: AuthViewModel

    private lateinit var auth: FirebaseAuth
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    {
        authViewModel = ViewModelProvider(this)[AuthViewModel::class.java]

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_auth, container, false)

        auth = Firebase.auth

/*
        TODO: Decide how to do signups
*/
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.resetButton.setOnClickListener {
            clearInput()
        }
        binding.submitButton.setOnClickListener {
            if (auth.currentUser == null) {
                submitLoginDetails(view)
            }
        }
    }

    private fun submitLoginDetails(view: View) {
            val email = binding.emailField.text.toString().trim()
            val password = binding.passwordField.text.toString().trim()
            logUserIn(email, password, view)
    }

    private fun logUserIn(email: String, password: String, view: View) {
        if ((email != "") && (password != "")) {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(requireActivity()) { task ->
                    if (task.isSuccessful) {
                        Snackbar.make(
                            binding.root,
                            "Authentication Successful",
                            Snackbar.LENGTH_LONG
                        ).setAction("Action", null).show()
                        view.findNavController().navigate(R.id.action_nav_auth_to_nav_profile)
                    } else {
                        Snackbar.make(binding.root, "Authentication Failed", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show()
                    }
                }
        }
    }

    private fun clearInput() {
        binding.emailField.setText("")
        binding.passwordField.setText("")
        /*TODO: add logging*/
    }

    override fun onDestroy() {
        clearInput()
        super.onDestroy()
        /*TODO: add logging*/
    }
}