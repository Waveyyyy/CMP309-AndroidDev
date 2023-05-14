package uk.ac.abertay.songoftheday.ui.auth

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth

class AuthViewModel() : ViewModel() {

    fun logUserIn(email: String, password: String, auth: FirebaseAuth): Boolean
    {
        var success: Boolean = false
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener{ task ->
            success = task.isSuccessful
            }
        return success
    }

}
