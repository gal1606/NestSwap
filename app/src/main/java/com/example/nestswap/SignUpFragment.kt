package com.example.nestswap

import NominatimClient
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.nestswap.Model.Model
import com.example.nestswap.Model.Networking.NominatimResponse
import com.example.nestswap.databinding.FragmentSignUpBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.InputStreamReader

data class City(val city: String, val country: String)

class SignUpFragment : Fragment() {

    private var _binding: FragmentSignUpBinding? = null
    private val binding get() = _binding!!

    private lateinit var cityList: List<City>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignUpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadCitiesFromAssets()

        val cityNames = cityList.map { "${it.city}, ${it.country}" }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, cityNames)
        binding.autoCity.setAdapter(adapter)

        binding.btnCompleteSignUp.setOnClickListener {
            val fullName = binding.editFullName.text.toString().trim()
            val cityInput = binding.autoCity.text.toString().trim()
            val email = binding.editEmail.text.toString().trim()
            val password = binding.editPassword.text.toString().trim()
            val passwordRepeat = binding.editPasswordRepeat.text.toString().trim()

            if (fullName.isEmpty()) {
                binding.editFullName.error = "Full name is required"
                return@setOnClickListener
            }
            if (cityInput.isEmpty() || !cityNames.contains(cityInput)) {
                binding.autoCity.error = "Select a valid city from the list"
                return@setOnClickListener
            }
            if (email.isEmpty()) {
                binding.editEmail.error = "Email is required"
                return@setOnClickListener
            }
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.editEmail.error = "Enter a valid email address"
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
            if (password != passwordRepeat) {
                binding.editPasswordRepeat.error = "Passwords do not match"
                return@setOnClickListener
            }


            Model.instance.signUp(email, password) { success, errorMessage ->
                if (success) {
                    val userId = Model.instance.getCurrentUserId()
                    if (userId != null) {
                        // Get lat/lon from Nominatim
                        NominatimClient.api.searchLocation(cityInput).enqueue(object :
                            Callback<List<NominatimResponse>> {
                            override fun onResponse(
                                call: Call<List<NominatimResponse>>,
                                response: Response<List<NominatimResponse>>
                            ) {
                                if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                                    val location = response.body()!![0]
                                    val lat = location.lat
                                    val lon = location.lon

                                    saveUserProfile(userId, fullName, email, cityInput, lat, lon)
                                } else {
                                    Snackbar.make(view, "Could not find location", Snackbar.LENGTH_LONG).show()
                                }
                            }

                            override fun onFailure(call: Call<List<NominatimResponse>>, t: Throwable) {
                                Log.e("SignUp", "Nominatim API failed: ${t.message}")
                                Snackbar.make(view, "Location lookup failed", Snackbar.LENGTH_LONG).show()
                            }
                        })
                    } else {
                        Snackbar.make(view, "Failed to get user ID", Snackbar.LENGTH_LONG).show()
                    }
                } else {
                    Snackbar.make(view, errorMessage ?: "Sign-up failed", Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun saveUserProfile(
        userId: String,
        fullName: String,
        email: String,
        city: String,
        lat: String,
        lon: String
    ) {
        val userProfile = hashMapOf(
            "fullName" to fullName,
            "email" to email,
            "city" to city,
            "latitude" to lat,
            "longitude" to lon
        )

        FirebaseFirestore.getInstance().collection("users").document(userId)
            .set(userProfile)
            .addOnSuccessListener {
                val bundle = Bundle().apply { putString("userId", userId) }
                findNavController().navigate(R.id.action_signup_to_profile, bundle)
            }
            .addOnFailureListener { e ->
                Snackbar.make(requireView(), "Failed to save profile: ${e.message}", Snackbar.LENGTH_LONG).show()
            }
    }

    private fun loadCitiesFromAssets() {
        val inputStream = requireContext().assets.open("cities.json")
        val reader = InputStreamReader(inputStream)
        val type = object : TypeToken<List<City>>() {}.type
        cityList = Gson().fromJson(reader, type)
        reader.close()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
