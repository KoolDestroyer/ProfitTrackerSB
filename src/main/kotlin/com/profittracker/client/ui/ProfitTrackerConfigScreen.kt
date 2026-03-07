package com.profittracker.client.ui

import com.profittracker.client.model.ProfitTrackerConfig
import com.profittracker.client.model.TrackedItem
import com.profittracker.client.service.ConfigManager
import com.profittracker.client.service.SuggestionService
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.gui.widget.TextFieldWidget
import net.minecraft.text.Text

class ProfitTrackerConfigScreen(
    private val clientRef: MinecraftClient,
    private val config: ProfitTrackerConfig,
    private val configManager: ConfigManager,
    private val hudRenderer: HudRenderer,
    private val suggestionService: SuggestionService
) : Screen(Text.literal("Profit Tracker Config")) {
    private lateinit var searchField: TextFieldWidget
    private lateinit var idleField: TextFieldWidget
    private val widget = TrackedItemsWidget(config.trackedItems)
    private var selectedIndex: Int = -1
    private var lastInteractionTime = System.currentTimeMillis()

    override fun init() {
        val centerX = width / 2
        val panelTop = 20

        searchField = TextFieldWidget(textRenderer, centerX - 150, panelTop + 12, 300, 20, Text.literal("Search"))
        searchField.setChangedListener { touch() }
        addDrawableChild(searchField)

        idleField = TextFieldWidget(textRenderer, centerX - 150, panelTop + 36, 80, 20, Text.literal("Idle"))
        idleField.text = config.idleSeconds.toString()
        idleField.setChangedListener { touch() }
        addDrawableChild(idleField)

        addDrawableChild(ButtonWidget.builder(Text.literal("Add")) {
            touch()
            val selected = suggestionService.complete(searchField.text, 1).firstOrNull()
            if (selected != null) {
                widget.add(TrackedItem(selected.itemId, selected.displayName, 1_000L, true))
            }
        }.dimensions(centerX - 60, panelTop + 36, 50, 20).build())

        addDrawableChild(ButtonWidget.builder(Text.literal("Remove")) {
            touch()
            widget.remove(selectedIndex)
            selectedIndex = -1
        }.dimensions(centerX - 5, panelTop + 36, 70, 20).build())

        addDrawableChild(ButtonWidget.builder(Text.literal("Up")) {
            touch(); widget.moveUp(selectedIndex); selectedIndex = (selectedIndex - 1).coerceAtLeast(0)
        }.dimensions(centerX + 70, panelTop + 36, 40, 20).build())

        addDrawableChild(ButtonWidget.builder(Text.literal("Down")) {
            touch(); widget.moveDown(selectedIndex); selectedIndex = (selectedIndex + 1).coerceAtMost(config.trackedItems.lastIndex)
        }.dimensions(centerX + 115, panelTop + 36, 50, 20).build())
    }

    override fun tick() {
        super.tick()
        if (System.currentTimeMillis() - lastInteractionTime > 10_000) {
            close()
        }
    }

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        renderBackground(context, mouseX, mouseY, delta)
        hudRenderer.render(context, preview = true)

        val centerX = width / 2
        val panelTop = 20
        val panelLeft = centerX - 170
        val panelRight = centerX + 170
        val panelBottom = height - 20

        context.fill(panelLeft, panelTop, panelRight, panelBottom, 0xCC101010.toInt())
        context.drawBorder(panelLeft, panelTop, panelRight - panelLeft, panelBottom - panelTop, 0xFFFFFFFF.toInt())
        context.drawText(textRenderer, "Search item", centerX - 150, panelTop + 2, 0xFFFFFF, false)
        context.drawText(textRenderer, "Idle sec", centerX - 150, panelTop + 26, 0xFFFFFF, false)
        context.drawText(textRenderer, "Tracked items", centerX - 150, panelTop + 64, 0xFFFFFF, false)

        val suggestions = suggestionService.complete(searchField.text)
        suggestions.forEachIndexed { index, suggestion ->
            context.drawText(
                textRenderer,
                "${index + 1}. ${suggestion.displayName} (${suggestion.itemId})",
                centerX - 150,
                panelTop + 86 + (index * 10),
                0xAAAACC,
                false
            )
        }

        config.trackedItems.forEachIndexed { index, trackedItem ->
            val color = if (index == selectedIndex) 0xFFFFFF00.toInt() else 0xFFFFFFFF.toInt()
            val rowY = panelTop + 150 + index * 12
            context.drawText(
                textRenderer,
                "${if (trackedItem.enabled) "[x]" else "[ ]"} ${trackedItem.displayName} ${trackedItem.value}",
                centerX - 150,
                rowY,
                color,
                false
            )
        }

        super.render(context, mouseX, mouseY, delta)
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        touch()
        val centerX = width / 2
        val panelTop = 20
        val panelLeft = centerX - 170
        val panelRight = centerX + 170
        val panelBottom = height - 20

        if (mouseX < panelLeft || mouseX > panelRight || mouseY < panelTop || mouseY > panelBottom) {
            close()
            return true
        }

        val listStartY = panelTop + 150
        val hit = ((mouseY - listStartY) / 12.0).toInt()
        if (hit in config.trackedItems.indices) {
            selectedIndex = hit
            if (button == 1) {
                widget.toggle(hit)
            }
        }
        return super.mouseClicked(mouseX, mouseY, button)
    }

    override fun close() {
        config.idleSeconds = idleField.text.toIntOrNull()?.coerceAtLeast(1) ?: config.idleSeconds
        configManager.save(config)
        clientRef.setScreen(null)
    }

    private fun touch() {
        lastInteractionTime = System.currentTimeMillis()
    }
}
