package ru.cloudpayments.sdk.api

import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

class AuthenticationInterceptor (private val publicId: String) : Interceptor {

	@Throws(IOException::class)
	override fun intercept(chain: Interceptor.Chain): Response {
		val original: Request = chain.request()
		val originalHttpUrl: HttpUrl = original.url

		val builder = originalHttpUrl.newBuilder()
		builder.addQueryParameter("publicId", publicId)

		val url = builder.build()

		val requestBuilder: Request.Builder = original.newBuilder()
			.url(url)

		val request: Request = requestBuilder.build()
		return chain.proceed(request)
	}
}
