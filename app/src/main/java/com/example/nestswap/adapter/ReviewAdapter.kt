package com.example.nestswap.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.nestswap.Review
import com.example.nestswap.databinding.ItemReviewLayoutBinding
import com.google.firebase.firestore.FirebaseFirestore

class ReviewAdapter(private val reviews: List<Review>) : RecyclerView.Adapter<ReviewAdapter.ViewHolder>() {

    private val database = FirebaseFirestore.getInstance()

    inner class ViewHolder(val binding: ItemReviewLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(review: Review) {
            review.itemImageUrl?.let { url ->
                Glide.with(binding.ivItemPicture.context)
                    .load(url)
                    .into(binding.ivItemPicture)
            } ?: binding.ivItemPicture.setImageDrawable(null)

            binding.tvItemName.text = review.itemName
            binding.tvRating.text = "★ ${review.rating}"
            binding.tvReviewBody.text = review.body

            getReviewerName(review.reviewer) { name ->
                binding.tvReviewerName.text = name
            }
        }

        private fun getReviewerName(reviewerId: String, callback: (String) -> Unit) {
            database.collection("users")
                .document(reviewerId)
                .get()
                .addOnSuccessListener { document ->
                    val fullName = document.getString("fullName") ?: "Unknown"
                    callback(fullName)
                }
                .addOnFailureListener {
                    callback("Unknown")
                }
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