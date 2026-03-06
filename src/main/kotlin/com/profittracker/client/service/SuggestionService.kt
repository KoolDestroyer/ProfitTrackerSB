package com.profittracker.client.service

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import net.minecraft.client.MinecraftClient

data class ItemSuggestion(val itemId: String, val displayName: String)

class SuggestionService(client: MinecraftClient) {
    private val suggestions: List<ItemSuggestion>

    init {
        val stream = client.resourceManager.getResource(net.minecraft.util.Identifier.of("profit_tracker", "suggestions.json"))
            .orElseThrow { IllegalStateException("Missing suggestions.json") }
            .inputStream
        val json = stream.bufferedReader().use { it.readText() }
        val type = object : TypeToken<List<ItemSuggestion>>() {}.type
        suggestions = Gson().fromJson(json, type)
    }

    fun complete(query: String, limit: Int = 5): List<ItemSuggestion> {
        if (query.isBlank()) return suggestions.take(limit)
        val q = query.trim().lowercase()
        val startsWithMatches = suggestions.filter {
            it.displayName.lowercase().startsWith(q) || it.itemId.lowercase().startsWith(q)
        }
        val containsMatches = suggestions
            .asSequence()
            .filter {
                !startsWithMatches.contains(it) &&
                    (it.displayName.lowercase().contains(q) || it.itemId.lowercase().contains(q))
            }
            .toList()

        return (startsWithMatches + containsMatches).take(limit)
    }
}
