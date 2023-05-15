package uk.ac.abertay.songoftheday.ui.auth

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.ktx.userProfileChangeRequest
import uk.ac.abertay.songoftheday.R
import uk.ac.abertay.songoftheday.databinding.ActivitySignupBinding

class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private lateinit var auth: FirebaseAuth

    // executes when the view is created
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // get instance of firebaseauth class
        auth = FirebaseAuth.getInstance()

        // Empties all of the values entered in each textbox when reset is pressed
        binding.resetButton.setOnClickListener {
            clearInput()
        }
        // Submits the data held in the textboxes to create a new user account
        binding.submitButton.setOnClickListener {
            createNewAccount()
        }
    }

    // handles creation of new accounts via fireabase
    private fun createNewAccount() {
        // retrieve values stored in each textbox
        var usersName = binding.nameField.text.toString().trim()
        var email = binding.emailField.text.toString().trim()
        var password = binding.passwordField.text.toString().trim()
        var confirmPasswd = binding.passwordconfirmField.text.toString().trim()

        if ((usersName != "") && (email != "") && ((password != "" && confirmPasswd != "") && (password == confirmPasswd))) {
            // create a new user with email and password
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Log.i("UserCreated", "uid: ${auth.currentUser?.uid}")
                        // feedback to the user that the account was successfully created
                        Snackbar.make(
                            binding.root,
                            "Account Creation Successful",
                            Snackbar.LENGTH_LONG
                        ).setAction("Action", null).show()
                        // add the users name to their profile
                        auth.currentUser!!.updateProfile(updateUsersName(usersName, auth))
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Log.i("UsernameUpdated", "value: $usersName")
                                }
                            }
                        // log out the user after they create their account
                        auth.signOut()
                        // redirect back to login page
                        findNavController(R.id.nav_host_fragment_content_main).navigate(R.id.nav_auth)
                    } else {
                        // if the account failed to create, inform the user of this
                        Snackbar.make(binding.root, "Account Creation Failed", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show()
                        Log.e("AccountCreationFailure", "Issue with firebase")
                    }
                }
        } else {
            // if any of the values are blank or the passwords do not match, then prompt the user that
            // an error occurred when creating the account
            Snackbar.make(binding.root, "Error Creating Account", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
            Log.w("AccountCreationError", "Empty string or password mismatch")
        }
    }

    // helper function to update the users profile with their name
    private fun updateUsersName(name: String, auth: FirebaseAuth): UserProfileChangeRequest {
        return  userProfileChangeRequest {
            displayName = name
        }
    }

    // clear the text box inputs
    private fun clearInput() {
        binding.nameField.setText("")
        binding.emailField.setText("")
        binding.passwordField.setText("")
        binding.passwordconfirmField.setText("")
        Log.i("InputCleared", "Signup Information")
    }
}