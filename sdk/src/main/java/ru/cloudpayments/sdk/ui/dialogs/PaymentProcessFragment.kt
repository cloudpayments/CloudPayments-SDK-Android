package ru.cloudpayments.sdk.ui.dialogs

import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import kotlinx.android.synthetic.main.dialog_payment_process.*
import ru.cloudpayments.sdk.R
import ru.cloudpayments.sdk.configuration.PaymentConfiguration
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
		fun onPaymentFinished()
		fun onPaymentFailed()
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

	override val viewModel: PaymentProcessViewModel by viewModels {
		InjectorUtils.providePaymentProcessViewModelFactory(paymentConfiguration!!.paymentData, cryptogram,email)
	}

	override fun render(state: PaymentProcessViewState) {
		updateWith(state.status, state.errorMessage)

		if (!state.acsUrl.isNullOrEmpty() && !state.paReq.isNullOrEmpty() && state.transaction?.transactionId != null) {
			val dialog = ThreeDsDialogFragment.newInstance(state.acsUrl, state.paReq, state.transaction.transactionId.toString())
			dialog.setTargetFragment(this, 1)
			dialog.show(parentFragmentManager, null)

			viewModel.clearThreeDsData()
		}
	}

	override fun getLayout() = R.layout.dialog_payment_process

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

			viewModel.charge()
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
				icon_status.startAnimation(rotate)

				text_status.setText(R.string.text_process_title)
				button_finish.isInvisible = true
			}

			PaymentProcessStatus.Succeeded, PaymentProcessStatus.Failed -> {
				icon_status.clearAnimation()
				icon_status.rotation = 0f
				button_finish.isInvisible = false

				val listener = requireActivity() as? IPaymentProcessFragment

				if (status == PaymentProcessStatus.Succeeded) {
					icon_status.setImageResource(R.drawable.ic_success)
					text_status.setText(R.string.text_process_title_success)
					button_finish.setText(R.string.text_process_button_success)

					button_finish.setOnClickListener {
						close(false) {
							listener?.onPaymentFinished()
						}
					}
				} else {
					icon_status.setImageResource(R.drawable.ic_failure)
					text_status.text = error ?: getString(R.string.text_process_title_error)
					button_finish.setText(R.string.text_process_button_error)

					button_finish.setOnClickListener {
						close(false) {
							listener?.onPaymentFailed()
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