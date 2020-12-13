package ru.cloudpayments.sdk.ui.dialogs

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.google.android.material.textfield.TextInputEditText
import kotlinx.android.synthetic.main.dialog_payment_card.*
import ru.cloudpayments.sdk.R
import ru.cloudpayments.sdk.card.Card
import ru.cloudpayments.sdk.card.CardType
import ru.cloudpayments.sdk.configuration.PaymentConfiguration
import ru.cloudpayments.sdk.scanner.CardData
import ru.cloudpayments.sdk.util.TextWatcherAdapter
import ru.cloudpayments.sdk.util.emailIsValid
import ru.cloudpayments.sdk.util.getCurrencyString
import ru.cloudpayments.sdk.util.hideKeyboard
import ru.cloudpayments.sdk.viewmodel.PaymentCardViewModel
import ru.cloudpayments.sdk.viewmodel.PaymentCardViewState
import ru.tinkoff.decoro.MaskDescriptor
import ru.tinkoff.decoro.parser.UnderscoreDigitSlotsParser
import ru.tinkoff.decoro.watchers.DescriptorFormatWatcher

internal class PaymentCardFragment: BasePaymentFragment<PaymentCardViewState, PaymentCardViewModel>() {
	interface IPaymentCardFragment {
		fun onPayClicked(cryptogram: String, email: String?)
	}

	companion object {
		const val REQUEST_CODE_SCANNER = 1

		fun newInstance(configuration: PaymentConfiguration) = PaymentCardFragment().apply {
			arguments = Bundle()
			setConfiguration(configuration)
		}
	}

	override fun getLayout() = R.layout.dialog_payment_card

	override val viewModel: PaymentCardViewModel by viewModels()

	override fun render(state: PaymentCardViewState) {
	}

	private val cardNumberFormatWatcher by lazy {
		val descriptor = MaskDescriptor.ofRawMask("____ ____ ____ ____ ___")
			.setTerminated(true)
			.setForbidInputWhenFilled(true)

		DescriptorFormatWatcher(UnderscoreDigitSlotsParser(), descriptor)
	}

	private val cardExpFormatWatcher by lazy {
		val descriptor = MaskDescriptor.ofRawMask("__/__")
			.setTerminated(true)
			.setForbidInputWhenFilled(true)

		DescriptorFormatWatcher(UnderscoreDigitSlotsParser(), descriptor)
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		activity().component.inject(viewModel)

		checkbox_receipt.setOnCheckedChangeListener { _, isChecked ->
			til_email.isGone = !isChecked
			requireActivity().hideKeyboard()
		}

		cardNumberFormatWatcher.installOn(edit_card_number)
		cardExpFormatWatcher.installOn(edit_card_exp)

		edit_card_number.addTextChangedListener(object : TextWatcherAdapter() {
			override fun afterTextChanged(s: Editable?) {
				super.afterTextChanged(s)

				val cardNumber = s.toString().replace(" ", "")
				if (Card.isValidNumber(cardNumber)) {
					edit_card_exp.requestFocus()
					errorMode(false, edit_card_number)
				} else {
					errorMode(cardNumber.length == 19, edit_card_number)
				}

				updatePaymentSystemIcon(cardNumber)
			}
		})

		edit_card_number.setOnFocusChangeListener { _, hasFocus ->
			errorMode(!hasFocus && !Card.isValidNumber(edit_card_number.text.toString()), edit_card_number)
		}

		edit_card_exp.addTextChangedListener(object : TextWatcherAdapter() {
			override fun afterTextChanged(s: Editable?) {
				super.afterTextChanged(s)

				val cardExp = s.toString()
				if (Card.isValidExpDate(cardExp)) {
					edit_card_cvv.requestFocus()
					errorMode(false, edit_card_exp)
				} else {
					errorMode(cardExp.length == 5, edit_card_exp)
				}
			}
		})

		edit_card_exp.setOnFocusChangeListener { _, hasFocus ->
			errorMode(!hasFocus && !Card.isValidExpDate(edit_card_exp.text.toString()), edit_card_exp)
		}

		edit_card_cvv.addTextChangedListener(object : TextWatcherAdapter() {
			override fun afterTextChanged(s: Editable?) {
				super.afterTextChanged(s)
				errorMode(false, edit_card_cvv)

				if (s != null && s.toString().length >= 3) {
					if (checkbox_receipt.isChecked) {
						edit_email.requestFocus()
					} else {
						requireActivity().hideKeyboard()
					}
				}
			}
		})

		edit_card_cvv.setOnFocusChangeListener { _, hasFocus ->
			errorMode(!hasFocus && edit_card_cvv.text.toString().length != 3, edit_card_cvv)
		}

		edit_email.setOnFocusChangeListener { _, hasFocus ->
			errorMode(!hasFocus && !emailIsValid(edit_email.text.toString()), edit_email)
		}

		button_close.setOnClickListener {
			close(true)
		}

		button_pay.setOnClickListener {
			val cardNumber = edit_card_number.text.toString()
			val cardExp = edit_card_exp.text.toString()
			val cardCvv = edit_card_cvv.text.toString()

			val cryptogram = Card.cardCryptogram(cardNumber, cardExp, cardCvv, paymentConfiguration?.paymentData?.publicId ?: "")
			val email = if (checkbox_receipt.isChecked) edit_email.text.toString() else null
			if (isValid() && cryptogram != null) {
				close(false) {
					val listener = requireActivity() as? IPaymentCardFragment
					listener?.onPayClicked(cryptogram, email)
				}
			}
		}

		btn_scan.setOnClickListener {
			val intent = paymentConfiguration?.scanner?.getScannerIntent(requireContext())
			if (intent != null) {
				startActivityForResult(intent, REQUEST_CODE_SCANNER)
			}
		}

		button_pay.text = getString(R.string.text_card_pay_button, requireContext().getCurrencyString(paymentConfiguration!!.paymentData.amount.toDouble()))

		updatePaymentSystemIcon("")
	}

	private fun errorMode(isErrorMode: Boolean, editText: TextInputEditText){
		if (isErrorMode) {
			editText.setTextColor(ContextCompat.getColor(requireContext(), R.color.pale_red))
			editText.setBackgroundResource(R.drawable.edit_text_underline_error)
		} else {
			editText.setTextColor(ContextCompat.getColor(requireContext(), R.color.dark))
			editText.setBackgroundResource(R.drawable.edit_text_underline)
		}
	}

	private fun updatePaymentSystemIcon(cardNumber: String){
		val cardType = CardType.getType(cardNumber)
		val psIcon = cardType.getIconRes()
		if (paymentConfiguration?.scanner != null && (cardNumber.isEmpty() || psIcon == null)) {
			ic_ps.isVisible = false
			btn_scan.isVisible = true
		} else {
			ic_ps.isVisible = true
			btn_scan.isVisible = false
			ic_ps.setImageResource(psIcon ?: 0)
		}
	}

	private fun isValid(): Boolean {
		val cardNumberIsValid = Card.isValidNumber(edit_card_number.text.toString())
		val cardExpIsValid = Card.isValidExpDate(edit_card_exp.text.toString())
		val cardCvvIsValid = edit_card_cvv.text.toString().length == 3
		val emailIsValid = !checkbox_receipt.isChecked || emailIsValid(edit_email.text.toString())

		errorMode(!cardNumberIsValid, edit_card_number)
		errorMode(!cardExpIsValid, edit_card_exp)
		errorMode(!cardCvvIsValid, edit_card_cvv)
		errorMode(!emailIsValid, edit_email)

		return cardNumberIsValid && cardExpIsValid && cardCvvIsValid && emailIsValid
	}

	private fun updateWithCardData(cardData: CardData) {
		edit_card_number.setText(cardData.cardNumber)
		edit_card_exp.setText("${cardData.cardExpMonth}/${cardData.cardExpYear}")
	}

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) = when (requestCode) {
		REQUEST_CODE_SCANNER -> {
			if (data != null) {
				val cardData = paymentConfiguration?.scanner?.getCardDataFromIntent(data)
				if (cardData != null) {
					updateWithCardData(cardData)
				}
			}

			super.onActivityResult(requestCode, resultCode, data)
		}
		else -> super.onActivityResult(requestCode, resultCode, data)
	}
}