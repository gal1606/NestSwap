package com.example.nestswap

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

    private fun setUpView() {
        binding?.btnCompleteSignUp?.setOnClickListener(::onSignUpClicked)
    }

    private fun onSignUpClicked(view: View){
        // To Do
    }

}