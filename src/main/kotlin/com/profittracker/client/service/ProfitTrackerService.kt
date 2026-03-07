package com.profittracker.client.service

import com.profittracker.client.model.ProfitTrackerConfig
import com.profittracker.client.model.TrackerState
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.ItemEntity
import net.minecraft.item.Item
import net.minecraft.registry.Registries

class ProfitTrackerService {
    val state: TrackerState = TrackerState()
    private val previousInventoryCounts: MutableMap<Item, Int> = mutableMapOf()
    private var tickCounter: Int = 0

    fun tick(client: MinecraftClient, config: ProfitTrackerConfig) {
        val world = client.world ?: return
        val player = client.player ?: return

        tickCounter++
        if (tickCounter % 10 != 0) {
            updatePauseState(config)
            return
        }

        val tracked = config.trackedItems.filter { it.enabled }.associateBy { it.itemId.lowercase() }
        state.activeItems.clear()
        state.activeItems.addAll(tracked.keys)

        val entities = world.entities.filterIsInstance<ItemEntity>()
        for (entity in entities) {
            val id = Registries.ITEM.getId(entity.stack.item).toString().lowercase()
            if (id in tracked && entity.distanceTo(player) <= 2.5f) {
                registerPickup(id, entity.stack.count.toLong(), tracked.getValue(id).value)
            }
        }

        val inventory = player.inventory.main
        val current = mutableMapOf<Item, Int>()
        inventory.forEach { stack ->
            if (!stack.isEmpty) {
                current[stack.item] = (current[stack.item] ?: 0) + stack.count
            }
        }

        current.forEach { (item, count) ->
            val old = previousInventoryCounts[item] ?: 0
            if (count > old) {
                val id = Registries.ITEM.getId(item).toString().lowercase()
                val trackedItem = tracked[id]
                if (trackedItem != null) {
                    registerPickup(id, (count - old).toLong(), trackedItem.value)
                }
            }
        }

        previousInventoryCounts.clear()
        previousInventoryCounts.putAll(current)

        updatePauseState(config)
    }

    private fun registerPickup(itemId: String, amount: Long, valueEach: Long) {
        state.totalProfit += amount * valueEach
        state.collectedCounts[itemId] = (state.collectedCounts[itemId] ?: 0L) + amount
        state.lastActivityTime = System.currentTimeMillis()
        state.paused = false
    }

    private fun updatePauseState(config: ProfitTrackerConfig) {
        val now = System.currentTimeMillis()
        state.paused = now - state.lastActivityTime > config.idleSeconds * 1000L
    }

    fun profitPerHour(): Long {
        val elapsedMs = (System.currentTimeMillis() - state.sessionStart).coerceAtLeast(1L)
        return (state.totalProfit * 3_600_000L) / elapsedMs
    }
}
