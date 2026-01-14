package com.mytheclipse.quizbattle.core.base

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

/**
 * Base RecyclerView Adapter with ViewBinding and DiffUtil support
 * Reduces boilerplate for adapter implementations
 * 
 * @param T Data type for list items
 * @param VB ViewBinding type for item views
 */
abstract class BaseAdapter<T, VB : ViewBinding>(
    diffCallback: DiffUtil.ItemCallback<T>
) : ListAdapter<T, BaseAdapter<T, VB>.BaseViewHolder>(diffCallback) {

    /**
     * Provides the ViewBinding inflater for item views
     */
    abstract val bindingInflater: (LayoutInflater, ViewGroup, Boolean) -> VB

    /**
     * Binds data to the ViewBinding
     */
    abstract fun bind(binding: VB, item: T, position: Int)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val binding = bindingInflater(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return BaseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        bind(holder.binding, getItem(position), position)
    }

    inner class BaseViewHolder(val binding: VB) : RecyclerView.ViewHolder(binding.root)
}

/**
 * Base adapter with click listener support
 */
abstract class BaseClickAdapter<T, VB : ViewBinding>(
    diffCallback: DiffUtil.ItemCallback<T>,
    protected val onItemClick: (T) -> Unit
) : BaseAdapter<T, VB>(diffCallback) {

    /**
     * Setup click listener in onCreateViewHolder
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val binding = bindingInflater(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return BaseViewHolder(binding).also { holder ->
            binding.root.setOnClickListener {
                val position = holder.adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(position))
                }
            }
        }
    }
}

/**
 * Helper function to create simple DiffUtil.ItemCallback
 */
inline fun <T> simpleDiffCallback(
    crossinline areItemsSame: (T, T) -> Boolean,
    crossinline areContentsSame: (T, T) -> Boolean = { old, new -> old == new }
): DiffUtil.ItemCallback<T> = object : DiffUtil.ItemCallback<T>() {
    override fun areItemsTheSame(oldItem: T & Any, newItem: T & Any): Boolean {
        return areItemsSame(oldItem, newItem)
    }

    override fun areContentsTheSame(oldItem: T & Any, newItem: T & Any): Boolean {
        return areContentsSame(oldItem, newItem)
    }
}
