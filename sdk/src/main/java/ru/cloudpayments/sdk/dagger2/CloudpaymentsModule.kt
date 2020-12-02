package ru.cloudpayments.sdk.dagger2

import dagger.Component
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import ru.cloudpayments.sdk.BuildConfig
import ru.cloudpayments.sdk.api.AuthenticationInterceptor
import ru.cloudpayments.sdk.api.CloudpaymentsApiService
import ru.cloudpayments.sdk.api.CloudpaymentsApi
import ru.cloudpayments.sdk.api.CloudpaymentsCardApiService
import ru.cloudpayments.sdk.viewmodel.PaymentCardViewModel
import ru.cloudpayments.sdk.viewmodel.PaymentOptionsViewModel
import ru.cloudpayments.sdk.viewmodel.PaymentProcessViewModel
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
class CloudpaymentsModule {
	@Provides
	@Singleton
	fun provideRepository(apiService: CloudpaymentsApiService, cardApiService: CloudpaymentsCardApiService)
			= CloudpaymentsApi(apiService, cardApiService)
}

@Module
class CloudpaymentsNetModule(private val publicId: String) {
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

		val retrofit = Retrofit.Builder()
			.baseUrl(BuildConfig.API_HOST)
			.addConverterFactory(GsonConverterFactory.create())
			.addCallAdapterFactory(RxJava2CallAdapterFactory.create())
			.client(client)
			.build()

		return retrofit.create(CloudpaymentsApiService::class.java)
	}

	@Provides
	@Singleton
	fun provideCardApiService(okHttpClientBuilder: OkHttpClient.Builder): CloudpaymentsCardApiService {
		val client = okHttpClientBuilder
				.connectTimeout(20, TimeUnit.SECONDS)
				.readTimeout(20, TimeUnit.SECONDS)
				.build()

		val retrofit = Retrofit.Builder()
				.baseUrl("https://widget.cloudpayments.ru/Home/")
				.addConverterFactory(GsonConverterFactory.create())
				.addCallAdapterFactory(RxJava2CallAdapterFactory.create())
				.client(client)
				.build()

		return retrofit.create(CloudpaymentsCardApiService::class.java)
	}
}

@Singleton
@Component(modules = [CloudpaymentsModule::class, CloudpaymentsNetModule::class])
internal interface CloudpaymentsComponent {
	fun inject(optionsViewModel: PaymentOptionsViewModel)
	fun inject(cardViewModel: PaymentCardViewModel)
	fun inject(processViewModel: PaymentProcessViewModel)
}
