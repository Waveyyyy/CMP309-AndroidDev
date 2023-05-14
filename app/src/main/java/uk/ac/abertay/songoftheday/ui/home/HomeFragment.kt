package uk.ac.abertay.songoftheday.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import uk.ac.abertay.songoftheday.databinding.FragmentHomeBinding
import uk.ac.abertay.songoftheday.R

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val fab = root.findViewById<FloatingActionButton>(R.id.fab)

        /*TODO: check if user is logged in, if not dont show fab*/



        fab.setOnClickListener { view ->
            Snackbar.make(view, "CUMMMMMMM", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
     fun checkLoggedIn(fab: FloatingActionButton): Boolean {
        val login = null
        if (login == null) {
            fab.hide()
            return false
        }
        fab.show()
        return true
    }

}