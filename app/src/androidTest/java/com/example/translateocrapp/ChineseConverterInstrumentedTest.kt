package com.example.translateocrapp

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*

/**
 * Instrumented test for ChineseConverter that runs on an Android device
 * This tests the actual Android ICU Transliterator functionality
 */
@RunWith(AndroidJUnit4::class)
class ChineseConverterInstrumentedTest {

    @Test
    fun testSimplifiedToTraditional_basicConversion() {
        // Test basic Simplified to Traditional conversion
        val simplified = "学习"  // Simplified Chinese for "study/learn"
        val traditional = ChineseConverter.toTraditional(simplified)

        println("Simplified: $simplified")
        println("Traditional: $traditional")

        // The traditional form should be 學習
        assertNotNull(traditional)
        assertTrue(traditional.isNotEmpty())
        assertNotEquals(simplified, traditional) // Should be different
        assertEquals("學習", traditional)
    }

    @Test
    fun testSimplifiedToTraditional_commonWords() {
        // Test common word conversions
        val testCases = mapOf(
            "学校" to "學校",  // school
            "图书馆" to "圖書館",  // library
            "电脑" to "電腦",  // computer
            "汉字" to "漢字",  // Chinese characters
            "中国" to "中國"   // China
        )

        testCases.forEach { (simplified, expectedTraditional) ->
            val result = ChineseConverter.toTraditional(simplified)
            println("$simplified -> $result (expected: $expectedTraditional)")
            assertEquals("Conversion failed for $simplified", expectedTraditional, result)
        }
    }

    @Test
    fun testSimplifiedToTraditional_sentences() {
        val simplified = "你好，这是一个测试。"
        val traditional = ChineseConverter.toTraditional(simplified)

        println("Simplified: $simplified")
        println("Traditional: $traditional")

        assertNotNull(traditional)
        assertTrue(traditional.isNotEmpty())
        // The traditional should contain 這 instead of 这
        assertTrue(traditional.contains("這"))
    }

    @Test
    fun testSimplifiedToTraditional_emptyString() {
        val result = ChineseConverter.toTraditional("")
        assertEquals("", result)
    }

    @Test
    fun testSimplifiedToTraditional_englishUnchanged() {
        val english = "Hello World"
        val result = ChineseConverter.toTraditional(english)
        assertEquals(english, result)
    }

    @Test
    fun testSimplifiedToTraditional_mixedContent() {
        val mixed = "Hello 世界 123"
        val result = ChineseConverter.toTraditional(mixed)

        println("Mixed input: $mixed")
        println("Mixed output: $result")

        assertNotNull(result)
        assertTrue(result.contains("Hello"))
        assertTrue(result.contains("123"))
    }

    @Test
    fun testSimplifiedToTraditional_numbers() {
        val numbers = "一二三四五六七八九十"
        val result = ChineseConverter.toTraditional(numbers)

        println("Numbers: $numbers -> $result")

        // Chinese numbers are generally the same in both forms
        assertNotNull(result)
        assertEquals(numbers, result)
    }

    @Test
    fun testSimplifiedToTraditional_punctuation() {
        val withPunctuation = "你好！这是测试。"
        val result = ChineseConverter.toTraditional(withPunctuation)

        println("With punctuation: $withPunctuation -> $result")

        assertNotNull(result)
        assertTrue(result.contains("！"))
        assertTrue(result.contains("。"))
    }

    @Test
    fun testConverterDoesNotCrash() {
        // Test that converter doesn't crash with various inputs
        val testInputs = listOf(
            "简体中文",
            "繁體中文",
            "Hello",
            "123",
            "!@#$%^&*()",
            "",
            "学校图书馆电脑",
            "你好世界",
            "This is a test 这是测试",
            "\n\t\r",
            "   spaces   "
        )

        testInputs.forEach { input ->
            try {
                val result = ChineseConverter.toTraditional(input)
                println("Input: '$input' -> Output: '$result'")
                assertNotNull("Result should not be null for input: '$input'", result)
            } catch (e: Exception) {
                fail("Converter crashed for input: '$input' with error: ${e.message}")
            }
        }
    }

    @Test
    fun testConverterPerformance() {
        // Test that conversion is reasonably fast
        val longText = "学习中文是一个很好的选择。" * 100 // Repeat 100 times

        val startTime = System.currentTimeMillis()
        val result = ChineseConverter.toTraditional(longText)
        val endTime = System.currentTimeMillis()

        val duration = endTime - startTime
        println("Conversion of ${longText.length} characters took $duration ms")

        assertNotNull(result)
        assertTrue("Conversion took too long: $duration ms", duration < 1000) // Should be under 1 second
    }
}

// Extension function to repeat string
private operator fun String.times(n: Int): String = this.repeat(n)
