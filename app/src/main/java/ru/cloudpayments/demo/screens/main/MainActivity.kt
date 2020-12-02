package ru.cloudpayments.demo.screens.main

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.GridLayoutManager
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.text_email
import kotlinx.android.synthetic.main.activity_main.text_phone
import ru.cloudpayments.demo.base.BaseListActivity
import ru.cloudpayments.demo.models.Product
import ru.cloudpayments.demo.screens.cart.CartActivity
import ru.cloudpayments.demo.support.SideSpaceItemDecoration
import ru.cloudpayments.demo.R
import ru.cloudpayments.demo.api.ShopApi
import ru.cloudpayments.demo.managers.CartManager

class MainActivity : BaseListActivity<ProductsAdapter?>(), ProductsAdapter.OnClickListener {
	override val layoutId = R.layout.activity_main

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		supportActionBar?.setDisplayHomeAsUpEnabled(false)
		setTitle(R.string.main_title)
		initList()
		getProducts()
		setupClickListeners()
	}

	private fun initList() {
		recycler_view.addItemDecoration(SideSpaceItemDecoration(this, 16, 2, true))
		adapter = ProductsAdapter()
		adapter!!.setHasStableIds(true)
		adapter!!.setListener(this)
		recycler_view.layoutManager = GridLayoutManager(this, 2)
		recycler_view.adapter = adapter
	}

	private fun setupClickListeners() {
		text_phone.setOnClickListener {
			val phone = getString(R.string.main_phone)
			val intent = Intent(Intent.ACTION_DIAL)
			intent.data = Uri.parse("tel:$phone")
			startActivity(intent)
		}

		text_email.setOnClickListener {
			val email = getString(R.string.main_email)
			val emailIntent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$email"))
			startActivity(Intent.createChooser(emailIntent, getString(R.string.main_select_app)))
		}
	}

	override fun onCreateOptionsMenu(menu: Menu?): Boolean {
		menuInflater.inflate(R.menu.main, menu)
		return super.onCreateOptionsMenu(menu)
	}

	override fun onOptionsItemSelected(item: MenuItem): Boolean {
		when (item.itemId) {
			R.id.action_cart -> {
				if (CartManager.getInstance()?.getProducts().orEmpty().isEmpty()) {
					showToast(R.string.main_cart_is_empty)
				} else {
					startActivity(Intent(this, CartActivity::class.java))
				}
			}
		}
		return super.onOptionsItemSelected(item)
	}

	override fun onProductClick(item: Product?) {
		item?.let {
			CartManager.getInstance()?.addProduct(item)
			showToast(R.string.main_product_added_to_cart)
		}
	}

	private fun getProducts() {
		compositeDisposable.add(
			ShopApi.products
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.doOnSubscribe { showLoading() }
				.doOnEach { hideLoading() }
				.subscribe({ products -> adapter?.update(products) }, this::handleError)
		)
	}
}