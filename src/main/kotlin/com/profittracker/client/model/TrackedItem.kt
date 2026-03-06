package com.profittracker.client.model

data class TrackedItem(
    var itemId: String,
    var displayName: String,
    var value: Long,
    var enabled: Boolean = true
)
