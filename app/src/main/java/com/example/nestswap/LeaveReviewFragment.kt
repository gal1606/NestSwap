package com.example.nestswap

import android.app.Dialog
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.nestswap.databinding.FragmentLeaveReviewBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class LeaveReviewFragment : DialogFragment() {

    private var _binding: FragmentLeaveReviewBinding? = null
    private val binding get() = _binding!!

    companion object {
        private const val ARG_ITEM_NAME = "itemName"
        private const val ARG_ITEM_OWNER = "itemOwner"
        fun newInstance(itemName: String, itemOwner: String): LeaveReviewFragment {
            val fragment = LeaveReviewFragment()
            val args = Bundle().apply {
                putString(ARG_ITEM_NAME, itemName)
                putString(ARG_ITEM_OWNER, itemOwner)
            }
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = FragmentLeaveReviewBinding.inflate(layoutInflater)

        val itemName = arguments?.getString(ARG_ITEM_NAME) ?: "Unknown Item"
        val itemOwner = arguments?.getString(ARG_ITEM_OWNER) ?: "Unknown Owner"

        binding.tvLeaveReviewTitle.text = "Leave a Review for $itemName"

        binding.btnSubmitReview.setOnClickListener {
            val rating = binding.ratingBar.rating
            val comment = binding.etReviewComment.text.toString()
            if (rating > 0 && comment.isNotEmpty()) {
                Toast.makeText(context, "Review submitted for $itemName", Toast.LENGTH_SHORT).show()
                dismiss()
            } else {
                Toast.makeText(context, "Please provide a rating and comment", Toast.LENGTH_SHORT).show()
            }
        }

        return MaterialAlertDialogBuilder(requireContext())
            .setTitle("Leave a Review")
            .setView(binding.root)
            .setPositiveButton("Close") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}