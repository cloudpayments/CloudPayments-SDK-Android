package ru.cloudpayments.sdk.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isInvisible
import androidx.fragment.app.viewModels
import ru.cloudpayments.sdk.R
import ru.cloudpayments.sdk.databinding.DialogCpsdkPaymentProcessBinding
import ru.cloudpayments.sdk.models.ApiError
import ru.cloudpayments.sdk.ui.dialogs.base.BasePaymentDialogFragment
import ru.cloudpayments.sdk.util.InjectorUtils
import ru.cloudpayments.sdk.viewmodel.PaymentProcessViewModel
import ru.cloudpayments.sdk.viewmodel.PaymentProcessViewState

internal enum class PaymentProcessStatus {
	InProcess,
	Succeeded,
	Failed;
}

internal class PaymentProcessFragment: BasePaymentDialogFragment<PaymentProcessViewState, PaymentProcessViewModel>(), ThreeDsDialogFragment.ThreeDSDialogListener {
	interface IPaymentProcessFragment {
		fun onPaymentFinished(transactionId: Int)
		fun onPaymentFailed(transactionId: Int, reasonCode: Int?)
		fun finishPayment()
		fun retryPayment()
	}

	companion object {
		private const val ARG_CRYPTOGRAM = "ARG_CRYPTOGRAM"

		fun newInstance(cryptogram: String) = PaymentProcessFragment().apply {
			arguments = Bundle()
			arguments?.putString(ARG_CRYPTOGRAM, cryptogram)
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
		return binding.root
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
				binding.iconStatus.setImageResource(R.drawable.cpsdk_ic_progress)
				binding.textStatus.setText(R.string.cpsdk_text_process_title)
				binding.buttonFinish.isInvisible = true
			}

			PaymentProcessStatus.Succeeded, PaymentProcessStatus.Failed -> {
				binding.buttonFinish.isInvisible = false

				val listener = requireActivity() as? IPaymentProcessFragment

				if (status == PaymentProcessStatus.Succeeded) {
					binding.iconStatus.setImageResource(R.drawable.cpsdk_ic_success)
					binding.textStatus.setText(R.string.cpsdk_text_process_title_success)
					binding.buttonFinish.setText(R.string.cpsdk_text_process_button_success)

					listener?.onPaymentFinished(currentState?.transaction?.transactionId ?: 0)

					binding.buttonFinish.setOnClickListener {

						listener?.finishPayment()
						dismiss()
					}
				} else {

					binding.iconStatus.setImageResource(R.drawable.cpsdk_ic_failure)
					binding.textStatus.text =
						context?.let { ApiError.getErrorDescription(it, currentState?.reasonCode.toString()) }
					binding.textDescription.text =
						context?.let { ApiError.getErrorDescriptionExtra(it, currentState?.reasonCode.toString()) }

					binding.buttonFinish.setText(R.string.cpsdk_text_process_button_error)

					listener?.onPaymentFailed(currentState?.transaction?.transactionId ?: 0, currentState?.reasonCode)

					binding.buttonFinish.setOnClickListener {

						listener?.retryPayment()
						dismiss()
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