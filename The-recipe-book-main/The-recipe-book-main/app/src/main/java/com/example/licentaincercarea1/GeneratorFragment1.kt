package com.example.licentaincercarea1

import ItemAdapter
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.licentaincercarea1.data.ingredient
import com.example.licentaincercarea1.data.types
import com.example.licentaincercarea1.databinding.GeneratorFragment1Binding
import org.json.JSONArray
import org.json.JSONObject
import java.io.InputStream


class GeneratorFragment1: Fragment() {

    val selectedIngredients = ArrayList<ingredient>()
    private var _binding: GeneratorFragment1Binding?=null
    private val binding get()=_binding!!
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = GeneratorFragment1Binding.inflate(inflater, container, false)

        _binding?.rvIngr?.apply {
            layoutManager = LinearLayoutManager(
                requireActivity(),
                LinearLayoutManager.VERTICAL,
                false
            )
            adapter = ItemAdapter(requireContext(), transf(), selectedIngredients)
        }

        val view = _binding?.root
        val searchView = view?.findViewById<androidx.appcompat.widget.SearchView>(R.id.search)
        searchView?.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                performSearch(query)
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                performSearch(newText)
                return false
            }
        })

        _binding?.next?.setOnClickListener {
            val bundle = Bundle().apply {
                putParcelableArrayList("ingred", selectedIngredients)
            }
            it.findNavController().navigate(R.id.action_generatorFragment_to_generatorFragment2, bundle)
        }

        return view
    }
    companion object {
        private var instance: GeneratorFragment1? = null

        fun getInstance(): GeneratorFragment1 {
            if (instance == null) {
                instance = GeneratorFragment1()
            }
            return instance as GeneratorFragment1
        }
    }

    private fun performSearch(query: String) {
        val filtered = arrayListOf<types>()
        for (type in transf()) {
            val ingr = arrayListOf<ingredient>()
            ingr.addAll(type.denumiri)
            val filteredRecipes = ingr.filter { it.nume.contains(query, true) }
            if (filteredRecipes.isNotEmpty()) {
                filtered.add(types(type.tip, filteredRecipes))
            }
        }
        if (filtered.isNotEmpty()) {
            binding.rvIngr.apply {
                layoutManager = LinearLayoutManager(
                    requireActivity(),
                    LinearLayoutManager.VERTICAL,
                    false
                )
                adapter = ItemAdapter(requireContext(), filtered, selectedIngredients)
            }
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        resetSharedPreferences()
    }

    private fun resetSharedPreferences() {
        val sharedPreferences = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().clear().apply()
    }

    fun transf(): List<types> {
        val retete = arrayListOf<types>()
        val obj = JSONObject(loadJSONFromAsset())
        val userArray: JSONArray = obj.getJSONArray("tipuri")
        for (i in 0 until userArray.length()) {
            val userDetail = userArray.getJSONObject(i)
            val ingredienteArray = userDetail.getJSONArray("denumiri")
            val ingrediente = arrayListOf<ingredient>()
            for (j in 0 until ingredienteArray.length()) {
                val ingredienteDetail = ingredienteArray.getJSONObject(j)
                val ingredient = ingredient(
                    isChecked = false,
                    nume = ingredienteDetail.getString("Nume"),
                    cantitate = 0,
                    masura=ingredienteDetail.getString("Masura")
                )
                ingrediente.add(ingredient)
            }
            retete.add(
                types(
                    tip = userDetail.getString("tip"),
                    denumiri = ingrediente
                )
            )
        }
        return retete
    }
    private fun loadJSONFromAsset(): String {
        val am = requireActivity().assets
        val inputStream: InputStream = am.open("tipuri.json")
        val size: Int = inputStream.available()
        val buffer = ByteArray(size)
        inputStream.read(buffer)
        return String(buffer)
    }
}