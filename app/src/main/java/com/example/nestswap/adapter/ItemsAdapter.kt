package com.example.nestswap.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.nestswap.ItemDetailsFragment
import com.example.nestswap.Model.dao.Item
import com.example.nestswap.databinding.ItemRowBinding

class ItemsAdapter(private val items: List<Item>) : RecyclerView.Adapter<ItemsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(private val binding: ItemRowBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Item) {
            binding.tvItemName.text = item.name
            binding.tvPrice.text = "Price: $${item.price}"

            item.imageUrl?.let { url ->
                Glide.with(binding.imageView.context)
                    .load(url)
                    .into(binding.imageView)
            } ?: binding.imageView.setImageDrawable(null) // Clear image if null

            itemView.setOnClickListener {
                val fragment = ItemDetailsFragment.newInstance(item)
                fragment.show((itemView.context as AppCompatActivity).supportFragmentManager, "ItemDetail")
            }
        }
    }
}