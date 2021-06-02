package ru.cloudpayments.sdk.configuration

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.cloudpayments.sdk.BuildConfig
import ru.cloudpayments.sdk.api.AuthenticationInterceptor
import ru.cloudpayments.sdk.api.CloudpaymentsApi
import ru.cloudpayments.sdk.api.CloudpaymentsApiService
import ru.cloudpayments.sdk.api.CloudpaymentsCardApiService
import ru.cloudpayments.sdk.ui.PaymentActivity
import java.util.concurrent.TimeUnit

interface CloudpaymentsSDK {
	fun start(configuration: PaymentConfiguration, from: AppCompatActivity, requestCode: Int)

	fun getStartIntent(context: Context, configuration: PaymentConfiguration): Intent

	companion object {
		fun getInstance(): CloudpaymentsSDK {
			return CloudpaymentsSDKImpl()
		}

		fun createApi(publicId: String, okHttpClient: OkHttpClient? = null) =
			CloudpaymentsApi(createService(publicId, okHttpClient), createCardService(okHttpClient))

		private fun createService(publicId: String, okHttpClient: OkHttpClient?): CloudpaymentsApiService {
			val retrofit = Retrofit.Builder()
				.baseUrl(BuildConfig.API_HOST)
				.addConverterFactory(GsonConverterFactory.create())
				.client(createClient(okHttpClient, publicId))
				.build()

			return retrofit.create(CloudpaymentsApiService::class.java)
		}

		private fun createCardService(okHttpClient: OkHttpClient?): CloudpaymentsCardApiService {
			val retrofit = Retrofit.Builder()
				.baseUrl("https://widget.cloudpayments.ru/Home/")
				.addConverterFactory(GsonConverterFactory.create())
				.client(createClient(okHttpClient, null))
				.build()

			return retrofit.create(CloudpaymentsCardApiService::class.java)
		}

		private fun createClient(okHttpClient: OkHttpClient?, publicId: String?): OkHttpClient {
			val builder = okHttpClient?.newBuilder() ?: OkHttpClient.Builder()

			builder
				.connectTimeout(20, TimeUnit.SECONDS)
				.readTimeout(20, TimeUnit.SECONDS)
				.followRedirects(false)
				.apply {
					if (BuildConfig.DEBUG) {
						addInterceptor(
							HttpLoggingInterceptor()
								.setLevel(HttpLoggingInterceptor.Level.BODY)
						)
					}
				}

			if (publicId != null) {
				builder.addInterceptor(AuthenticationInterceptor(publicId))
			}

			return builder.build()
		}
	}
}

internal class CloudpaymentsSDKImpl : CloudpaymentsSDK {
	override fun start(configuration: PaymentConfiguration, from: AppCompatActivity, requestCode: Int) {
		from.startActivityForResult(this.getStartIntent(from, configuration), requestCode)
	}

	override fun getStartIntent(context: Context, configuration: PaymentConfiguration): Intent {
		return PaymentActivity.getStartIntent(context, configuration)
	}
}