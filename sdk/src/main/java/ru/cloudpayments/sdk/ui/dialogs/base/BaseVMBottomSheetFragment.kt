package ru.cloudpayments.sdk.ui.dialogs.base

import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import ru.cloudpayments.sdk.viewmodel.BaseViewModel
import ru.cloudpayments.sdk.viewmodel.BaseViewState

internal abstract class BaseVMBottomSheetFragment<VS: BaseViewState, VM: BaseViewModel<VS>>: BottomSheetDialogFragment() {
	abstract val viewModel: VM
	abstract fun render(state: VS)

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		viewModel.viewState.observe(viewLifecycleOwner, Observer {
			render(it)
		})

	}
	override fun onCancel(dialog: DialogInterface) {
		super.onCancel(dialog)
		activity?.finish()
	}
}