package ru.cloudpayments.demo.base

import androidx.recyclerview.widget.RecyclerView

abstract class BaseListActivity<T : RecyclerView.Adapter<*>?> : BaseActivity() {
	protected var adapter: T? = null
}