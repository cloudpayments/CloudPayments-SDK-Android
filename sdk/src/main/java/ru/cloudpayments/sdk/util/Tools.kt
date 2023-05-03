package ru.cloudpayments.sdk.util

import java.util.regex.Pattern

fun emailIsValid(email: String?): Boolean {
	if (email.isNullOrBlank()) {
		return false
	}

	val emailPattern = "^.+@([A-Za-z0-9-]+\\.)+[A-Za-z]{2}[A-Za-z]*\$"
	return Pattern.compile(emailPattern).matcher(email).matches()
}