package ru.cloudpayments.sdk.viewmodel

import androidx.lifecycle.MutableLiveData
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
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
	private val email: String?
): BaseViewModel<PaymentProcessViewState>() {
	override var currentState = PaymentProcessViewState()
	override val viewState: MutableLiveData<PaymentProcessViewState> by lazy {
		MutableLiveData(currentState)
	}

	private var disposable: Disposable? = null

	@Inject
	lateinit var api: CloudpaymentsApi

	fun charge() {
		val body = PaymentRequestBody(amount = paymentData.amount,
									  currency = paymentData.currency,
									  ipAddress = "",
									  name = "",
									  cryptogram = cryptogram,
									  email = email)
		disposable = api.charge(body)
			.toObservable()
			.observeOn(AndroidSchedulers.mainThread())
			.map { response ->
				checkTransactionResponse(response)
			}
			.onErrorReturn {
				val state = currentState.copy(status = PaymentProcessStatus.Failed)
				stateChanged(state)
			}
			.subscribe()
	}

	fun postThreeDs(md: String, paRes: String) {
		disposable = api.postThreeDs(md, currentState.transaction?.threeDsCallbackId ?: "", paRes)
			.toObservable()
			.observeOn(AndroidSchedulers.mainThread())
			.map {
				val state: PaymentProcessViewState = if (it.success) {
					currentState.copy(status = PaymentProcessStatus.Succeeded)
				} else {
					currentState.copy(status = PaymentProcessStatus.Failed, errorMessage = it.message)
				}

				stateChanged(state)
			}
			.subscribe()
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
					errorMessage = transactionResponse.message
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
						errorMessage = transactionResponse.transaction?.cardHolderMessage
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

		disposable?.dispose()
	}
}

internal data class PaymentProcessViewState(
	val status: PaymentProcessStatus = PaymentProcessStatus.InProcess,
	val succeeded: Boolean = false,
	val transaction: CloudpaymentsTransaction? = null,
	val paReq: String? = null,
	val acsUrl: String? = null,
	val errorMessage: String? = null
): BaseViewState()