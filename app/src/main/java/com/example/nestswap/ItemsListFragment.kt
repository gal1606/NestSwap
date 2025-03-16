package com.example.nestswap

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nestswap.Model.Model
import com.example.nestswap.adapter.ItemsAdapter
import com.example.nestswap.databinding.FragmentItemsListBinding

class ItemsListFragment : Fragment() {

    private var _binding: FragmentItemsListBinding? = null
    private val binding get() = _binding!!

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

        val items = Model.instance.items.toMutableList()
        val adapter = ItemsAdapter(items)
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = adapter

        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<String>("refresh")
            ?.observe(viewLifecycleOwner) { refreshFlag ->
                if (refreshFlag == "true") {
                    items.clear()
                    items.addAll(Model.instance.items)
                    adapter.notifyDataSetChanged()
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}