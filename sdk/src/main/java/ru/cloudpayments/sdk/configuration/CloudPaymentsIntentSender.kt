package ru.cloudpayments.sdk.configuration

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import ru.cloudpayments.sdk.models.Transaction

internal class CloudPaymentsIntentSender : ActivityResultContract<PaymentConfiguration, Transaction>() {
	override fun createIntent(context: Context, input: PaymentConfiguration): Intent {
		return CloudpaymentsSDK.getInstance().getStartIntent(context, input)
	}

	override fun parseResult(resultCode: Int, intent: Intent?): Transaction {
		if (resultCode == Activity.RESULT_OK) {
			val id = intent?.getIntExtra(CloudpaymentsSDK.IntentKeys.TransactionId.name, 0) ?: 0
			val status = intent?.getSerializableExtra(CloudpaymentsSDK.IntentKeys.TransactionStatus.name) as? CloudpaymentsSDK.TransactionStatus
			val reasonCode = intent?.getIntExtra(CloudpaymentsSDK.IntentKeys.TransactionReasonCode.name, 0) ?: 0

			return Transaction(id, status, reasonCode)
		}

		return Transaction(0, null, 0)
	}
}