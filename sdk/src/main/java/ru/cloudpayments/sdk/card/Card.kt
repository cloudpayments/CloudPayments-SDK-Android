package ru.cloudpayments.sdk.card

import android.text.TextUtils
import android.util.Base64
import java.io.UnsupportedEncodingException
import java.security.*
import java.security.spec.InvalidKeySpecException
import java.security.spec.X509EncodedKeySpec
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*
import javax.crypto.BadPaddingException
import javax.crypto.Cipher
import javax.crypto.IllegalBlockSizeException
import javax.crypto.NoSuchPaddingException

class Card {
	companion object {

		private fun getKeyVersion(): String {
			return "04"
		}

		private fun getPublicKey(): String {
			return "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEArBZ1NNjvszen6BNWsgyDUJvDUZDtvR4jKNQtEwW1iW7hqJr0TdD8hgTxw3DfH+Hi/7ZjSNdH5EfChvgVW9wtTxrvUXCOyJndReq7qNMo94lHpoSIVW82dp4rcDB4kU+q+ekh5rj9Oj6EReCTuXr3foLLBVpH0/z1vtgcCfQzsLlGkSTwgLqASTUsuzfI8viVUbxE1a+600hN0uBh/CYKoMnCp/EhxV8g7eUmNsWjZyiUrV8AA/5DgZUCB+jqGQT/Dhc8e21tAkQ3qan/jQ5i/QYocA/4jW3WQAldMLj0PA36kINEbuDKq8qRh25v+k4qyjb7Xp4W2DywmNtG3Q20MQIDAQAB"
		}

		fun getType(number: String): CardType? {
			return CardType.fromString(number)
		}

		fun isValidNumber(cardNumber: String?): Boolean {
			return if (cardNumber == null) {
				false
			} else {
				val number = prepareCardNumber(cardNumber)
				return if (TextUtils.isEmpty(number) || number.length < 14) {
					false
				} else {
					var sum = 0
					if (number.length % 2 == 0) {
						for (i in number.indices step 2) {
							var c = number.substring(i, i + 1).toInt()
							c *= 2
							if (c > 9) {
								c -= 9
							}
							sum += c
							sum += number.substring(i + 1, i + 2).toInt()
						}
					} else {
						for (i in 1 until number.length step 2) {
							var c: Int = number.substring(i, i + 1).toInt()
							c *= 2
							if (c > 9) {
								c -= 9
							}
							sum += c
							sum += number.substring(i - 1, i).toInt()
						}
					}
					sum % 10 == 0
				}
			}
		}

		fun isValidExpDate(exp: String?): Boolean {
			return if (exp == null) {
				false
			} else {
				val expDate = exp.replace("/", "")
				if (expDate.length != 4) {
					false
				} else {
					val format: DateFormat = SimpleDateFormat("MMyy", Locale.ENGLISH)
					format.isLenient = false
					return try {
						var date = format.parse(expDate)
						val calendar = Calendar.getInstance()
						calendar.time = date ?: Date()
						calendar[Calendar.DAY_OF_MONTH] = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
						date = calendar.time
						//val currentDate = Date()
						val currentDate = format.parse("0222")
						currentDate.before(date)
					} catch (e: ParseException) {
						e.printStackTrace()
						false
					}
				}
			}
		}

		fun isValidCvv(cardNumber: String?, cvv: String?): Boolean {
			return if (cvv?.length == 3) {
				true
			} else isUzcardCard(cardNumber) || isHumoCard(cardNumber)
		}

		fun isUzcardCard(cardNumber: String?): Boolean {
			//Uzcard 8600
			if (cardNumber?.length!! > 3 && cardNumber?.substring(0, 4) == "8600") {
				return true
			}
			return false
		}

		fun isHumoCard(cardNumber: String?): Boolean {
			//Humo 9860
			if (cardNumber?.length!! > 3 && cardNumber?.substring(0, 4) == "9860") {
				return true
			}
			return false
		}

		@Throws(UnsupportedEncodingException::class, NoSuchPaddingException::class, NoSuchAlgorithmException::class, BadPaddingException::class,
				IllegalBlockSizeException::class, InvalidKeyException::class)
		fun cardCryptogram(number: String, cardExp: String, cardCvv: String, publicId: String): String? {
			val cardNumber = prepareCardNumber(number)
			var exp = cardExp.replace("/", "")
			if (cardNumber.length < 14 || exp.length != 4) {
				return null
			}
			val shortNumber =
				cardNumber.substring(0, 6) + cardNumber.substring(cardNumber.length - 4, cardNumber.length)

			exp = exp.substring(2, 4) + cardExp.substring(0, 2)
			val s = "$cardNumber@$exp@$cardCvv@$publicId"
			val bytes = s.toByteArray(charset("ASCII"))
			val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
			val random = SecureRandom()
			cipher.init(Cipher.ENCRYPT_MODE, getRSAKey(), random)
			val crypto = cipher.doFinal(bytes)
			var crypto64 = "01" +
					shortNumber +
					exp + getKeyVersion() +
					Base64.encodeToString(crypto, Base64.DEFAULT)
			val cr_array = crypto64.split("\n").toTypedArray()
			crypto64 = ""
			for (i in cr_array.indices) {
				crypto64 += cr_array[i]
			}
			return crypto64
		}

		/**
		 * Генерим криптограму для CVV
		 * @param cardCvv
		 * @return
		 * @throws UnsupportedEncodingException
		 * @throws NoSuchPaddingException
		 * @throws NoSuchAlgorithmException
		 * @throws BadPaddingException
		 * @throws IllegalBlockSizeException
		 * @throws InvalidKeyException
		 */

		@Throws(UnsupportedEncodingException::class, NoSuchPaddingException::class, NoSuchAlgorithmException::class, BadPaddingException::class, IllegalBlockSizeException::class, InvalidKeyException::class)
		fun cardCryptogramForCVV(cardCvv: String): String? {
			val bytes = cardCvv.toByteArray(charset("ASCII"))
			val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
			val random = SecureRandom()
			cipher.init(Cipher.ENCRYPT_MODE, getRSAKey(), random)
			val crypto = cipher.doFinal(bytes)
			var crypto64 = "03" + getKeyVersion() + Base64.encodeToString(crypto, Base64.DEFAULT)
			val crArray = crypto64.split("\n").toTypedArray()
			crypto64 = ""
			for (i in crArray.indices) {
				crypto64 += crArray[i]
			}
			return crypto64
		}

		private fun prepareCardNumber(cardNumber: String): String {
			return cardNumber.replace("\\s".toRegex(), "")
		}

		private fun getRSAKey(): PublicKey? {
			return try {
				val keyBytes: ByteArray =
					Base64.decode(getPublicKey().toByteArray(charset("utf-8")), Base64.DEFAULT)
				val spec = X509EncodedKeySpec(keyBytes)
				val kf: KeyFactory = KeyFactory.getInstance("RSA")
				kf.generatePublic(spec)
			} catch (e: NoSuchAlgorithmException) {
				e.printStackTrace()
				null
			} catch (e: InvalidKeySpecException) {
				e.printStackTrace()
				null
			} catch (e: UnsupportedEncodingException) {
				e.printStackTrace()
				null
			}
		}
	}
}