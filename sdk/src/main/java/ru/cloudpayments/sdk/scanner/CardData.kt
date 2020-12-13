package ru.cloudpayments.sdk.scanner

data class CardData(val cardNumber: String?, // Номер карты без пробелов
					val cardExpMonth: String?, // Месяц. Например, январь - 01
					val cardExpYear: String?, // Последние 2 цифры года
					val cardholderName: String?)