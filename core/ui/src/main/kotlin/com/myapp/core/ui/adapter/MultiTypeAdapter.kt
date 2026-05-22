package com.myapp.core.ui.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

abstract class MultiTypeAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val items = mutableListOf<Any>()
    private val delegates = mutableMapOf<Int, ViewHolderDelegate<Any>>()

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> registerDelegate(viewType: Int, delegate: ViewHolderDelegate<T>) {
        delegates[viewType] = delegate as ViewHolderDelegate<Any>
    }

    fun addItem(item: Any) {
        items.add(item)
        notifyItemInserted(items.size - 1)
    }

    fun addItems(newItems: List<Any>) {
        val start = items.size
        items.addAll(newItems)
        notifyItemRangeInserted(start, newItems.size)
    }

    fun setItems(newItems: List<Any>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    fun clearItems() {
        items.clear()
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = items.size

    override fun getItemViewType(position: Int): Int = getViewTypeForItem(items[position])

    abstract fun getViewTypeForItem(item: Any): Int

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return delegates[viewType]?.createViewHolder(parent)
            ?: error("No delegate registered for viewType $viewType")
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val viewType = getItemViewType(position)
        delegates[viewType]?.bindViewHolder(holder, items[position])
    }
}

interface ViewHolderDelegate<T : Any> {
    fun createViewHolder(parent: ViewGroup): RecyclerView.ViewHolder
    fun bindViewHolder(holder: RecyclerView.ViewHolder, item: T)
}
