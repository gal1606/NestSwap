package com.example.nestswap

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nestswap.Model.Model
import com.example.nestswap.adapter.ReviewAdapter
import com.example.nestswap.databinding.FragmentReviewsBinding
import com.google.android.material.snackbar.Snackbar

class ReviewsFragment : Fragment() {

    private var _binding: FragmentReviewsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReviewsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userId = arguments?.getString("userId")

        Model.instance.getAllItems { items, itemsSource ->
            if (userId != null) {
                val ownerItemIds = items.filter { it.owner == userId }.map { it.id.toString() }
                Model.instance.getAllReviews { reviews ->
                    val filteredReviews = reviews.filter { it.itemId in ownerItemIds }
                    val adapter = ReviewAdapter(filteredReviews)
                    binding.rvReviews.layoutManager = LinearLayoutManager(context)
                    binding.rvReviews.adapter = adapter
                }
            } else {
                Snackbar.make(requireView(), "No user ID provided.", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}