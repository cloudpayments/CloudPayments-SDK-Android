package ru.cloudpayments.sdk.api.models

enum class ErrorCodes(val code: Int, val message: String?) {
	INSUFFICIENT_FUNDS(5051, "Недостаточно средств на карте")
}