package uk.ac.abertay.songoftheday

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote
import uk.ac.abertay.songoftheday.databinding.ActivityMainBinding


class SpotifyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val clientId: String = "fa6df34db120464fa68c8035b24a8697"
    private val redirectUri: String = "http://uk.ac.abertay.songoftheday/callback"
    private var spotifyAppRemote: SpotifyAppRemote? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onStart() {
        super.onStart()
        val connectionParams = ConnectionParams.Builder(clientId)
            .setRedirectUri(redirectUri)
            .showAuthView(true)
            .build()

        SpotifyAppRemote.connect(this, connectionParams, object: Connector.ConnectionListener {
            override fun onConnected(appRemote: SpotifyAppRemote?) {
                spotifyAppRemote = appRemote
                Log.d("SpotifyActivity", "Connection Successful")
                connected()
            }

            override fun onFailure(error: Throwable?) {
                Log.e("SpotifyActivity", "Connection Failed")
            }
        })
    }

    private fun connected() {
        Log.d("SpotifyActivity", "connected function")
    }

    // disconnect from spotify when exiting
    override fun onStop() {
        super.onStop()
        spotifyAppRemote?.let {
            SpotifyAppRemote.disconnect(it)
        }
    }

}