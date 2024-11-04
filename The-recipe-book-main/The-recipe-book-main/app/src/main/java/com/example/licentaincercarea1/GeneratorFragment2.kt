package com.example.licentaincercarea1

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.licentaincercarea1.data.ingredient
import com.example.licentaincercarea1.data.reteta
import com.example.licentaincercarea1.databinding.GeneratorFragment2Binding
import org.json.JSONArray
import org.json.JSONObject
import java.io.InputStream

class GeneratorFragment2: Fragment() {
    private var _binding: GeneratorFragment2Binding?=null
    private val binding get()=_binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding= GeneratorFragment2Binding.inflate(inflater, container, false)
        val bundle = arguments
        @Suppress("DEPRECATION") val selectedIngredients = bundle?.getParcelableArrayList<ingredient>("ingred")
        selectedIngredients?.add(ingredient(true,"Sare",5,"gr"))
        selectedIngredients?.add(ingredient(true,"Piper",5,"gr"))
        selectedIngredients?.add(ingredient(true,"Apa",2000,"ml"))

        val recipes = mutableListOf<reteta>()
        recipes.addAll(transfretete("dejun.json", "breakfast"))
        recipes.addAll(transfretete("Pui.json", "chicken"))
        recipes.addAll(transfretete("desert.json","dessert"))
        recipes.addAll(transfretete("oaie.json","lamb"))
        recipes.addAll(transfretete("porc.json","pork"))
        recipes.addAll(transfretete("vita.json","beef"))
        recipes.addAll(transfretete("paste.json","pasta"))
        recipes.addAll(transfretete("vegan.json","vegan"))
        recipes.addAll(transfretete("mare.json","seafood"))
        val adaptlist=generateMatchingRecipes(selectedIngredients,recipes)
        binding.rvRetete.apply {
            layoutManager = LinearLayoutManager(
                requireActivity(),
                LinearLayoutManager.VERTICAL,
                false
            )
            adapter = ReteteAdapter(adaptlist)
        }
        return binding.root
    }
    fun generateMatchingRecipes(ingredientList: ArrayList<ingredient>?, recipeList: MutableList<reteta>): List<reteta> {
        val matchingRecipes = mutableListOf<reteta>()

        for (recipe in recipeList) {
            val matchingIngredients = recipe.ingrediente.filter { recipeIngredient ->
                val matchingIngredient = ingredientList!!.find { ingredient ->
                    ingredient.nume == recipeIngredient.nume && ingredient.cantitate >= recipeIngredient.cantitate / 2
                }
                matchingIngredient != null
            }
            if (matchingIngredients.size >= recipe.ingrediente.size / 2) {
                val nonMatchingIngredients = recipe.ingrediente.filter { recipeIngredient ->
                    val matchingIngredient = ingredientList!!.find { ingredient ->
                        ingredient.nume == recipeIngredient.nume && ingredient.cantitate >= recipeIngredient.cantitate / 2
                    }
                    matchingIngredient == null
                }
                recipe.lipsa = nonMatchingIngredients

                matchingRecipes.add(recipe)
            }
        }

        matchingRecipes.sortByDescending {
            it.ingrediente.count { recipeIngredient ->
                val matchingIngredient = ingredientList!!.find { ingredient ->
                    ingredient.nume == recipeIngredient.nume && ingredient.cantitate >= recipeIngredient.cantitate / 2
                }
                matchingIngredient != null
            }
        }

        return matchingRecipes
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
                    cantitate = ingredienteDetail.getInt("Cantitate"),
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