package com.example.translateocrapp

import android.icu.text.Transliterator

object ChineseConverter {

    private val transliterator: Transliterator by lazy {
        // Use Android's built-in ICU Transliterator for Simplified to Traditional Chinese
        // This is available from API 24+
        Transliterator.getInstance("Simplified-Traditional")
    }

    /**
     * Convert Simplified Chinese to Traditional Chinese using Android's ICU library
     * @param simplifiedText The simplified Chinese text
     * @return The traditional Chinese text
     */
    fun toTraditional(simplifiedText: String): String {
        return try {
            if (simplifiedText.isEmpty()) {
                return simplifiedText
            }
            transliterator.transliterate(simplifiedText)
        } catch (e: Exception) {
            // If conversion fails, return original text
            android.util.Log.e("ChineseConverter", "Conversion failed: ${e.message}")
            simplifiedText
        }
    }
}
