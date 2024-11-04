package com.example.licentaincercarea1

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.licentaincercarea1.data.ingredient
import com.example.licentaincercarea1.data.reteta
import com.example.licentaincercarea1.databinding.RezultatFragmentBinding

class RezultatFragment: Fragment() {
    private var _binding: RezultatFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = RezultatFragmentBinding.inflate(inflater, container, false)
        val view = binding.root
        var retete: ArrayList<reteta>? = null
        arguments?.let { bundle ->
            @Suppress("DEPRECATION")
            retete = bundle.getParcelableArrayList<reteta>("retete")
        }
        binding.rvRezultat.apply {
            layoutManager = LinearLayoutManager(
                requireActivity(),
                LinearLayoutManager.VERTICAL,
                false
            )
            adapter = retete?.let { ReteteAdapter(it) }
        }
        return view
    }
}