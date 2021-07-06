package ru.cloudpayments.demo.screens.cart

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.activity_cart.*
import ru.cloudpayments.sdk.configuration.CloudpaymentsSDK
import ru.cloudpayments.sdk.configuration.PaymentConfiguration
import ru.cloudpayments.sdk.configuration.PaymentData
import ru.cloudpayments.demo.Constants
import ru.cloudpayments.demo.R
import ru.cloudpayments.demo.base.BaseListActivity
import ru.cloudpayments.demo.managers.CartManager
import ru.cloudpayments.demo.models.Product
import ru.cloudpayments.demo.screens.checkout.CheckoutActivity
import ru.cloudpayments.demo.support.CardIOScanner
import ru.cloudpayments.demo.support.SideSpaceItemDecoration

class CartActivity : BaseListActivity<CartAdapter?>(), CartAdapter.OnClickListener {

	override val layoutId = R.layout.activity_cart

	private val paymentCaller: ActivityResultLauncher<Intent> = registerForActivityResult(
		ActivityResultContracts.StartActivityForResult()
	) { result ->
		val data = result.data
		val transactionId = data?.getIntExtra(CloudpaymentsSDK.IntentKeys.TransactionId.name, 0) ?: 0
		val transactionStatus = data?.getSerializableExtra(CloudpaymentsSDK.IntentKeys.TransactionStatus.name) as? CloudpaymentsSDK.TransactionStatus


		if (transactionStatus != null) {
			if (transactionStatus == CloudpaymentsSDK.TransactionStatus.Succeeded) {
				Toast.makeText(this, "Успешно! Транзакция №$transactionId", Toast.LENGTH_SHORT).show()
				CartManager.getInstance()?.clear()
				finish()
			} else {
				val reasonCode =
					data.getIntExtra(CloudpaymentsSDK.IntentKeys.TransactionReasonCode.name, 0)
				if (reasonCode > 0) {
					Toast.makeText(this, "Ошибка! Транзакция №$transactionId. Код ошибки $reasonCode", Toast.LENGTH_SHORT).show()
				} else {
					Toast.makeText(this, "Ошибка! Транзакция №$transactionId.", Toast.LENGTH_SHORT).show()
				}
			}
		}
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setTitle(R.string.cart_title)
		initList()
		initTotal()
		setupClickListeners()
	}

	private fun initList() {
		adapter = CartAdapter()
		adapter!!.setHasStableIds(true)
		adapter!!.setListener(this)

		recycler_view.addItemDecoration(SideSpaceItemDecoration(this, 16, 1, true))
		recycler_view.layoutManager = GridLayoutManager(this, 1)
		recycler_view.adapter = adapter
		adapter!!.update(CartManager.getInstance()?.getProducts().orEmpty())
	}

	private fun initTotal() {
		var total = 0
		val products = CartManager.getInstance()?.getProducts().orEmpty()
		products.forEach { product ->
			total += product.price?.toInt() ?: 0
		}
		text_total.text = getString(R.string.cart_total_currency, total.toString())
	}

	private fun setupClickListeners(){
		text_phone.setOnClickListener {
			val phone: String = getString(R.string.main_phone)
			val intent = Intent(Intent.ACTION_DIAL)
			intent.data = Uri.parse("tel:$phone")
			startActivity(intent)
		}

		text_email.setOnClickListener {
			val email: String = getString(R.string.main_email)
			val emailIntent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$email"))
			startActivity(Intent.createChooser(emailIntent, getString(R.string.main_select_app)))
		}

		button_go_to_payment.setOnClickListener {
			val sources = arrayOf("Своя форма оплаты", "Форма оплаты Cloudpayments")

			val builder = MaterialAlertDialogBuilder(this)
				.setTitle("Выберите источник")
				.setItems(sources){_, which ->
					when (which) {
						0 -> startActivity(Intent(this, CheckoutActivity::class.java))
						1 -> {
							var total = 0.0
							val products = CartManager.getInstance()?.getProducts().orEmpty()
							products.forEach {
								total += it.price?.toInt() ?: 0
							}
							val jsonData: HashMap<String, Any> = hashMapOf("name" to "Иван")
							val paymentData = PaymentData(Constants.merchantPublicId, total.toString(), "RUB", jsonData = jsonData)
							val configuration = PaymentConfiguration(paymentData, CardIOScanner())
							val intent = CloudpaymentsSDK.getInstance().getStartIntent(this, configuration)
							paymentCaller.launch(intent)
						}
					}
				}

			builder.show()
		}
	}

	override fun onProductClick(item: Product?) {

	}
}