package com.example.nestswap.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.nestswap.Review
import com.example.nestswap.databinding.ItemReviewLayoutBinding

class ReviewAdapter(
    private val reviews: List<Review>
) : RecyclerView.Adapter<ReviewAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemReviewLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(review: Review) {
            binding.ivItemPicture.setImageResource(review.itemImageResId)
            binding.tvItemName.text = review.itemName
            binding.tvReviewerName.text = review.reviewer
            binding.tvReviewBody.text = review.body
            binding.tvRating.text = "★ ${review.rating}"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemReviewLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(reviews[position])
    }

    override fun getItemCount(): Int = reviews.size
}