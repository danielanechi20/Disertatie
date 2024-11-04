package com.example.licentaincercarea1


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.licentaincercarea1.data.category
import com.example.licentaincercarea1.databinding.CategoryItemBinding

class CategoriesAdapter(val categoryList : List<category>) :
    RecyclerView.Adapter<CategoriesAdapter.CategoriesViewHolder>() {
    var listener: CategoryClickListener? = null

    fun setCategoryClickListener(categoryListener: CategoryClickListener) {
        listener = categoryListener
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
            CategoriesViewHolder {
        val binding = CategoryItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CategoriesViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoriesViewHolder, position: Int) {
        holder.bind()
    }

    override fun getItemCount(): Int {
        return categoryList.size
    }

    inner class CategoriesViewHolder(private val binding: CategoryItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            val category=categoryList[adapterPosition]
            binding.description.text = category.Description
            binding.name.text= category.Name

            Glide.with(binding.root.context).load(category.Thumb).fitCenter().into(binding.profile)
            setOnItemClickListener()
        }
        private fun setOnItemClickListener(){
            binding.root.setOnClickListener{
                listener?.onCategoryClick(categoryList[adapterPosition])

            }
        }
    }
    interface CategoryClickListener{
        fun onCategoryClick(category: category)
    }

}