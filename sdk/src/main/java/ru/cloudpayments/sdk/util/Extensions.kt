package ru.cloudpayments.sdk.util

import android.app.Activity
import android.content.Context
import android.os.Build
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import ru.cloudpayments.sdk.R
import java.text.NumberFormat

fun Activity.hideKeyboard() {
	currentFocus?.let {
		window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
		val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
		inputMethodManager.hideSoftInputFromWindow(it.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
	}
}

fun Activity.showKeyboard(){
	currentFocus?.let {
		val imm = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
		imm.showSoftInput(currentFocus, 0)
	}
}

fun Fragment.nextFragment(fragment: Fragment, addToBackStack: Boolean = true, contentFrame: Int, animated: Boolean = false){
	activity?.nextFragment(fragment, addToBackStack, contentFrame, animated)
}

fun FragmentActivity.nextFragment(fragment: Fragment, addToBackStack: Boolean = true, contentFrame: Int, animated: Boolean = false) {
	hideKeyboard()

	val transaction = supportFragmentManager.beginTransaction()

	if (animated) {
		transaction.setCustomAnimations(R.anim.slide_in, R.anim.slide_out)
	}

	transaction.add(contentFrame, fragment, supportFragmentManager.backStackEntryCount.toString())

	if (addToBackStack) {
		transaction.addToBackStack(fragment::class.java.toString())
	}
	transaction.commit()
}

fun Context.getCurrencyString(currency: Double) = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
	NumberFormat.getCurrencyInstance(getRussianLocale()).format(currency)
} else {
	getString(R.string.currency_template, currency)
}