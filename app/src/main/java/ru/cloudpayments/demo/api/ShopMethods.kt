package ru.cloudpayments.demo.api

import retrofit2.http.GET
import retrofit2.http.Header
import ru.cloudpayments.demo.models.Product

interface ShopMethods {
	@GET("products")
	suspend fun getProducts(
		@Header("Content-Type") contentType: String?,
		@Header("Authorization") authKey: String?
	): List<Product>
}