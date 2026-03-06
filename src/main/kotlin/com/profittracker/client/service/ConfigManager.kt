package com.profittracker.client.service

import com.google.gson.GsonBuilder
import com.profittracker.client.model.ProfitTrackerConfig
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.exists

class ConfigManager(private val configDir: Path) {
    private val gson = GsonBuilder().setPrettyPrinting().create()
    private val configFile: Path = configDir.resolve("profit-tracker.json")

    fun load(): ProfitTrackerConfig {
        if (!configDir.exists()) {
            Files.createDirectories(configDir)
        }
        if (!configFile.exists()) {
            val defaults = ProfitTrackerConfig()
            save(defaults)
            return defaults
        }
        return gson.fromJson(Files.readString(configFile), ProfitTrackerConfig::class.java)
    }

    fun save(config: ProfitTrackerConfig) {
        if (!configDir.exists()) {
            Files.createDirectories(configDir)
        }
        Files.writeString(configFile, gson.toJson(config))
    }
}
