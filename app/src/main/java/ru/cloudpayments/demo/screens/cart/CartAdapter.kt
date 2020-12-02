package ru.cloudpayments.demo.screens.cart

import android.view.ViewGroup
import ru.cloudpayments.demo.base.BaseAdapter
import ru.cloudpayments.demo.models.Product
import java.util.*

class CartAdapter : BaseAdapter<CartHolder?>() {
	private var items: List<Product> = ArrayList<Product>()
	private var listener: OnClickListener? = null
	fun update(items: List<Product>) {
		this.items = items
		notifyDataSetChanged()
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartHolder {
		return CartHolder.create(parent)
	}

	override fun onBindViewHolder(holder: CartHolder, position: Int) {
		holder.bind(items[position])
		with(holder.itemView) {
			setOnClickListener{ listener?.onProductClick(items[position]) }
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