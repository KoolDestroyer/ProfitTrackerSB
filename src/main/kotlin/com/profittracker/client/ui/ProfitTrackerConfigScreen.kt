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
    private lateinit var itemIdField: TextFieldWidget
    private lateinit var displayNameField: TextFieldWidget
    private lateinit var valueField: TextFieldWidget

    private val widget = TrackedItemsWidget(config.trackedItems)
    private var selectedIndex = -1
    private var lastInteractionTime = System.currentTimeMillis()

    override fun init() {
        val cx = width / 2
        val top = 20

        searchField = addField(cx - 160, top + 14, 320, "Search")
        idleField = addField(cx - 160, top + 44, 70, "Idle seconds", config.idleSeconds.toString())
        itemIdField = addField(cx - 160, top + 74, 200, "Item ID")
        displayNameField = addField(cx + 45, top + 74, 115, "Display")
        valueField = addField(cx - 160, top + 104, 120, "Value")

        addDrawableChild(ButtonWidget.builder(Text.literal("Pick Suggestion")) {
            touch()
            val suggestion = suggestionService.complete(searchField.text, 1).firstOrNull() ?: return@builder
            itemIdField.text = suggestion.itemId
            displayNameField.text = suggestion.displayName
        }.dimensions(cx - 35, top + 104, 130, 20).build())

        addDrawableChild(ButtonWidget.builder(Text.literal("Add")) {
            touch()
            val item = formItem() ?: return@builder
            widget.add(item)
            selectedIndex = config.trackedItems.lastIndex
        }.dimensions(cx + 100, top + 104, 60, 20).build())

        addDrawableChild(ButtonWidget.builder(Text.literal("Apply Edit")) {
            touch()
            if (selectedIndex !in config.trackedItems.indices) return@builder
            val item = formItem() ?: return@builder
            widget.update(selectedIndex, item)
        }.dimensions(cx - 160, top + 132, 90, 20).build())

        addDrawableChild(ButtonWidget.builder(Text.literal("Toggle")) {
            touch()
            widget.toggle(selectedIndex)
            syncFormFromSelected()
        }.dimensions(cx - 65, top + 132, 55, 20).build())

        addDrawableChild(ButtonWidget.builder(Text.literal("Up")) {
            touch()
            widget.moveUp(selectedIndex)
            if (selectedIndex > 0) selectedIndex--
        }.dimensions(cx - 5, top + 132, 40, 20).build())

        addDrawableChild(ButtonWidget.builder(Text.literal("Down")) {
            touch()
            widget.moveDown(selectedIndex)
            if (selectedIndex in 0 until config.trackedItems.lastIndex) selectedIndex++
        }.dimensions(cx + 40, top + 132, 50, 20).build())

        addDrawableChild(ButtonWidget.builder(Text.literal("Remove")) {
            touch()
            widget.remove(selectedIndex)
            selectedIndex = -1
            clearEditFields()
        }.dimensions(cx + 95, top + 132, 65, 20).build())
    }

    override fun tick() {
        super.tick()
        if (System.currentTimeMillis() - lastInteractionTime > 10_000L) {
            close()
        }
    }

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        renderBackground(context, mouseX, mouseY, delta)
        hudRenderer.render(context, preview = true)

        val cx = width / 2
        val top = 20
        val left = cx - 180
        val right = cx + 180
        val bottom = height - 20

        context.fill(left, top, right, bottom, 0xCC111111.toInt())
        context.drawBorder(left, top, right - left, bottom - top, 0xFFFFFFFF.toInt())

        context.drawText(textRenderer, "Search", cx - 160, top + 4, 0xFFFFFF, false)
        context.drawText(textRenderer, "Idle", cx - 160, top + 34, 0xFFFFFF, false)
        context.drawText(textRenderer, "Item id", cx - 160, top + 64, 0xFFFFFF, false)
        context.drawText(textRenderer, "Display", cx + 45, top + 64, 0xFFFFFF, false)
        context.drawText(textRenderer, "Value", cx - 160, top + 94, 0xFFFFFF, false)

        suggestionService.complete(searchField.text).forEachIndexed { index, s ->
            context.drawText(
                textRenderer,
                "${index + 1}. ${s.displayName} (${s.itemId})",
                cx - 160,
                top + 160 + index * 10,
                0xBBD0FF,
                false
            )
        }

        config.trackedItems.forEachIndexed { idx, item ->
            val rowY = top + 220 + idx * 11
            if (rowY > bottom - 10) return@forEachIndexed
            val color = if (idx == selectedIndex) 0xFFFF55 else 0xFFFFFF
            context.drawText(
                textRenderer,
                "${if (item.enabled) "[x]" else "[ ]"} ${item.displayName} (${item.itemId}) = ${item.value}",
                cx - 160,
                rowY,
                color,
                false
            )
        }

        super.render(context, mouseX, mouseY, delta)
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        touch()
        val cx = width / 2
        val top = 20
        val left = cx - 180
        val right = cx + 180
        val bottom = height - 20

        if (mouseX < left || mouseX > right || mouseY < top || mouseY > bottom) {
            close()
            return true
        }

        val row = ((mouseY - (top + 220)) / 11.0).toInt()
        if (row in config.trackedItems.indices) {
            selectedIndex = row
            syncFormFromSelected()
            if (button == 1) widget.toggle(row)
        }

        return super.mouseClicked(mouseX, mouseY, button)
    }

    override fun close() {
        config.idleSeconds = idleField.text.toIntOrNull()?.coerceAtLeast(1) ?: config.idleSeconds
        configManager.save(config)
        clientRef.setScreen(null)
    }

    private fun addField(x: Int, y: Int, w: Int, hint: String, initial: String = ""): TextFieldWidget {
        val field = TextFieldWidget(textRenderer, x, y, w, 18, Text.literal(hint))
        field.text = initial
        field.setChangedListener { touch() }
        addDrawableChild(field)
        return field
    }

    private fun formItem(): TrackedItem? {
        val id = itemIdField.text.trim().lowercase()
        val name = displayNameField.text.trim()
        val value = valueField.text.trim().toLongOrNull()
        if (id.isBlank() || name.isBlank() || value == null || value < 0) return null

        val existing = if (selectedIndex in config.trackedItems.indices) config.trackedItems[selectedIndex] else null
        return TrackedItem(id, name, value, existing?.enabled ?: true)
    }

    private fun syncFormFromSelected() {
        val item = config.trackedItems.getOrNull(selectedIndex) ?: return
        itemIdField.text = item.itemId
        displayNameField.text = item.displayName
        valueField.text = item.value.toString()
    }

    private fun clearEditFields() {
        itemIdField.text = ""
        displayNameField.text = ""
        valueField.text = ""
    }

    private fun touch() {
        lastInteractionTime = System.currentTimeMillis()
    }
}
