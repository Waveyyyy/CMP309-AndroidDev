package uk.ac.abertay.songoftheday.ui.home

import android.app.AlertDialog
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.adamratzman.spotify.*
import com.adamratzman.spotify.auth.pkce.startSpotifyClientPkceLoginActivity
import com.adamratzman.spotify.models.*
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.type.TimeOfDay
import kotlinx.coroutines.*
import uk.ac.abertay.songoftheday.R
import uk.ac.abertay.songoftheday.data.FirebaseData
import uk.ac.abertay.songoftheday.data.Model
import uk.ac.abertay.songoftheday.data.TrackData
import uk.ac.abertay.songoftheday.databinding.FragmentHomeBinding
import uk.ac.abertay.songoftheday.spotify.SpotifyPkceLoginActivityImpl
import java.time.LocalDateTime


class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding


    private lateinit var auth: FirebaseAuth
    private var db: FirebaseFirestore = Firebase.firestore
    private var playlistList: ArrayList<TextView>? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        binding = FragmentHomeBinding.inflate(inflater, container, false)


        auth = Firebase.auth
        checkLoggedIn()
        if (auth.currentUser != null) {
            var playlist = FirebaseData(mutableListOf())
            val playlistList = ArrayList<TextView>()

            val linearLayout: LinearLayout =
                binding.root.findViewById<LinearLayout>(R.id.linearLayout_home)
            val playlists = db.collection("playlists")
            playlists.document(auth.uid.toString()).get().addOnSuccessListener { document ->
                playlist = document.toObject(FirebaseData::class.java)!!
                linearLayout.removeAllViews()
                playlistList.clear()
                playlist.tracks.forEach { item ->
                    val tv = TextView(requireContext())
                    val layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    layoutParams.setMargins(10, 10, 10, 10)
                    tv.layoutParams = layoutParams
                    tv.text = HtmlCompat.fromHtml(
                        "<b>" + item.track + " : " +
                                item.dateAdded + "</b>" + "<br />" + "<a href=\"" + item.href + "\">Spotify Link" + "</a>" + "<b>" +
                                "<br />Added By: </b>" + item.addedBy,
                        HtmlCompat.FROM_HTML_MODE_COMPACT
                    )
                    tv.setTextColor(-0x1000000)
                    tv.setBackgroundColor(-0x19191a)
                    tv.gravity = Gravity.CENTER
                    tv.setPadding(0, 40, 0, 20)
                    linearLayout.addView(tv, 0)
                    playlistList.add(tv)
                }
            }
        }

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
        val playlistName = getString(R.string.playlistName)
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
            Snackbar.make(view, "Playlist Created", Snackbar.LENGTH_LONG).setAction("Action", null)
                .show()
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
                    }
                }
            }
            builder.show()
        }
    }

    private suspend fun addTrackToPlaylist(
        track: String, api: SpotifyClientApi, trackUri: PlayableUri, view: View
    ) {
        val playlistName = getString(R.string.playlistName)
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


            // firebayse
            val firebaseTrack = api.tracks.getTrack(trackUri.uri)
            var playlists = FirebaseData(mutableListOf())
            val firebaseRef = db.collection("playlists").document(auth.uid.toString())
            firebaseRef.update(
                "tracks", FieldValue.arrayUnion(
                    TrackData
                        (
                        track = firebaseTrack!!.name,
                        dateAdded = LocalDateTime.now().toString(),
                        addedBy = auth.currentUser!!.email!!.split('@')[0],
                        href = firebaseTrack.href
                    )
                )
            )
            return
        }
    }

    private fun checkLoggedIn(): Boolean {
        if (auth.currentUser == null) {
            binding.root.getViewById(R.id.fab_home).visibility = View.GONE
            return false
        }
        binding.root.getViewById(R.id.fab_home).visibility = View.VISIBLE
        return true
    }

    override fun onDestroy() {
        super.onDestroy()

    }

}