package ru.cloudpayments.sdk.dagger2

import dagger.Component
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.cloudpayments.sdk.BuildConfig
import ru.cloudpayments.sdk.Constants
import ru.cloudpayments.sdk.api.AuthenticationInterceptor
import ru.cloudpayments.sdk.api.CloudpaymentsApiService
import ru.cloudpayments.sdk.api.CloudpaymentsApi
import ru.cloudpayments.sdk.viewmodel.PaymentCardViewModel
import ru.cloudpayments.sdk.viewmodel.PaymentOptionsViewModel
import ru.cloudpayments.sdk.viewmodel.PaymentProcessViewModel
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
class CloudpaymentsModule {
	@Provides
	@Singleton
	fun provideRepository(apiService: CloudpaymentsApiService) = CloudpaymentsApi(apiService)
}

@Module
class CloudpaymentsNetModule(private val publicId: String, private var apiUrl: String = Constants.baseApiUrl) {
	@Provides
	@Singleton
	fun providesHttpLoggingInterceptor(): HttpLoggingInterceptor = HttpLoggingInterceptor()
		.apply {
			if (BuildConfig.DEBUG) {
				setLevel(HttpLoggingInterceptor.Level.BODY)
			} else {
				setLevel(HttpLoggingInterceptor.Level.NONE)
			}
		}

	@Provides
	@Singleton
	fun providesAuthenticationInterceptor(): AuthenticationInterceptor
			= AuthenticationInterceptor(publicId)

	@Provides
	@Singleton
	fun provideOkHttpClientBuilder(
		authenticationInterceptor: AuthenticationInterceptor,
		loggingInterceptor: HttpLoggingInterceptor
	): OkHttpClient = OkHttpClient.Builder()
		.addInterceptor(authenticationInterceptor)
		.addInterceptor(loggingInterceptor)
		.connectTimeout(20, TimeUnit.SECONDS)
		.readTimeout(20, TimeUnit.SECONDS)
		.followRedirects(false)
		.build()

	@Provides
	@Singleton
	fun provideApiService(
		okHttpClient: OkHttpClient
	): CloudpaymentsApiService {
		if (apiUrl.isEmpty())
			apiUrl = Constants.baseApiUrl

		val retrofit = Retrofit.Builder()
			.baseUrl(apiUrl)
			.addConverterFactory(GsonConverterFactory.create())
			.client(okHttpClient)
			.build()

		return retrofit.create(CloudpaymentsApiService::class.java)
	}
}

@Singleton
@Component(modules = [CloudpaymentsModule::class, CloudpaymentsNetModule::class])
internal interface CloudpaymentsComponent {
	fun inject(optionsViewModel: PaymentOptionsViewModel)
	fun inject(cardViewModel: PaymentCardViewModel)
	fun inject(processViewModel: PaymentProcessViewModel)
}
