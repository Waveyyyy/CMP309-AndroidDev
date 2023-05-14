package uk.ac.abertay.songoftheday.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import uk.ac.abertay.songoftheday.R
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
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
                submitLoginDetails()
            }
        }


    }

    private fun submitLoginDetails() {
            val email = binding.emailField.text.toString().trim()
            val password = binding.passwordField.text.toString().trim()
            Snackbar.make(binding.root, "Username:$email + Password:$password", Snackbar.LENGTH_LONG).setAction("Action", null).show()
            if (authViewModel.logUserIn(email, password, auth))
            {
                Snackbar.make(binding.root, "Authentication Successful", Snackbar.LENGTH_LONG).setAction("Action", null).show()
            } else {
                Snackbar.make(binding.root, "Authentication Failed", Snackbar.LENGTH_LONG).setAction("Action", null).show()
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