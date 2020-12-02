package ru.cloudpayments.demo.models

import com.google.gson.annotations.SerializedName

data class Product(
		@SerializedName("id") val id: String?,
		@SerializedName("name") val name: String?,
		@SerializedName("price") val price: String?,
		@SerializedName("images") private val images: List<Image>?) {

	val imageUrl: String?
		get() = images?.firstOrNull()?.imageUrl

	data class Image(@SerializedName("src") val imageUrl: String? = null)
}