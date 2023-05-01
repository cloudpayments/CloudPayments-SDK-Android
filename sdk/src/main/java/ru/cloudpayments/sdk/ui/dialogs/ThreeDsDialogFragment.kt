package ru.cloudpayments.sdk.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.os.bundleOf
import androidx.core.view.isGone
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.google.gson.JsonParser
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import ru.cloudpayments.sdk.databinding.DialogCpsdkThreeDsBinding
import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import java.util.Locale

class ThreeDsDialogFragment : DialogFragment() {

	private var _binding: DialogCpsdkThreeDsBinding? = null

	private val binding get() = _binding!!

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		_binding = DialogCpsdkThreeDsBinding.inflate(inflater, container, false)
		val view = binding.root
		return view
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
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

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		isCancelable = false

		binding.webView.webViewClient = ThreeDsWebViewClient()
		binding.webView.settings.domStorageEnabled = true
		binding.webView.settings.javaScriptEnabled = true
		binding.webView.settings.javaScriptCanOpenWindowsAutomatically = true
		binding.webView.addJavascriptInterface(ThreeDsJavaScriptInterface(), "JavaScriptThreeDs")

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

		binding.icClose.setOnClickListener {
			setFragmentResult(RESULT_FAILED, Bundle())
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
			if (url.lowercase(Locale.getDefault()) == POST_BACK_URL.lowercase(Locale.getDefault())) {
				view.isGone = true
				view.loadUrl("javascript:window.JavaScriptThreeDs.processHTML('<head>'+document.getElementsByTagName('html')[0].innerHTML+'</head>');")
			}
		}
	}

	internal inner class ThreeDsJavaScriptInterface {
		@JavascriptInterface
		fun processHTML(html: String?) {
			if (html == null) {
				setFragmentResult(RESULT_FAILED, Bundle())
				return
			}
			val doc: Document = Jsoup.parse(html)
			val element = doc.select("body").first()
			if (element == null) {
				setFragmentResult(RESULT_FAILED, Bundle())
				return
			}
			val jsonObject = JsonParser().parse(element.ownText()).asJsonObject
			val paRes = jsonObject["PaRes"].asString
			requireActivity().runOnUiThread {
				if (!paRes.isNullOrEmpty()) {
					setFragmentResult(
						RESULT_COMPLETED,
						bundleOf(
							RESULT_COMPLETED_MD to md,
							RESULT_COMPLETED_PA_RES to paRes
						)
					)
				} else {
					setFragmentResult(RESULT_FAILED, bundleOf(RESULT_FAILED to html))
				}
				dismissAllowingStateLoss()
			}
		}
	}

	companion object {
		private const val POST_BACK_URL = "https://demo.cloudpayments.ru/WebFormPost/GetWebViewData"
		private const val ARG_ACS_URL = "acs_url"
		private const val ARG_MD = "md"
		private const val ARG_PA_REQ = "pa_req"

		const val RESULT_COMPLETED = "result_completed"
		const val RESULT_FAILED = "result_failed"
		const val RESULT_COMPLETED_MD = "result_completed_md"
		const val RESULT_COMPLETED_PA_RES = "result_completed_pa_res"

		fun newInstance(acsUrl: String, paReq: String, md: String) = ThreeDsDialogFragment().apply {
			arguments = Bundle().also {
				it.putString(ARG_ACS_URL, acsUrl)
				it.putString(ARG_MD, md)
				it.putString(ARG_PA_REQ, paReq)
			}
		}
	}
}
