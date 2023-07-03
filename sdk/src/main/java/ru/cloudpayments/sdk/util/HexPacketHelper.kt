package ru.cloudpayments.sdk.util

class HexPacketHelper {

	companion object {
		fun numberToEvenLengthString(number: Int): String {
			var numberStr = number.toString()

			return if (numberStr.length % 2 == 0) numberStr
			else "0$numberStr";
		}
	}
}