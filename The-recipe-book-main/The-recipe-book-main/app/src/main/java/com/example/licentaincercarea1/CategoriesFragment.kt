package com.example.licentaincercarea1


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.licentaincercarea1.data.category
import com.example.licentaincercarea1.data.ingredient
import com.example.licentaincercarea1.data.reteta
import com.example.licentaincercarea1.databinding.CategoriesFragmentBinding
import org.json.JSONArray
import org.json.JSONObject
import java.io.InputStream


class CategoriesFragment : Fragment() {

    private var _binding: CategoriesFragmentBinding?=null
    private val binding get()=_binding!!


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        _binding = CategoriesFragmentBinding.inflate(inflater,container,false)
        val view = binding.root
        val categorii=transfcat()
        val categoriesAdapter = CategoriesAdapter(categorii)
        setupRv(categoriesAdapter)
        binding.toate.setOnClickListener {
            it.findNavController().navigate(R.id.action_categoriesFragment_to_manifesting)
        }

        return view
    }

    private fun transfcat():List<category>{
        val categoryList=arrayListOf<category>()
        val obj = JSONObject(loadJSONFromAsset(CATEGORIES))
        val userArray: JSONArray = obj.getJSONArray("categories")
        for (i in 0 until userArray.length()) {
            val userDetail = userArray.getJSONObject(i)
            categoryList.add(
                category(
                    Name = userDetail.getString("Name"),
                    Thumb = userDetail.getString("Thumb"),
                    Description = userDetail.getString("Description")
                )
            )
        }
        return categoryList
    }
    fun transfretete(fileName: String, name: String): List<reteta> {
        val retete = arrayListOf<reteta>()
        val obj = JSONObject(loadJSONFromAsset(fileName))
        val userArray: JSONArray = obj.getJSONArray(name)
        for (i in 0 until userArray.length()) {
            val userDetail = userArray.getJSONObject(i)
            val ingredienteArray = userDetail.getJSONArray("Ingredients")
            val ingrediente = arrayListOf<ingredient>()
            for (j in 0 until ingredienteArray.length()) {
                val ingredienteDetail = ingredienteArray.getJSONObject(j)
                val ingredient = ingredient(
                    nume = ingredienteDetail.getString("Name"),
                    cantitate = ingredienteDetail.getInt("Quantity"),
                    masura=ingredienteDetail.getString("Unit")
                )
                ingrediente.add(ingredient)
            }
            retete.add(
                reteta(
                    Nume = userDetail.getString("Name"),
                    Thumb = userDetail.getString("Thumb"),
                    ingrediente = ingrediente,
                    P = userDetail.getString("Instructions")
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
    private fun setupRv(adapterc: CategoriesAdapter) {

        binding.rvCategories.apply {
            layoutManager = GridLayoutManager(requireActivity(), 2)
            adapter = adapterc
        }

        adapterc.setCategoryClickListener(object : CategoriesAdapter.CategoryClickListener {
            override fun onCategoryClick(category: category) {
                @Suppress("DEPRECATION") val retete = transfretete(getFileNameForCategory(category.Name), category.Name.toLowerCase())

                val bundle = Bundle().apply {
                    putParcelableArrayList("reteteList", ArrayList(retete))
                }

                binding.root.findNavController().navigate(
                    R.id.action_categoriesFragment_to_reteteFragment,
                    bundle
                )
            }
        })

    }
    private fun getFileNameForCategory(categoryName: String): String {
        return when (categoryName) {
            "Breakfast" -> Breakfast
            "Chicken" -> Chicken
            "Beef" -> Beef
            "Lamb" -> Lamb
            "Pork" -> Pork
            "Seafood" -> Seafood
            "Pasta" -> Pasta
            "Vegetariene"->Vegan
            "Dessert"->Dessert
            else -> ""
        }
    }

    companion object {
        const val CATEGORIES = "categories.json"
        const val Beef="vita.json"
        const val Chicken="Pui.json"
        const val Breakfast="dejun.json"
        const val Lamb="oaie.json"
        const val Pork="porc.json"
        const val Seafood="mare.json"
        const val Pasta="paste.json"
        const val Vegan="vegan.json"
        const val Dessert="desert.json"
    }
}

