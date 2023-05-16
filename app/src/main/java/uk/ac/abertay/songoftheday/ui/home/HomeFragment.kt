package uk.ac.abertay.songoftheday.ui.home

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.adamratzman.spotify.*
import com.adamratzman.spotify.auth.pkce.startSpotifyClientPkceLoginActivity
import com.adamratzman.spotify.models.Playlist
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.*
import uk.ac.abertay.songoftheday.R
import uk.ac.abertay.songoftheday.data.Model
import uk.ac.abertay.songoftheday.databinding.FragmentHomeBinding
import uk.ac.abertay.songoftheday.spotify.SpotifyPkceLoginActivityImpl


class HomeFragment : Fragment(){

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


        auth = Firebase.auth
        storage = Firebase.storage

        checkLoggedIn()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkLoggedIn()
        binding.root.findViewById<View>(R.id.fab_home).setOnClickListener { view ->
                if (Model.credentialStore.spotifyToken == null)
                {
                    Log.i("HomeFragment", "Re-Authentication is needed")
                    Snackbar.make(view, "Re-Authenticatin Needed", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show()
                    activity?.startSpotifyClientPkceLoginActivity(SpotifyPkceLoginActivityImpl::class.java)
                    Snackbar.make(view, "Authentication Successful", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show()
                }else {
                    lifecycleScope.launch {
                        val sotdPlaylist = playlistExistsAsync().await()
                    }
                    searchTrackDialog(view)
                }
            }
        }

    private fun playlistExistsAsync(): Deferred<Playlist?> = lifecycleScope.async {
        val playlistName = "Song of the Day app"
        var sotdPlaylist: Playlist?
        val api = Model.credentialStore.getSpotifyClientPkceApi()

        if (api!!.token.expiresAt < System.currentTimeMillis()){
            api.refreshToken()
        }

        sotdPlaylist = api.playlists.getPlaylist(playlistName)
        if (sotdPlaylist?.id == "") {
                createSOTDPlaylist(api, playlistName)
                sotdPlaylist = api.playlists.getPlaylist(playlistName)
        }else {
            Log.i("HomeFragment", "Playlist $playlistName exists")
        }
        return@async sotdPlaylist
    }

    private suspend fun createSOTDPlaylist(api: SpotifyClientApi, playlistName: String) {
        // collab playlists can only be private
        api.playlists.createClientPlaylist(playlistName, null, false, true, api.getUserId())
        Log.i("HomeFragment", "Playlist $playlistName created")
    }

    private fun searchTrackDialog(view: View) {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = layoutInflater
        builder.setTitle("Search for a song")
        val dialogLayout = inflater.inflate(R.layout.dialog_search_song ,null)
        val editText = dialogLayout.findViewById<EditText>(R.id.search_song)
        builder.setView(dialogLayout)
        builder.setPositiveButton("Search") {
                _, _ -> lifecycleScope.launch {searchResults(editText, view)}
        }
        builder.show()
    }

    private suspend fun searchResults(searchString: EditText, view: View)
    {
        val api = Model.credentialStore.getSpotifyClientPkceApi()

        if (api!!.token.expiresAt < System.currentTimeMillis()){
            api.refreshToken()
        }
        val tracks = withContext(Dispatchers.IO) {
            val searchRes = api.search.searchTrack(searchString.text.toString().trim())
            val tracks = searchRes.items.map { it.name }.toTypedArray()
            Log.d("HomeFragment", "Search Query: ${searchString.text.toString()}")
            Log.d("HomeFragment", "Search Result: ${searchRes.items}")
            Log.d("HomeFragment", "Tracks: ${tracks}")
            tracks
        }
        val builder = AlertDialog.Builder(requireContext())
        with (builder){
            setTitle("Search Result")
            setItems(tracks) {
                _, item ->
                addTrackToPlaylist(tracks[item])
                Snackbar.make(view, "${tracks[item]}", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
            }
        }
        builder.show()


    }

    private fun addTrackToPlaylist(track: String)
    {
        null
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