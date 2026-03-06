package com.profittracker.client.service

import com.profittracker.client.model.TrackedItem
import com.profittracker.client.ui.TrackedItemsWidget
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class TrackedItemsWidgetTest {
    @Test
    fun `reorder and toggle should mutate list as expected`() {
        val items = mutableListOf(
            TrackedItem("a", "A", 1),
            TrackedItem("b", "B", 2),
            TrackedItem("c", "C", 3)
        )
        val widget = TrackedItemsWidget(items)

        widget.moveUp(2)
        assertEquals(listOf("a", "c", "b"), items.map { it.itemId })

        widget.moveDown(0)
        assertEquals(listOf("c", "a", "b"), items.map { it.itemId })

        widget.toggle(1)
        assertFalse(items[1].enabled)
    }
}
