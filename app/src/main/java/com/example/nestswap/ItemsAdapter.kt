package com.example.nestswap

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.nestswap.databinding.ItemRowBinding

class ItemsAdapter(private val items: List<Item>) : RecyclerView.Adapter<ItemsAdapter.ItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding = ItemRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int = items.size

    class ItemViewHolder(private val binding: ItemRowBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Item) {
            binding.tvItemName.text = item.name
            binding.tvPrice.text = "Price: $${item.price}"
//            binding.tvItemDescription.text = item.description
//            binding.tvItemCategory.text = "Category: ${item.category}"
//            binding.tvItemCondition.text = "Condition: ${item.condition}"
//            binding.tvItemOwner.text = "Owner: ${item.owner}"

            itemView.setOnClickListener {
                val fragment = ItemDetailsFragment.newInstance(item)
                fragment.show((itemView.context as AppCompatActivity).supportFragmentManager, "ItemDetail")
            }
        }
    }
}