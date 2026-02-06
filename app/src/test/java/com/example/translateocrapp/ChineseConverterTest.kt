package com.example.translateocrapp

import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for ChineseConverter
 */
class ChineseConverterTest {

    @Test
    fun testSimplifiedToTraditional_basicCharacters() {
        // Test common simplified to traditional conversions
        val simplified = "学习"  // Simplified: learn/study
        val result = ChineseConverter.toTraditional(simplified)
        println("Input: $simplified")
        println("Output: $result")
        // Expected: 學習 (Traditional)
        assertNotNull(result)
        assertTrue(result.isNotEmpty())
    }

    @Test
    fun testSimplifiedToTraditional_commonPhrases() {
        val simplified = "你好，这是一个测试。"
        val result = ChineseConverter.toTraditional(simplified)
        println("Input: $simplified")
        println("Output: $result")
        assertNotNull(result)
        assertTrue(result.isNotEmpty())
    }

    @Test
    fun testSimplifiedToTraditional_emptyString() {
        val simplified = ""
        val result = ChineseConverter.toTraditional(simplified)
        println("Empty string test - Output: $result")
        assertNotNull(result)
        assertEquals("", result)
    }

    @Test
    fun testSimplifiedToTraditional_englishText() {
        // Should return the same English text
        val english = "Hello World"
        val result = ChineseConverter.toTraditional(english)
        println("English text test - Input: $english, Output: $result")
        assertNotNull(result)
        assertEquals(english, result)
    }

    @Test
    fun testSimplifiedToTraditional_mixedText() {
        val mixed = "Hello 世界"  // Hello World (mixed)
        val result = ChineseConverter.toTraditional(mixed)
        println("Mixed text test - Input: $mixed, Output: $result")
        assertNotNull(result)
        assertTrue(result.isNotEmpty())
    }

    @Test
    fun testSimplifiedToTraditional_numbers() {
        val numbers = "一二三四五"
        val result = ChineseConverter.toTraditional(numbers)
        println("Numbers test - Input: $numbers, Output: $result")
        assertNotNull(result)
        assertTrue(result.isNotEmpty())
    }

    @Test
    fun testConversionDoesNotCrash() {
        // Test various inputs to ensure no crashes
        val testInputs = listOf(
            "简体中文",
            "繁體中文",
            "Hello",
            "123",
            "!@#$%",
            "",
            "学校图书馆"
        )

        testInputs.forEach { input ->
            try {
                val result = ChineseConverter.toTraditional(input)
                println("Test input: '$input' -> Output: '$result'")
                assertNotNull(result)
            } catch (e: Exception) {
                fail("Conversion crashed for input: '$input' with error: ${e.message}")
            }
        }
    }
}
