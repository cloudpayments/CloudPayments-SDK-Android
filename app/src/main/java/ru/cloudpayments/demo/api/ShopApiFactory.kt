package ru.cloudpayments.demo.api

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ShopApiFactory {
	private const val HOST = "https://wp-demo.cloudpayments.ru/index.php/wp-json/"
	private const val API_URL = "wc/v3/"
	private const val TIMEOUT = 10
	private const val WRITE_TIMEOUT = 20
	private const val CONNECT_TIMEOUT = 10
	private val LOGGING_INTERCEPTOR =
		HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
	const val API_ENDPOINT = HOST + API_URL

	// API implementations
	val shopMethods: ShopMethods
		get() = retrofit.create(ShopMethods::class.java)

	// API implementations
	private val CLIENT = OkHttpClient.Builder()
		.connectTimeout(CONNECT_TIMEOUT.toLong(), TimeUnit.SECONDS)
		.writeTimeout(WRITE_TIMEOUT.toLong(), TimeUnit.SECONDS)
		.readTimeout(TIMEOUT.toLong(), TimeUnit.SECONDS)
		.addInterceptor(LOGGING_INTERCEPTOR)
		.build()
	private val GSON = GsonBuilder()
		.setLenient()
		.create()
	private val retrofit: Retrofit
		private get() = Retrofit.Builder()
			.baseUrl(API_ENDPOINT)
			.addConverterFactory(GsonConverterFactory.create(GSON))
			.addCallAdapterFactory(RxJava2CallAdapterFactory.create())
			.client(CLIENT)
			.build()
}