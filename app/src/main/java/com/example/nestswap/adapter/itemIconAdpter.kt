package com.example.nestswap.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.nestswap.databinding.ItemIconLayoutBinding

data class ItemIcon(
    val iconRes: Int,
    val name: String,
    val details: String,
    val itemOwner: String
)

class ItemIconAdapter(
    private val items: List<ItemIcon>,
    private val onItemClick: (ItemIcon) -> Unit,
    private val onDeleteClick: (ItemIcon) -> Unit = {},
    private var isEditMode: Boolean = false
) : RecyclerView.Adapter<ItemIconAdapter.ItemIconViewHolder>() {

    private val mutableItems = items.toMutableList()

    inner class ItemIconViewHolder(val binding: ItemIconLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ItemIcon) {
            binding.ivItemIcon.setImageResource(item.iconRes)
            binding.tvItemName.text = item.name
            binding.tvItemDetails.text = item.details

            binding.btnDeleteItem.visibility = if (isEditMode) View.VISIBLE else View.GONE

            binding.btnDeleteItem.setOnClickListener {
                onDeleteClick(item)
                if (mutableItems.contains(item)) {
                    val position = mutableItems.indexOf(item)
                    mutableItems.removeAt(position)
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemIconViewHolder {
        val binding = ItemIconLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemIconViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemIconViewHolder, position: Int) {
        holder.bind(mutableItems[position])
    }

    override fun getItemCount(): Int = mutableItems.size

    fun setEditMode(editMode: Boolean) {
        isEditMode = editMode
        notifyDataSetChanged()
    }
}