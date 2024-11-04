package com.example.licentaincercarea1

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.licentaincercarea1.CategoriesFragment.Companion.CATEGORIES
import com.example.licentaincercarea1.data.category
import com.example.licentaincercarea1.data.ingredient
import com.example.licentaincercarea1.data.reteta
import com.example.licentaincercarea1.databinding.CategoriesFragmentBinding
import com.example.licentaincercarea1.databinding.ReteteFragmentBinding
import org.json.JSONArray
import org.json.JSONObject
import java.io.InputStream

class ReteteFragment : Fragment() {

    private var _binding: ReteteFragmentBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ReteteFragmentBinding.inflate(inflater, container, false)
        val view = binding.root

        @Suppress("DEPRECATION") val reteteList = arguments?.getParcelableArrayList<reteta>("reteteList")
        val reteteAdapter = ReteteAdapter(reteteList ?: emptyList())
        setuprvRetete(reteteAdapter)
        return view
    }

    private fun setuprvRetete(adapterr: ReteteAdapter){
        binding.rvRetete.apply {
            layoutManager = LinearLayoutManager(
                requireActivity(),
                LinearLayoutManager.VERTICAL,
                false
            )
            adapter = adapterr
        }

    }



}