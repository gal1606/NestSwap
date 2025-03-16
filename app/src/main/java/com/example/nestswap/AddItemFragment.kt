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
import com.example.nestswap.adapter.ItemIcon
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
                "pick" -> pickImageLauncher.launch("image/*")
            }
            lastAction = null
        } else {
            Snackbar.make(
                requireView(),
                "Permission required to proceed",
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
                Log.d(TAG, "Image picked and set successfully")
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e(TAG, "Failed to load image: ${e.message}")
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
            lastAction = "pick"
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                pickImageLauncher.launch("image/*")
            } else {
                requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
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

            val newId = (System.currentTimeMillis() / 1000).toInt()
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
                    binding.ivItemImage.isDrawingCacheEnabled = true
                    binding.ivItemImage.buildDrawingCache()
                    val drawable = binding.ivItemImage.drawable
                    val bitmap = if (drawable is BitmapDrawable) {
                        drawable.bitmap
                    } else {
                        Log.e(TAG, "Drawable is not a BitmapDrawable")
                        null
                    }
                    if (bitmap != null) {
                        Model.instance.addItem(newItem, bitmap) {
                            requireActivity().runOnUiThread {
                                binding.progressBar.visibility = View.GONE
                                Log.d(TAG, "After save (with image) - Items size: ${Model.instance.items.size}, Rentals size: ${Model.instance.rentals.size}")
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
                Model.instance.addItem(newItem, null) {
                    requireActivity().runOnUiThread {
                        binding.progressBar.visibility = View.GONE
                        Log.d(TAG, "After save (no image) - Items size: ${Model.instance.items.size}, Rentals size: ${Model.instance.rentals.size}")
                        findNavController().previousBackStackEntry?.savedStateHandle?.set("refresh", "true")
                        Snackbar.make(
                            requireView(),
                            "Item added successfully",
                            Snackbar.LENGTH_SHORT
                        ).show()
                        findNavController().navigateUp()
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
        return "logged_in_user_id"
    }
}