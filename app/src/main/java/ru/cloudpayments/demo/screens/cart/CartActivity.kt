package ru.cloudpayments.demo.screens.cart

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import ru.cloudpayments.demo.Constants
import ru.cloudpayments.demo.R
import ru.cloudpayments.demo.base.BaseListActivity
import ru.cloudpayments.demo.databinding.ActivityCartBinding
import ru.cloudpayments.demo.managers.CartManager
import ru.cloudpayments.demo.models.Product
import ru.cloudpayments.demo.screens.checkout.CheckoutActivity
import ru.cloudpayments.demo.support.CardIOScanner
import ru.cloudpayments.demo.support.SideSpaceItemDecoration
import ru.cloudpayments.sdk.configuration.CloudpaymentsSDK
import ru.cloudpayments.sdk.configuration.PaymentConfiguration
import ru.cloudpayments.sdk.configuration.PaymentData

class CartActivity : BaseListActivity<CartAdapter?>(), CartAdapter.OnClickListener {
	companion object {
		private const val REQUEST_CODE_PAYMENT = 1
	}

	override val layoutId = R.layout.activity_cart

	private val cpSdkLauncher = CloudpaymentsSDK.getInstance().launcher(this, result = {
		if (it.status != null) {
			if (it.status == CloudpaymentsSDK.TransactionStatus.Succeeded) {
				Toast.makeText(this, "Успешно! Транзакция №${it.transactionId}", Toast.LENGTH_SHORT).show()
				CartManager.getInstance()?.clear()
				finish()
			} else {
				if (it.reasonCode != 0) {
					Toast.makeText(this, "Ошибка! Транзакция №${it.transactionId}. Код ошибки ${it.reasonCode}", Toast.LENGTH_SHORT).show()
				} else {
					Toast.makeText(this, "Ошибка! Транзакция №${it.transactionId}.", Toast.LENGTH_SHORT).show()
				}
			}
		}
	})

	private lateinit var binding: ActivityCartBinding

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		binding =ActivityCartBinding.inflate(layoutInflater)
		val view = binding.root
		setContentView(view)

		setTitle(R.string.cart_title)
		initList()
		initTotal()
		setupClickListeners()
	}

	private fun initList() {
		adapter = CartAdapter()
		adapter!!.setHasStableIds(true)
		adapter!!.setListener(this)

		binding.recyclerView.addItemDecoration(SideSpaceItemDecoration(this, 16, 1, true))
		binding.recyclerView.layoutManager = GridLayoutManager(this, 1)
		binding.recyclerView.adapter = adapter

		CartManager.getInstance()?.addProduct(Product())

		adapter!!.update(CartManager.getInstance()?.getProducts().orEmpty())
	}

	private fun initTotal() {
		var total = 0
		val products = CartManager.getInstance()?.getProducts().orEmpty()
		products.forEach { product ->
			total += product.price?.toInt() ?: 0
		}
		binding.textTotal.text = getString(R.string.cart_total_currency, total.toString())
	}

	private fun setupClickListeners(){
		binding.textPhone.setOnClickListener {
			val phone: String = getString(R.string.main_phone)
			val intent = Intent(Intent.ACTION_DIAL)
			intent.data = Uri.parse("tel:$phone")
			startActivity(intent)
		}

		binding.textEmail.setOnClickListener {
			val email: String = getString(R.string.main_email)
			val emailIntent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$email"))
			startActivity(Intent.createChooser(emailIntent, getString(R.string.main_select_app)))
		}

		binding.buttonGoToPayment.setOnClickListener {
			val sources = arrayOf("Своя форма оплаты", "Форма оплаты CloudPayments")

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

							val paymentData = PaymentData(
								Constants.merchantPublicId,
								total.toString(),
								currency = "RUB",
								jsonData = jsonData
							)

							val configuration = PaymentConfiguration(
								paymentData,
								CardIOScanner(),
								showEmailField = true,
								email = "test@cp.ru",
								useDualMessagePayment = false,
								disableGPay = false,
								disableYandexPay = false,
								yandexPayMerchantID = "1423423564546767575"
							)
							cpSdkLauncher.launch(configuration)
						}
					}
				}

			builder.show()
		}
	}

	override fun onProductClick(item: Product?) {

	}

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) = when (requestCode) {
		REQUEST_CODE_PAYMENT -> {
			val transactionId = data?.getIntExtra(CloudpaymentsSDK.IntentKeys.TransactionId.name, 0) ?: 0
			val transactionStatus = data?.getSerializableExtra(CloudpaymentsSDK.IntentKeys.TransactionStatus.name) as? CloudpaymentsSDK.TransactionStatus

			if (transactionStatus != null) {
				if (transactionStatus == CloudpaymentsSDK.TransactionStatus.Succeeded) {
					Toast.makeText(this, "Успешно! Транзакция №$transactionId", Toast.LENGTH_SHORT).show()
					CartManager.getInstance()?.clear()
					finish()
				} else {
					val reasonCode = data.getIntExtra(CloudpaymentsSDK.IntentKeys.TransactionReasonCode.name, 0) ?: 0
					if (reasonCode > 0) {
						Toast.makeText(this, "Ошибка! Транзакция №$transactionId. Код ошибки $reasonCode", Toast.LENGTH_SHORT).show()
					} else {
						Toast.makeText(this, "Ошибка! Транзакция №$transactionId.", Toast.LENGTH_SHORT).show()
					}
				}
			}

			Unit
		}
		else -> super.onActivityResult(requestCode, resultCode, data)
	}
}