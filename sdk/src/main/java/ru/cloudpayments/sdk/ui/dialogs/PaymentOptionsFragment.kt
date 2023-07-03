package ru.cloudpayments.sdk.ui.dialogs

import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.fragment.app.viewModels
import com.google.android.material.checkbox.MaterialCheckBox
import com.yandex.pay.core.data.Amount
import com.yandex.pay.core.data.AuthMethod
import com.yandex.pay.core.data.CardNetwork
import com.yandex.pay.core.data.Gateway
import com.yandex.pay.core.data.GatewayMerchantID
import com.yandex.pay.core.data.Order
import com.yandex.pay.core.data.OrderDetails
import com.yandex.pay.core.data.OrderID
import com.yandex.pay.core.data.PaymentMethod
import com.yandex.pay.core.data.PaymentMethodType
import ru.cloudpayments.sdk.databinding.DialogCpsdkPaymentOptionsBinding
import ru.cloudpayments.sdk.ui.PaymentActivity
import ru.cloudpayments.sdk.ui.dialogs.base.BasePaymentBottomSheetFragment
import ru.cloudpayments.sdk.util.PublicKey
import ru.cloudpayments.sdk.util.TextWatcherAdapter
import ru.cloudpayments.sdk.util.emailIsValid
import ru.cloudpayments.sdk.util.hideKeyboard
import ru.cloudpayments.sdk.viewmodel.PaymentOptionsViewModel
import ru.cloudpayments.sdk.viewmodel.PaymentOptionsViewState


internal class PaymentOptionsFragment :
	BasePaymentBottomSheetFragment<PaymentOptionsViewState, PaymentOptionsViewModel>() {
	interface IPaymentOptionsFragment {
		fun onGooglePayClicked()
		fun onCardClicked()
	}

	companion object {
		fun newInstance() = PaymentOptionsFragment().apply {
			arguments = Bundle()
		}
	}

	private var _binding: DialogCpsdkPaymentOptionsBinding? = null

	private val binding get() = _binding!!

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
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

		if ((activity as PaymentActivity).yandexPayAvailable) {
			binding.buttonYandexpay.visibility = View.VISIBLE
		} else {
			binding.buttonYandexpay.visibility = View.GONE
		}

		if (state.publicKeyPem != null && state.publicKeyVersion != null ) {
			context?.let {
				PublicKey.getInstance(it).savePem(state.publicKeyPem)
				PublicKey.getInstance(it).saveVersion(state.publicKeyVersion)
			}
		}
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		activity().component.inject(viewModel)

		binding.editEmail.setText(paymentConfiguration!!.paymentData.email)

		binding.editEmail.setOnFocusChangeListener { _, hasFocus ->
			errorMode(
				!hasFocus && !emailIsValid(binding.editEmail.text.toString()),
				binding.editEmail, binding.textFieldEmail
			)
		}

		binding.editEmail.addTextChangedListener(object : TextWatcherAdapter() {
			override fun afterTextChanged(s: Editable?) {
				super.afterTextChanged(s)
				updateStateButtons()
			}
		})

		if (paymentConfiguration!!.requireEmail) {
			binding.checkboxSendReceipt.visibility = View.GONE
			binding.textFieldEmail.visibility = View.VISIBLE
			binding.textEmailRequire.visibility = View.VISIBLE
		} else {
			binding.checkboxSendReceipt.visibility = View.VISIBLE
			if (paymentConfiguration!!.paymentData.email.isNullOrEmpty()) {
				binding.checkboxSendReceipt.checkedState = MaterialCheckBox.STATE_UNCHECKED
				binding.textFieldEmail.visibility = View.GONE
			} else {
				binding.checkboxSendReceipt.checkedState = MaterialCheckBox.STATE_CHECKED
				binding.textFieldEmail.visibility = View.VISIBLE
			}
			binding.textEmailRequire.visibility = View.GONE
		}

		binding.checkboxSendReceipt.setOnCheckedChangeListener { _, isChecked ->
			binding.textFieldEmail.isGone = !isChecked
			requireActivity().hideKeyboard()
			updateStateButtons()
		}

		updateStateButtons()

		binding.buttonPayCard.setOnClickListener {

			if (paymentConfiguration!!.requireEmail || binding.checkboxSendReceipt.isChecked) {
				paymentConfiguration?.paymentData?.email = binding.editEmail.text.toString()
			} else {
				paymentConfiguration?.paymentData?.email = ""
			}

			val listener = requireActivity() as? IPaymentOptionsFragment
			listener?.onCardClicked()
			dismiss()
		}

		binding.buttonYandexpay.setOnClickListener { ->

			val orderDetails = OrderDetails(
				Order(
					// Order details
					OrderID.from("ORDER_ID"), // Order ID
					Amount.from(paymentConfiguration!!.paymentData.amount), // Total price for all items combined
				),
				listOf( // a list of payment methods available with your PSP
					PaymentMethod(
						listOf(AuthMethod.PanOnly), // What the payment token will contain: encrypted card details or a card token
						PaymentMethodType.Card, // Currently it's a single supported payment method: CARD
						Gateway.from("cloudpayments"), // PSP Gateway ID
						listOf(
							CardNetwork.Visa,
							CardNetwork.MasterCard,
							CardNetwork.MIR
						), // Payment networks supported by the PSP
						GatewayMerchantID.from(paymentConfiguration!!.publicId), // Merchant ID with the PSP
					)
				)
			)

			(activity as PaymentActivity).runYandexPay(orderDetails)
			dismiss()
		}

		binding.buttonGooglepay.root.setOnClickListener {
			val listener = requireActivity() as? IPaymentOptionsFragment
			listener?.onGooglePayClicked()
			dismiss()
		}

		viewModel.getPublicKey()
	}

	private fun updateStateButtons() {
		if (paymentConfiguration!!.requireEmail) {
			if (isValid()) {
				enableButtons()
			} else {
				disableButtons()
			}
		} else {
			if (binding.checkboxSendReceipt.checkedState == MaterialCheckBox.STATE_CHECKED) {
				if (isValid()) {
					enableButtons()
				} else {
					disableButtons()
				}
			} else {
				enableButtons()
			}
		}
	}
	private fun isValid(): Boolean {

		val valid = if (paymentConfiguration!!.requireEmail) {
			emailIsValid(binding.editEmail.text.toString())
		} else {
			!binding.checkboxSendReceipt.isChecked || emailIsValid(binding.editEmail.text.toString())
		}

		return valid
	}

	private fun disableButtons() {
		binding.viewBlockButtons.visibility = View.VISIBLE
	}

	private fun enableButtons() {
		binding.viewBlockButtons.visibility = View.GONE
	}
}