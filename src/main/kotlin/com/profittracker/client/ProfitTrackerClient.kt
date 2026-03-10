package com.profittracker.client

import com.profittracker.client.model.ProfitTrackerConfig
import com.profittracker.client.service.ConfigManager
import com.profittracker.client.service.ProfitTrackerService
import com.profittracker.client.service.SuggestionService
import com.profittracker.client.ui.HudRenderer
import com.profittracker.client.ui.ProfitTrackerConfigScreen
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback
import net.minecraft.client.MinecraftClient
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.util.InputUtil
import org.lwjgl.glfw.GLFW

object ProfitTrackerClient : ClientModInitializer {
    private lateinit var configManager: ConfigManager
    private lateinit var config: ProfitTrackerConfig
    private lateinit var trackerService: ProfitTrackerService
    private lateinit var hudRenderer: HudRenderer
    private lateinit var suggestionService: SuggestionService

    override fun onInitializeClient() {
        val client = MinecraftClient.getInstance()
        configManager = ConfigManager(client.runDirectory.toPath().resolve("config"))
        config = configManager.load()
        trackerService = ProfitTrackerService()
        suggestionService = SuggestionService()
        hudRenderer = HudRenderer(client, trackerService, config)

        val openConfigKeybind = KeyBindingHelper.registerKeyBinding(
            KeyBinding("key.profit_tracker.open_config", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_K, "category.profit_tracker")
        )

        ClientTickEvents.END_CLIENT_TICK.register(ClientTickEvents.EndTick { mc ->
            trackerService.tick(mc, config)
            while (openConfigKeybind.wasPressed()) {
                mc.setScreen(ProfitTrackerConfigScreen(mc, config, configManager, hudRenderer, suggestionService))
            }
            handleHudManipulation(mc)
        })

        HudRenderCallback.EVENT.register(HudRenderCallback { context, _ ->
            if (client.currentScreen !is ProfitTrackerConfigScreen) {
                hudRenderer.render(context)
            }
        })
    }

    private var dragging = false
    private var resizing = false
    private var lastMouseX = 0
    private var lastMouseY = 0

    private fun handleHudManipulation(mc: MinecraftClient) {
        if (mc.currentScreen != null) return

        val window = mc.window
        val mouse = mc.mouse
        val scale = window.scaleFactor
        val mx = (mouse.x / scale).toInt()
        val my = (mouse.y / scale).toInt()
        val leftDown = GLFW.glfwGetMouseButton(window.handle, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS

        val onHud = mx in config.hudX..(config.hudX + config.hudWidth) && my in config.hudY..(config.hudY + config.hudHeight)
        val onResizeCorner = mx in (config.hudX + config.hudWidth - 10)..(config.hudX + config.hudWidth) &&
            my in (config.hudY + config.hudHeight - 10)..(config.hudY + config.hudHeight)

        if (leftDown && !dragging && !resizing && onResizeCorner) {
            resizing = true
            lastMouseX = mx
            lastMouseY = my
        } else if (leftDown && !dragging && !resizing && onHud) {
            dragging = true
            lastMouseX = mx
            lastMouseY = my
        }

        if (leftDown && (dragging || resizing)) {
            val dx = mx - lastMouseX
            val dy = my - lastMouseY
            if (dragging) hudRenderer.dragBy(dx, dy)
            if (resizing) hudRenderer.resizeBy(dx, dy)
            lastMouseX = mx
            lastMouseY = my
            configManager.save(config)
        }

        if (!leftDown) {
            dragging = false
            resizing = false
        }
    }
}
