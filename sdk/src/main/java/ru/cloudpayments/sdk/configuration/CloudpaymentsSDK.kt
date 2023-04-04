package ru.cloudpayments.sdk.configuration

import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.cloudpayments.sdk.BuildConfig
import ru.cloudpayments.sdk.Constants
import ru.cloudpayments.sdk.api.AuthenticationInterceptor
import ru.cloudpayments.sdk.api.CloudpaymentsApi
import ru.cloudpayments.sdk.api.CloudpaymentsApiService
import ru.cloudpayments.sdk.models.Transaction
import ru.cloudpayments.sdk.ui.PaymentActivity
import java.util.concurrent.TimeUnit

interface CloudpaymentsSDK {
	@Deprecated(
		message = "Please use [CloudpaymentsSDK.getStartIntent] with [ActivityResultCaller] API"
	)
	fun start(configuration: PaymentConfiguration, from: AppCompatActivity, requestCode: Int)
	fun launcher(from: AppCompatActivity, result: (Transaction) -> Unit) : ActivityResultLauncher<PaymentConfiguration>
	fun launcher(from: FragmentActivity, result: (Transaction) -> Unit) : ActivityResultLauncher<PaymentConfiguration>
	fun launcher(from: Fragment, result: (Transaction) -> Unit) : ActivityResultLauncher<PaymentConfiguration>

	fun getStartIntent(context: Context, configuration: PaymentConfiguration): Intent

	enum class TransactionStatus {
		Succeeded,
		Failed;
	}
	enum class IntentKeys {
		TransactionId,
		TransactionStatus,
		TransactionReasonCode;
	}

	companion object {

		fun getInstance(): CloudpaymentsSDK {
			return CloudpaymentsSDKImpl()
		}

		fun createApi(publicId: String, okHttpClient: OkHttpClient? = null) =
			CloudpaymentsApi(createService(publicId, okHttpClient))

		private fun createService(publicId: String, okHttpClient: OkHttpClient?): CloudpaymentsApiService {
			val retrofit = Retrofit.Builder()
				.baseUrl(Constants.baseApiUrl)
				.addConverterFactory(GsonConverterFactory.create())
				.client(createClient(okHttpClient, publicId))
				.build()

			return retrofit.create(CloudpaymentsApiService::class.java)
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

@Suppress("OverridingDeprecatedMember", "DEPRECATION")
internal class CloudpaymentsSDKImpl : CloudpaymentsSDK {
	override fun start(configuration: PaymentConfiguration, from: AppCompatActivity, requestCode: Int) {
		from.startActivityForResult(this.getStartIntent(from, configuration), requestCode)
	}

	override fun launcher(
		from: AppCompatActivity,
		result: (Transaction) -> Unit): ActivityResultLauncher<PaymentConfiguration> {
		return from.registerForActivityResult(CloudPaymentsIntentSender(), result)
	}

	override fun launcher(
		from: FragmentActivity,
		result: (Transaction) -> Unit): ActivityResultLauncher<PaymentConfiguration> {
		return from.registerForActivityResult(CloudPaymentsIntentSender(), result)
	}

	override fun launcher(
		from: Fragment,
		result: (Transaction) -> Unit
	): ActivityResultLauncher<PaymentConfiguration> {
		return from.registerForActivityResult(CloudPaymentsIntentSender(), result)
	}

	override fun getStartIntent(context: Context, configuration: PaymentConfiguration): Intent {
		return PaymentActivity.getStartIntent(context, configuration)
	}
}