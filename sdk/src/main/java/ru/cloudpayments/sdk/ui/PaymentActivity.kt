package ru.cloudpayments.sdk.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.core.content.IntentCompat
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.wallet.AutoResolveHelper
import com.google.android.gms.wallet.PaymentData
import kotlinx.coroutines.launch
import ru.cloudpayments.sdk.R
import ru.cloudpayments.sdk.configuration.CloudpaymentsSDK
import ru.cloudpayments.sdk.configuration.PaymentConfiguration
import ru.cloudpayments.sdk.dagger2.CloudpaymentsComponent
import ru.cloudpayments.sdk.dagger2.CloudpaymentsModule
import ru.cloudpayments.sdk.dagger2.CloudpaymentsNetModule
import ru.cloudpayments.sdk.dagger2.DaggerCloudpaymentsComponent
import ru.cloudpayments.sdk.databinding.ActivityCpsdkPaymentBinding
import ru.cloudpayments.sdk.ui.dialogs.BasePaymentFragment
import ru.cloudpayments.sdk.ui.dialogs.PaymentCardFragment
import ru.cloudpayments.sdk.ui.dialogs.PaymentOptionsFragment
import ru.cloudpayments.sdk.ui.dialogs.PaymentProcessFragment
import ru.cloudpayments.sdk.util.GooglePayHandler
import ru.cloudpayments.sdk.util.handlePaymentSuccess
import ru.cloudpayments.sdk.util.nextFragment

internal class PaymentActivity: FragmentActivity(), BasePaymentFragment.IPaymentFragment,
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

	internal val component: CloudpaymentsComponent by lazy {
		DaggerCloudpaymentsComponent
			.builder()
			.cloudpaymentsModule(CloudpaymentsModule())
			.cloudpaymentsNetModule(CloudpaymentsNetModule(configuration.publicId, configuration.apiUrl))
			.build()
	}

	private val configuration: PaymentConfiguration by lazy {
		IntentCompat.getParcelableExtra(intent, EXTRA_CONFIGURATION, PaymentConfiguration::class.java)
			?: throw NullPointerException("EXTRA_CONFIGURATION is not existing in parcelable data")
	}

	var googlePayAvailable: Boolean = false

	private lateinit var binding: ActivityCpsdkPaymentBinding

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = ActivityCpsdkPaymentBinding.inflate(layoutInflater)
		val view = binding.root
		setContentView(view)

		checkCurrency()

		if (supportFragmentManager.backStackEntryCount == 0) {
			lifecycleScope.launch {
				val isShowUi = GooglePayHandler.isReadyToMakeGooglePay(this@PaymentActivity)
				showUi(isShowUi)
			}
		}
	}

	private fun showUi(googlePayAvailable: Boolean) {
		this.googlePayAvailable = googlePayAvailable && !configuration.disableGPay

		binding.iconProgress.isVisible = false

		val fragment = if (this.googlePayAvailable) {
			PaymentOptionsFragment.newInstance(configuration)
		} else {
			PaymentCardFragment.newInstance(configuration)
		}
		nextFragment(fragment, true, R.id.frame_content)
	}

	override fun onBackPressed() {
		val fragment = supportFragmentManager.findFragmentById(R.id.frame_content)
		if (fragment is BasePaymentFragment<*, *>) {
			fragment.handleBackButton()
		} else {
			onBackPressedDispatcher.onBackPressed()
		}
	}

	override fun onGooglePayClicked() {
		GooglePayHandler.present(configuration, this, REQUEST_CODE_GOOGLE_PAY)
	}

	override fun onCardClicked() {
		val fragment = PaymentCardFragment.newInstance(configuration)
		nextFragment(fragment, true, R.id.frame_content)
	}

	override fun onPayClicked(cryptogram: String, email: String?) {
		val fragment = PaymentProcessFragment.newInstance(configuration, cryptogram, email)
		nextFragment(fragment, true, R.id.frame_content)
	}

	override fun onPaymentFinished(transactionId: Int) {
		setResult(Activity.RESULT_OK, Intent().apply {
			putExtra(CloudpaymentsSDK.IntentKeys.TransactionId.name, transactionId)
			putExtra(
				CloudpaymentsSDK.IntentKeys.TransactionStatus.name,
				CloudpaymentsSDK.TransactionStatus.Succeeded
			)
		})
	}

	override fun onPaymentFailed(transactionId: Int, reasonCode: Int?) {
		setResult(Activity.RESULT_OK, Intent().apply {
			putExtra(CloudpaymentsSDK.IntentKeys.TransactionId.name, transactionId)
			putExtra(
				CloudpaymentsSDK.IntentKeys.TransactionStatus.name,
				CloudpaymentsSDK.TransactionStatus.Failed
			)
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

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) =
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

	private fun handleGooglePaySuccess(intent: Intent?) {
		if (intent != null) {
			val paymentData = PaymentData.getFromIntent(intent)
			val token = paymentData?.handlePaymentSuccess()

			if (token != null) {
				val runnable = {
					val fragment = PaymentProcessFragment.newInstance(configuration, token, null)
					nextFragment(fragment, true, R.id.frame_content)
				}
				Handler(Looper.getMainLooper()).postDelayed(runnable, 1000)
			}
		}
	}

	private fun handleGooglePayFailure(intent: Intent?) {
		finish()
	}

	private fun checkCurrency() {
		if (configuration.paymentData.currency.isEmpty()) {
			configuration.paymentData.currency = "RUB"
		}
	}
}