package ru.cloudpayments.sdk.models

import android.content.Context
import ru.cloudpayments.sdk.R

class ApiError {

	companion object {

		fun getFullErrorDescription(context: Context, code: String): String {
			var error = getErrorDescription(context, code)
			var errorExtra = getErrorDescriptionExtra(context, code)
			return "$error. $errorExtra"
		}

		fun getErrorDescription(context: Context, code: String): String {
			return when(code) {
				"3001" -> context.getString(R.string.cpsdk_error_3001)
				"3002" -> context.getString(R.string.cpsdk_error_3002)
				"3003" -> context.getString(R.string.cpsdk_error_3003)
				"3004" -> context.getString(R.string.cpsdk_error_3004)
				"3005" -> context.getString(R.string.cpsdk_error_3005)
				"3006" -> context.getString(R.string.cpsdk_error_3006)
				"3007" -> context.getString(R.string.cpsdk_error_3007)
				"3008" -> context.getString(R.string.cpsdk_error_3008)
				"5001" -> context.getString(R.string.cpsdk_error_5001)
				"5005" -> context.getString(R.string.cpsdk_error_5005)
				"5006" -> context.getString(R.string.cpsdk_error_5006)
				"5012" -> context.getString(R.string.cpsdk_error_5012)
				"5013" -> context.getString(R.string.cpsdk_error_5013)
				"5030" -> context.getString(R.string.cpsdk_error_5030)
				"5031" -> context.getString(R.string.cpsdk_error_5031)
				"5034" -> context.getString(R.string.cpsdk_error_5034)
				"5041" -> context.getString(R.string.cpsdk_error_5041)
				"5043" -> context.getString(R.string.cpsdk_error_5043)
				"5051" -> context.getString(R.string.cpsdk_error_5051)
				"5054" -> context.getString(R.string.cpsdk_error_5054)
				"5057" -> context.getString(R.string.cpsdk_error_5057)
				"5065" -> context.getString(R.string.cpsdk_error_5065)
				"5082" -> context.getString(R.string.cpsdk_error_5082)
				"5091" -> context.getString(R.string.cpsdk_error_5091)
				"5092" -> context.getString(R.string.cpsdk_error_5092)
				"5096" -> context.getString(R.string.cpsdk_error_5096)
				"5204" -> context.getString(R.string.cpsdk_error_5204)
				"5206" -> context.getString(R.string.cpsdk_error_5206)
				"5207" -> context.getString(R.string.cpsdk_error_5207)
				"5300" -> context.getString(R.string.cpsdk_error_5300)
				else -> context.getString(R.string.cpsdk_error_5204)
			}
		}

		fun getErrorDescriptionExtra(context: Context, code: String): String {
			return when(code) {
				"3001" -> context.getString(R.string.cpsdk_error_3001_extra)
				"3002" -> context.getString(R.string.cpsdk_error_3002_extra)
				"3003" -> context.getString(R.string.cpsdk_error_3003_extra)
				"3004" -> context.getString(R.string.cpsdk_error_3004_extra)
				"3005" -> context.getString(R.string.cpsdk_error_3005_extra)
				"3006" -> context.getString(R.string.cpsdk_error_3006_extra)
				"3007" -> context.getString(R.string.cpsdk_error_3007_extra)
				"3008" -> context.getString(R.string.cpsdk_error_3008_extra)
				"5001" -> context.getString(R.string.cpsdk_error_5001_extra)
				"5005" -> context.getString(R.string.cpsdk_error_5005_extra)
				"5006" -> context.getString(R.string.cpsdk_error_5006_extra)
				"5012" -> context.getString(R.string.cpsdk_error_5012_extra)
				"5013" -> context.getString(R.string.cpsdk_error_5013_extra)
				"5030" -> context.getString(R.string.cpsdk_error_5030_extra)
				"5031" -> context.getString(R.string.cpsdk_error_5031_extra)
				"5034" -> context.getString(R.string.cpsdk_error_5034_extra)
				"5041" -> context.getString(R.string.cpsdk_error_5041_extra)
				"5043" -> context.getString(R.string.cpsdk_error_5043_extra)
				"5051" -> context.getString(R.string.cpsdk_error_5051_extra)
				"5054" -> context.getString(R.string.cpsdk_error_5054_extra)
				"5057" -> context.getString(R.string.cpsdk_error_5057_extra)
				"5065" -> context.getString(R.string.cpsdk_error_5065_extra)
				"5082" -> context.getString(R.string.cpsdk_error_5082_extra)
				"5091" -> context.getString(R.string.cpsdk_error_5091_extra)
				"5092" -> context.getString(R.string.cpsdk_error_5092_extra)
				"5096" -> context.getString(R.string.cpsdk_error_5096_extra)
				"5204" -> context.getString(R.string.cpsdk_error_5204_extra)
				"5206" -> context.getString(R.string.cpsdk_error_5206_extra)
				"5207" -> context.getString(R.string.cpsdk_error_5207_extra)
				"5300" -> context.getString(R.string.cpsdk_error_5300_extra)
				else -> context.getString(R.string.cpsdk_error_5204_extra)
			}
		}
	}
}