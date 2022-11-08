package ru.cloudpayments.sdk.configuration

import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.yandex.pay.core.YandexPayEnvironment
import com.yandex.pay.core.YandexPayLib
import com.yandex.pay.core.YandexPayLibConfig
import com.yandex.pay.core.YandexPayLocale
import com.yandex.pay.core.data.Merchant
import com.yandex.pay.core.data.MerchantId
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import ru.cloudpayments.sdk.BuildConfig
import ru.cloudpayments.sdk.api.AuthenticationInterceptor
import ru.cloudpayments.sdk.api.CloudpaymentsApiService
import ru.cloudpayments.sdk.api.CloudpaymentsApi
import ru.cloudpayments.sdk.models.Transaction
import ru.cloudpayments.sdk.ui.PaymentActivity
import java.util.concurrent.TimeUnit

interface CloudpaymentsSDK {
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

		fun initialize(context: Context, yandexPayAppId: String, yandexPaySandboxMode: Boolean) {
			if (YandexPayLib.isSupported) {
				YandexPayLib.initialize(
					context = context,
					config = YandexPayLibConfig(
						merchantDetails = Merchant(
							id = MerchantId.from(yandexPayAppId),
							name = "Cloud",
							url = "https://cp.ru/",
						),
						environment = if (yandexPaySandboxMode) YandexPayEnvironment.SANDBOX else YandexPayEnvironment.PROD,
						locale = YandexPayLocale.SYSTEM,
						logging = false
					)
				)
			}
		}

		fun getInstance(): CloudpaymentsSDK {
			return CloudpaymentsSDKImpl()
		}

		fun createApi(publicId: String) = CloudpaymentsApi(createService(publicId))

		private fun createService(publicId: String): CloudpaymentsApiService {
			val retrofit = Retrofit.Builder()
				.baseUrl(BuildConfig.API_HOST)
				.addConverterFactory(GsonConverterFactory.create())
				.addCallAdapterFactory(RxJava2CallAdapterFactory.create())
				.client(createClient(publicId))
				.build()

			return retrofit.create(CloudpaymentsApiService::class.java)
		}

		private fun createClient(publicId: String?): OkHttpClient {
			val okHttpClientBuilder = OkHttpClient.Builder()
					.addInterceptor(HttpLoggingInterceptor()
											.setLevel(HttpLoggingInterceptor.Level.BODY))
			val client = okHttpClientBuilder
					.connectTimeout(20, TimeUnit.SECONDS)
					.readTimeout(20, TimeUnit.SECONDS)
					.followRedirects(false)

			if (publicId != null){
				client.addInterceptor(AuthenticationInterceptor(publicId))
			}

			return client.build()
		}
	}
}

internal class CloudpaymentsSDKImpl: CloudpaymentsSDK {
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