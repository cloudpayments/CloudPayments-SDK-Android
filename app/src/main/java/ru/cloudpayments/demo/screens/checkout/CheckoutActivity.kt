package ru.cloudpayments.demo.screens.checkout

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import ru.cloudpayments.demo.R
import ru.cloudpayments.demo.api.PayApi
import ru.cloudpayments.demo.base.BaseActivity
import ru.cloudpayments.demo.databinding.ActivityCheckoutBinding
import ru.cloudpayments.demo.managers.CartManager
import ru.cloudpayments.demo.support.Constants
import ru.cloudpayments.sdk.api.models.CloudpaymentsThreeDsResponse
import ru.cloudpayments.sdk.api.models.CloudpaymentsTransaction
import ru.cloudpayments.sdk.card.Card
import ru.cloudpayments.sdk.ui.dialogs.ThreeDsDialogFragment
import ru.cloudpayments.sdk.ui.dialogs.ThreeDsDialogFragment.Companion.RESULT_COMPLETED
import ru.cloudpayments.sdk.ui.dialogs.ThreeDsDialogFragment.Companion.RESULT_COMPLETED_MD
import ru.cloudpayments.sdk.ui.dialogs.ThreeDsDialogFragment.Companion.RESULT_COMPLETED_PA_RES
import ru.cloudpayments.sdk.ui.dialogs.ThreeDsDialogFragment.Companion.RESULT_FAILED
import ru.tinkoff.decoro.MaskDescriptor
import ru.tinkoff.decoro.parser.UnderscoreDigitSlotsParser
import ru.tinkoff.decoro.watchers.DescriptorFormatWatcher

class CheckoutActivity : BaseActivity(R.layout.activity_checkout) {

	private var total = 0
	private var threeDsCallbackId: String? = null

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

		binding = ActivityCheckoutBinding.inflate(layoutInflater)
		val view = binding.root
		setContentView(view)

		setTitle(R.string.checkout_title)
		initTotal()

		cardNumberFormatWatcher.installOn(binding.editCardNumber)
		cardExpFormatWatcher.installOn(binding.editCardDate)

		setupTextChangedListeners()
		setupClickListeners()

		supportFragmentManager.setFragmentResultListener(RESULT_COMPLETED, this) { _, bundle ->
			val md = bundle.getString(RESULT_COMPLETED_MD) ?: return@setFragmentResultListener
			val paRes = bundle.getString(RESULT_COMPLETED_PA_RES) ?: return@setFragmentResultListener
			post3ds(md, paRes)
		}

		supportFragmentManager.setFragmentResultListener(RESULT_FAILED, this) { _, bundle ->
			val error = bundle.getString(RESULT_FAILED)
			showToast("AuthorizationFailed: $error")
		}
	}

	private fun initTotal() {
		val products = CartManager.getInstance()?.getProducts().orEmpty()
		products.forEach {
			total += it.price.toInt()
		}
		binding.textTotal.text = getString(R.string.checkout_total_currency, total.toString())
	}

	private fun setupTextChangedListeners(){
		binding.editCardNumber.addTextChangedListener(
			afterTextChanged = { s ->
				val cardNumber = s?.toString()?.replace(" ", "")
				if (Card.isValidNumber(cardNumber)) {
					binding.editCardDate.requestFocus()
				}
			}
		)

		binding.editCardDate.addTextChangedListener(
			afterTextChanged = { s ->
				val cardExp = s?.toString()
				if (Card.isValidExpDate(cardExp)) {
					binding.editCardCvc.requestFocus()
				}
			}
		)

		binding.editCardCvc.addTextChangedListener(
			afterTextChanged = { s ->
				val cardCvc = s?.toString()
				if (cardCvc?.length == 3) {
					binding.editCardHolderName.requestFocus()
				}
			}
		)
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
		lifecycleScope.launch {
			try {
				val info = PayApi.getBinInfo(firstSixDigits)
				Log.d("Bank name", info.bankName.orEmpty())
			} catch (e: Exception) {
				handleError(e)
			}
		}
	}

	// Запрос на проведение двустадийного платежа
	private fun auth(cardCryptogramPacket: String, cardHolderName: String, amount: Int) {
		lifecycleScope.launch {
			try {
				showLoading()
				val transaction = PayApi.auth(cardCryptogramPacket, cardHolderName, amount)
				checkResponse(transaction)
			} catch (e: Exception) {
				handleError(e)
			} finally {
				hideLoading()
			}
		}
	}

	// Проверяем, необходимо ли подтверждение с использованием 3DS
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
		// Открываем 3DS форму
		ThreeDsDialogFragment
			.newInstance(transaction?.acsUrl.orEmpty(), transaction?.paReq.orEmpty(), transaction?.transactionId?.toString().orEmpty())
			.show(supportFragmentManager, "3DS")
	}

	// Завершаем транзакцию после прохождения 3DS формы
	private fun post3ds(md: String, paRes: String) {
		lifecycleScope.launch {
			try {
				showLoading()
				val response = PayApi.postThreeDs(md, threeDsCallbackId.orEmpty(), paRes)
				checkThreeDsResponse(response)
			} catch (e: Exception) {
				handleError(e)
			} finally {
				hideLoading()
			}
		}
	}
}