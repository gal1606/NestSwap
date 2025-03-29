package com.example.nestswap

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.example.nestswap.databinding.FragmentCompleteTransactionBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.example.nestswap.Model.Model
import android.util.Log

class CompleteTransactionFragment : DialogFragment() {

    private var _binding: FragmentCompleteTransactionBinding? = null
    private val binding get() = _binding!!

    companion object {
        private const val ARG_ITEM_NAME = "itemName"
        private const val ARG_ITEM_OWNER = "itemOwner"
        private const val ARG_RENTAL_ID = "rentalId"

        fun newInstance(itemName: String, itemOwner: String, rentalId: Int): CompleteTransactionFragment {
            val fragment = CompleteTransactionFragment()
            val args = Bundle().apply {
                putString(ARG_ITEM_NAME, itemName)
                putString(ARG_ITEM_OWNER, itemOwner)
                putInt(ARG_RENTAL_ID, rentalId)
            }
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = FragmentCompleteTransactionBinding.inflate(layoutInflater)

        val itemName = arguments?.getString(ARG_ITEM_NAME) ?: "Unknown Item"
        val itemOwner = arguments?.getString(ARG_ITEM_OWNER) ?: "Unknown Owner"
        val rentalId = arguments?.getInt(ARG_RENTAL_ID) ?: -1

        binding.tvItemName.text = itemName

        binding.btnCompleteTransaction.setOnClickListener {
            Model.instance.getAllItems { items, source ->
                if (isAdded) {
                    val item = items.find { it.name == itemName && it.owner == itemOwner }
                    if (item != null) {
                        val leaveReviewFragment = LeaveReviewFragment.newInstance(
                            itemId = item.id.toString(),
                            itemName = itemName,
                            itemOwner = itemOwner,
                            itemImageUrl = item.imageUrl
                        )
                        leaveReviewFragment.show(parentFragmentManager, "LeaveReviewFragment")
                    } else {
                        Log.e("CompleteTransaction", "Item not found for name: $itemName, owner: $itemOwner")
                    }
                } else {
                    Log.w("CompleteTransaction", "Fragment not attached, skipping operation")
                }
            }

            Model.instance.deleteRental(rentalId) { success ->
                if (success) {
                    Log.d("CompleteTransaction", "Rental deleted successfully")
                } else {
                    Log.e("CompleteTransaction", "Failed to delete rental")
                }
            }
            dismiss()
        }

        return MaterialAlertDialogBuilder(requireContext())
            .setTitle("Complete Transaction")
            .setView(binding.root)
            .setPositiveButton("Close") { dialog, _ -> dialog.dismiss() }
            .create()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}