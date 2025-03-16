package com.example.nestswap

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.example.nestswap.databinding.FragmentPaymentBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class PaymentFragment : DialogFragment() {

    private var _binding: FragmentPaymentBinding? = null
    private val binding get() = _binding!!

    companion object {
        private const val ARG_ITEM_NAME = "itemName"
        private const val ARG_PRICE = "price"

        fun newInstance(itemName: String?, price: Int): PaymentFragment {
            val fragment = PaymentFragment()
            val args = Bundle().apply {
                putString(ARG_ITEM_NAME, itemName)
                putInt(ARG_PRICE, price)
            }
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = FragmentPaymentBinding.inflate(layoutInflater)

        val itemName = arguments?.getString(ARG_ITEM_NAME)
        val price = arguments?.getInt(ARG_PRICE, 0)

        binding.tvPaymentHeader.text = "Please fill in your card details"
        binding.tvCardNumber.text = "Card Number:"
        binding.tvExpirationDate.text = "Expiration date:"
        binding.tvCvv.text = "Cvv:"
        binding.tvId.text = "Id:"
        binding.tvTotalPrice.text = "Total: $$price"

        binding.btnCompleteSignUp.setOnClickListener {
            val cardNumber = binding.editTextCardNumber.text.toString()
            val expirationDate = binding.editTextDate.text.toString()
            val cvv = binding.editTextCvv.text.toString()
            val id = binding.editTextId.text.toString()

            if (cardNumber.isNotEmpty() && expirationDate.isNotEmpty() && cvv.isNotEmpty() && id.isNotEmpty()) {
                dialog?.dismiss()
            } else {
                // Show error (e.g., using a Toast)
                // For now, just log or do nothing
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