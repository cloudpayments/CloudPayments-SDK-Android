package ru.cloudpayments.sdk.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import ru.cloudpayments.sdk.viewmodel.BaseViewModel
import ru.cloudpayments.sdk.viewmodel.BaseViewState

internal abstract class BaseVMFragment<VS: BaseViewState, VM: BaseViewModel<VS>>: Fragment() {
	abstract val viewModel: VM
	abstract fun render(state: VS)

	abstract fun getLayout(): Int

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		return inflater.inflate(getLayout(), container, false)
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		viewModel.viewState.observe(viewLifecycleOwner, Observer {
			render(it)
		})
	}
}