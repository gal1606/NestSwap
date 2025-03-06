package com.example.nestswap

import android.app.Dialog
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.fragment.app.DialogFragment
import com.example.nestswap.databinding.FragmentItemDetailsBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ItemDetailsFragment : DialogFragment() {

    private var _binding: FragmentItemDetailsBinding? = null
    private val binding get() = _binding!!
    private var item: Item? = null

    companion object {
        private const val ARG_ITEM = "item"
        fun newInstance(item: Item): ItemDetailsFragment {
            val fragment = ItemDetailsFragment()
            val args = Bundle().apply {
                putParcelable(ARG_ITEM, item)
            }
            fragment.arguments = args
            return fragment
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = FragmentItemDetailsBinding.inflate(layoutInflater)

        item = arguments?.getParcelable(ARG_ITEM, Item::class.java)
        item?.let {
            binding.tvItemName.text = it.name
            binding.tvDescription.text = it.description
            binding.tvItemOwner.text = "Owner: ${it.owner}"
            binding.tvPrice.text = "Price: $${it.price}"
        }

        binding.btnRent.setOnClickListener {
            dismiss()

            val paymentFragment = PaymentFragment.newInstance(item?.name, item?.price ?: 0)

            paymentFragment.show(parentFragmentManager, "PaymentDialog")
        }

        return MaterialAlertDialogBuilder(requireContext())
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