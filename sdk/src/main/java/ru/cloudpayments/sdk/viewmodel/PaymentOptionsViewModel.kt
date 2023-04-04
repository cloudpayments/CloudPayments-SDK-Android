package ru.cloudpayments.sdk.viewmodel

import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import ru.cloudpayments.sdk.api.CloudpaymentsApi
import javax.inject.Inject

internal class PaymentOptionsViewModel: BaseViewModel<PaymentOptionsViewState>() {
    override var currentState = PaymentOptionsViewState()
    override val viewState: StateFlow<PaymentOptionsViewState> by lazy {
        MutableStateFlow(currentState)
    }

    private var disposable: Job? = null

    @Inject
    lateinit var api: CloudpaymentsApi

    override fun onCleared() {
        super.onCleared()

        disposable?.cancel()
    }
}

internal data class PaymentOptionsViewState(val a: String? = null): BaseViewState()