package ru.cloudpayments.sdk.ui.view

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.core.view.isGone
import com.google.gson.JsonParser
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import ru.cloudpayments.sdk.databinding.ViewCpsdkThreeDsWebviewBinding
import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import java.util.*

class ThreeDsWebView @JvmOverloads constructor(
	context: Context,
	attrs: AttributeSet? = null,
	defStyleAttr: Int = -1
) : FrameLayout(context, attrs, defStyleAttr) {

	interface ThreeDSAuthorizationListener {
		fun onAuthorizationCompleted(md: String, paRes: String)
		fun onAuthorizationFailed(error: String?)
	}

	internal interface ThreeDSProcessListener {
		fun onProcessFinished()
	}

	private var _binding: ViewCpsdkThreeDsWebviewBinding? = null
	private val binding get() = _binding!!

	private var authorizationListener: ThreeDSAuthorizationListener? = null
	private var processListener: ThreeDSProcessListener? = null

	init {
		_binding = ViewCpsdkThreeDsWebviewBinding.inflate(LayoutInflater.from(context), this, true)
	}

	fun setThreeDSAuthorizationListener(listener: ThreeDSAuthorizationListener?) {
		this.authorizationListener = listener
	}

	internal fun setThreeDSProcessListener(listener: ThreeDSProcessListener?) {
		this.processListener = listener
	}

	fun load(acsUrl: String, paReq: String, md: String) {

		binding.webView.webViewClient = ThreeDsWebViewClient()
		binding.webView.settings.domStorageEnabled = true
		binding.webView.settings.javaScriptEnabled = true
		binding.webView.settings.javaScriptCanOpenWindowsAutomatically = true

		val javaScriptInterface = ThreeDsJavaScriptInterface(md)
		binding.webView.addJavascriptInterface(javaScriptInterface, "JavaScriptThreeDs")

		try {
			val params = StringBuilder()
				.append("PaReq=").append(URLEncoder.encode(paReq, "UTF-8"))
				.append("&MD=").append(URLEncoder.encode(md, "UTF-8"))
				.append("&TermUrl=").append(URLEncoder.encode(POST_BACK_URL, "UTF-8"))
				.toString()
			binding.webView.postUrl(acsUrl, params.toByteArray())
		} catch (e: UnsupportedEncodingException) {
			e.printStackTrace()
		}
	}

	private inner class ThreeDsWebViewClient : WebViewClient() {
		override fun onPageFinished(view: WebView, url: String) {
			if (url.toLowerCase(Locale.getDefault()) == POST_BACK_URL.toLowerCase(Locale.getDefault())) {
				view.isGone = true
				view.loadUrl("javascript:window.JavaScriptThreeDs.processHTML('<head>'+document.getElementsByTagName('html')[0].innerHTML+'</head>');")
			}
		}
	}

	internal inner class ThreeDsJavaScriptInterface(private val md: String) {
		@JavascriptInterface
		fun processHTML(html: String?) {
			val doc: Document = Jsoup.parse(html)
			val element: Element = doc.select("body").first()
			val jsonObject = JsonParser().parse(element.ownText()).asJsonObject
			val paRes = jsonObject["PaRes"].asString

			this@ThreeDsWebView.post {
				if (!paRes.isNullOrEmpty()) {
					authorizationListener?.onAuthorizationCompleted(md, paRes)
				} else {
					Log.e("ERROR", html ?: "empty")
					authorizationListener?.onAuthorizationFailed(html ?: "")
				}
				processListener?.onProcessFinished()
			}
		}
	}

	companion object {
		private const val POST_BACK_URL = "https://demo.cloudpayments.ru/WebFormPost/GetWebViewData"
	}
}