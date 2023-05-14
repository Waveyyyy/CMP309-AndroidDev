package uk.ac.abertay.songoftheday.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import uk.ac.abertay.songoftheday.R
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import uk.ac.abertay.songoftheday.databinding.FragmentAuthBinding
import uk.ac.abertay.songoftheday.databinding.FragmentCalendarBinding

class AuthFragment : Fragment() {

    private var _binding: FragmentAuthBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    {
        val authViewModel = ViewModelProvider(this).get(AuthViewModel::class.java)

        _binding = FragmentAuthBinding.inflate(inflater, container, false)
        val root: View = binding.root



        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}