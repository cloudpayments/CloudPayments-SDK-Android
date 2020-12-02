package ru.cloudpayments.sdk.ui.dialogs

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import kotlinx.android.synthetic.main.dialog_payment_options.*
import ru.cloudpayments.sdk.R
import ru.cloudpayments.sdk.configuration.PaymentConfiguration
import ru.cloudpayments.sdk.ui.PaymentActivity
import ru.cloudpayments.sdk.viewmodel.PaymentCardViewModel
import ru.cloudpayments.sdk.viewmodel.PaymentOptionsViewModel
import ru.cloudpayments.sdk.viewmodel.PaymentOptionsViewState

internal class PaymentOptionsFragment: BasePaymentFragment<PaymentOptionsViewState, PaymentOptionsViewModel>() {
	interface IPaymentOptionsFragment {
		fun onGooglePayClicked()
		fun onCardClicked()
	}

	companion object {
		fun newInstance(configuration: PaymentConfiguration) = PaymentOptionsFragment().apply {
			arguments = Bundle()
			setConfiguration(configuration)
		}
	}

	override val viewModel: PaymentOptionsViewModel by viewModels()

	override fun getLayout() = R.layout.dialog_payment_options

	override fun render(state: PaymentOptionsViewState) {

	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		activity().component.inject(viewModel)

		button_close.setOnClickListener {
			close(true)
		}

		button_googlepay.setOnClickListener {
			close(false) {
				val listener = requireActivity() as? IPaymentOptionsFragment
				listener?.onGooglePayClicked()
			}
		}

		button_pay_card.setOnClickListener {
			close(false) {
				val listener = requireActivity() as? IPaymentOptionsFragment
				listener?.onCardClicked()
			}
		}
	}
}