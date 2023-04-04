package ru.cloudpayments.sdk.ui.dialogs

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import androidx.core.view.isInvisible
import androidx.fragment.app.viewModels
import ru.cloudpayments.sdk.R
import ru.cloudpayments.sdk.configuration.PaymentConfiguration
import ru.cloudpayments.sdk.databinding.DialogCpsdkPaymentProcessBinding
import ru.cloudpayments.sdk.models.ApiError
import ru.cloudpayments.sdk.util.InjectorUtils
import ru.cloudpayments.sdk.viewmodel.PaymentProcessViewModel
import ru.cloudpayments.sdk.viewmodel.PaymentProcessViewState

internal enum class PaymentProcessStatus {
	InProcess,
	Succeeded,
	Failed;
}

internal class PaymentProcessFragment: BasePaymentFragment<PaymentProcessViewState, PaymentProcessViewModel>(), ThreeDsDialogFragment.ThreeDSDialogListener {
	interface IPaymentProcessFragment {
		fun onPaymentFinished(transactionId: Int)
		fun onPaymentFailed(transactionId: Int, reasonCode: Int?)
		fun finishPayment()
		fun retryPayment()
	}

	companion object {
		private const val ARG_CRYPTOGRAM = "ARG_CRYPTOGRAM"
		private const val ARG_EMAIL = "ARG_EMAIL"

		fun newInstance(configuration: PaymentConfiguration, cryptogram: String, email: String?) = PaymentProcessFragment().apply {
			arguments = Bundle()
			setConfiguration(configuration)
			arguments?.putString(ARG_CRYPTOGRAM, cryptogram)
			email?.let { arguments?.putString(ARG_EMAIL, it) }
		}
	}

	private var _binding: DialogCpsdkPaymentProcessBinding? = null

	private val binding get() = _binding!!

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		_binding = DialogCpsdkPaymentProcessBinding.inflate(inflater, container, false)
		val view = binding.root
		return view
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}

	private var currentState: PaymentProcessViewState? = null

	override val viewModel: PaymentProcessViewModel by viewModels {
		InjectorUtils.providePaymentProcessViewModelFactory(
			paymentConfiguration!!.paymentData,
			cryptogram,
			email,
			paymentConfiguration!!.useDualMessagePayment)
	}

	override fun render(state: PaymentProcessViewState) {
		currentState = state
		updateWith(state.status, state.errorMessage)

		if (!state.acsUrl.isNullOrEmpty() && !state.paReq.isNullOrEmpty() && state.transaction?.transactionId != null) {
			val dialog = ThreeDsDialogFragment.newInstance(state.acsUrl, state.paReq, state.transaction.transactionId.toString())
			dialog.setTargetFragment(this, 1)
			dialog.show(parentFragmentManager, null)

			viewModel.clearThreeDsData()
		}
	}

	private val cryptogram by lazy {
		arguments?.getString(ARG_CRYPTOGRAM) ?: ""
	}

	private val email by lazy {
		arguments?.getString(ARG_EMAIL)?: ""
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		if (savedInstanceState == null) {
			activity().component.inject(viewModel)
			updateWith(PaymentProcessStatus.InProcess)

			viewModel.pay()
		}
	}

	private fun updateWith(status: PaymentProcessStatus, error: String? = null) {
		when (status) {
			PaymentProcessStatus.InProcess -> {
				val rotate = RotateAnimation(
					0f, 357f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
				rotate.duration = 1000
				rotate.interpolator = AccelerateDecelerateInterpolator()
				rotate.repeatCount = -1
				rotate.repeatMode = Animation.INFINITE
				rotate.fillAfter = true
				binding.iconStatus.startAnimation(rotate)

				binding.textStatus.setText(R.string.cpsdk_text_process_title)
				binding.buttonFinish.isInvisible = true
			}

			PaymentProcessStatus.Succeeded, PaymentProcessStatus.Failed -> {
				binding.iconStatus.clearAnimation()
				binding.iconStatus.rotation = 0f
				binding.buttonFinish.isInvisible = false

				val listener = requireActivity() as? IPaymentProcessFragment

				if (status == PaymentProcessStatus.Succeeded) {
					binding.iconStatus.setImageResource(R.drawable.cpsdk_ic_success)
					binding.textStatus.setText(R.string.cpsdk_text_process_title_success)
					binding.buttonFinish.setText(R.string.cpsdk_text_process_button_success)

					listener?.onPaymentFinished(currentState?.transaction?.transactionId ?: 0)

					binding.buttonFinish.setOnClickListener {
						close(false) {
							listener?.finishPayment()
						}
					}
				} else {

					binding.iconStatus.setImageResource(R.drawable.cpsdk_ic_failure)
					binding.textStatus.text =
						context?.let { ApiError.getFullErrorDescription(it, currentState?.reasonCode.toString()) }

					binding.buttonFinish.setText(R.string.cpsdk_text_process_button_error)

					listener?.onPaymentFailed(currentState?.transaction?.transactionId ?: 0, currentState?.reasonCode)

					binding.buttonFinish.setOnClickListener {
						close(false) {
							listener?.retryPayment()
						}
					}
				}
			}
		}
	}

	override fun onAuthorizationCompleted(md: String, paRes: String) {
		viewModel.postThreeDs(md, paRes)
	}

	override fun onAuthorizationFailed(error: String?) {
		updateWith(PaymentProcessStatus.Failed, error)
	}
}