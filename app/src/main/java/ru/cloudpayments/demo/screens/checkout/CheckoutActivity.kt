package ru.cloudpayments.demo.screens.checkout

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.widget.Toast
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.wallet.AutoResolveHelper
import com.google.android.gms.wallet.PaymentData
import com.google.android.gms.wallet.PaymentsClient
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import ru.cloudpayments.sdk.api.models.CloudpaymentsThreeDsResponse
import ru.cloudpayments.sdk.api.models.CloudpaymentsTransaction
import ru.cloudpayments.sdk.card.Card
import ru.cloudpayments.demo.R
import ru.cloudpayments.demo.api.PayApi
import ru.cloudpayments.demo.base.BaseActivity
import ru.cloudpayments.demo.databinding.ActivityCheckoutBinding
import ru.cloudpayments.demo.googlepay.PaymentsUtil
import ru.cloudpayments.demo.managers.CartManager
import ru.cloudpayments.demo.support.Constants
import ru.cloudpayments.sdk.ui.dialogs.ThreeDsDialogFragment
import ru.cloudpayments.sdk.util.TextWatcherAdapter
import ru.tinkoff.decoro.MaskDescriptor
import ru.tinkoff.decoro.parser.UnderscoreDigitSlotsParser
import ru.tinkoff.decoro.watchers.DescriptorFormatWatcher
import java.lang.Exception

class CheckoutActivity : BaseActivity(), ThreeDsDialogFragment.ThreeDSDialogListener {
	companion object {
		private const val LOAD_PAYMENT_DATA_REQUEST_CODE = 991
	}

	private var paymentsClient: PaymentsClient? = null
	private var total = 0
	private var threeDsCallbackId: String? = null

	override val layoutId: Int = R.layout.activity_checkout

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

	private lateinit var binding: ActivityCheckoutBinding

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		binding =ActivityCheckoutBinding.inflate(layoutInflater)
		val view = binding.root
		setContentView(view)

		setTitle(R.string.checkout_title)
		initTotal()

		// GOOGLE PAY

		// It's recommended to create the PaymentsClient object inside of the onCreate method.
		paymentsClient = PaymentsUtil.createPaymentsClient(this)
		checkIsReadyToPay()
		binding.pwgButton.root.setOnClickListener {
			requestPayment()
		}

		cardNumberFormatWatcher.installOn(binding.editCardNumber)
		cardExpFormatWatcher.installOn(binding.editCardDate)

		setupTextChangedListeners()
		setupClickListeners()
	}

	private fun initTotal() {
		val products = CartManager.getInstance()?.getProducts().orEmpty()
		products.forEach {
			total += it.price?.toInt() ?: 0
		}
		binding.textTotal.text = getString(R.string.checkout_total_currency, total.toString())
	}

	private fun setupTextChangedListeners(){
		binding.editCardNumber.addTextChangedListener(object : TextWatcherAdapter() {
			override fun afterTextChanged(s: Editable?) {
				super.afterTextChanged(s)

				val cardNumber = s.toString().replace(" ", "")
				if (Card.isValidNumber(cardNumber)) {
					binding.editCardDate.requestFocus()
				}
			}
		})

		binding.editCardDate.addTextChangedListener(object : TextWatcherAdapter() {
			override fun afterTextChanged(s: Editable?) {
				super.afterTextChanged(s)

				val cardExp = s.toString()
				if (Card.isValidExpDate(cardExp)) {
					binding.editCardCvc.requestFocus()
				}
			}
		})

		binding.editCardCvc.addTextChangedListener(object : TextWatcherAdapter() {
			override fun afterTextChanged(s: Editable?) {
				super.afterTextChanged(s)

				val cardCvc = s.toString()
				if (cardCvc.length == 3) {
					binding.editCardHolderName.requestFocus()
				}
			}
		})
	}

	private fun setupClickListeners() {
		binding.textPhone.setOnClickListener {
			val phone = getString(R.string.main_phone)
			val intent = Intent(Intent.ACTION_DIAL)
			intent.data = Uri.parse("tel:$phone")
			startActivity(intent)
		}

		binding.textEmail.setOnClickListener {
			val email = getString(R.string.main_email)
			val emailIntent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$email"))
			startActivity(Intent.createChooser(emailIntent, getString(R.string.main_select_app)))
		}

		binding.buttonPayment.setOnClickListener {
			val cardNumber = binding.editCardNumber.text.toString().replace(" ", "")
			val cardDate = binding.editCardDate.text.toString().replace("/", "")
			val cardCVC = binding.editCardCvc.text.toString()
			val cardHolderName = binding.editCardHolderName.text.toString()

			getBinInfo(cardNumber)

			when {
				!Card.isValidNumber(cardNumber) -> showToast(R.string.checkout_error_card_number)
				!Card.isValidExpDate(cardDate) -> showToast(R.string.checkout_error_card_date)
				cardCVC.length != 3 -> showToast(R.string.checkout_error_card_cvc)
				else -> {
					try {
						// Чтобы создать криптограмму необходим PublicID (его можно посмотреть в личном кабинете)
						val cardCryptogram = Card.cardCryptogram(cardNumber, cardDate, cardCVC, Constants.MERCHANT_PUBLIC_ID)
						cardCryptogram?.let {
							auth(it, cardHolderName, total)
						}
					} catch (e: Exception) {
						e.printStackTrace()
					}
				}
			}
		}
	}

	private fun getBinInfo(firstSixDigits: String) {
		compositeDisposable.add(
				PayApi.getBinInfo(firstSixDigits)
						.subscribeOn(Schedulers.io())
						.observeOn(AndroidSchedulers.mainThread())
						.subscribe({ info -> Log.d("Bank name", info.bankName.orEmpty()) }, this::handleError)
		)
	}

	// Запрос на прведение одностадийного платежа
	private fun charge(cardCryptogramPacket: String, cardHolderName: String, amount: Int) {
		compositeDisposable.add(
			PayApi.charge(cardCryptogramPacket, cardHolderName, amount)
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.doOnSubscribe { showLoading() }
				.doOnEach { hideLoading() }
				.subscribe({ transaction -> checkResponse(transaction) }, this::handleError)
		)
	}

	// Запрос на проведение двустадийного платежа
	private fun auth(cardCryptogramPacket: String, cardHolderName: String, amount: Int) {
		compositeDisposable.add(
			PayApi.auth(cardCryptogramPacket, cardHolderName, amount)
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.doOnSubscribe { showLoading() }
				.doOnEach { hideLoading() }
				.subscribe({ transaction -> checkResponse(transaction) }, this::handleError)
		)
	}

	// Проверяем необходимо ли подтверждение с использованием 3DS
	private fun checkResponse(transaction: CloudpaymentsTransaction?) {
		threeDsCallbackId = transaction?.threeDsCallbackId
		if (!transaction?.paReq.isNullOrEmpty() && !transaction?.acsUrl.isNullOrEmpty()) {
			// Показываем 3DS форму
			show3DS(transaction)
		} else {
			// Показываем результат
			showToast(transaction?.cardHolderMessage)
		}
	}

	private fun checkThreeDsResponse(response: CloudpaymentsThreeDsResponse) {
		if (response.success) {
			showToast("Успешно!")
			finish()
		} else {
			// Показываем результат
			showToast(response.message)
		}
	}

	private fun show3DS(transaction: CloudpaymentsTransaction?) {
		// Открываем 3ds форму
		ThreeDsDialogFragment
			.newInstance(transaction?.acsUrl.orEmpty(), transaction?.paReq.orEmpty(), transaction?.transactionId?.toString().orEmpty())
			.show(supportFragmentManager, "3DS")
	}

	// Завершаем транзакцию после прохождения 3DS формы
	private fun post3ds(md: String, paRes: String) {
		compositeDisposable.add(
			PayApi.postThreeDs(md, threeDsCallbackId.orEmpty(), paRes)
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.doOnSubscribe { showLoading() }
				.doOnEach { hideLoading() }
				.subscribe({ response -> checkThreeDsResponse(response) }, this::handleError)
		)
	}

	// GOGGLE PAY
	private fun checkIsReadyToPay() {
		// The call to isReadyToPay is asynchronous and returns a Task. We need to provide an
		// OnCompleteListener to be triggered when the result of the call is known.
		PaymentsUtil.isReadyToPay(paymentsClient).addOnCompleteListener { task ->
			try {
				val result = task.getResult(ApiException::class.java)!!
				setPwgAvailable(result)
			} catch (exception: ApiException) {
				// Process error
				Log.w("isReadyToPay failed", exception)
			}
		}
	}

	private fun setPwgAvailable(available: Boolean) {
		// If isReadyToPay returned true, show the button and hide the "checking" text. Otherwise,
		// notify the user that Pay with Google is not available.
		// Please adjust to fit in with your current user flow. You are not required to explicitly
		// let the user know if isReadyToPay returns false.
		if (available) {
			binding.pwgStatus.isGone = true
			binding.pwgButton.root.isVisible = true
		} else {
			binding.pwgStatus.setText(R.string.pwg_status_unavailable)
		}
	}

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		super.onActivityResult(requestCode, resultCode, data)
		when (requestCode) {
			LOAD_PAYMENT_DATA_REQUEST_CODE -> {
				when (resultCode) {
					RESULT_OK -> {
						val paymentData = PaymentData.getFromIntent(
							data!!
						)
						handlePaymentSuccess(paymentData)
					}
					RESULT_CANCELED -> {
					}
					AutoResolveHelper.RESULT_ERROR -> {
						val status = AutoResolveHelper.getStatusFromIntent(data)
						handlePaymentError(status!!.statusCode)
					}
				}

				// Re-enables the Pay with Google button.
				binding.pwgButton.root.isClickable = true
			}
		}
	}

	private fun handlePaymentSuccess(paymentData: PaymentData?) {
		// PaymentMethodToken contains the payment information, as well as any additional
		// requested information, such as billing and shipping address.
		//
		// Refer to your processor's documentation on how to proceed from here.
		val token = paymentData!!.paymentMethodToken

		// getPaymentMethodToken will only return null if PaymentMethodTokenizationParameters was
		// not set in the PaymentRequest.
		if (token != null) {
			val billingName = paymentData.cardInfo.billingAddress!!.name
			Toast.makeText(this, getString(R.string.payments_show_name, billingName), Toast.LENGTH_LONG).show()

			// Use token.getToken() to get the token string.
			Log.d("GooglePaymentToken", token.token)
			charge(token.token, "Google Pay", total)
		}
	}

	private fun handlePaymentError(statusCode: Int) {
		// At this stage, the user has already seen a popup informing them an error occurred.
		// Normally, only logging is required.
		// statusCode will hold the value of any constant from CommonStatusCode or one of the
		// WalletConstants.ERROR_CODE_* constants.
		Log.w("loadPaymentData failed", String.format("Error code: %d", statusCode))
	}

	// This method is called when the Pay with Google button is clicked.
	private fun requestPayment() {
		// Disables the button to prevent multiple clicks.
		binding.pwgButton.root.isClickable = false

		// The price provided to the API should include taxes and shipping.
		// This price is not displayed to the user.
		val price = PaymentsUtil.microsToString(total.toLong())
		val transaction = PaymentsUtil.createTransaction(price)
		val request = PaymentsUtil.createPaymentDataRequest(transaction)
		val futurePaymentData = paymentsClient!!.loadPaymentData(request)

		// Since loadPaymentData may show the UI asking the user to select a payment method, we use
		// AutoResolveHelper to wait for the user interacting with it. Once completed,
		// onActivityResult will be called with the result.
		AutoResolveHelper.resolveTask(futurePaymentData, this, LOAD_PAYMENT_DATA_REQUEST_CODE)
	}

	override fun onAuthorizationCompleted(md: String, paRes: String) {
		post3ds(md, paRes)
	}

	override fun onAuthorizationFailed(error: String?) {
		showToast("AuthorizationFailed: $error")
	}


}