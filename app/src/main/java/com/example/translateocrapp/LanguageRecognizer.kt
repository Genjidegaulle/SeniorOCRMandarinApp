package com.example.translateocrapp

import android.graphics.Rect
import android.util.Log
import com.google.mlkit.nl.languageid.LanguageIdentification
import com.google.mlkit.nl.languageid.LanguageIdentificationOptions
import com.google.mlkit.nl.languageid.LanguageIdentifier;
import com.google.mlkit.vision.text.Text
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks


class LanguageRecognizer {

    private val languageIdentifierClient: LanguageIdentifier
    private val languageIdentifierOptions: LanguageIdentificationOptions

    init {
        // Initialize the language identifier client in the class constructor
        languageIdentifierOptions = LanguageIdentificationOptions.Builder()
            .setConfidenceThreshold(0.5f)
            .build()
        languageIdentifierClient = LanguageIdentification.getClient(languageIdentifierOptions)
    }

    fun recognizeLanguage(ocrMap: Map<Rect, Text.TextBlock>): String {

        // Iterate through the map of OCR results and recognize the language of each textBlock
        // Find if English is present in the text
        // if English is not found, return "und"

        // Create a map to store the language of each line
        val languageMap = mutableMapOf<Rect, String>()

        // Iterate through the map of OCR results
        for ((rect, textBlock) in ocrMap) {
            // Get the text from the textBlock
            val text = textBlock.text

            // Create a task to recognize the language of the textBlock
            val task: Task<String> = languageIdentifierClient.identifyLanguage(text)

            // Wait for the task to complete
            val result = Tasks.await(task)

            // Store the language of the textBlock in the map
            languageMap[rect] = result
        }

        // Count the occurrences of English language
        val englishCount = languageMap.values.count { it == "en" }

        return when {
            englishCount > 0 -> "en" // English is present
            else -> "und" // English is not found
        }


    }
}