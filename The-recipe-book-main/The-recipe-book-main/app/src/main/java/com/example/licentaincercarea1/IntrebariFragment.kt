package com.example.licentaincercarea1

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.example.licentaincercarea1.R
import com.example.licentaincercarea1.data.ingredient
import com.example.licentaincercarea1.data.reteta
import com.example.licentaincercarea1.databinding.IntrebariFragmentBinding
import org.json.JSONArray
import org.json.JSONObject
import java.io.InputStream

class IntrebariFragment : Fragment() {
    data class Intrebare(
        val index: Int,
        val text: String,
        val raspunsAdevaratIndex: Int,
        val raspunsFalsIndex: Int
    )

    private val intrebari: MutableList<Intrebare> = mutableListOf(
        Intrebare(1, "Ai pofta de ceva dulce?", 2, 3),
        Intrebare(2, "Poate ceva cu ciocolata?", 4, 5),
        Intrebare(3, "Ai pofta de ceva ce contine carne?", 6, 7),
        Intrebare(4, "Poate un tort?", 8, 9),
        Intrebare(5, "Atunci ceva cu fructe?", 10, 11),
        Intrebare(6,"Ceva cu peste sau fructe de mare?",12,13),
        Intrebare(7,"Ceva vegetarian/ de post?",14,15),
        Intrebare(13,"Ai vrea un preparat cu carne alba?",26,27),
        Intrebare(27,"Ceva pregatit la cuptor?",28,29)
    )
    
    private var intrebareCurenta: Intrebare? = null
    private var _binding: IntrebariFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = IntrebariFragmentBinding.inflate(inflater, container, false)
        intrebareCurenta = intrebari.firstOrNull { it.index == 1 }
        binding.intrebare = intrebareCurenta

        binding.raspunsTrueButton.setOnClickListener {
            verificaRaspuns(true)
        }
        binding.raspunsFalseButton.setOnClickListener {
            verificaRaspuns(false)
        }

        return binding.root
    }

    private fun verificaRaspuns(raspunsUtilizator: Boolean) {
        val indexRaspuns: Int = if (raspunsUtilizator) {
            intrebareCurenta?.raspunsAdevaratIndex ?: 0
        } else {
            intrebareCurenta?.raspunsFalsIndex ?: 0
        }

        actualizareLista(indexRaspuns)
        val forbiddenValues = listOf(8, 9, 10, 11, 12,14,15, 26, 28,29)
        if (indexRaspuns !in forbiddenValues) {
            intrebareCurenta = intrebari.firstOrNull { it.index == indexRaspuns }
            binding.intrebare = intrebareCurenta
        } else {
            val bundle = Bundle().apply {
                putParcelableArrayList("retete", ArrayList(retete))
            }
            binding.root.findNavController()
                .navigate(R.id.action_intrebariFragment_to_rezultatFragment, bundle)
            resetFragment()
        }
    }

    private var retete: MutableList<reteta> = mutableListOf()

    private fun actualizareLista(indexRaspuns: Int) {
        when (indexRaspuns) {
            4 -> {
                retete.clear()
                val filtru = transfretete("desert.json", "desert")
                for (reteta in filtru)
                    for (ingredient in reteta.ingrediente)
                        if (ingredient.nume == "Ciocolată")
                            retete.add(reteta)
            }
            8 -> {
                val reteteFiltrate = retete.filter { reteta ->
                    reteta.Nume.contains("tort", ignoreCase = true)
                }
                retete = reteteFiltrate.toMutableList()
            }
            9 -> {
                val reteteFiltrate = retete.filterNot { reteta ->
                    reteta.Nume.contains("tort", ignoreCase = true)
                }
                retete = reteteFiltrate.toMutableList()

            }
            10 -> {
                retete.clear()
                val filtru = mutableListOf<reteta>()
                filtru.addAll(transfretete("desert.json", "desert"))
                filtru.addAll(transfretete("dejun.json", "mic dejun"))
                for (reteta in filtru) {
                    outer@ for (ingredient in reteta.ingrediente) {
                        if (ingredient.nume == "Portocală" || ingredient.nume == "Kiwi" || ingredient.nume == "Pepene galben" || ingredient.nume == "Afine" || ingredient.nume == "Mere" || ingredient.nume == "Lămâie" || ingredient.nume == "Lime") {
                            retete.add(reteta)
                            break@outer
                        }
                    }
                }
            }
            11 -> {
                retete.clear()
                val filtru = mutableListOf<reteta>()
                filtru.addAll(transfretete("desert.json", "desert"))
                outer@ for (reteta in filtru) {
                    for (ingredient in reteta.ingrediente) {
                        if (ingredient.nume == "Ciocolată" || ingredient.nume == "Portocală" || ingredient.nume == "Kiwi" || ingredient.nume == "Pepene galben" || ingredient.nume == "Afine" || ingredient.nume == "Mere" || ingredient.nume == "Lămâie" || ingredient.nume == "Lime") {
                            continue@outer
                        }
                    }
                    retete.add(reteta)
                }

            }
            12 -> {
                retete.clear()
                retete.addAll(transfretete("mare.json", "fructe de mare"))
            }
            14->{
                retete.clear()
                retete.addAll(transfretete("vegan.json", "vegetariene"))
            }
            15->{
                retete.clear()
                retete.addAll(transfretete("paste.json", "paste"))
                val reteteFiltrate = retete.filterNot { reteta ->
                    reteta.Nume.contains("vită", ignoreCase = true)
                }
                retete = reteteFiltrate.toMutableList()
            }
            26 -> {
                retete.clear()
                retete.addAll(transfretete("Pui.json", "pui"))
            }
            27->{
                retete.clear()
                retete.addAll(transfretete("vita.json", "vita"))
                retete.addAll(transfretete("oaie.json", "oaie"))
                retete.addAll(transfretete("porc.json", "porc"))
            }
            28 -> {
                val reteteFiltrate = retete.filter { reteta ->
                    reteta.Nume.contains("cuptor", ignoreCase = true)
                }
                retete = reteteFiltrate.toMutableList()
            }
            29 -> {
                val reteteFiltrate = retete.filterNot { reteta ->
                    reteta.Nume.contains("cuptor", ignoreCase = true)
                }
                retete = reteteFiltrate.toMutableList()
            }
        }
    }

    private fun resetFragment() {
        intrebareCurenta = intrebari.firstOrNull { it.index == 1 }
        binding.intrebare = intrebareCurenta
        retete.clear()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun transfretete(fileName: String, name: String): List<reteta> {
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
