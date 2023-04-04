package ru.cloudpayments.sdk.dagger2

import dagger.Component
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
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
	fun provideRepository(apiService: CloudpaymentsApiService)
			= CloudpaymentsApi(apiService)
}

@Module
class CloudpaymentsNetModule(private val publicId: String, private var apiUrl: String = Constants.baseApiUrl) {
	@Provides
	@Singleton
	fun providesHttpLoggingInterceptor(): HttpLoggingInterceptor = HttpLoggingInterceptor()
		.setLevel(HttpLoggingInterceptor.Level.BODY)

	@Provides
	@Singleton
	fun providesAuthenticationInterceptor(): AuthenticationInterceptor
			= AuthenticationInterceptor(publicId)

	@Provides
	@Singleton
	fun provideOkHttpClientBuilder(loggingInterceptor: HttpLoggingInterceptor): OkHttpClient.Builder
			= OkHttpClient.Builder()
		.addInterceptor(loggingInterceptor)

	@Provides
	@Singleton
	fun provideApiService(okHttpClientBuilder: OkHttpClient.Builder,
						  authenticationInterceptor: AuthenticationInterceptor): CloudpaymentsApiService {
		val client = okHttpClientBuilder
			.addInterceptor(authenticationInterceptor)
			.connectTimeout(20, TimeUnit.SECONDS)
			.readTimeout(20, TimeUnit.SECONDS)
			.followRedirects(false)
			.build()

		if (apiUrl.isEmpty())
			apiUrl = Constants.baseApiUrl

		val retrofit = Retrofit.Builder()
			.baseUrl(apiUrl)
			.addConverterFactory(GsonConverterFactory.create())
			.client(client)
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
