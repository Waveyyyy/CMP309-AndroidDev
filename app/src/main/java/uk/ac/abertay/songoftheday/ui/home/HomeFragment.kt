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
import com.adamratzman.spotify.models.*
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


        auth = Firebase.auth
        storage = Firebase.storage

        checkLoggedIn()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkLoggedIn()
        binding.root.findViewById<View>(R.id.fab_home).setOnClickListener { view ->
            if (Model.credentialStore.spotifyToken == null) {
                Log.i("HomeFragment", "Re-Authentication is needed")
                Snackbar.make(view, "Re-Authenticatin Needed", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
                activity?.startSpotifyClientPkceLoginActivity(SpotifyPkceLoginActivityImpl::class.java)
                Snackbar.make(view, "Authentication Successful", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
            } else {
                lifecycleScope.launch {
                    val sotdPlaylist = playlistExistsAsync(view).await()
                    searchTrackDialog(view)
                }
            }
        }
    }

    private fun playlistExistsAsync(view: View): Deferred<Playlist?> = lifecycleScope.async {
        val playlistName = "Song of the Day app"
        var sotdPlaylist: Playlist? = null
        val api = Model.credentialStore.getSpotifyClientPkceApi()

        if (api!!.token.expiresAt < System.currentTimeMillis()) {
            api.refreshToken()
        }

        var sotdPlaylistId: String? = null
        api.playlists.getClientPlaylists().items.forEach {
            if (it.name == playlistName) {
                sotdPlaylistId = it.id
                return@forEach
            }
        }

        Log.d("HomeFragment", "playlistId: $sotdPlaylistId")
        if (sotdPlaylistId == null) {
            createSOTDPlaylist(api, playlistName)
            sotdPlaylist = api.playlists.getPlaylist(playlistName)
            Snackbar.make(view, "Playlist Created", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        } else {
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
        val dialogLayout = inflater.inflate(R.layout.dialog_search_song, null)
        val editText = dialogLayout.findViewById<EditText>(R.id.search_song)
        builder.setView(dialogLayout)
        builder.setPositiveButton("Search") { _, _ ->
            lifecycleScope.launch { searchResults(editText, view) }
        }
        builder.show()
    }

    private suspend fun searchResults(searchString: EditText, view: View) {
        if (searchString.text.toString().isEmpty()) {
            searchTrackDialog(view)
        } else {
            val api = Model.credentialStore.getSpotifyClientPkceApi()
            var trackUris = emptyArray<PlayableUri>()

            if (api!!.token.expiresAt < System.currentTimeMillis()) {
                api.refreshToken()
            }
            val tracks = withContext(Dispatchers.IO) {
                val searchRes = api.search.searchTrack(searchString.text.toString().trim())
                val tracks = searchRes.items.map { it.name }.toTypedArray()
                trackUris = searchRes.items.map { it.uri }.toTypedArray()
                Log.d("HomeFragment", "Search Query: ${searchString.text.toString()}")
                Log.d("HomeFragment", "Search Result: ${searchRes.items}")
                Log.d("HomeFragment", "Tracks: ${tracks.joinToString(", ")}")
                tracks
            }
            val builder = AlertDialog.Builder(requireContext())
            with(builder) {
                setTitle("Search Result")
                setItems(tracks) { _, item ->
                    lifecycleScope.launch {
                        addTrackToPlaylist(tracks[item], api, trackUris[item], view)
/*
                        Snackbar.make(view, "${tracks[item]}", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show()
*/
                    }
                }
            }
            builder.show()
        }
    }

    private suspend fun addTrackToPlaylist(
        track: String,
        api: SpotifyClientApi,
        trackUri: PlayableUri,
        view: View
    ) {
        val playlistName = "Song of the Day app"
        var sotdPlaylistId: String? = null

        withContext(Dispatchers.IO) {
            api.playlists.getClientPlaylists().items.forEach {
                if (it.name == playlistName && it.id != "") {
                    sotdPlaylistId = it.id
                }
                return@forEach
            }
        }
        Log.d("HomeFragment", "playlistId: $sotdPlaylistId")

        if (sotdPlaylistId != null) {
            api.playlists.addPlayableToClientPlaylist(sotdPlaylistId!!, trackUri)
            Log.i("HomeFragment", "Track: $track was added to the playlist")
            Snackbar.make(view, "$track was added to the playlist", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
            return
        }
        Snackbar.make(view, "$track is already in the playlist", Snackbar.LENGTH_LONG)
            .setAction("Action", null).show()
        Log.i("HomeFragment", "Track: $track was a duplicate, not added to the playlist")
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
        if (auth.currentUser == null) {
            binding.root.getViewById(R.id.fab_home).visibility = View.GONE
            return false
        }
        binding.root.getViewById(R.id.fab_home).visibility = View.VISIBLE
        return true
    }


}