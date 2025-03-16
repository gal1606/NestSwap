package com.example.nestswap

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.nestswap.Model.Model
import com.example.nestswap.databinding.FragmentReviewsBinding
import android.widget.ExpandableListView
import android.widget.SimpleExpandableListAdapter

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

        val reviews = Model.instance.reviews

        val groupData = reviews.map { it.reviewer }.distinct()
        val groupList = groupData.map { mapOf("reviewer" to it) }

        val childData = groupData.map { reviewer ->
            reviews.filter { it.reviewer == reviewer }.map { review ->
                mapOf(
                    "itemName" to review.itemName,
                    "rating" to review.rating.toString(),
                    "body" to (review.body ?: "")
                )
            }
        }

        val groupFrom = arrayOf("reviewer")
        val groupTo = intArrayOf(android.R.id.text1)
        val childFrom = arrayOf("itemName", "rating", "body")
        val childTo = intArrayOf(android.R.id.text1, android.R.id.text2, android.R.id.text2) // Reuse text2 for body

        val adapter = SimpleExpandableListAdapter(
            requireContext(),
            groupList,
            android.R.layout.simple_expandable_list_item_1,
            groupFrom,
            groupTo,
            childData,
            android.R.layout.simple_list_item_2,
            childFrom,
            childTo
        )

        binding.elvReviews.setAdapter(adapter)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}