package ru.cloudpayments.sdk.viewmodel

import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.Job
import ru.cloudpayments.sdk.api.CloudpaymentsApi
import javax.inject.Inject

internal class PaymentOptionsViewModel: BaseViewModel<PaymentOptionsViewState>() {
    override var currentState = PaymentOptionsViewState()
    override val viewState: MutableLiveData<PaymentOptionsViewState> by lazy {
        MutableLiveData(currentState)
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