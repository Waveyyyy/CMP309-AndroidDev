package uk.ac.abertay.songoftheday.spotify

import android.app.Activity
import android.content.Intent
import android.util.Log
import com.adamratzman.spotify.SpotifyClientApi
import com.adamratzman.spotify.SpotifyScope
import com.adamratzman.spotify.auth.pkce.AbstractSpotifyPkceLoginActivity
import uk.ac.abertay.songoftheday.BuildConfig
import uk.ac.abertay.songoftheday.MainActivity
import uk.ac.abertay.songoftheday.SpotifyApplication
import uk.ac.abertay.songoftheday.ui.home.HomeFragment

internal var pkceClassBackTo: Class<out Activity>? = null

class SpotifyPkceLoginActivityImpl : AbstractSpotifyPkceLoginActivity() {
    override val clientId: String = BuildConfig.SPOTIFY_CLIENT_ID
    override val redirectUri = BuildConfig.SPOTIFY_REDIRECT_URI_PKCE
    override val scopes = SpotifyScope.values().toList()


    override fun onSuccess(api: SpotifyClientApi) {
        val model = (application as SpotifyApplication).model
        model.credentialStore.setSpotifyApi(api)
        val classBackTo = pkceClassBackTo ?: MainActivity::class.java
        pkceClassBackTo = null
        Log.i("SpotifyPkceLoginActivityImpl", "Auth successful")
        startActivity(Intent(this, classBackTo))
    }

    override fun onFailure(exception: Exception) {
        exception.printStackTrace()
        pkceClassBackTo = null
        Log.e("SpotifyPkceLoginActivityImpl", "auth failed: ${exception.message}")
    }

}