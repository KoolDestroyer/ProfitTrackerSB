package com.profittracker.client.service

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.InputStream

data class ItemSuggestion(val itemId: String, val displayName: String)

class SuggestionService(
    suggestionsStreamProvider: () -> InputStream = {
        SuggestionService::class.java.classLoader.getResourceAsStream("suggestions.json")
            ?: error("Missing suggestions.json on classpath")
    }
) {
    private val suggestions: List<ItemSuggestion>

    init {
        val json = suggestionsStreamProvider().bufferedReader().use { it.readText() }
        val type = object : TypeToken<List<ItemSuggestion>>() {}.type
        suggestions = Gson().fromJson(json, type)
    }

    fun complete(query: String, limit: Int = 5): List<ItemSuggestion> {
        if (query.isBlank()) return suggestions.take(limit)
        val q = query.trim().lowercase()

        val startsWith = suggestions.filter {
            it.displayName.lowercase().startsWith(q) || it.itemId.lowercase().startsWith(q)
        }
        val contains = suggestions.filter {
            it !in startsWith &&
                (it.displayName.lowercase().contains(q) || it.itemId.lowercase().contains(q))
        }
        return (startsWith + contains).take(limit)
    }
}
