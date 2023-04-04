package ru.cloudpayments.sdk.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import ru.cloudpayments.sdk.configuration.PaymentConfiguration
import ru.cloudpayments.sdk.databinding.DialogCpsdkPaymentOptionsBinding
import ru.cloudpayments.sdk.ui.PaymentActivity
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

	private var _binding: DialogCpsdkPaymentOptionsBinding? = null

	private val binding get() = _binding!!

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		_binding = DialogCpsdkPaymentOptionsBinding.inflate(inflater, container, false)
		return binding.root
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}

	override val viewModel: PaymentOptionsViewModel by viewModels()

	override fun render(state: PaymentOptionsViewState) {
		if ((activity as PaymentActivity).googlePayAvailable) {
			binding.buttonGooglepay.root.visibility = View.VISIBLE
		} else {
			binding.buttonGooglepay.root.visibility = View.GONE
		}
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		activity().component.inject(viewModel)

		binding.buttonClose.setOnClickListener {
			close(true)
		}

		binding.buttonGooglepay.root.setOnClickListener {
			close(false) {
				val listener = requireActivity() as? IPaymentOptionsFragment
				listener?.onGooglePayClicked()
			}
		}

		binding.buttonPayCard.setOnClickListener {
			close(false) {
				val listener = requireActivity() as? IPaymentOptionsFragment
				listener?.onCardClicked()
			}
		}
	}
}