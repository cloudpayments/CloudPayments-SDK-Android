package ru.cloudpayments.demo.base

import androidx.recyclerview.widget.RecyclerView

abstract class BaseAdapter<VH : RecyclerView.ViewHolder?> : RecyclerView.Adapter<VH>()