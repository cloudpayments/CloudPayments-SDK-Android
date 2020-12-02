package ru.cloudpayments.sdk.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.dialog_payment_card.*
import ru.cloudpayments.sdk.R
import ru.cloudpayments.sdk.configuration.PaymentConfiguration
import ru.cloudpayments.sdk.ui.PaymentActivity
import ru.cloudpayments.sdk.viewmodel.BaseViewModel
import ru.cloudpayments.sdk.viewmodel.BaseViewState

internal abstract class BasePaymentFragment<VS: BaseViewState, VM: BaseViewModel<VS>>: BaseVMFragment<VS, VM>() {
	interface IPaymentFragment {
		fun paymentWillFinish()
	}

	companion object {
		private const val ARG_CONFIGURATION = "ARG_CONFIGURATION"
	}

	protected fun setConfiguration(configuration: PaymentConfiguration) {
		arguments?.putParcelable(ARG_CONFIGURATION, configuration)
	}

	protected val paymentConfiguration by lazy {
		arguments?.getParcelable<PaymentConfiguration>(ARG_CONFIGURATION)
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		val fadeAnim = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in)
		fadeAnim.fillAfter = true
		background.startAnimation(fadeAnim)

		val slideAnim = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_in)
		slideAnim.fillAfter = true
		content.startAnimation(slideAnim)
	}

	protected fun close(force: Boolean, completion: (() -> (Unit))? = null){
		val slideAnim = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_out)
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
		content.startAnimation(slideAnim)

		val fadeAnim = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_out)
		fadeAnim.fillAfter = true
		background.startAnimation(fadeAnim)
	}

	fun handleBackButton(){
		close(true)
	}

	internal fun activity(): PaymentActivity {
		return activity as  PaymentActivity
	}
}