package com.example.licentaincercarea1

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.licentaincercarea1.data.ingredient
import com.example.licentaincercarea1.data.reteta
import com.example.licentaincercarea1.databinding.SearchFragmentBinding
import org.json.JSONArray
import org.json.JSONObject
import java.io.InputStream

class SearchFragment: Fragment() {
    private var _binding: SearchFragmentBinding?=null
    private val binding get()=_binding!!
    val recipes = mutableListOf<reteta>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = SearchFragmentBinding.inflate(inflater, container, false)
        val view = binding.root
        val searchView = view?.findViewById<androidx.appcompat.widget.SearchView>(R.id.search_view)
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
        recipes.addAll(transfretete("dejun.json", "mic dejun"))
        recipes.addAll(transfretete("Pui.json", "pui"))
        recipes.addAll(transfretete("desert.json","desert"))
        recipes.addAll(transfretete("oaie.json","oaie"))
        recipes.addAll(transfretete("porc.json","porc"))
        recipes.addAll(transfretete("vita.json","vita"))
        recipes.addAll(transfretete("paste.json","paste"))
        recipes.addAll(transfretete("vegan.json","vegetariene"))
        recipes.addAll(transfretete("mare.json","fructe de mare"))

        recipes.sortBy { it.Nume}
        binding.rvSearch.apply {
            layoutManager = LinearLayoutManager(
                requireActivity(),
                LinearLayoutManager.VERTICAL,
                false
            )
            adapter = ReteteAdapter(recipes)
        }
        return view
    }
    private fun performSearch(query: String) {

        val filteredRecipes = recipes.filter { it.Nume.contains(query, true) }

        binding.rvSearch.apply {
            layoutManager = LinearLayoutManager(
                requireActivity(),
                LinearLayoutManager.VERTICAL,
                false
            )
            adapter = ReteteAdapter(filteredRecipes)
        }
    }
    fun transfretete(fileName: String, name: String): List<reteta> {
        val retete = arrayListOf<reteta>()
        val obj = JSONObject(loadJSONFromAsset(fileName))
        val userArray: JSONArray = obj.getJSONArray(name)
        for (i in 0 until userArray.length()) {
            val userDetail = userArray.getJSONObject(i)
            val ingredienteArray = userDetail.getJSONArray("In")
            val ingrediente = arrayListOf<ingredient>()
            for (j in 0 until ingredienteArray.length()) {
                val ingredienteDetail = ingredienteArray.getJSONObject(j)
                val ingredient = ingredient(
                    nume = ingredienteDetail.getString("Nume"),
                    cantitate = 0,
                    masura = ingredienteDetail.getString("Masura")
                )
                ingrediente.add(ingredient)
            }
            retete.add(
                reteta(
                    Nume = userDetail.getString("Nume"),
                    Thumb = userDetail.getString("Thumb"),
                    ingrediente = ingrediente,
                    P = userDetail.getString("P")
                )
            )
        }
        return retete
    }
    private fun loadJSONFromAsset(fileName: String): String {
        val am = requireActivity().assets
        val inputStream: InputStream = am.open(fileName)
        val size: Int = inputStream.available()
        val buffer = ByteArray(size)
        inputStream.read(buffer)
        return String(buffer)
    }

}