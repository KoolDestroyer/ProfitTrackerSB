package com.profittracker.client.model

data class TrackerState(
    var sessionStart: Long = System.currentTimeMillis(),
    var totalProfit: Long = 0,
    val collectedCounts: MutableMap<String, Long> = mutableMapOf(),
    val activeItems: MutableSet<String> = mutableSetOf(),
    var lastActivityTime: Long = System.currentTimeMillis(),
    var paused: Boolean = false
)
