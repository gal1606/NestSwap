package com.example.nestswap

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nestswap.adapter.ItemIcon
import com.example.nestswap.adapter.ItemIconAdapter
import com.example.nestswap.databinding.FragmentProfileBinding
import com.example.nestswap.Model.Model
import com.google.android.material.snackbar.Snackbar

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    // Temporary workaround for ProfileFragmentArgs issue
    private val userId: String by lazy {
        arguments?.getString("userId") ?: "default_user_id"
    }

    private var isEditMode = false
    private var originalName: String = ""
    private var originalLocation: String = ""
    private lateinit var currentlyListedItems: MutableList<ItemIcon>
    private lateinit var currentlyListedAdapter: ItemIconAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("ProfileFragment", "onViewCreated called for userId: $userId")

        val currentUserId = getCurrentUserId()

        originalName = if (userId == currentUserId) "LoggedInUser" else "Michael Scott"
        originalLocation = "Tel Aviv"
        binding.tvName.text = originalName
        binding.etName.setText(originalName)
        binding.tvRating.text = "★ 4.9"
        binding.tvLocation.text = originalLocation
        binding.etLocation.setText(originalLocation)

        if (userId == currentUserId) {
            binding.btnEditProfile.visibility = View.VISIBLE
            binding.btnFollow.visibility = View.GONE
        } else {
            binding.btnEditProfile.visibility = View.GONE
            binding.btnFollow.visibility = View.VISIBLE
        }

        setupCurrentlyListedItemsRecyclerView()
        setupRentedOutItemsRecyclerView()
        setupRentedInItemsRecyclerView()

        binding.tvViewAllReviews.setOnClickListener {
            Log.d("ProfileFragment", "View All Reviews clicked")
            findNavController().navigate(R.id.action_profile_to_reviews)
        }

        binding.btnEditProfile.setOnClickListener {
            isEditMode = true
            binding.tvName.visibility = View.GONE
            binding.tvLocation.visibility = View.GONE
            binding.etName.visibility = View.VISIBLE
            binding.etLocation.visibility = View.VISIBLE
            binding.btnEditProfile.visibility = View.GONE
            binding.btnSaveProfile.visibility = View.VISIBLE
            binding.btnCancelEdit.visibility = View.VISIBLE
            currentlyListedAdapter.setEditMode(true)
        }

        binding.btnSaveProfile.setOnClickListener {
            originalName = binding.etName.text.toString()
            originalLocation = binding.etLocation.text.toString()
            binding.tvName.text = originalName
            binding.tvLocation.text = originalLocation
            isEditMode = false
            binding.tvName.visibility = View.VISIBLE
            binding.tvLocation.visibility = View.VISIBLE
            binding.etName.visibility = View.GONE
            binding.etLocation.visibility = View.GONE
            binding.btnEditProfile.visibility = View.VISIBLE
            binding.btnSaveProfile.visibility = View.GONE
            binding.btnCancelEdit.visibility = View.GONE
            currentlyListedAdapter.setEditMode(false)
            Snackbar.make(requireView(), "Profile updated", Snackbar.LENGTH_SHORT).show()
        }

        binding.btnCancelEdit.setOnClickListener {
            binding.etName.setText(originalName)
            binding.etLocation.setText(originalLocation)
            currentlyListedItems.clear()
            currentlyListedItems.addAll(
                Model.instance.items
                    .filter { it.owner == currentUserId }
                    .map {
                        ItemIcon(
                            iconRes = R.drawable.projector,
                            name = it.name,
                            details = "Price $${it.price}",
                            itemOwner = it.owner
                        )
                    }
                    .toMutableList()
            )
            currentlyListedAdapter.notifyDataSetChanged()
            isEditMode = false
            binding.tvName.visibility = View.VISIBLE
            binding.tvLocation.visibility = View.VISIBLE
            binding.etName.visibility = View.GONE
            binding.etLocation.visibility = View.GONE
            binding.btnEditProfile.visibility = View.VISIBLE
            binding.btnSaveProfile.visibility = View.GONE
            binding.btnCancelEdit.visibility = View.GONE
            currentlyListedAdapter.setEditMode(false)
        }

        binding.btnFollow.setOnClickListener {
            Snackbar.make(requireView(), "Follow clicked", Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun setupCurrentlyListedItemsRecyclerView() {
        currentlyListedItems = Model.instance.items
            .filter { it.owner == getCurrentUserId() }
            .map {
                ItemIcon(
                    iconRes = R.drawable.projector,
                    name = it.name,
                    details = "Price $${it.price}",
                    itemOwner = it.owner
                )
            }
            .toMutableList()

        currentlyListedAdapter = ItemIconAdapter(
            items = currentlyListedItems,
            onItemClick = { /* No click action in view mode */ },
            onDeleteClick = { item ->
                val itemToRemove = Model.instance.items.find { it.name == item.name && it.owner == item.itemOwner }
                if (itemToRemove != null) {
                    Model.instance.items = (Model.instance.items - itemToRemove).toMutableList()
                    currentlyListedItems.remove(item)
                    currentlyListedAdapter.notifyDataSetChanged()
                }
            }
        )
        binding.rvCurrentlyListedItems.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.rvCurrentlyListedItems.adapter = currentlyListedAdapter

        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<String>("refresh")
            ?.observe(viewLifecycleOwner) { refreshFlag ->
                if (refreshFlag == "true") {
                    currentlyListedItems.clear()
                    currentlyListedItems.addAll(
                        Model.instance.items
                            .filter { it.owner == getCurrentUserId() }
                            .map {
                                ItemIcon(
                                    iconRes = R.drawable.projector,
                                    name = it.name,
                                    details = "Price $${it.price}",
                                    itemOwner = it.owner
                                )
                            }
                            .toMutableList()
                    )
                    currentlyListedAdapter.notifyDataSetChanged()
                }
            }
    }

    private fun setupRentedOutItemsRecyclerView() {
        val currentUserId = getCurrentUserId()
        Log.d("ProfileFragment", "Current User ID: $currentUserId")
        val rentedOutItems = Model.instance.items
            .filter { it.owner == currentUserId }
            .map { ItemIcon(R.drawable.projector, it.name, "Price $${it.price}", currentUserId) }
        Log.d("ProfileFragment", "Rented Out Items: ${rentedOutItems.size} items - $rentedOutItems")
        binding.rvRentedOutItems.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.rvRentedOutItems.adapter = ItemIconAdapter(
            items = rentedOutItems,
            onItemClick = { item ->
                val completeTransactionFragment = CompleteTransactionFragment.newInstance(item.name, item.itemOwner)
                completeTransactionFragment.show(parentFragmentManager, "CompleteTransactionFragment")
            },
            onDeleteClick = {},
            isEditMode = false
        )
    }

    private fun setupRentedInItemsRecyclerView() {
        val currentUserId = getCurrentUserId()
        val rentedInItems = Model.instance.rentals
            .filter { it.renter == currentUserId }
            .map { rental ->
                val item = Model.instance.items.find { it.id == rental.itemId }
                ItemIcon(R.drawable.projector, item?.name ?: "Unknown", "Rented from ${rental.owner}", rental.owner)
            }
        binding.rvRentedInItems.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.rvRentedInItems.adapter = ItemIconAdapter(
            items = rentedInItems,
            onItemClick = { item ->
                val completeTransactionFragment = CompleteTransactionFragment.newInstance(item.name, item.itemOwner)
                completeTransactionFragment.show(parentFragmentManager, "CompleteTransactionFragment")
            },
            onDeleteClick = {},
            isEditMode = false
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun getCurrentUserId(): String {
        return "logged_in_user_id"
    }
}