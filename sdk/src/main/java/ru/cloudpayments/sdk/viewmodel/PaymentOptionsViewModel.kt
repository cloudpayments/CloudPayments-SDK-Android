package ru.cloudpayments.sdk.viewmodel

import androidx.lifecycle.MutableLiveData
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import ru.cloudpayments.sdk.api.CloudpaymentsApi
import javax.inject.Inject

internal class PaymentOptionsViewModel: BaseViewModel<PaymentOptionsViewState>() {
    override var currentState = PaymentOptionsViewState()
    override val viewState: MutableLiveData<PaymentOptionsViewState> by lazy {
        MutableLiveData(currentState)
    }

    private var disposable: Disposable? = null

    @Inject
    lateinit var api: CloudpaymentsApi

    fun getPublicKey() {
        disposable = api.getPublicKey()
            .toObservable()
            .observeOn(AndroidSchedulers.mainThread())
            .map { response ->
                val state = currentState.copy(publicKeyPem = response.pem, publicKeyVersion = response.version)
                stateChanged(state)
            }
            .onErrorReturn {

            }
            .subscribe()
    }

    fun getMerchantConfiguration(publicId: String) {
        disposable = api.getMerchantConfiguration(publicId)
            .toObservable()
            .observeOn(AndroidSchedulers.mainThread())
            .map { response ->

                var isTinkoffPayAvailable = false

                for (paymentMethod in response.model?.externalPaymentMethods!!) {
                    if (paymentMethod.type == 6) {
                        isTinkoffPayAvailable = paymentMethod.enabled!!
                        break
                    }
                }

                val state = currentState.copy(isTinkoffPayAvailable = isTinkoffPayAvailable, isSaveCard = response.model?.features?.isSaveCard)
                stateChanged(state)
            }
            .onErrorReturn {

            }
            .subscribe()
    }

    private fun stateChanged(viewState: PaymentOptionsViewState) {
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

internal data class PaymentOptionsViewState(
    val publicKeyPem: String? = null,
    val publicKeyVersion: Int? = null,
    val isTinkoffPayAvailable: Boolean? = null,
    val isSaveCard: Int? = null
): BaseViewState()