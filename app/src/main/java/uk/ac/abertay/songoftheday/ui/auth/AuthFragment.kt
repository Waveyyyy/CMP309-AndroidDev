package uk.ac.abertay.songoftheday.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import uk.ac.abertay.songoftheday.R
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import uk.ac.abertay.songoftheday.databinding.FragmentAuthBinding

class AuthFragment : Fragment() {

    private lateinit var binding: FragmentAuthBinding


    private lateinit var auth: FirebaseAuth
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_auth, container, false)

        auth = Firebase.auth

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // reset the values held in textboxes
        binding.resetButton.setOnClickListener {
            clearInput()
        }
        // submit login data, username and password
        binding.submitButton.setOnClickListener {
            if (auth.currentUser == null) {
                submitLoginDetails(view)
            }
        }
        // change to signup page
        binding.signupButton.setOnClickListener {
            startActivity(Intent(getActivity(), SignupActivity::class.java))
        }
    }

    // helper function for logging a user in
    private fun submitLoginDetails(view: View) {
            val email = binding.emailField.text.toString().trim()
            val password = binding.passwordField.text.toString().trim()
            logUserIn(email, password, view)
    }

    // Handle user login functionality with firebase
    private fun logUserIn(email: String, password: String, view: View) {
        // check that email and password are not emtpy
        if ((email != "") && (password != "")) {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(requireActivity()) { task ->
                    if (task.isSuccessful) {
                        Snackbar.make(
                            binding.root,
                            "Authentication Successful",
                            Snackbar.LENGTH_LONG
                        ).setAction("Action", null).show()
                        Log.i("LoginSuccessful", "user logged in successfully")
                        // send user to profile page after logging in
                        view.findNavController().navigate(R.id.action_nav_auth_to_nav_profile)
                    } else {
                        // if the login failed inform the user
                        Snackbar.make(binding.root, "Authentication Failed", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show()
                        Log.w("LoginFailure", "User failed to login, possible firebase issue")
                    }
                }
        } else {
            // if email or password are incorrect tell the user so
            Snackbar.make(binding.root, "Error logging in", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
            Log.w("LoginError", "empty string supplied")

        }
    }

    // Clear input boxes
    private fun clearInput() {
        binding.emailField.setText("")
        binding.passwordField.setText("")
        Log.i("InputCleared", "Input boxes cleared")
    }

    override fun onDestroy() {
        clearInput()
        super.onDestroy()
        Log.i("FragmentDestroyed", "fragment was destroyed")
    }
}