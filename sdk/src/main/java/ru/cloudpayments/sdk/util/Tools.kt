package ru.cloudpayments.sdk.util

import android.app.Activity
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import java.util.*
import java.util.regex.Pattern

open class TextWatcherAdapter: TextWatcher {
	override fun afterTextChanged(s: Editable?) {}

	override fun beforeTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {}

	override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {}
}

fun emailIsValid(email: String?): Boolean {
	if (email.isNullOrBlank()) {
		return false
	}

	val emailPattern = "^.+@([A-Za-z0-9-]+\\.)+[A-Za-z]{2}[A-Za-z]*\$"
	return Pattern.compile(emailPattern).matcher(email).matches()
}

fun getRussianLocale() = Locale("ru", "RU")