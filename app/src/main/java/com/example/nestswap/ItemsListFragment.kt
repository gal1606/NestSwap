package com.example.nestswap

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nestswap.Model.Model
import com.example.nestswap.Model.Model.DataSource
import com.example.nestswap.Model.dao.Item
import com.example.nestswap.adapter.ItemsAdapter
import com.example.nestswap.databinding.FragmentItemsListBinding

class ItemsListFragment : Fragment() {

    private var _binding: FragmentItemsListBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: ItemsAdapter
    private val items = mutableListOf<Item>()
    private var isDataLoaded = false
    private var itemsLoaded = false
    private var rentalsLoaded = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentItemsListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = ItemsAdapter(items)
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = adapter

        loadItems()

        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<String>("refresh")
            ?.observe(viewLifecycleOwner) { refreshFlag ->
                if (refreshFlag == "true") {
                    loadItems()
                }
            }
    }

    private fun loadItems() {
        val currentUserId = Model.instance.getCurrentUserId()
        if (currentUserId == null) {
            return // Silently exit if not authenticated
        }

        isDataLoaded = false
        itemsLoaded = false
        rentalsLoaded = false

        binding.progressBar.visibility = View.VISIBLE
        binding.recyclerView.visibility = View.GONE

        val handler = Handler(Looper.getMainLooper())
        val timeoutRunnable = Runnable {
            if (!isDataLoaded && _binding != null) {
                binding.progressBar.visibility = View.GONE
                binding.recyclerView.visibility = View.VISIBLE
            }
        }
        handler.postDelayed(timeoutRunnable, 5000)

        Model.instance.getAllItems { allItems, itemsSource ->
            if (!isAdded) return@getAllItems

            if (itemsSource == Model.DataSource.FIREBASE) {
                itemsLoaded = true
            }

            Model.instance.getAllRentals { rentals, rentalsSource ->
                if (!isAdded) return@getAllRentals

                if (rentalsSource == Model.DataSource.FIREBASE) {
                    rentalsLoaded = true
                }

                if (itemsLoaded && rentalsLoaded && !isDataLoaded && _binding != null) {
                    handler.removeCallbacks(timeoutRunnable)
                    val rentedPairs = rentals.map { it.itemName to it.owner }.toSet()
                    val availableItems = allItems.filter {
                        (it.name to it.owner) !in rentedPairs && it.owner != currentUserId
                    }
                    items.clear()
                    items.addAll(availableItems)
                    adapter.notifyDataSetChanged()

                    binding.progressBar.visibility = View.GONE
                    binding.recyclerView.visibility = View.VISIBLE
                    isDataLoaded = true
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}