package ru.cloudpayments.demo.screens.main

import android.view.ViewGroup
import kotlinx.android.synthetic.main.item_list_product.view.*
import ru.cloudpayments.demo.base.BaseAdapter
import ru.cloudpayments.demo.models.Product
import java.util.*

class ProductsAdapter : BaseAdapter<ProductHolder?>() {
	private var items: List<Product> = ArrayList<Product>()
	private var listener: OnClickListener? = null
	fun update(items: List<Product>) {
		this.items = items
		notifyDataSetChanged()
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductHolder {
		return ProductHolder.create(parent)
	}

	override fun onBindViewHolder(holder: ProductHolder, position: Int) {
		holder.bind(items[position])
		with(holder.itemView) {
			val listener = {
				listener?.onProductClick(items[position])
			}
			setOnClickListener{ listener() }
			button_add_to_cart.setOnClickListener { listener() }
		}
	}

	override fun getItemId(position: Int): Long {
		return 0
	}

	override fun getItemCount(): Int {
		return items.size
	}

	fun setListener(listener: OnClickListener?) {
		this.listener = listener
	}

	interface OnClickListener {
		fun onProductClick(item: Product?)
	}
}