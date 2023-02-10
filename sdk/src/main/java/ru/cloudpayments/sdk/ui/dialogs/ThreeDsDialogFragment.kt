package ru.cloudpayments.sdk.ui.dialogs

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import ru.cloudpayments.sdk.databinding.DialogCpsdkThreeDsBinding
import ru.cloudpayments.sdk.ui.view.ThreeDsWebView

class ThreeDsDialogFragment : DialogFragment() {
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

	private var _binding: DialogCpsdkThreeDsBinding? = null

	private val binding get() = _binding!!

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
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

	private var authorizationListener: ThreeDsWebView.ThreeDSAuthorizationListener? = null
	private var processListener =  object : ThreeDsWebView.ThreeDSProcessListener {
		override fun onProcessFinished() {
			dismissAllowingStateLoss()
		}
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		isCancelable = false

		binding.threeDsWebView.setThreeDSAuthorizationListener(authorizationListener)
		binding.threeDsWebView.setThreeDSProcessListener(processListener)
		binding.threeDsWebView.load(acsUrl, paReq, md,)

		binding.icClose.setOnClickListener {
			authorizationListener?.onAuthorizationFailed(null)
			dismiss()
		}
	}

	override fun onStart() {
		super.onStart()
		val window = dialog!!.window
		window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
	}

	override fun onAttach(context: Context) {
		super.onAttach(context)

		authorizationListener = targetFragment as? ThreeDsWebView.ThreeDSAuthorizationListener
		if (authorizationListener == null) {
			authorizationListener = context as? ThreeDsWebView.ThreeDSAuthorizationListener
		}
	}

	override fun onAttach(activity: Activity) {
		super.onAttach(activity)

		authorizationListener = targetFragment as? ThreeDsWebView.ThreeDSAuthorizationListener
		if (authorizationListener == null) {
			authorizationListener = activity as? ThreeDsWebView.ThreeDSAuthorizationListener
		}
	}
}
