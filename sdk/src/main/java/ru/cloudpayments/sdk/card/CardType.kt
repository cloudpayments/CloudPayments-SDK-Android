package ru.cloudpayments.sdk.card

import ru.cloudpayments.sdk.R
import java.util.*

enum class CardType {
	UNKNOWN,
	VISA,
	MASTER_CARD,
	MAESTRO,
	MIR,
	JCB;

	companion object {
		fun fromString(value: String): CardType = when(value.toLowerCase(Locale.getDefault())){
			"visa" -> VISA
			"mastercard" -> MASTER_CARD
			"maestro" -> MAESTRO
			"mir" -> MIR
			"jcb" -> JCB
			else -> UNKNOWN
		}

		fun getType(cardNumber: String?): CardType {
			if (cardNumber == null || cardNumber.isEmpty()) return UNKNOWN
			val first = Integer.valueOf(cardNumber.substring(0, 1))
			if (first == 4) return VISA
			if (first == 6) return MAESTRO
			if (cardNumber.length < 2) return UNKNOWN
			val firstTwo = Integer.valueOf(cardNumber.substring(0, 2))
			if (firstTwo == 35) return JCB
			if (firstTwo == 50 || (firstTwo in 56..58)) return MAESTRO
			if (firstTwo in 51..55) return MASTER_CARD
			if (cardNumber.length < 4) return UNKNOWN
			val firstFour = Integer.valueOf(cardNumber.substring(0, 4))
			if (firstFour in 2200..2204) return MIR
			return if (firstFour in 2221..2720) MASTER_CARD else UNKNOWN
		}
	}

	override fun toString(): String {
		return when (this) {
			VISA -> "Visa"
			MASTER_CARD -> "MasterCard"
			MAESTRO -> "Maestro"
			MIR -> "MIR"
			JCB -> "JCB"
			else -> "Unknown"
		}
	}

	fun getIconRes(): Int? = when (this) {
		VISA -> R.drawable.ic_ps_visa
		MASTER_CARD -> R.drawable.ic_ps_mastercard
		MAESTRO -> R.drawable.ic_ps_maestro
		MIR -> R.drawable.ic_ps_mir
		JCB -> R.drawable.ic_ps_jcb
		else -> null
	}
}