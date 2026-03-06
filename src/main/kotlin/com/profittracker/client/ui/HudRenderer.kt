package com.profittracker.client.ui

import com.profittracker.client.model.ProfitTrackerConfig
import com.profittracker.client.service.ProfitTrackerService
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import kotlin.math.max

class HudRenderer(
    private val client: MinecraftClient,
    private val trackerService: ProfitTrackerService,
    private val config: ProfitTrackerConfig
) {
    fun render(context: DrawContext, preview: Boolean = false) {
        val x = config.hudX
        val y = config.hudY
        val width = config.hudWidth
        val height = config.hudHeight

        context.fill(x, y, x + width, y + height, 0x88000000.toInt())
        context.drawBorder(x, y, width, height, 0xFFFFFFFF.toInt())

        val textRenderer = client.textRenderer
        context.drawText(textRenderer, "Profit Tracker", x + 6, y + 6, 0xFFFFFF, true)
        context.drawText(textRenderer, "Total: ${trackerService.state.totalProfit}", x + 6, y + 20, 0x55FF55, false)
        context.drawText(textRenderer, "/hr: ${trackerService.profitPerHour()}", x + 6, y + 32, 0x55FF55, false)
        context.drawText(
            textRenderer,
            if (trackerService.state.paused) "PAUSED" else "ACTIVE",
            x + 6,
            y + 44,
            if (trackerService.state.paused) 0xFF5555 else 0x55FFFF,
            true
        )

        val prefix = if (preview) "Preview items:" else "Tracked items:"
        context.drawText(textRenderer, prefix, x + 6, y + 58, 0xFFFFFF, false)
        config.trackedItems.take(3).forEachIndexed { i, item ->
            val enabledPrefix = if (item.enabled) "[x]" else "[ ]"
            context.drawText(textRenderer, "$enabledPrefix ${item.displayName} (${item.value})", x + 6, y + 70 + (i * 10), 0xDDDDDD, false)
        }

        context.fill(x + width - 8, y + height - 8, x + width - 2, y + height - 2, 0xFFFFFFFF.toInt())
    }

    fun dragBy(dx: Int, dy: Int) {
        config.hudX += dx
        config.hudY += dy
    }

    fun resizeBy(dx: Int, dy: Int) {
        config.hudWidth = max(140, config.hudWidth + dx)
        config.hudHeight = max(80, config.hudHeight + dy)
    }
}
