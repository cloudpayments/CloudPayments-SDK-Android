package ru.cloudpayments.demo.base

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import com.afollestad.materialdialogs.MaterialDialog
import io.reactivex.disposables.CompositeDisposable
import ru.cloudpayments.sdk.api.models.CloudpaymentsTransactionError
import ru.cloudpayments.demo.R
import ru.cloudpayments.demo.databinding.ToolbarBinding
import java.net.UnknownHostException
import java.util.*

abstract class BaseActivity : AppCompatActivity() {
	protected val TAG = "TAG_" + javaClass.simpleName.toUpperCase(Locale.getDefault())
	protected var compositeDisposable = CompositeDisposable()
	private var loadingDialog: MaterialDialog? = null
	protected abstract val layoutId: Int


	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		val toolbar = ToolbarBinding.inflate(layoutInflater).root
		setSupportActionBar(toolbar)
		supportActionBar?.setDisplayHomeAsUpEnabled(true)
		initLoadingDialog()
	}

	private fun initLoadingDialog() {
		loadingDialog = MaterialDialog.Builder(this)
			.progress(true, 0)
			.title(R.string.dialog_loading_title)
			.content(R.string.dialog_loading_content)
			.cancelable(false)
			.build()
	}


	/*@Override
    public void onDestroy() {
        super.onDestroy();
        compositeSubscription.unsubscribe();
    }*/

	fun showLoading() {
		if (loadingDialog?.isShowing == true) {
			return
		}
		loadingDialog?.show()
	}

	fun hideLoading() {
		if (loadingDialog?.isShowing != true) {
			return
		}
		loadingDialog?.dismiss()
	}

	override fun onOptionsItemSelected(item: MenuItem): Boolean {
		when (item.itemId) {
			android.R.id.home -> {
				onBackPressed()
				return true
			}
		}
		return super.onOptionsItemSelected(item)
	}

	fun showToast(@StringRes resId: Int) {
		showToast(getString(resId))
	}

	fun showToast(message: String?) {
		Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
	}

	fun log(message: String?) {
		Log.d(TAG, message.orEmpty())
	}

	fun handleError(throwable: Throwable, vararg ignoreClasses: Class<*>?) {
		if (ignoreClasses.isNotEmpty()) {
			val classList = listOf(*ignoreClasses)
			if (classList.contains(throwable.javaClass)) {
				return
			}
		}
		when (throwable) {
			is CloudpaymentsTransactionError -> {
				val message: String = throwable.message
				showToast(message)
			}
			is UnknownHostException -> showToast(R.string.common_no_internet_connection)
			else -> showToast(throwable.message)
		}
	}
}