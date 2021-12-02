package ru.cloudpayments.sdk.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.wallet.AutoResolveHelper
import com.google.android.gms.wallet.PaymentData
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_payment.*
import ru.cloudpayments.sdk.R
import ru.cloudpayments.sdk.configuration.CloudpaymentsSDK
import ru.cloudpayments.sdk.configuration.PaymentConfiguration
import ru.cloudpayments.sdk.dagger2.CloudpaymentsComponent
import ru.cloudpayments.sdk.dagger2.CloudpaymentsModule
import ru.cloudpayments.sdk.dagger2.CloudpaymentsNetModule
import ru.cloudpayments.sdk.dagger2.DaggerCloudpaymentsComponent
import ru.cloudpayments.sdk.ui.dialogs.BasePaymentFragment
import ru.cloudpayments.sdk.ui.dialogs.PaymentCardFragment
import ru.cloudpayments.sdk.ui.dialogs.PaymentOptionsFragment
import ru.cloudpayments.sdk.ui.dialogs.PaymentProcessFragment
import ru.cloudpayments.sdk.util.GooglePayHandler
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
			.cloudpaymentsNetModule(CloudpaymentsNetModule(configuration.paymentData.publicId))
			.build()
	}

	private val configuration by lazy {
		intent.getParcelableExtra<PaymentConfiguration>(EXTRA_CONFIGURATION)
	}

	private var googlePayAvailable: Boolean = false

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_payment)

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
		this.googlePayAvailable = googlePayAvailable && !configuration.disableGPay

		icon_progress.isVisible = false

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
			super.onBackPressed()
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

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) = when (requestCode) {
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
			val token = paymentData?.paymentMethodToken?.token

			if (token != null) {
				val runnable = {
					val fragment = PaymentProcessFragment.newInstance(configuration, token, null)
					nextFragment(fragment, true, R.id.frame_content)
				}
				Handler().postDelayed(runnable, 1000)
			}
		}
	}

	private fun handleGooglePayFailure(intent: Intent?) {
		finish()
	}
}