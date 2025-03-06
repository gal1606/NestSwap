package com.example.nestswap

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.nestswap.databinding.FragmentSignUpBinding

class SignUpFragment : Fragment() {

    private var binding : FragmentSignUpBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSignUpBinding.inflate(layoutInflater, container, false)
        setUpView()
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
            onSignUpClicked(view)
    }

    private fun setUpView() {
        binding?.btnCompleteSignUp?.setOnClickListener(::onSignUpClicked)
    }

    private fun onSignUpClicked(view: View){
        binding?.btnCompleteSignUp?.setOnClickListener {
            val intent = Intent(requireContext(), ItemsListActivity::class.java)
            startActivity(intent)
        }    }

}