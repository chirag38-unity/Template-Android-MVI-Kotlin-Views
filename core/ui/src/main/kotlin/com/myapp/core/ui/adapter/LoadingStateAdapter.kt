package com.myapp.core.ui.adapter

import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ProgressBar
import androidx.recyclerview.widget.RecyclerView

class LoadingStateAdapter(
    private val retry: () -> Unit,
) : RecyclerView.Adapter<LoadingStateAdapter.ViewHolder>() {

    private var isLoading = false
    private var isError = false

    fun showLoading() {
        isLoading = true
        isError = false
        notifyDataSetChanged()
    }

    fun showError() {
        isLoading = false
        isError = true
        notifyDataSetChanged()
    }

    fun hide() {
        isLoading = false
        isError = false
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = if (isLoading || isError) 1 else 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val container = FrameLayout(parent.context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            )
        }
        val progressBar = ProgressBar(parent.context).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
            ).also { params ->
                params.gravity = android.view.Gravity.CENTER
            }
        }
        container.addView(progressBar)
        val holder = ViewHolder(container)
        container.setOnClickListener { if (isError) retry() }
        return holder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = Unit

    class ViewHolder(val container: FrameLayout) : RecyclerView.ViewHolder(container)
}
