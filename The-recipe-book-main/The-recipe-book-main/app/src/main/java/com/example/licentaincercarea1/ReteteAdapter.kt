package com.example.licentaincercarea1

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.licentaincercarea1.data.ingredient
import com.example.licentaincercarea1.data.reteta
import com.example.licentaincercarea1.databinding.ReteteItemBinding

class ReteteAdapter(val retete:List<reteta>) :
    RecyclerView.Adapter<ReteteAdapter.ReteteViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
            ReteteViewHolder {
        val binding = ReteteItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ReteteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReteteAdapter.ReteteViewHolder, position: Int) {
        holder.bind()
    }

    override fun getItemCount(): Int {
        return retete.size
    }

    inner class ReteteViewHolder(private val binding: ReteteItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind() {
            val reteta=retete[adapterPosition]
            binding.name.text=reteta.Nume
            Glide.with(binding.root.context).load(reteta.Thumb).fitCenter().into(binding.imagine)
            val missingIngredients = reteta.lipsa.joinToString { it.nume }
            binding.lipsa.text = missingIngredients
            setOnItemClickListener()
        }
        private fun setOnItemClickListener() {
            val reteta=retete[adapterPosition]
            binding.root.setOnClickListener {
                val bundle = Bundle().apply {
                    putParcelable("reteta", reteta)
                }
                it.findNavController().navigate(R.id.retetaFragment, bundle)
            }
        }
    }
}