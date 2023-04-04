package ru.cloudpayments.demo.base

import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView

abstract class BaseListActivity<T : RecyclerView.Adapter<*>?>(@LayoutRes layoutRes: Int) : BaseActivity(layoutRes) {
	protected var adapter: T? = null
}