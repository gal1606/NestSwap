package com.example.nestswap

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.nestswap.Model.Model
import com.example.nestswap.Model.dao.Item
import com.example.nestswap.databinding.FragmentAddItemBinding
import com.google.android.material.snackbar.Snackbar

class AddItemFragment : Fragment() {

    private var _binding: FragmentAddItemBinding? = null
    private val binding get() = _binding!!
    private var didSetItemImage = false
    private var lastAction: String? = null

    private val TAG = "AddItemFragment"

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            when (lastAction) {
                "take" -> takePictureLauncher.launch()
            }
            lastAction = null
        } else {
            Snackbar.make(
                requireView(),
                "Camera permission required to take pictures",
                Snackbar.LENGTH_LONG
            ).show()
        }
    }

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            try {
                val inputStream = requireContext().contentResolver.openInputStream(it)
                val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
                inputStream?.close()
                binding.ivItemImage.setImageBitmap(bitmap)
                didSetItemImage = true
            } catch (e: Exception) {
                e.printStackTrace()
                Snackbar.make(
                    requireView(),
                    "Failed to load image",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }
    }

    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        bitmap?.let {
            binding.ivItemImage.setImageBitmap(it)
            didSetItemImage = true
            Log.d(TAG, "Picture taken and set successfully")
        } ?: Log.w(TAG, "Camera returned null bitmap")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddItemBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnPickImage.setOnClickListener {
            pickImageLauncher.launch("image/*") // No permission check needed
        }

        binding.btnTakePicture.setOnClickListener {
            lastAction = "take"
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                takePictureLauncher.launch()
            } else {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }

        binding.btnSaveItem.setOnClickListener { view ->
            val name = binding.etItemName.text.toString()
            val price = binding.etItemPrice.text.toString().toDoubleOrNull()
            val description = binding.etItemDescription.text.toString()

            if (name.isBlank() || price == null) {
                Snackbar.make(
                    requireView(),
                    "Please enter a valid name and price",
                    Snackbar.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val newId = System.currentTimeMillis().toInt() // Use milliseconds for more uniqueness
            val category = "General"
            val condition = "Used"
            val owner = getCurrentUserId()

            val newItem = Item(
                id = newId,
                name = name,
                description = description,
                category = category,
                condition = condition,
                owner = owner,
                price = price
            )

            binding.progressBar.visibility = View.VISIBLE

            if (didSetItemImage) {
                try {
                    val drawable = binding.ivItemImage.drawable
                    val bitmap = if (drawable is BitmapDrawable) {
                        drawable.bitmap
                    } else {
                        null
                    }
                    if (bitmap != null) {
                        Model.instance.addItem(newItem, bitmap) {
                            requireActivity().runOnUiThread {
                                binding.progressBar.visibility = View.GONE
                                findNavController().previousBackStackEntry?.savedStateHandle?.set("refresh", "true")
                                Snackbar.make(
                                    requireView(),
                                    "Item added successfully",
                                    Snackbar.LENGTH_SHORT
                                ).show()
                                findNavController().navigateUp()
                            }
                        }
                    } else {
                        requireActivity().runOnUiThread {
                            binding.progressBar.visibility = View.GONE
                            Snackbar.make(
                                requireView(),
                                "Failed to process image",
                                Snackbar.LENGTH_SHORT
                            ).show()
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error saving item with image: ${e.message}", e)
                    requireActivity().runOnUiThread {
                        binding.progressBar.visibility = View.GONE
                        Snackbar.make(
                            requireView(),
                            "Error saving item: ${e.message}",
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }
                }
            } else {
                Model.instance.addItem(newItem, null) { success: Boolean ->
                    requireActivity().runOnUiThread {
                        binding.progressBar.visibility = View.GONE
                        if (success) {
                            findNavController().previousBackStackEntry?.savedStateHandle?.set("refresh", "true")
                            Snackbar.make(
                                requireView(),
                                "Item added successfully",
                                Snackbar.LENGTH_SHORT
                            ).show()
                            findNavController().navigateUp()
                        } else {
                            Snackbar.make(
                                requireView(),
                                "Item added to the server, but failed to save on the device. It will sync later.",
                                Snackbar.LENGTH_LONG
                            ).show()
                            findNavController().navigateUp()
                        }
                    }
                }
            }
        }

        binding.btnCancel.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun getCurrentUserId(): String {
        return Model.instance.getCurrentUserId() ?: throw IllegalStateException("User not signed in")
    }
}