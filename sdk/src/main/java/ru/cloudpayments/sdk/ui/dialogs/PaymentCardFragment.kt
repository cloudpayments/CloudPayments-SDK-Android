package ru.cloudpayments.sdk.ui.dialogs

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import ru.cloudpayments.sdk.R
import ru.cloudpayments.sdk.card.Card
import ru.cloudpayments.sdk.card.CardType
import ru.cloudpayments.sdk.databinding.DialogCpsdkPaymentCardBinding
import ru.cloudpayments.sdk.models.Currency
import ru.cloudpayments.sdk.scanner.CardData
import ru.cloudpayments.sdk.ui.dialogs.base.BasePaymentBottomSheetFragment
import ru.cloudpayments.sdk.util.PublicKey
import ru.cloudpayments.sdk.util.TextWatcherAdapter
import ru.cloudpayments.sdk.util.hideKeyboard
import ru.cloudpayments.sdk.viewmodel.PaymentCardViewModel
import ru.cloudpayments.sdk.viewmodel.PaymentCardViewState
import ru.tinkoff.decoro.MaskDescriptor
import ru.tinkoff.decoro.parser.UnderscoreDigitSlotsParser
import ru.tinkoff.decoro.watchers.DescriptorFormatWatcher

internal class PaymentCardFragment :
	BasePaymentBottomSheetFragment<PaymentCardViewState, PaymentCardViewModel>() {
	interface IPaymentCardFragment {
		fun onPayClicked(cryptogram: String)
	}

	companion object {
		const val REQUEST_CODE_SCANNER = 1

		fun newInstance() = PaymentCardFragment().apply {
			arguments = Bundle()
		}
	}

	private var _binding: DialogCpsdkPaymentCardBinding? = null

	private val binding get() = _binding!!

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		_binding = DialogCpsdkPaymentCardBinding.inflate(inflater, container, false)
		val view = binding.root
		return view
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}

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

		cardNumberFormatWatcher.installOn(binding.editCardNumber)
		cardExpFormatWatcher.installOn(binding.editCardExp)

		binding.editCardNumber.addTextChangedListener(object : TextWatcherAdapter() {
			override fun afterTextChanged(s: Editable?) {
				super.afterTextChanged(s)

				val cardNumber = s.toString().replace(" ", "")
				if (Card.isValidNumber(cardNumber)) {
					//edit_card_exp.requestFocus()
					errorMode(false, binding.editCardNumber, binding.tilCardNumber)
				} else {
					errorMode(cardNumber.length == 19, binding.editCardNumber, binding.tilCardNumber)
				}

				updatePaymentSystemIcon(cardNumber)
				updateStateButtons()
			}
		})

		binding.editCardNumber.setOnFocusChangeListener { _, hasFocus ->
			errorMode(
				!hasFocus && !Card.isValidNumber(binding.editCardNumber.text.toString()),
				binding.editCardNumber, binding.tilCardNumber
			)
		}

		binding.editCardExp.addTextChangedListener(object : TextWatcherAdapter() {
			override fun afterTextChanged(s: Editable?) {
				super.afterTextChanged(s)

				val cardExp = s.toString()
				if (Card.isValidExpDate(cardExp)) {
					//edit_card_cvv.requestFocus()
					errorMode(false, binding.editCardExp, binding.tilCardExp)
				} else {
					errorMode(cardExp.length == 5, binding.editCardExp, binding.tilCardExp)
				}
				updateStateButtons()
			}
		})

		binding.editCardExp.setOnFocusChangeListener { _, hasFocus ->
			errorMode(
				!hasFocus && !Card.isValidExpDate(binding.editCardExp.text.toString()),
				binding.editCardExp, binding.tilCardExp
			)
		}

		binding.editCardCvv.addTextChangedListener(object : TextWatcherAdapter() {
			override fun afterTextChanged(s: Editable?) {
				super.afterTextChanged(s)
				errorMode(false, binding.editCardCvv, binding.tilCardCvv)

				if (Card.isValidCvv(binding.editCardNumber.toString(), s.toString())) {
					requireActivity().hideKeyboard()
				}
				updateStateButtons()
			}
		})

		binding.editCardCvv.setOnFocusChangeListener { _, hasFocus ->
			errorMode(
				!hasFocus && !Card.isValidCvv(
					binding.editCardNumber.toString(),
					binding.editCardCvv.text.toString()
				), binding.editCardCvv, binding.tilCardCvv
			)
		}

		binding.buttonPay.setOnClickListener {
			val cardNumber = binding.editCardNumber.text.toString()
			val cardExp = binding.editCardExp.text.toString()
			val cardCvv = binding.editCardCvv.text.toString()

			val cryptogram = Card.createHexPacketFromData(
				cardNumber,
				cardExp,
				cardCvv,
				paymentConfiguration?.publicId ?: "",
				PublicKey.getInstance(requireContext()).pem ?: "",
				PublicKey.getInstance(requireContext()).version ?: 0
			)

			if (isValid() && cryptogram != null) {

				val listener = requireActivity() as? IPaymentCardFragment
				listener?.onPayClicked(cryptogram)
				dismiss()
			}
		}

		binding.btnScan.setOnClickListener {
			val intent = paymentConfiguration?.scanner?.getScannerIntent(requireContext())
			if (intent != null) {
				startActivityForResult(intent, REQUEST_CODE_SCANNER)
			}
		}

		binding.buttonPay.text = getString(
			R.string.cpsdk_text_card_pay_button,
			String.format(
				"%.2f " + Currency.getSymbol(paymentConfiguration!!.paymentData.currency),
				paymentConfiguration!!.paymentData.amount.toDouble()
			)
		)

		updatePaymentSystemIcon("")
		updateStateButtons()
	}

	private fun updatePaymentSystemIcon(cardNumber: String) {
		val cardType = CardType.getType(cardNumber)
		val psIcon = cardType.getIconRes()
		if (paymentConfiguration?.scanner != null && (cardNumber.isEmpty() || psIcon == null)) {
			binding.icPs.isVisible = false
			binding.btnScan.isVisible = true
		} else {
			binding.icPs.isVisible = true
			binding.btnScan.isVisible = false
			binding.icPs.setImageResource(psIcon ?: 0)
		}
	}

	private fun updateStateButtons() {
		if (isValid()) {
			enableButtons()
		} else {
			disableButtons()
		}
	}

	private fun disableButtons() {
		binding.viewBlockButtons.visibility = View.VISIBLE
	}

	private fun enableButtons() {
		binding.viewBlockButtons.visibility = View.GONE
	}

	private fun isValid(): Boolean {
		val cardNumber = binding.editCardNumber.text.toString()
		val cardNumberIsValid = Card.isValidNumber(cardNumber)
		val cardExpIsValid = Card.isValidExpDate(binding.editCardExp.text.toString())
		val cardCvvIsValid = Card.isValidCvv(cardNumber, binding.editCardCvv.text.toString())

//		errorMode(!cardNumberIsValid, binding.editCardNumber)
//		errorMode(!cardExpIsValid, binding.editCardExp)
//		errorMode(!cardCvvIsValid, binding.editCardCvv)

		return cardNumberIsValid && cardExpIsValid && cardCvvIsValid
	}

	private fun updateWithCardData(cardData: CardData) {
		binding.editCardNumber.setText(cardData.cardNumber)
		binding.editCardExp.setText("${cardData.cardExpMonth}/${cardData.cardExpYear}")
	}

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) =
		when (requestCode) {
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