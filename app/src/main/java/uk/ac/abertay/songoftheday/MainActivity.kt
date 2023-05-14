package uk.ac.abertay.songoftheday

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import androidx.navigation.NavController
import androidx.navigation.Navigation
import uk.ac.abertay.songoftheday.databinding.ActivityMainBinding
import uk.ac.abertay.songoftheday.ui.auth.AuthFragment
import uk.ac.abertay.songoftheday.ui.profile.ProfileFragment

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_playlist, R.id.nav_calendar
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_profile)
        {
            val navctrl :NavController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main)
            navctrl.navigate(R.id.nav_profile)
        }
        else if (item.itemId == R.id.action_login)
        {
            val navctrl :NavController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main)
            navctrl.navigate(R.id.nav_auth)
        }
        return super.onOptionsItemSelected(item)
    }

    fun checkLoggedIn(menu:Menu) {
        val login = null
        if (login == null)
        {
            menu.findItem(R.id.action_login).setVisible(true)
            menu.findItem(R.id.action_profile).setVisible(false)
        }
        menu.findItem(R.id.action_login).setVisible(false)
        menu.findItem(R.id.action_profile).setVisible(true)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}