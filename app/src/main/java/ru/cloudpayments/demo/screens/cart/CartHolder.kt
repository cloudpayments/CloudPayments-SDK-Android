package ru.cloudpayments.demo.screens.cart

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.cloudpayments.demo.R
import ru.cloudpayments.demo.databinding.ItemListCartBinding
import ru.cloudpayments.demo.models.Product

class CartHolder(private val itemListCartBinding: ItemListCartBinding) : RecyclerView.ViewHolder(itemListCartBinding.root) {
	fun bind(item: Product) {

		itemListCartBinding.textName.text = item.name
		itemListCartBinding.textPrice.text = item.price + " " + itemListCartBinding.root.context.getString(R.string.main_rub)
		itemListCartBinding.imageProduct.setImageResource(item.image)
	}

	companion object {
		fun create(parent: ViewGroup): CartHolder {
			val layoutInflater = LayoutInflater.from(parent.context)
			val itemListCartBinding = ItemListCartBinding.inflate(layoutInflater, parent, false)
			return CartHolder(itemListCartBinding)
		}
	}
}