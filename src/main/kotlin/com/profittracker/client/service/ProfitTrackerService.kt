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
    private val recentlyCountedEntities: MutableMap<Int, Long> = mutableMapOf()
    private var tickCounter: Int = 0

    fun tick(client: MinecraftClient, config: ProfitTrackerConfig) {
        val world = client.world ?: return
        val player = client.player ?: return

        tickCounter++
        if (tickCounter % 10 != 0) {
            updatePauseState(config)
            cleanupEntityCache()
            return
        }

        val trackedById = config.trackedItems
            .filter { it.enabled }
            .associateBy { it.itemId.lowercase() }

        state.activeItems.clear()
        state.activeItems.addAll(trackedById.keys)

        val inventoryDeltas = scanInventoryDeltas(player.inventory.main)
        val remainingInventorySignals = inventoryDeltas.toMutableMap()

        inventoryDeltas.forEach { (itemId, delta) ->
            val tracked = trackedById[itemId] ?: return@forEach
            registerPickup(itemId, delta.toLong(), tracked.value)
        }

        world.entities
            .filterIsInstance<ItemEntity>()
            .forEach { entity ->
                if (entity.distanceTo(player) > 2.5f) return@forEach

                val itemId = Registries.ITEM.getId(entity.stack.item).toString().lowercase()
                val tracked = trackedById[itemId] ?: return@forEach
                if (recentlyCountedEntities.containsKey(entity.id)) return@forEach

                val remainingInventoryDelta = remainingInventorySignals[itemId] ?: 0
                val entityCount = entity.stack.count
                if (remainingInventoryDelta > 0) {
                    remainingInventorySignals[itemId] = (remainingInventoryDelta - entityCount).coerceAtLeast(0)
                } else {
                    registerPickup(itemId, entityCount.toLong(), tracked.value)
                }
                recentlyCountedEntities[entity.id] = System.currentTimeMillis()
            }

        updatePauseState(config)
        cleanupEntityCache()
    }

    private fun scanInventoryDeltas(mainInventory: List<net.minecraft.item.ItemStack>): Map<String, Int> {
        val currentCounts = mutableMapOf<Item, Int>()
        mainInventory.forEach { stack ->
            if (!stack.isEmpty) {
                currentCounts[stack.item] = (currentCounts[stack.item] ?: 0) + stack.count
            }
        }

        val deltasByItemId = mutableMapOf<String, Int>()
        currentCounts.forEach { (item, count) ->
            val previous = previousInventoryCounts[item] ?: 0
            if (count > previous) {
                val itemId = Registries.ITEM.getId(item).toString().lowercase()
                deltasByItemId[itemId] = (deltasByItemId[itemId] ?: 0) + (count - previous)
            }
        }

        previousInventoryCounts.clear()
        previousInventoryCounts.putAll(currentCounts)
        return deltasByItemId
    }

    private fun registerPickup(itemId: String, amount: Long, valueEach: Long) {
        if (amount <= 0) return
        state.totalProfit += amount * valueEach
        state.collectedCounts[itemId] = (state.collectedCounts[itemId] ?: 0L) + amount
        state.lastActivityTime = System.currentTimeMillis()
        state.paused = false
    }

    private fun updatePauseState(config: ProfitTrackerConfig) {
        val idleMillis = config.idleSeconds.coerceAtLeast(1) * 1000L
        state.paused = System.currentTimeMillis() - state.lastActivityTime > idleMillis
    }

    private fun cleanupEntityCache() {
        val cutoff = System.currentTimeMillis() - 15_000L
        recentlyCountedEntities.entries.removeIf { it.value < cutoff }
    }

    fun profitPerHour(now: Long = System.currentTimeMillis()): Long {
        val elapsedMs = (now - state.sessionStart).coerceAtLeast(1L)
        return (state.totalProfit * 3_600_000L) / elapsedMs
    }
}
