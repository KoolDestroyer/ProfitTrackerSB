package com.profittracker.client.service

import kotlin.test.Test
import kotlin.test.assertEquals

class SuggestionServiceTest {
    @Test
    fun `complete returns startsWith before contains with cap`() {
        val json = """
            [
              {"itemId":"minecraft:ender_pearl","displayName":"Ender Pearl"},
              {"itemId":"minecraft:end_stone","displayName":"End Stone"},
              {"itemId":"minecraft:enchanted_ender_pearl","displayName":"Enchanted Ender Pearl"},
              {"itemId":"minecraft:blaze_rod","displayName":"Blaze Rod"}
            ]
        """.trimIndent()

        val service = SuggestionService { json.byteInputStream() }
        val actual = service.complete("end", limit = 3)

        assertEquals(
            listOf("Ender Pearl", "End Stone", "Enchanted Ender Pearl"),
            actual.map { it.displayName }
        )
    }
}
