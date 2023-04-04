package ru.cloudpayments.sdk.ui.dialogs

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import ru.cloudpayments.sdk.viewmodel.BaseViewModel
import ru.cloudpayments.sdk.viewmodel.BaseViewState

internal abstract class BaseVMFragment<VS: BaseViewState, VM: BaseViewModel<VS>>: Fragment() {
	abstract val viewModel: VM
	abstract fun render(state: VS)

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		viewLifecycleOwner.lifecycleScope.launch {
			viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
				viewModel.viewState.collect {
					render(it)
				}
			}
		}
	}
}