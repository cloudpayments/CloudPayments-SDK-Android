package ru.cloudpayments.sdk.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.yandex.pay.core.data.*
import com.yandex.pay.core.ui.YandexPayButton
import ru.cloudpayments.sdk.configuration.PaymentConfiguration
import ru.cloudpayments.sdk.databinding.DialogPaymentOptionsBinding
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

	private var _binding: DialogPaymentOptionsBinding? = null

	private val binding get() = _binding!!

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		_binding = DialogPaymentOptionsBinding.inflate(inflater, container, false)
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

		if ((activity as PaymentActivity).yandexPayAvailable) {
			binding.buttonYandexpay.visibility = View.VISIBLE
		} else {
			binding.buttonYandexpay.visibility = View.GONE
		}
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		activity().component.inject(viewModel)

		binding.buttonClose.setOnClickListener {
			close(true)
		}

		binding.buttonYandexpay.setOnClickListener { ->

			val orderDetails = OrderDetails(
				Merchant(
					MerchantID.from(paymentConfiguration!!.yandexPayMerchantID), // Merchant ID
					"Cloud", // Merchant name to display to a user
					"https:/cp.ru/", // Merchant Origin
				),
				Order( // Order details
					OrderID.from("ORDER_ID"), // Order ID
					Amount.from(paymentConfiguration!!.paymentData.amount), // Total price for all items combined
				),
				listOf( // a list of payment methods available with your PSP
					PaymentMethod(
						listOf(AuthMethod.PanOnly), // What the payment token will contain: encrypted card details or a card token
						PaymentMethodType.Card, // Currently it's a single supported payment method: CARD
						Gateway.from("cloudpayments"), // PSP Gateway ID
						listOf(CardNetwork.Visa, CardNetwork.MasterCard, CardNetwork.MIR), // Payment networks supported by the PSP
						GatewayMerchantID.from(paymentConfiguration!!.paymentData.publicId), // Merchant ID with the PSP
					)
				)
			)

			(activity as PaymentActivity).runYandexPay(orderDetails)
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