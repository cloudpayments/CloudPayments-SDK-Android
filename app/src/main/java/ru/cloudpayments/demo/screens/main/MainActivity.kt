package ru.cloudpayments.demo.screens.main

import android.os.Bundle
import android.widget.Toast
import ru.cloudpayments.demo.R
import ru.cloudpayments.demo.base.BaseActivity
import ru.cloudpayments.demo.databinding.ActivityMainBinding
import ru.cloudpayments.demo.support.CardIOScanner
import ru.cloudpayments.sdk.api.models.PaymentDataPayer
import ru.cloudpayments.sdk.configuration.CloudpaymentsSDK
import ru.cloudpayments.sdk.configuration.PaymentConfiguration
import ru.cloudpayments.sdk.configuration.PaymentData

class MainActivity : BaseActivity(R.layout.activity_main) {

	private val cpSdkLauncher = CloudpaymentsSDK.getInstance().launcher(this, result = {
		if (it.status != null) {
			if (it.status == CloudpaymentsSDK.TransactionStatus.Succeeded) {
				Toast.makeText(this, "Успешно! Транзакция №${it.transactionId}", Toast.LENGTH_SHORT).show()
			} else {
				if (it.reasonCode != 0) {
					Toast.makeText(this, "Ошибка! Транзакция №${it.transactionId}. Код ошибки ${it.reasonCode}", Toast.LENGTH_SHORT).show()
				} else {
					Toast.makeText(this, "Ошибка! Транзакция №${it.transactionId}.", Toast.LENGTH_SHORT).show()
				}
			}
		}
	})

	private lateinit var binding: ActivityMainBinding

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		binding = ActivityMainBinding.inflate(layoutInflater)
		val view = binding.root
		setContentView(view)

		binding.buttonRun.setOnClickListener {

			val apiUrl = binding.editApiUrl.text.toString()
			val publicId = binding.editPublicId.text.toString()
			val amount = binding.editAmount.text.toString()
			val currency = binding.editCurrency.text.toString()
			val invoiceId = binding.editInvoiceId.text.toString()
			val description = binding.editDescription.text.toString()
			val accountId = binding.editAccountId.text.toString()
			val email = binding.editEmail.text.toString()

			val payerFirstName = binding.editPayerFirstName.text.toString()
			val payerLastName = binding.editPayerLastName.text.toString()
			val payerMiddleName = binding.editPayerMiddleName.text.toString()
			val payerBirthDay = binding.editPayerBirth.text.toString()
			val payerAddress = binding.editPayerAddress.text.toString()
			val payerStreet = binding.editPayerStreet.text.toString()
			val payerCity = binding.editPayerCity.text.toString()
			val payerCountry = binding.editPayerCountry.text.toString()
			val payerPhone = binding.editPayerPhone.text.toString()
			val payerPostcode = binding.editPayerPostcode.text.toString()

			val jsonData = binding.editJsonData.text.toString()
			val isDualMessagePayment = binding.checkboxDualMessagePayment.isChecked

			var payer = PaymentDataPayer()
			payer.firstName = payerFirstName
			payer.lastName = payerLastName
			payer.middleName = payerMiddleName
			payer.birthDay = payerBirthDay
			payer.address = payerAddress
			payer.street = payerStreet
			payer.city = payerCity
			payer.country = payerCountry
			payer.phone = payerPhone
			payer.postcode = payerPostcode

			val paymentData = PaymentData(
				amount = amount,
				currency = currency,
				invoiceId = invoiceId,
				description = description,
				accountId = accountId,
				email = email,
				payer = payer,
				jsonData = jsonData
			)

			val configuration = PaymentConfiguration(
				publicId = publicId,
				paymentData = paymentData,
				scanner = CardIOScanner(),
				showEmailField = true,
				useDualMessagePayment = isDualMessagePayment,
				disableGPay = false,
				apiUrl = apiUrl
			)
			cpSdkLauncher.launch(configuration)
		}
	}
}