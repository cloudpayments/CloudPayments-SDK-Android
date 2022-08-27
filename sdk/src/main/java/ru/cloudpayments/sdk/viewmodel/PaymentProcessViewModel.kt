package ru.cloudpayments.sdk.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import com.google.gson.Gson
import ru.cloudpayments.sdk.api.CloudpaymentsApi
import ru.cloudpayments.sdk.api.models.CloudpaymentsTransaction
import ru.cloudpayments.sdk.api.models.CloudpaymentsTransactionResponse
import ru.cloudpayments.sdk.api.models.PaymentRequestBody
import ru.cloudpayments.sdk.configuration.PaymentData
import ru.cloudpayments.sdk.ui.dialogs.PaymentProcessStatus
import javax.inject.Inject

internal class PaymentProcessViewModel(
	private val paymentData: PaymentData,
	private val cryptogram: String,
	private val email: String?,
	private val useDualMessagePayment: Boolean
): BaseViewModel<PaymentProcessViewState>() {
	override var currentState = PaymentProcessViewState()
	override val viewState: MutableLiveData<PaymentProcessViewState> by lazy {
		MutableLiveData(currentState)
	}

	private var disposable: Job? = null

	@Inject
	lateinit var api: CloudpaymentsApi

	fun pay() {
		val jsonString = if (paymentData.jsonData != null) {
			Gson().toJson(paymentData.jsonData)
		} else {
			""
		}
		val body = PaymentRequestBody(amount = paymentData.amount,
									  currency = paymentData.currency,
									  ipAddress = paymentData.ipAddress ?: "",
									  name = paymentData.cardholderName ?: "",
									  cryptogram = cryptogram,
									  email = email,
									  invoiceId = paymentData.invoiceId ?: "",
									  description = paymentData.description ?: "",
									  accountId = paymentData.accountId ?: "",
									  jsonData = jsonString)
		viewModelScope.launch {
			if (useDualMessagePayment) {
				try {
					val response = api.auth(body)
					checkTransactionResponse(response)
				} catch (e: Exception) {
					val state = currentState.copy(status = PaymentProcessStatus.Failed)
					stateChanged(state)
				}
			} else {
				try {
					val response = api.charge(body)
					checkTransactionResponse(response)
				} catch (e: Exception) {
					val state = currentState.copy(status = PaymentProcessStatus.Failed)
					stateChanged(state)
				}
			}
		}
	}

	fun postThreeDs(md: String, paRes: String) {
		disposable = viewModelScope.launch {
			val response = api.postThreeDs(md, currentState.transaction?.threeDsCallbackId ?: "", paRes)
			val state: PaymentProcessViewState = if (response.success) {
				currentState.copy(status = PaymentProcessStatus.Succeeded)
			} else {
				currentState.copy(status = PaymentProcessStatus.Failed, errorMessage = response.message, reasonCode = response.reasonCode)
			}

			stateChanged(state)
		}
	}

	fun clearThreeDsData(){
		val state = currentState.copy(acsUrl = null, paReq = null)
		stateChanged(state)
	}

	private fun checkTransactionResponse(transactionResponse: CloudpaymentsTransactionResponse){
		val state = if (transactionResponse.success == true) {
			currentState.copy(
				transaction = transactionResponse.transaction,
				status = PaymentProcessStatus.Succeeded
			)
		} else {
			if (!transactionResponse.message.isNullOrEmpty()) {
				currentState.copy(
					transaction = transactionResponse.transaction,
					status = PaymentProcessStatus.Failed,
					errorMessage = transactionResponse.message,
					reasonCode = transactionResponse.transaction?.reasonCode
				)
			} else {
				val paReq = transactionResponse.transaction?.paReq
				val acsUrl = transactionResponse.transaction?.acsUrl

				if (!paReq.isNullOrEmpty() && !acsUrl.isNullOrEmpty()) {
					currentState.copy(
						transaction = transactionResponse.transaction,
						paReq = paReq,
						acsUrl = acsUrl
					)
				} else {
					currentState.copy(
						transaction = transactionResponse.transaction,
						status = PaymentProcessStatus.Failed,
						errorMessage = transactionResponse.transaction?.cardHolderMessage,
						reasonCode = transactionResponse.transaction?.reasonCode
					)
				}
			}
		}

		stateChanged(state)
	}

	private fun stateChanged(viewState: PaymentProcessViewState) {
		currentState = viewState.copy()
		this.viewState.apply {
			value = viewState
		}
	}

	override fun onCleared() {
		super.onCleared()

		disposable?.cancel()
	}
}

internal data class PaymentProcessViewState(
	val status: PaymentProcessStatus = PaymentProcessStatus.InProcess,
	val succeeded: Boolean = false,
	val transaction: CloudpaymentsTransaction? = null,
	val paReq: String? = null,
	val acsUrl: String? = null,
	val errorMessage: String? = null,
	val reasonCode: Int? = null
): BaseViewState()