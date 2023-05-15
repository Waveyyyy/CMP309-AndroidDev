package uk.ac.abertay.songoftheday.ui.profile

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth

class ProfileViewModel : ViewModel() {

    fun logOut(auth: FirebaseAuth): Boolean {
        auth.signOut()
        if (auth.currentUser == null){
            return true
        }
        return false
    }
}