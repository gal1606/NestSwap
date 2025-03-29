package com.example.nestswap

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import com.example.nestswap.Model.Model
import com.example.nestswap.Model.dao.Rental
import com.example.nestswap.databinding.FragmentPaymentBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import java.util.UUID

class PaymentFragment : DialogFragment() {

    private var _binding: FragmentPaymentBinding? = null
    private val binding get() = _binding!!

    companion object {
        private const val ARG_ITEM_NAME = "itemName"
        private const val ARG_PRICE = "price"
        private const val ARG_OWNER = "owner"

        fun newInstance(itemName: String?, price: Int, owner: String): PaymentFragment {
            val fragment = PaymentFragment()
            val args = Bundle().apply {
                putString(ARG_ITEM_NAME, itemName)
                putInt(ARG_PRICE, price)
                putString(ARG_OWNER, owner)
            }
            fragment.arguments = args
            return fragment
        }
    }

    @SuppressLint("StringFormatMatches")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = FragmentPaymentBinding.inflate(layoutInflater)

        val itemName = arguments?.getString(ARG_ITEM_NAME)
        val price = arguments?.getInt(ARG_PRICE, 0)
        val owner = arguments?.getString(ARG_OWNER)

        binding.tvPaymentHeader.text = getString(R.string.fill_card)
        binding.tvCardNumber.text = getString(R.string.card_number)
        binding.tvExpirationDate.text = getString(R.string.expiration_date)
        binding.tvCvv.text = getString(R.string.cvv)
        binding.tvId.text = getString(R.string.id)
        binding.tvTotalPrice.text = getString(R.string.total, price)

        binding.btnCompleteSignUp.setOnClickListener {
            val cardNumber = binding.editTextCardNumber.text.toString()
            val expirationDate = binding.editTextDate.text.toString()
            val cvv = binding.editTextCvv.text.toString()
            val id = binding.editTextId.text.toString()

            if (cardNumber.isNotEmpty() && expirationDate.isNotEmpty() && cvv.isNotEmpty() && id.isNotEmpty()) {
                val currentUserId = Model.instance.getCurrentUserId()
                if (currentUserId == owner) {
                    Snackbar.make(binding.root, "You cannot rent your own item.", Snackbar.LENGTH_LONG).show()
                    return@setOnClickListener
                }
                val rental = Rental(
                    id = UUID.randomUUID().hashCode(),
                    itemName = itemName ?: "Unknown",
                    owner = owner ?: "default_owner_id",
                    renter = currentUserId ?: "default_renter_id"
                )
                Model.instance.addRental(rental) { success ->
                    if (success) {
                        findNavController().previousBackStackEntry?.savedStateHandle?.set("refresh", "true")
                        Snackbar.make(binding.root, "Rental completed successfully!", Snackbar.LENGTH_SHORT).show()
                        dialog?.dismiss()
                    } else {
                        Snackbar.make(binding.root, "Failed to complete rental. Please try again.", Snackbar.LENGTH_LONG).show()
                    }
                }
            } else {
                Snackbar.make(binding.root, "Please fill in all payment details.", Snackbar.LENGTH_LONG).show()
            }
        }

        return MaterialAlertDialogBuilder(requireContext())
            .setView(binding.root)
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}