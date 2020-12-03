package ru.cloudpayments.sdk.ui.dialogs

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.view.isGone
import androidx.fragment.app.DialogFragment
import com.google.gson.JsonParser
import kotlinx.android.synthetic.main.dialog_three_ds.*
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import ru.cloudpayments.sdk.R
import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import java.util.*

class ThreeDsDialogFragment : DialogFragment() {
	interface ThreeDSDialogListener {
		fun onAuthorizationCompleted(md: String, paRes: String)
		fun onAuthorizationFailed(error: String?)
	}

	companion object {
		private const val POST_BACK_URL = "https://demo.cloudpayments.ru/WebFormPost/GetWebViewData"
		private const val ARG_ACS_URL = "acs_url"
		private const val ARG_MD = "md"
		private const val ARG_PA_REQ = "pa_req"

		fun newInstance(acsUrl: String, paReq: String, md: String) = ThreeDsDialogFragment().apply {
			arguments = Bundle().also {
				it.putString(ARG_ACS_URL, acsUrl)
				it.putString(ARG_MD, md)
				it.putString(ARG_PA_REQ, paReq)
			}
		}
	}
	private val acsUrl by lazy {
		requireArguments().getString(ARG_ACS_URL) ?: ""
	}

	private val md by lazy {
		requireArguments().getString(ARG_MD) ?: ""
	}

	private val paReq by lazy {
		requireArguments().getString(ARG_PA_REQ) ?: ""
	}

	private var listener: ThreeDSDialogListener? = null

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		return inflater.inflate(R.layout.dialog_three_ds, container, false)
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		isCancelable = false

		web_view.webViewClient = ThreeDsWebViewClient()
		web_view.settings.domStorageEnabled = true
		web_view.settings.javaScriptEnabled = true
		web_view.settings.javaScriptCanOpenWindowsAutomatically = true
		web_view.addJavascriptInterface(ThreeDsJavaScriptInterface(), "JavaScriptThreeDs")

		try {
			val params = StringBuilder()
					.append("PaReq=").append(URLEncoder.encode(paReq, "UTF-8"))
					.append("&MD=").append(URLEncoder.encode(md, "UTF-8"))
					.append("&TermUrl=").append(URLEncoder.encode(POST_BACK_URL, "UTF-8"))
					.toString()
			web_view.postUrl(acsUrl, params.toByteArray())
		} catch (e: UnsupportedEncodingException) {
			e.printStackTrace()
		}

		ic_close.setOnClickListener {
			listener?.onAuthorizationFailed(null)
			dismiss()
		}
	}

	override fun onStart() {
		super.onStart()
		val window = dialog!!.window
		window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
	}

	private inner class ThreeDsWebViewClient : WebViewClient() {
		override fun onPageFinished(view: WebView, url: String) {
			if (url.toLowerCase(Locale.getDefault()) == POST_BACK_URL.toLowerCase(Locale.getDefault())) {
				view.isGone = true
				view.loadUrl("javascript:window.JavaScriptThreeDs.processHTML('<head>'+document.getElementsByTagName('html')[0].innerHTML+'</head>');")
			}
		}
	}

	internal inner class ThreeDsJavaScriptInterface {
		@JavascriptInterface
		fun processHTML(html: String?) {
			val doc: Document = Jsoup.parse(html)
			val element: Element = doc.select("body").first()
			val jsonObject = JsonParser().parse(element.ownText()).asJsonObject
			val paRes = jsonObject["PaRes"].asString
			requireActivity().runOnUiThread {
				if (!paRes.isNullOrEmpty()) {
					listener?.onAuthorizationCompleted(md, paRes)
				} else {
					listener?.onAuthorizationFailed(html ?: "")
				}
				dismiss()
			}
		}
	}

	override fun onAttach(context: Context) {
		super.onAttach(context)

		listener = targetFragment as? ThreeDSDialogListener
		if (listener == null) {
			listener = context as? ThreeDSDialogListener
		}
	}

	override fun onAttach(activity: Activity) {
		super.onAttach(activity)

		listener = targetFragment as? ThreeDSDialogListener
		if (listener == null) {
			listener = activity as? ThreeDSDialogListener
		}
	}
}
