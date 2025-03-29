package com.example.nestswap

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nestswap.Model.Model
import com.example.nestswap.Model.Model.DataSource
import com.example.nestswap.Model.dao.Item
import com.example.nestswap.Model.dao.Rental
import com.example.nestswap.adapter.ItemIcon
import com.example.nestswap.adapter.ItemIconAdapter
import com.example.nestswap.databinding.FragmentProfileBinding
import com.google.firebase.firestore.FirebaseFirestore

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val userId: String by lazy {
        arguments?.getString("userId") ?: Model.instance.getCurrentUserId()
        ?: throw IllegalStateException("User ID not provided and no user signed in")
    }

    private var isEditMode = false
    private var originalName: String = ""
    private var originalLocation: String = ""
    private lateinit var currentlyListedItems: MutableList<ItemIcon>
    private lateinit var currentlyListedAdapter: ItemIconAdapter
    private lateinit var rentedOutItemsAdapter: ItemIconAdapter
    private lateinit var rentedInItemsAdapter: ItemIconAdapter
    private lateinit var allItems: List<Item>
    private lateinit var allRentals: List<Rental>
    private var listsUpdated = false

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
        val currentUserId = Model.instance.getCurrentUserId()
        if (currentUserId == null) {
            findNavController().navigate(R.id.action_profile_to_login)
            return
        }

        binding.tvName.visibility = View.GONE
        binding.tvLocation.apply {
            isClickable = true
            setTextColor(resources.getColor(android.R.color.holo_blue_dark, null))
            paint.isUnderlineText = true
            setOnClickListener {
                val action = ProfileFragmentDirections.actionProfileToMap(originalLocation)
                findNavController().navigate(action)
            }
        }
        binding.tvRating.visibility = View.GONE
        binding.btnEditProfile.visibility = View.GONE

        binding.progressBarLists.visibility = View.VISIBLE
        binding.rvCurrentlyListedItems.visibility = View.GONE
        binding.rvRentedOutItems.visibility = View.GONE
        binding.rvRentedInItems.visibility = View.GONE
        binding.tvCurrentlyListedItemsLabel.visibility = View.GONE
        binding.tvRentedOutItemsLabel.visibility = View.GONE
        binding.tvRentedInItemsLabel.visibility = View.GONE

        val profileHandler = Handler(Looper.getMainLooper())
        val profileTimeoutRunnable = Runnable {
            if (_binding != null) {
                originalName = "LoggedInUser"
                originalLocation = "Tel Aviv"
                binding.tvName.text = originalName
                binding.etName.setText(originalName)
                binding.tvLocation.text = originalLocation
                binding.etLocation.setText(originalLocation)
                binding.tvName.visibility = View.VISIBLE
                binding.tvLocation.visibility = View.VISIBLE
                binding.tvRating.visibility = View.VISIBLE
                binding.btnEditProfile.visibility = if (userId == currentUserId) View.VISIBLE else View.GONE
            }
        }
        profileHandler.postDelayed(profileTimeoutRunnable, 5000)

        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(currentUserId)
            .get()
            .addOnSuccessListener { document ->
                profileHandler.removeCallbacks(profileTimeoutRunnable)
                if (_binding != null) {
                    originalName = document.takeIf { it.exists() }?.getString("fullName") ?: "LoggedInUser"
                    originalLocation = document.takeIf { it.exists() }?.getString("city") ?: "Tel Aviv"
                    binding.tvName.text = originalName
                    binding.etName.setText(originalName)
                    binding.tvLocation.text = originalLocation
                    binding.etLocation.setText(originalLocation)
                    binding.tvName.visibility = View.VISIBLE
                    binding.tvLocation.visibility = View.VISIBLE
                    binding.tvRating.visibility = View.VISIBLE
                    binding.btnEditProfile.visibility = if (userId == currentUserId) View.VISIBLE else View.GONE
                }
            }
            .addOnFailureListener {
                profileHandler.removeCallbacks(profileTimeoutRunnable)
                if (_binding != null) {
                    originalName = "LoggedInUser"
                    originalLocation = "Tel Aviv"
                    binding.tvName.text = originalName
                    binding.etName.setText(originalName)
                    binding.tvLocation.text = originalLocation
                    binding.etLocation.setText(originalLocation)
                    binding.tvName.visibility = View.VISIBLE
                    binding.tvLocation.visibility = View.VISIBLE
                    binding.tvRating.visibility = View.VISIBLE
                    binding.btnEditProfile.visibility = if (userId == currentUserId) View.VISIBLE else View.GONE
                }
            }

        Model.instance.getReviewsForUser(userId) { reviews ->
            if (_binding != null) {
                binding.tvRating.text = if (reviews.isNotEmpty()) {
                    "★ %.1f".format(reviews.map { it.rating }.average())
                } else {
                    binding.tvViewAllReviews.visibility = View.GONE
                    getString(R.string.no_reviews)
                }
            }
        }

        currentlyListedItems = mutableListOf()
        currentlyListedAdapter = ItemIconAdapter(
            items = currentlyListedItems,
            onItemClick = {},
            onDeleteClick = { item ->
                if (isEditMode) {
                    val itemToRemove = allItems.find { it.name == item.name && it.owner == item.itemOwner }
                    itemToRemove?.let {
                        Model.instance.deleteItem(it) { success ->
                            if (success) refreshAllLists(currentUserId)
                        }
                    }
                }
            }
        )
        binding.rvCurrentlyListedItems.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.rvCurrentlyListedItems.adapter = currentlyListedAdapter

        val rentedOutItems = mutableListOf<ItemIcon>()
        rentedOutItemsAdapter = ItemIconAdapter(
            items = rentedOutItems,
            onItemClick = {},
            onDeleteClick = {},
            isEditMode = false
        )
        binding.rvRentedOutItems.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.rvRentedOutItems.adapter = rentedOutItemsAdapter

        val rentedInItems = mutableListOf<ItemIcon>()
        rentedInItemsAdapter = ItemIconAdapter(
            items = rentedInItems,
            onItemClick = { item ->
                if (!item.isPlaceholder) {
                    val rental = allRentals.find { it.itemName == item.name && it.owner == item.itemOwner && it.renter == currentUserId }
                    rental?.let {
                        CompleteTransactionFragment.newInstance(item.name, item.itemOwner, it.id)
                            .show(parentFragmentManager, "CompleteTransactionFragment")
                    } ?: Log.e("ProfileFragment", "Rental not found for item: ${item.name}")
                }
            },
            onDeleteClick = {},
            isEditMode = false
        )
        binding.rvRentedInItems.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.rvRentedInItems.adapter = rentedInItemsAdapter

        refreshAllLists(currentUserId)

        binding.tvViewAllReviews.setOnClickListener {
            findNavController().navigate(ProfileFragmentDirections.actionProfileToReviews(userId))
        }

        binding.btnEditProfile.setOnClickListener { toggleEditMode(true) }

        binding.btnSaveProfile.setOnClickListener {
            originalName = binding.etName.text.toString()
            originalLocation = binding.etLocation.text.toString()
            binding.tvName.text = originalName
            binding.tvLocation.text = originalLocation
            toggleEditMode(false)
            if (userId == currentUserId) {
                val updatedProfile = hashMapOf("fullName" to originalName, "city" to originalLocation)
                db.collection("users").document(currentUserId)
                    .update(updatedProfile as Map<String, Any>)
            }
        }

        binding.btnCancelEdit.setOnClickListener {
            binding.etName.setText(binding.tvName.text)
            binding.etLocation.setText(binding.tvLocation.text)
            toggleEditMode(false)
        }

        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<String>("refresh")
            ?.observe(viewLifecycleOwner) { if (it == "true") refreshAllLists(currentUserId) }
    }

    private fun updateCurrentlyListedItems(currentUserId: String) {
        if (_binding == null) return
        currentlyListedItems.clear()
        val availableItems = if (::allRentals.isInitialized) {
            allItems.filter { it.owner == currentUserId && allRentals.none { r -> r.itemName == it.name && r.owner == currentUserId } }
        } else {
            allItems.filter { it.owner == currentUserId }
        }
        currentlyListedItems.addAll(
            if (availableItems.isEmpty()) listOf(ItemIcon(null, "", "Currently no items listed", currentUserId, true))
            else availableItems.map { ItemIcon(it.imageUrl, it.name, "Price $${it.price}", it.owner) }
        )
        binding.rvCurrentlyListedItems.visibility = View.VISIBLE
        currentlyListedAdapter.notifyDataSetChanged()
    }

    private fun updateRentedOutItems(currentUserId: String) {
        if (_binding == null) return
        val rentedOutItems = mutableListOf<ItemIcon>()
        if (::allRentals.isInitialized) {
            allItems.filter { it.owner == currentUserId }.forEach { item ->
                if (allRentals.any { it.itemName == item.name && it.owner == currentUserId }) {
                    rentedOutItems.add(ItemIcon(item.imageUrl, item.name, "Price $${item.price}", currentUserId))
                }
            }
        }
        if (rentedOutItems.isEmpty()) rentedOutItems.add(ItemIcon(null, "", "Currently no items rented out", currentUserId, true))
        rentedOutItemsAdapter = ItemIconAdapter(rentedOutItems, {}, {}, isEditMode = false)
        binding.rvRentedOutItems.adapter = rentedOutItemsAdapter
        binding.rvRentedOutItems.visibility = View.VISIBLE
    }

    private fun updateRentedInItems(currentUserId: String) {
        if (_binding == null) return
        val rentedInItems = if (::allRentals.isInitialized) {
            allRentals.filter { it.renter == currentUserId }.mapNotNull { rental ->
                allItems.find { it.name == rental.itemName && it.owner == rental.owner }?.let {
                    ItemIcon(it.imageUrl, rental.itemName, "Rented from ${rental.owner}", rental.owner)
                }
            }.toMutableList()
        } else mutableListOf()
        if (rentedInItems.isEmpty()) rentedInItems.add(ItemIcon(null, "", "Currently no items rented in", currentUserId, true))
        rentedInItemsAdapter = ItemIconAdapter(rentedInItems, { item ->
            if (!item.isPlaceholder) {
                allRentals.find { it.itemName == item.name && it.owner == item.itemOwner && it.renter == currentUserId }?.let {
                    CompleteTransactionFragment.newInstance(item.name, item.itemOwner, it.id)
                        .show(parentFragmentManager, "CompleteTransactionFragment")
                } ?: Log.e("ProfileFragment", "Rental not found for item: ${item.name}")
            }
        }, {}, isEditMode = false)
        binding.rvRentedInItems.adapter = rentedInItemsAdapter
        binding.rvRentedInItems.visibility = View.VISIBLE
    }

    private fun refreshAllLists(currentUserId: String) {
        Log.d("ProfileFragment", "Starting refreshAllLists for userId: $currentUserId")
        var localItems: List<Item>? = null
        var localRentals: List<Rental>? = null
        var firebaseItems: List<Item>? = null
        var firebaseRentals: List<Rental>? = null
        listsUpdated = false

        if (!isNetworkAvailable()) {
            Log.d("ProfileFragment", "No network available, using local data immediately")
            Model.instance.getAllItems { items, source ->
                if (source == DataSource.LOCAL) localItems = items
            }
            Model.instance.getAllRentals { rentals, source ->
                if (source == DataSource.LOCAL) localRentals = rentals
            }
            allItems = localItems ?: emptyList()
            allRentals = localRentals ?: emptyList()
            updateLists(currentUserId)
            finishListRefresh()
            return
        }

        val handler = Handler(Looper.getMainLooper())
        val timeoutRunnable = Runnable {
            if (!listsUpdated && _binding != null) {
                Log.d("ProfileFragment", "Timeout reached, falling back to local data")
                allItems = localItems ?: emptyList()
                allRentals = localRentals ?: emptyList()
                updateLists(currentUserId)
                finishListRefresh()
            }
        }
        handler.postDelayed(timeoutRunnable, 10000)

        Model.instance.getAllItems { updatedItems, source ->
            if (_binding == null) return@getAllItems
            Log.d("ProfileFragment", "Items fetched: ${updatedItems.size} from $source")
            when (source) {
                DataSource.LOCAL -> localItems = updatedItems
                DataSource.FIREBASE -> if (!listsUpdated) {
                    firebaseItems = updatedItems
                    firebaseRentals?.let {
                        handler.removeCallbacks(timeoutRunnable)
                        Log.d("ProfileFragment", "Firebase items and rentals synced")
                        allItems = firebaseItems!!
                        allRentals = firebaseRentals!!
                        updateLists(currentUserId)
                        finishListRefresh()
                    }
                }
            }
        }

        Model.instance.getAllRentals { updatedRentals, source ->
            if (_binding == null) return@getAllRentals
            Log.d("ProfileFragment", "Rentals fetched: ${updatedRentals.size} from $source")
            when (source) {
                DataSource.LOCAL -> localRentals = updatedRentals
                DataSource.FIREBASE -> if (!listsUpdated) {
                    firebaseRentals = updatedRentals
                    firebaseItems?.let {
                        handler.removeCallbacks(timeoutRunnable)
                        Log.d("ProfileFragment", "Firebase rentals and items synced")
                        allItems = firebaseItems!!
                        allRentals = firebaseRentals!!
                        updateLists(currentUserId)
                        finishListRefresh()
                    }
                }
            }
        }
    }

    private fun toggleEditMode(enable: Boolean) {
        isEditMode = enable
        currentlyListedAdapter.setEditMode(enable)
        binding.apply {
            tvName.visibility = if (enable) View.GONE else View.VISIBLE
            etName.visibility = if (enable) View.VISIBLE else View.GONE
            tvLocation.visibility = if (enable) View.GONE else View.VISIBLE
            etLocation.visibility = if (enable) View.VISIBLE else View.GONE
            tvRating.visibility = if (enable) View.GONE else View.VISIBLE
            tvViewAllReviews.visibility = if (enable) View.GONE else View.VISIBLE
            btnEditProfile.visibility = if (enable) View.GONE else View.VISIBLE
            btnSaveProfile.visibility = if (enable) View.VISIBLE else View.GONE
            btnCancelEdit.visibility = if (enable) View.VISIBLE else View.GONE
            rvCurrentlyListedItems.visibility = View.VISIBLE
            rvRentedOutItems.visibility = if (enable) View.GONE else View.VISIBLE
            rvRentedInItems.visibility = if (enable) View.GONE else View.VISIBLE
            tvCurrentlyListedItemsLabel.visibility = View.VISIBLE
            tvRentedOutItemsLabel.visibility = if (enable) View.GONE else View.VISIBLE
            tvRentedInItemsLabel.visibility = if (enable) View.GONE else View.VISIBLE
        }
    }

    private fun updateLists(currentUserId: String) {
        updateCurrentlyListedItems(currentUserId)
        updateRentedOutItems(currentUserId)
        updateRentedInItems(currentUserId)
    }

    private fun finishListRefresh() {
        binding.progressBarLists.visibility = View.GONE
        binding.rvCurrentlyListedItems.visibility = View.VISIBLE
        binding.rvRentedOutItems.visibility = View.VISIBLE
        binding.rvRentedInItems.visibility = View.VISIBLE
        binding.tvCurrentlyListedItemsLabel.visibility = View.VISIBLE
        binding.tvRentedOutItemsLabel.visibility = View.VISIBLE
        binding.tvRentedInItemsLabel.visibility = View.VISIBLE
        listsUpdated = true
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = context?.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val network = connectivityManager?.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}