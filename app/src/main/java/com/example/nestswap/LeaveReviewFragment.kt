package com.example.nestswap

import android.app.Dialog
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.nestswap.Model.Model
import com.example.nestswap.databinding.FragmentLeaveReviewBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.UUID

class LeaveReviewFragment : DialogFragment() {

    private var _binding: FragmentLeaveReviewBinding? = null
    private val binding get() = _binding!!

    companion object {
        fun newInstance(itemId: String, itemName: String, itemOwner: String, itemImageUrl: String?): LeaveReviewFragment {
            val fragment = LeaveReviewFragment()
            val args = Bundle().apply {
                putString("itemId", itemId)
                putString("itemName", itemName)
                putString("itemOwner", itemOwner)
                putString("itemImageUrl", itemImageUrl)
            }
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = FragmentLeaveReviewBinding.inflate(layoutInflater)

        val itemId = arguments?.getString("itemId") ?: ""
        val itemName = arguments?.getString("itemName") ?: "Unknown Item"
        val itemOwner = arguments?.getString("itemOwner") ?: "Unknown Owner"
        val itemImageUrl = arguments?.getString("itemImageUrl")

        binding.btnSubmitReview.setOnClickListener {
            val rating = binding.ratingBar.rating.toInt()
            val comment = binding.etReviewComment.text.toString()
            if (rating > 0 && comment.isNotEmpty()) {
                val review = Review(
                    id = UUID.randomUUID().hashCode(),
                    itemId = itemId,
                    itemImageUrl = itemImageUrl, // Use Cloudinary URL
                    itemName = itemName,
                    reviewer = Model.instance.getCurrentUserId() ?: "Anonymous",
                    body = comment,
                    rating = rating
                )
                Model.instance.addReview(review) { success ->
                    if (success) {
                        Toast.makeText(context, "Review submitted successfully", Toast.LENGTH_SHORT).show()
                        dismiss()
                    } else {
                        Toast.makeText(context, "Failed to submit review", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(context, "Please provide a rating and comment", Toast.LENGTH_SHORT).show()
            }
        }

        return MaterialAlertDialogBuilder(requireContext())
            .setTitle("Leave a Review")
            .setView(binding.root)
            .setPositiveButton("Close") { dialog, _ -> dialog.dismiss() }
            .create()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}