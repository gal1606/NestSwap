package com.example.nestswap

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.example.nestswap.Model.dao.Item
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

    @SuppressLint("StringFormatMatches")
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = FragmentItemDetailsBinding.inflate(layoutInflater)

        item = arguments?.getParcelable(ARG_ITEM, Item::class.java)
        item?.let {
            binding.tvItemName.text = it.name
            binding.tvDescription.text = it.description
            binding.tvItemOwner.text = getString(R.string.owner, it.owner)
            binding.tvPrice.text = getString(R.string.price, it.price)

            it.imageUrl?.let { url ->
                Glide.with(binding.imageView.context)
                    .load(url)
                    .into(binding.imageView)
            } ?: binding.imageView.setImageDrawable(null)
        }

        binding.btnRent.setOnClickListener {
            dismiss()

            val priceAsInt = (item?.price ?: 0.0).toInt()
            val paymentFragment = PaymentFragment.newInstance(item?.name, priceAsInt, item?.owner ?: "default_owner_id")
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