package ru.cloudpayments.sdk.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.content.IntentCompat
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
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
import ru.cloudpayments.sdk.util.nextFragment

internal class PaymentActivity: FragmentActivity(), BasePaymentFragment.IPaymentFragment,
		PaymentOptionsFragment.IPaymentOptionsFragment, PaymentCardFragment.IPaymentCardFragment,
		PaymentProcessFragment.IPaymentProcessFragment {

	companion object {
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

	private lateinit var binding: ActivityCpsdkPaymentBinding

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = ActivityCpsdkPaymentBinding.inflate(layoutInflater)
		val view = binding.root
		setContentView(view)

		checkCurrency()

		if (supportFragmentManager.backStackEntryCount == 0) {
			lifecycleScope.launch {
				showUi()
			}
		}
	}

	private fun showUi() {
		binding.iconProgress.isVisible = false
		val fragment = PaymentCardFragment.newInstance(configuration)
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
		showUi()
	}

	override fun paymentWillFinish() {
		finish()
	}

	private fun checkCurrency() {
		if (configuration.paymentData.currency.isEmpty()) {
			configuration.paymentData.currency = "RUB"
		}
	}
}