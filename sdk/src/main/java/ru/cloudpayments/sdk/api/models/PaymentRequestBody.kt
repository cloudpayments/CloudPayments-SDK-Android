package ru.cloudpayments.sdk.api.models

import com.google.gson.annotations.SerializedName

data class PaymentRequestBody(
		@SerializedName("Amount") val amount: String, // Сумма (Обязательный)
		@SerializedName("Currency") val currency: String, // Валюта (Обязательный)
		@SerializedName("IpAddress") val ipAddress: String, // IP адрес плательщика (Обязательный)
		@SerializedName("Name") val name: String, // Имя держателя карты в латинице (Обязательный для всех платежей кроме Apple Pay и Google Pay)
		@SerializedName("CardCryptogramPacket") val cryptogram: String, // Криптограмма платежных данных (Обязательный)
		@SerializedName("Email") val email: String? = null, // E-mail, на который будет отправлена квитанция об оплате)
		@SerializedName("InvoiceId") val invoiceId: String? = null, // Номер счета или заказа в вашей системе (необязательный)
		@SerializedName("Description") val description: String? = null, // Описание оплаты в свободной форме (необязательный)
		@SerializedName("AccountId") val accountId: String? = null, // Идентификатор пользователя в вашей системе (необязательный)
		@SerializedName("JsonData") val jsonData: String? = null) //"{\"age\":27,\"name\":\"Ivan\",\"phone\":\"+79998881122\"}"  Любые другие данные, которые будут связаны с транзакцией (необязательный)