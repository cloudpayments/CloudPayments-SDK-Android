package ru.cloudpayments.sdk.ui.dialogs.base

import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.core.content.ContextCompat
import com.google.android.material.textfield.TextInputEditText
import ru.cloudpayments.sdk.R
import ru.cloudpayments.sdk.configuration.PaymentConfiguration
import ru.cloudpayments.sdk.ui.PaymentActivity
import ru.cloudpayments.sdk.viewmodel.BaseViewModel
import ru.cloudpayments.sdk.viewmodel.BaseViewState

internal abstract class BasePaymentDialogFragment<VS: BaseViewState, VM: BaseViewModel<VS>>: BaseVMDialogFragment<VS, VM>() {
	interface IPaymentFragment {
		fun paymentWillFinish()
	}

	private fun getConfiguration(): PaymentConfiguration? {
		if (activity is PaymentActivity) {
			return (activity as PaymentActivity).paymentConfiguration
		}
		return null
	}

	protected val paymentConfiguration by lazy {
		getConfiguration()
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		val fadeAnim = AnimationUtils.loadAnimation(requireContext(), R.anim.cpsdk_fade_in)
		fadeAnim.fillAfter = true

		val slideAnim = AnimationUtils.loadAnimation(requireContext(), R.anim.cpsdk_slide_in)
		slideAnim.fillAfter = true
	}

	protected fun close(force: Boolean, completion: (() -> (Unit))? = null){
		val slideAnim = AnimationUtils.loadAnimation(requireContext(), R.anim.cpsdk_slide_out)
		slideAnim.fillAfter = true
		slideAnim.setAnimationListener(object : Animation.AnimationListener{
			override fun onAnimationStart(animation: Animation?) {
			}

			override fun onAnimationEnd(animation: Animation?) {
				animation?.setAnimationListener(null)
				requireActivity().supportFragmentManager.popBackStack()
				if (force) {
					val listener = requireActivity() as? IPaymentFragment
					listener?.paymentWillFinish()
				}
				completion?.invoke()
			}

			override fun onAnimationRepeat(animation: Animation?) {
			}
		})

		val fadeAnim = AnimationUtils.loadAnimation(requireContext(), R.anim.cpsdk_fade_out)
		fadeAnim.fillAfter = true
	}

	fun handleBackButton(){
		close(true)
	}

	internal fun activity(): PaymentActivity {
		return activity as  PaymentActivity
	}

	protected fun errorMode(isErrorMode: Boolean, editText: TextInputEditText){
		if (isErrorMode) {
			editText.setTextColor(ContextCompat.getColor(requireContext(), R.color.cpsdk_pale_red))
			editText.setBackgroundResource(R.drawable.cpsdk_bg_edit_text_selector_error)
		} else {
			editText.setTextColor(ContextCompat.getColor(requireContext(), R.color.cpsdk_dark))
			editText.setBackgroundResource(R.drawable.cpsdk_bg_edit_text_selector)
		}
	}
}