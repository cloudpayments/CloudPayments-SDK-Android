package ru.cloudpayments.sdk.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.StateFlow

internal abstract class BaseViewModel<VS: BaseViewState> : ViewModel() {
	abstract val viewState: StateFlow<VS>
	abstract var currentState: VS
}