package ru.cloudpayments.sdk.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

internal abstract class BaseViewModel<VS: BaseViewState> : ViewModel() {
	abstract val viewState: MutableLiveData<VS>
	abstract var currentState: VS
}