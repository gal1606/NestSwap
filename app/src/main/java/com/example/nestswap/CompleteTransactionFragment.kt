package com.example.nestswap

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.example.nestswap.databinding.FragmentCompleteTransactionBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class CompleteTransactionFragment : DialogFragment() {

    private var _binding: FragmentCompleteTransactionBinding? = null
    private val binding get() = _binding!!

    companion object {
        private const val ARG_ITEM_NAME = "itemName"
        private const val ARG_ITEM_OWNER = "itemOwner"
        fun newInstance(itemName: String, itemOwner: String): CompleteTransactionFragment {
            val fragment = CompleteTransactionFragment()
            val args = Bundle().apply {
                putString(ARG_ITEM_NAME, itemName)
                putString(ARG_ITEM_OWNER, itemOwner)
            }
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = FragmentCompleteTransactionBinding.inflate(layoutInflater)

        val itemName = arguments?.getString(ARG_ITEM_NAME) ?: "Unknown Item"
        val itemOwner = arguments?.getString(ARG_ITEM_OWNER) ?: "Unknown Owner"

        binding.tvItemName.text = itemName
        binding.tvItemOwner.text = "Owner: $itemOwner"

        binding.btnCompleteTransaction.setOnClickListener {
            val leaveReviewFragment = LeaveReviewFragment.newInstance(itemName, itemOwner)
            leaveReviewFragment.show(parentFragmentManager, "LeaveReviewFragment")
            dismiss()
        }

        return MaterialAlertDialogBuilder(requireContext())
            .setTitle("Complete Transaction")
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