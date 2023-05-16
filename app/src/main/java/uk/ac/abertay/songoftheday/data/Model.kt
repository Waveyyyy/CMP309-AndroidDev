package uk.ac.abertay.songoftheday.data

import com.adamratzman.spotify.auth.SpotifyDefaultCredentialStore
import uk.ac.abertay.songoftheday.BuildConfig
import uk.ac.abertay.songoftheday.MainActivity
import uk.ac.abertay.songoftheday.SpotifyApplication

object Model {
    val credentialStore by lazy {
        SpotifyDefaultCredentialStore(
            clientId = BuildConfig.SPOTIFY_CLIENT_ID,
            redirectUri = BuildConfig.SPOTIFY_REDIRECT_URI_PKCE,
            applicationContext = SpotifyApplication.context
        )
    }
}