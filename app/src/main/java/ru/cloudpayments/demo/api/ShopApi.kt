package ru.cloudpayments.demo.api

import android.util.Base64
import ru.cloudpayments.demo.models.Product
import ru.cloudpayments.demo.support.Constants
import java.io.UnsupportedEncodingException

object ShopApi {
	private const val CONTENT_TYPE = "application/json"

	private val shopAuthToken: String
		get() {
			var data = ByteArray(0)
			try {
				data =
					(Constants.CONSUMER_KEY + ":" + Constants.CONSUMER_SECRET).toByteArray(
						charset("UTF-8")
					)
			} catch (e: UnsupportedEncodingException) {
				e.printStackTrace()
			}
			return "Basic " + Base64.encodeToString(data, Base64.NO_WRAP)
		}

	suspend fun getProducts(): List<Product> {
		return ShopApiFactory.shopMethods
			.getProducts(CONTENT_TYPE, shopAuthToken)
	}
}