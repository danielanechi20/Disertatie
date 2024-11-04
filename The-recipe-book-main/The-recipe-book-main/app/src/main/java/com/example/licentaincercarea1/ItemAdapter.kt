import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.licentaincercarea1.R
import com.example.licentaincercarea1.data.ingredient
import com.example.licentaincercarea1.data.types
import com.example.licentaincercarea1.databinding.IngrItemBinding
import com.example.licentaincercarea1.databinding.IngrSubitemBinding

class ItemAdapter(
    private val context: Context,
    private val types: List<types>,
    private val selectedIngredients: ArrayList<ingredient>
) : RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {

    private val sharedPreferences: SharedPreferences by lazy {
        context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding = IngrItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val type = types[position]
        holder.bind(type)
    }

    override fun getItemCount(): Int {
        return types.size
    }

    inner class ItemViewHolder(private val binding: IngrItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("ResourceAsColor")
        fun bind(type: types) {
            binding.denumiriGroup.removeAllViews()
            binding.tipul.text=type.tip
            for (ingredient in type.denumiri) {
                val ingrSubitemBinding =
                    IngrSubitemBinding.inflate(LayoutInflater.from(binding.root.context), binding.denumiriGroup, true)
                ingrSubitemBinding.checkbox.text = ingredient.nume
                ingrSubitemBinding.editText.hint = "Cantitate"
                ingrSubitemBinding.textView.text = ingredient.masura

                val checkboxState = sharedPreferences.getBoolean("Checkbox_${ingredient.nume}", false)
                ingrSubitemBinding.checkbox.isChecked = checkboxState
                ingrSubitemBinding.editText.setText(getQuantityFromSharedPreferences(ingredient))

                val backgroundColor = if (checkboxState) R.color.purple_200 else R.color.first
                ingrSubitemBinding.ingredientLayout.setBackgroundColor(ContextCompat.getColor(binding.root.context, backgroundColor))

                ingrSubitemBinding.checkbox.setOnCheckedChangeListener { _, isChecked ->
                    ingredient.isChecked = isChecked
                    sharedPreferences.edit().putBoolean("Checkbox_${ingredient.nume}", isChecked).apply()

                    val backgroundColor = if (isChecked) R.color.purple_200 else R.color.first
                    ingrSubitemBinding.ingredientLayout.setBackgroundColor(ContextCompat.getColor(binding.root.context, backgroundColor))

                    if (isChecked) {
                        selectedIngredients.add(ingredient)
                    } else {
                        selectedIngredients.remove(ingredient)
                    }
                    saveQuantityToSharedPreferences(ingredient, ingrSubitemBinding.editText.text.toString())
                }

                ingrSubitemBinding.editText.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                    override fun afterTextChanged(s: Editable?) {
                        val quantity = s.toString().toIntOrNull()
                        if (ingrSubitemBinding.checkbox.isChecked && quantity != null) {
                            val updatedIngredient = ingredient.copy(isChecked = true, cantitate = quantity)
                            selectedIngredients.remove(ingredient)
                            selectedIngredients.add(updatedIngredient)
                        }
                        saveQuantityToSharedPreferences(ingredient, s.toString())
                    }
                })
            }
        }

        private fun getQuantityFromSharedPreferences(ingredient: ingredient): String {
            return sharedPreferences.getString(ingredient.nume, "") ?: ""
        }

        private fun saveQuantityToSharedPreferences(ingredient: ingredient, quantity: String) {
            sharedPreferences.edit().putString(ingredient.nume, quantity).apply()
        }
    }
}
