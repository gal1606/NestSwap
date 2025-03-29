package com.example.nestswap

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.nestswap.Model.Model
import com.example.nestswap.databinding.FragmentLoginBinding
import com.google.android.material.snackbar.Snackbar

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val navController = findNavController()
        if (Model.instance.isUserSignedIn() && navController.currentDestination?.id == R.id.loginFragment) {
            navController.navigate(R.id.action_login_to_profile)
            return
        }

        binding.btnSignUp.setOnClickListener {
            navController.navigate(R.id.action_login_to_signup)
        }

        binding.btnSignIn.setOnClickListener {
            val email = binding.editTextTextEmailAddress.text.toString().trim()
            val password = binding.editPassword.text.toString().trim()

            if (email.isEmpty()) {
                binding.editTextTextEmailAddress.error = "Email is required"
                return@setOnClickListener
            }
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.editTextTextEmailAddress.error = "Enter a valid email address"
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                binding.editPassword.error = "Password is required"
                return@setOnClickListener
            }
            if (password.length < 6) {
                binding.editPassword.error = "Password must be at least 6 characters"
                return@setOnClickListener
            }

            Model.instance.signIn(email, password) { success, errorMessage ->
                if (success) {
                    navController.navigate(R.id.action_login_to_profile)
                } else {
                    Snackbar.make(
                        requireView(),
                        errorMessage ?: "Sign-in failed. Please try again.",
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}