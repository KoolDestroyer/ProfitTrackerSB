package com.profittracker.client.ui

import com.profittracker.client.model.TrackedItem

class TrackedItemsWidget(private val items: MutableList<TrackedItem>) {
    fun add(item: TrackedItem) {
        items.add(item)
    }

    fun remove(index: Int) {
        if (index in items.indices) items.removeAt(index)
    }

    fun moveUp(index: Int) {
        if (index > 0 && index < items.size) {
            val item = items.removeAt(index)
            items.add(index - 1, item)
        }
    }

    fun moveDown(index: Int) {
        if (index >= 0 && index < items.lastIndex) {
            val item = items.removeAt(index)
            items.add(index + 1, item)
        }
    }

    fun update(index: Int, updated: TrackedItem) {
        if (index in items.indices) items[index] = updated
    }

    fun toggle(index: Int) {
        if (index in items.indices) {
            items[index].enabled = !items[index].enabled
        }
    }
}
