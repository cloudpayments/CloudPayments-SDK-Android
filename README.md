[![](https://jitpack.io/v/cloudpayments/CloudPayments-SDK-Android.svg)](https://jitpack.io/#cloudpayments/CloudPayments-SDK-Android)

## CloudPayments SDK for Android 

CloudPayments SDK позволяет интегрировать прием платежей в мобильные приложение для платформы Android.

### Требования
Для работы CloudPayments SDK необходим Android версии 4.4 или выше (API level 19)

### Подключение
В build.gradle уровня проекта добавить репозиторий Jitpack

```
repositories {
	maven { url 'https://jitpack.io' }
}
```
В build.gradle уровня приложения добавить зависимость
```
<<<<<<< HEAD
implementation 'com.github.cloudpayments:CloudPayments-SDK-Android:latest-version'
=======
implementation 'com.github.cloudpayments:CloudPayments-SDK-Android:1.0.2'
>>>>>>> c664493706ce5c1864ddf173650cace5f909e42f
```
### Структура проекта:

* **app** - Пример реализации приложения с использованием SDK
* **sdk** - Исходный код SDK


### Возможности CloudPayments SDK:

Вы можете использовать SDK одним из трех способов: 
* использовать стандартную платежную форму Cloudpayments
* реализовать свою платежную форму с использованием функций CloudpaymentsApi без вашего сервера
* реализовать свою платежную форму, сформировать криптограмму и отправить ее на свой сервер

### Использование стандартной платежной формы Cloudpayments:

1. Создайте объект PaymentData, передайте в него Public Id из [личного кабинета Cloudpayments](https://merchant.cloudpayments.ru/), сумму платежа и валюту.

```
val paymentData = PaymentData(Constants.merchantPublicId, "10.00", "RUB")
```

2. Создайте объект PaymentConfiguration, передайте в него объект PaymentData.

```
val configuration = PaymentConfiguration(paymentData)
```

3. Вызовите форму оплаты. При вызове формы передайте requestCode и activity, в onActivityResult которого получите результат оплаты

```
CloudpaymentsSDK.getInstance().start(configuration, this, REQUEST_CODE_PAYMENT)

val transactionId = data?.getIntExtra(CloudpaymentsSDK.IntentKeys.TransactionId.name, 0)
val transactionStatus = data?.getSerializableExtra(CloudpaymentsSDK.IntentKeys.TransactionStatus.name) as? CloudpaymentsSDK.TransactionStatus

if (transactionStatus != null) {
    // Значит платеж завершился (успешно или ошибкой)
}

```

### Использование вашей платежной формы с использованием функций CloudpaymentsApi:

1. Создайте криптограмму карточных данных

```
// Обязательно проверяйте входящие данные карты (номер, срок действия и cvc код) на корректность, иначе функция создания криптограммы вернет null.
val cardCryptogram = Card.cardCryptogram(cardNumber, cardDate, cardCVC, Constants.MERCHANT_PUBLIC_ID)
```

2. Выполните запрос на проведения платежа. Создайте объект CloudpaymentApi и вызовите функцию auth для одностадийного платежа или charge для двухстадийного. Укажите email, на который будет выслана квитанция об оплате.

```
val api = CloudpaymentsSDK.createApi(Constants.merchantPublicId)
val body = PaymentRequestBody(amount = "10.00", currency = "RUB", ipAddress = "85.54.125.55", name = cardHolderName, cryptogram = cardCryptogramPacket)
api.charge(body)
	.toObservable()
	.flatMap(CloudpaymentsTransactionResponse::handleError)
	.map { it.transaction }
```

3. Если необходимо, покажите 3DS форму для подтверждения платежа

```
val acsUrl = transaction.acsUrl
val paReq = transaction.paReq
val md = transaction.transactionId
ThreeDsDialogFragment
	.newInstance(acsUrl, paReq, md)
	.show(supportFragmentManager, "3DS")
```

4. Для получения формы 3DS и получения результатов прохождения 3DS аутентификации реализуйте протокол ThreeDSDialogListener. Передайте в запрос также threeDsCallbackId, полученный в ответ на auth или charge

```
override fun onAuthorizationCompleted(md: String, paRes: String) {
	api.postThreeDs(transactionId, threeDsCallbackId, paRes)
}

override fun onAuthorizationFailed(error: String?) {
	Log.d("Error", "AuthorizationFailed: $error")
}
```

#### Подключение Google Pay  через CloudPayments

[О Google Pay](https://cloudpayments.ru/wiki/integration/products/googlepay)

[Документация](https://developers.google.com/payments/setup)

#### Включение Google Pay 

В файл build.gradle подключите следующую зависимость:

```
implementation 'com.google.android.gms:play-services-wallet:18.1.2'
```

В файл манифест приложения добавьте мета информацию:

```
<meta-data
	android:name="com.google.android.gms.wallet.api.enabled"
	android:value="true" />
```
#### Проведение платежа через Google Pay с помощью формы Cloudpayments

Никаких дополнительных шагов не требуется. Форма автоматически определяет, подключен Google Pay или нет. В зависимости от этого покажется форма выбора способа оплаты (Google Pay или карта) или форма ввода карточных данных

#### Проведение платежа через Google Pay в своей форме

Сконфигурируйте параметры:

```
PaymentMethodTokenizationParameters params =
		PaymentMethodTokenizationParameters.newBuilder()
				.setPaymentMethodTokenizationType(WalletConstants.PAYMENT_METHOD_TOKENIZATION_TYPE_PAYMENT_GATEWAY)
				.addParameter("gateway", "cloudpayments")
				.addParameter("gatewayMerchantId", "Ваш Public ID")
				.build();
```

Укажите тип оплаты через шлюз (Wallet-Constants.PAYMENT_METHOD_TOKENIZATION_TYPE_PAYMENT_GATEWAY) и добавьте два параметра:

1) gateway: cloudpayments

2) gatewayMerchantId: Ваш Public ID, его можно посмотреть в [личном кабинете](https://merchant.cloudpayments.ru/).

С этими параметрами запросите токен Google Pay:

```
String tokenGP = paymentData.getPaymentMethodToken().getToken();
```

Используя токен Google Pay в качестве криптограммы карточных данных, совершите платёж методами API, указанными ранее.

**В случае проведения платежа с токеном Google Pay в качестве имени держателя карты неоходимо указать: "Google Pay"**

### Другие функции

* Проверка карточного номера на корректность

```
Card.isValidNumber(cardNumber)

```

* Проверка срока действия карты

```
Card.isValidExpDate(expDate) // expDate в формате MM/yy

```

* Определение типа платежной системы

```
let cardType: CardType = Card.cardType(from: cardNumberString)
```

* Определение банка эмитента

```
val api = CloudpaymentsSDK.createApi(Constants.merchantPublicId)
api.getBinInfo(firstSixDigits)
	.subscribeOn(Schedulers.io())
	.observeOn(AndroidSchedulers.mainThread())
	.subscribe({ info -> Log.d("Bank name", info.bankName.orEmpty()) }, this::handleError)
```

* Шифрование карточных данных и создание криптограммы для отправки на сервер

```
val cardCryptogram = Card.cardCryptogram(cardNumber, cardDate, cardCVC, Constants.MERCHANT_PUBLIC_ID)
```

* Шифрование cvv при оплате сохраненной картой и создание криптограммы для отправки на сервер

```
val cvvCryptogramPacket = Card.cardCryptogramForCVV(cvv)
```

* Отображение 3DS формы и получении результата 3DS аутентификации

```
val acsUrl = transaction.acsUrl
val paReq = transaction.paReq
val md = transaction.transactionId
ThreeDsDialogFragment
	.newInstance(acsUrl, paReq, md)
	.show(supportFragmentManager, "3DS")

interface ThreeDSDialogListener {
	fun onAuthorizationCompleted(md: String, paRes: String)
	fun onAuthorizationFailed(error: String?)
}
```

* Сканер карт
Вы можете подключить любой сканер карт, который вызывается с помощью Activity. Для этого нужно реализовать протокол CardScanner и передать объект, реализующий протокол, при создании PaymentConfiguration. Если протокол не будет реализован, то кнопка сканирования не будет показана

Пример со сканером CardIO

```
@Parcelize
class CardIOScanner: CardScanner() {
	override fun getScannerIntent(context: Context) =
		Intent(context, CardIOActivity::class.java).apply {
			putExtra(CardIOActivity.EXTRA_REQUIRE_EXPIRY, true)
		}

	override fun getCardDataFromIntent(data: Intent) =
		if (data.hasExtra(CardIOActivity.EXTRA_SCAN_RESULT)) {
			val scanResult = data.getParcelableExtra(CardIOActivity.EXTRA_SCAN_RESULT) as? CreditCard
			val month = (scanResult?.expiryMonth ?: 0).toString().padStart(2, '0')
			val yearString = scanResult?.expiryYear?.toString() ?: "00"
			val year = if (yearString.length > 2) {
				yearString.substring(yearString.lastIndex - 1)
			} else {
				yearString.padStart(2, '0')
			}
			val cardData = CardData(scanResult?.cardNumber, month, year, scanResult?.cardholderName)
			cardData
		} else {
			null
		}
}
```

### Поддержка

По возникающим вопросам техничечкого характера обращайтесь на support@cloudpayments.ru
