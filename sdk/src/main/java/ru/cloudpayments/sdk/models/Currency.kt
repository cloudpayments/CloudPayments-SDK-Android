package ru.cloudpayments.sdk.models

class Currency {

	companion object {

		val CODE_RUB = "RUB"
		val CODE_USD = "USD"
		val CODE_EUR = "EUR"
		val CODE_GBP = "GBP"
		val CODE_KZT = "KZT"
		val CODE_BYN = "BYN"
		val CODE_UAH = "UAH"
		val CODE_CHF = "CHF"
		val CODE_AZN = "AZN"
		val CODE_CZK = "CZK"
		val CODE_CAD = "CAD"
		val CODE_PLN = "PLN"
		val CODE_SEK = "SEK"
		val CODE_TRY = "TRY"
		val CODE_CNY = "CNY"
		val CODE_INR = "INR"
		val CODE_BRL = "BRL"
		val CODE_ZAR = "ZAR"

		val SYMBOL_RUB = "\u20BD"
		val SYMBOL_USD = "$"
		val SYMBOL_EUR = "€"
		val SYMBOL_GBP = "£"
		val SYMBOL_KZT = "₸"
		val SYMBOL_BYN = "Br"
		val SYMBOL_UAH = "грн"
		val SYMBOL_CHF = "Fr"
		val SYMBOL_AZN = "man"
		val SYMBOL_CZK = "Kč"
		val SYMBOL_CAD = "C$"
		val SYMBOL_PLN = "zł"
		val SYMBOL_SEK = "kr"
		val SYMBOL_TRY = "₺"
		val SYMBOL_CNY = "CNY"
		val SYMBOL_INR = "र"
		val SYMBOL_BRL = "R$"
		val SYMBOL_ZAR = "R"

		fun getSymbol(code: String): String {
			when (code) {
				CODE_RUB -> return SYMBOL_RUB
				CODE_USD -> return SYMBOL_USD
				CODE_EUR -> return SYMBOL_EUR
				CODE_GBP -> return SYMBOL_GBP
				CODE_KZT -> return SYMBOL_KZT
				CODE_BYN -> return SYMBOL_BYN
				CODE_UAH -> return SYMBOL_UAH
				CODE_CHF -> return SYMBOL_CHF
				CODE_AZN -> return SYMBOL_AZN
				CODE_CZK -> return SYMBOL_CZK
				CODE_CAD -> return SYMBOL_CAD
				CODE_PLN -> return SYMBOL_PLN
				CODE_SEK -> return SYMBOL_SEK
				CODE_TRY -> return SYMBOL_TRY
				CODE_CNY -> return SYMBOL_CNY
				CODE_INR -> return SYMBOL_INR
				CODE_BRL -> return SYMBOL_BRL
				CODE_ZAR -> return SYMBOL_ZAR
			}
			return code
		}
	}
}