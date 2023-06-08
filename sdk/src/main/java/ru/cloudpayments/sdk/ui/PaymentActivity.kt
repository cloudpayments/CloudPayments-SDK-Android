package ru.cloudpayments.sdk.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Base64
import android.util.Log
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.wallet.AutoResolveHelper
import com.google.android.gms.wallet.PaymentData
import com.yandex.pay.core.*
import com.yandex.pay.core.data.Merchant
import com.yandex.pay.core.data.MerchantId
import com.yandex.pay.core.data.OrderDetails
import io.reactivex.android.schedulers.AndroidSchedulers
import ru.cloudpayments.sdk.R
import ru.cloudpayments.sdk.configuration.CloudpaymentsSDK
import ru.cloudpayments.sdk.configuration.PaymentConfiguration
import ru.cloudpayments.sdk.dagger2.CloudpaymentsComponent
import ru.cloudpayments.sdk.dagger2.CloudpaymentsModule
import ru.cloudpayments.sdk.dagger2.CloudpaymentsNetModule
import ru.cloudpayments.sdk.dagger2.DaggerCloudpaymentsComponent
import ru.cloudpayments.sdk.databinding.ActivityCpsdkPaymentBinding
import ru.cloudpayments.sdk.ui.dialogs.base.BasePaymentBottomSheetFragment
import ru.cloudpayments.sdk.ui.dialogs.PaymentCardFragment
import ru.cloudpayments.sdk.ui.dialogs.PaymentOptionsFragment
import ru.cloudpayments.sdk.ui.dialogs.PaymentProcessFragment
import ru.cloudpayments.sdk.util.GooglePayHandler
import ru.cloudpayments.sdk.util.nextFragment

internal class PaymentActivity: FragmentActivity(), BasePaymentBottomSheetFragment.IPaymentFragment,
								PaymentOptionsFragment.IPaymentOptionsFragment, PaymentCardFragment.IPaymentCardFragment,
								PaymentProcessFragment.IPaymentProcessFragment {

	companion object {
		private const val REQUEST_CODE_GOOGLE_PAY = 1

		private const val EXTRA_CONFIGURATION = "EXTRA_CONFIGURATION"

		fun getStartIntent(context: Context, configuration: PaymentConfiguration): Intent {
			val intent = Intent(context, PaymentActivity::class.java)
			intent.putExtra(EXTRA_CONFIGURATION, configuration)
			return intent
		}
	}

	override fun finish() {
		super.finish()
		overridePendingTransition(R.anim.cpsdk_fade_in, R.anim.cpsdk_fade_out)
	}

	internal val component: CloudpaymentsComponent by lazy {
		DaggerCloudpaymentsComponent
			.builder()
			.cloudpaymentsModule(CloudpaymentsModule())
			.cloudpaymentsNetModule(CloudpaymentsNetModule(paymentConfiguration!!.publicId, paymentConfiguration!!.apiUrl))
			.build()
	}

	val paymentConfiguration by lazy {
		intent.getParcelableExtra<PaymentConfiguration>(EXTRA_CONFIGURATION)
	}

	var googlePayAvailable: Boolean = false
	var yandexPayAvailable: Boolean = false

	private lateinit var binding: ActivityCpsdkPaymentBinding

	private val yandexPayLauncher = registerForActivityResult(OpenYandexPayContract()) { result ->
		when (result) {
			is YandexPayResult.Success -> {

				val token = Base64.decode(result.paymentToken.toString(), Base64.DEFAULT)

				val runnable = {
					val fragment = PaymentProcessFragment.newInstance(String(token))
					nextFragment(fragment, true, R.id.frame_content)
				}
				Handler().postDelayed(runnable, 1000)
			}
			is YandexPayResult.Failure -> when (result) {
				is YandexPayResult.Failure.Validation -> {
					Log.e("YaPay", "failure: ${result.details}")
					finish()
				}
				is YandexPayResult.Failure.Internal -> {
					Log.e("YaPay", "failure: ${result.message}")
					finish()
				}
			}
			is YandexPayResult.Cancelled -> {
				Log.e("YaPay","cancelled")
				finish()
			}
		}
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = ActivityCpsdkPaymentBinding.inflate(layoutInflater)
		val view = binding.root
		setContentView(view)

		checkCurrency()

		if (YandexPayLib.isSupported) {
			YandexPayLib.initialize(
				context = this,
				config = YandexPayLibConfig(
					merchantDetails = Merchant(
						id = MerchantId.from(paymentConfiguration!!.yandexPayMerchantID),
						name = "Cloud",
						url = "https://cp.ru/",
					),
					environment = YandexPayEnvironment.PROD,
					locale = YandexPayLocale.SYSTEM,
					logging = false
				)
			)
			yandexPayAvailable = !paymentConfiguration!!.disableYandexPay && paymentConfiguration!!.yandexPayMerchantID.isNotEmpty()
		} else {
			yandexPayAvailable = false
		}

		if (supportFragmentManager.backStackEntryCount == 0) {
			GooglePayHandler.isReadyToMakeGooglePay(this)
					.toObservable()
					.observeOn(AndroidSchedulers.mainThread())
					.map {
						showUi(it)
					}
					.onErrorReturn { showUi(false) }
					.subscribe()
		}
	}

	private fun showUi(googlePayAvailable: Boolean) {
		this.googlePayAvailable = googlePayAvailable && !paymentConfiguration!!.disableGPay

		binding.iconProgress.isVisible = false

		val fragment = PaymentOptionsFragment.newInstance()

		fragment.show(supportFragmentManager, "")
	}

	override fun onBackPressed() {
		val fragment = supportFragmentManager.findFragmentById(R.id.frame_content)
		if (fragment is BasePaymentBottomSheetFragment<*, *>) {
			fragment.handleBackButton()
		} else {
			super.onBackPressed()
		}
	}

	fun runYandexPay(orderDetails: OrderDetails) {
		yandexPayLauncher.launch(orderDetails)
	}

	override fun onGooglePayClicked() {
		GooglePayHandler.present(paymentConfiguration!!, this, REQUEST_CODE_GOOGLE_PAY)
	}

	override fun onCardClicked() {
		val fragment = PaymentCardFragment.newInstance()
		fragment.show(supportFragmentManager, "")
		//nextFragment(fragment, true, R.id.frame_content)
	}

	override fun onPayClicked(cryptogram: String) {
		val fragment = PaymentProcessFragment.newInstance(cryptogram)
		fragment.show(supportFragmentManager, "")
		//nextFragment(fragment, true, R.id.frame_content)
	}

	override fun onPaymentFinished(transactionId: Int) {
		setResult(Activity.RESULT_OK, Intent().apply {
			putExtra(CloudpaymentsSDK.IntentKeys.TransactionId.name, transactionId)
			putExtra(CloudpaymentsSDK.IntentKeys.TransactionStatus.name, CloudpaymentsSDK.TransactionStatus.Succeeded)
		})
	}

	override fun onPaymentFailed(transactionId: Int, reasonCode: Int?) {
		setResult(Activity.RESULT_OK, Intent().apply {
			putExtra(CloudpaymentsSDK.IntentKeys.TransactionId.name, transactionId)
			putExtra(CloudpaymentsSDK.IntentKeys.TransactionStatus.name, CloudpaymentsSDK.TransactionStatus.Failed)
			reasonCode?.let { putExtra(CloudpaymentsSDK.IntentKeys.TransactionReasonCode.name, it) }
		})
	}

	override fun finishPayment() {
		finish()
	}

	override fun retryPayment() {
		setResult(Activity.RESULT_CANCELED, Intent())
		showUi(this.googlePayAvailable)
	}

	override fun paymentWillFinish() {
		finish()
	}

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

		when (requestCode) {

			REQUEST_CODE_GOOGLE_PAY -> {
				when (resultCode) {
					Activity.RESULT_OK -> {
						handleGooglePaySuccess(data)
					}
					Activity.RESULT_CANCELED, AutoResolveHelper.RESULT_ERROR -> {
						handleGooglePayFailure(data)
					}
					else -> super.onActivityResult(requestCode, resultCode, data)
				}
			}

			else -> super.onActivityResult(requestCode, resultCode, data)
		}
	}

	private fun handleGooglePaySuccess(intent: Intent?) {
		if (intent != null) {
			val paymentData = PaymentData.getFromIntent(intent)
			val token = paymentData?.paymentMethodToken?.token

			if (token != null) {
				val runnable = {
					val fragment = PaymentProcessFragment.newInstance(token)
					nextFragment(fragment, true, R.id.frame_content)
				}
				Handler().postDelayed(runnable, 1000)
			}
		}
	}

	private fun handleGooglePayFailure(intent: Intent?) {
		finish()
	}

	private fun checkCurrency() {
		if (paymentConfiguration!!.paymentData.currency.isEmpty()) {
			paymentConfiguration!!.paymentData.currency = "RUB"
		}
	}
}