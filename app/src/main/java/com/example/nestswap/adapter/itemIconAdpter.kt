package com.example.nestswap.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.nestswap.R
import com.example.nestswap.databinding.ItemIconLayoutBinding

data class ItemIcon(
    val imageUrl: String?,
    val name: String,
    val details: String,
    val itemOwner: String,
    val isPlaceholder: Boolean = false
)

class ItemIconAdapter(
    private val items: MutableList<ItemIcon>,
    private val onItemClick: (ItemIcon) -> Unit,
    private val onDeleteClick: (ItemIcon) -> Unit = {},
    private var isEditMode: Boolean = false
) : RecyclerView.Adapter<ItemIconAdapter.ItemIconViewHolder>() {

    companion object {
        private const val VIEW_TYPE_ITEM = 0
        private const val VIEW_TYPE_PLACEHOLDER = 1
    }

    inner class ItemIconViewHolder(val binding: ItemIconLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ItemIcon) {
            if (item.isPlaceholder) {
                binding.ivItemIcon.visibility = View.GONE // Hide image for placeholders
                binding.tvItemName.text = item.name
                binding.tvItemDetails.text = item.details
                binding.btnDeleteItem.visibility = View.GONE
                binding.root.isClickable = false
            } else {
                binding.ivItemIcon.visibility = View.VISIBLE // Show image for regular items
                item.imageUrl?.let { url ->
                    Glide.with(binding.ivItemIcon.context)
                        .load(url)
                        .into(binding.ivItemIcon)
                } ?: binding.ivItemIcon.setImageDrawable(null) // Clear image if no URL
                binding.tvItemName.text = item.name
                binding.tvItemDetails.text = item.details
                binding.btnDeleteItem.visibility = if (isEditMode) View.VISIBLE else View.GONE
                binding.btnDeleteItem.setOnClickListener {
                    onDeleteClick(item)
                    if (items.contains(item)) {
                        val position = items.indexOf(item)
                        items.removeAt(position)
                        notifyItemRemoved(position)
                    }
                }
                binding.root.setOnClickListener {
                    if (!isEditMode) {
                        onItemClick(item)
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemIconViewHolder {
        val binding = ItemIconLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemIconViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemIconViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    override fun getItemViewType(position: Int): Int {
        return if (items[position].isPlaceholder) VIEW_TYPE_PLACEHOLDER else VIEW_TYPE_ITEM
    }

    fun setEditMode(editMode: Boolean) {
        isEditMode = editMode
        notifyDataSetChanged()
    }
}