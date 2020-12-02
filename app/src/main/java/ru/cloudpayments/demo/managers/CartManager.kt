package ru.cloudpayments.demo.managers

import ru.cloudpayments.demo.models.Product
import kotlin.collections.ArrayList

class CartManager private constructor() {
	private var products: ArrayList<Product> = arrayListOf()

	fun clear() {
		products.clear()
	}

	fun getProducts() = products

	fun setProducts(products: ArrayList<Product>?) {
		this.products = ArrayList(products.orEmpty())
	}

	fun addProduct(product: Product) {
		products.add(product)
	}

	companion object {
		private var instance: CartManager? = null
		@Synchronized fun getInstance(): CartManager? {
			if (instance == null) {
				synchronized(CartManager::class.java) { instance = CartManager() }
			}
			return instance
		}
	}
}