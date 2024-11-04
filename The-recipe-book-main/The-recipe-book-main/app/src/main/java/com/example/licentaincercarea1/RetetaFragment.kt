package com.example.licentaincercarea1

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.licentaincercarea1.data.reteta
import com.example.licentaincercarea1.databinding.RetetaBinding
import java.io.File
import java.io.PrintWriter

class RetetaFragment : Fragment() {
    private var _binding: RetetaBinding? = null
    private val binding get() = _binding!!
    private var numarPortii = 1
    private lateinit var drawerLayout: DrawerLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = RetetaBinding.inflate(inflater, container, false)

        // Initialize DrawerLayout
        drawerLayout = binding.root.findViewById(R.id.drawer_layout)

        // Load recipe data if available
        val reteta = arguments?.getParcelable<reteta>("reteta")
        if (reteta != null) {
            val ingrediente = StringBuilder()
            for (ingredient in reteta.ingrediente) {
                ingrediente.append(ingredient.cantitate)
                ingrediente.append(" ")
                ingrediente.append(ingredient.masura)
                ingrediente.append(" ")
                ingrediente.append(ingredient.nume)
                ingrediente.append("\n")
            }
            binding.Nume.text = reteta.Nume
            binding.ingrediente.text = ingrediente.toString()
            binding.preparare.text = reteta.P
            Glide.with(binding.root.context).load(reteta.Thumb).fitCenter().into(binding.imagine)
        }

        // Set initial serving size
        binding.portii.text = numarPortii.toString()

        // Handle increase/decrease buttons for servings
        binding.increase.setOnClickListener {
            binding.portii.text = (binding.portii.text.toString().toInt() + 1).toString()
            numarPortii = binding.portii.text.toString().toInt()
            reteta?.let { Actualizare(it) }
        }

        binding.decrease.setOnClickListener {
            if (binding.portii.text.toString().toInt() > 1) {
                binding.portii.text = (binding.portii.text.toString().toInt() - 1).toString()
                numarPortii = binding.portii.text.toString().toInt()
                reteta?.let { Actualizare(it) }
            } else {
                Toast.makeText(context, "Numarul de portii nu poate fi mai mic decat 1", Toast.LENGTH_SHORT).show()
            }
        }

        // Open Left Drawer (Ingredients) when the label is clicked
        binding.labelIngredients.setOnClickListener {
            drawerLayout.openDrawer(Gravity.LEFT)
        }

        // Open Right Drawer (Preparation) when the label is clicked
        binding.labelPreparation.setOnClickListener {
            drawerLayout.openDrawer(Gravity.RIGHT)
        }

        return binding.root
    }

    private fun Actualizare(reteta: reteta) {
        val ingrediente = StringBuilder()
        for (ingredient in reteta.ingrediente) {
            val nouaCantitate = ingredient.cantitate * numarPortii
            ingrediente.append(nouaCantitate.toString())
            ingrediente.append(" ")
            ingrediente.append(ingredient.masura)
            ingrediente.append(" ")
            ingrediente.append(ingredient.nume)
            ingrediente.append("\n")
        }
        binding.ingrediente.text = ingrediente.toString()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

