package com.profittracker.client.model

data class ProfitTrackerConfig(
    var hudX: Int = 20,
    var hudY: Int = 20,
    var hudWidth: Int = 220,
    var hudHeight: Int = 110,
    var idleSeconds: Int = 20,
    var trackedItems: MutableList<TrackedItem> = mutableListOf(
        TrackedItem("minecraft:summoning_eye", "Summoning Eye", 120_000L, true)
    )
)
