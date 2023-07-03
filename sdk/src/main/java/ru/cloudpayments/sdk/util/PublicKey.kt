package ru.cloudpayments.sdk.util

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences

class PublicKey private constructor() {

	companion object {
		private val publicKey = PublicKey()
		private lateinit var sharedPreferences: SharedPreferences

		private const val PEM = "pem"
		private const val VERSION = "version"

		fun getInstance(context: Context): PublicKey {
			if (!::sharedPreferences.isInitialized) {
				synchronized(PublicKey::class.java) {
					if (!::sharedPreferences.isInitialized) {
						sharedPreferences = context.getSharedPreferences(context.packageName, Activity.MODE_PRIVATE)
					}
				}
			}
			return publicKey
		}
	}

	val pem: String?
		get() = sharedPreferences.getString(PEM, "")

	fun savePem(pem: String) {
		sharedPreferences.edit()
			.putString(PEM, pem)
			.apply()
	}

	val version: Int?
		get() = sharedPreferences.getInt(VERSION, 0)

	fun saveVersion(version: Int) {
		sharedPreferences.edit()
			.putInt(VERSION, version)
			.apply()
	}

	fun clearAll() {
		sharedPreferences.edit().clear().apply()
	}

}