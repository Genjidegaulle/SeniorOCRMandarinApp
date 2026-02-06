package com.example.translateocrapp


import android.app.Activity
import android.graphics.Rect
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks

import android.content.Context
import android.util.Log
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import android.os.Handler
import android.os.Looper

import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.TranslatorOptions
import com.google.mlkit.vision.text.Text

import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.translate.TranslateRemoteModel

class TextTranslator(private val context: Context) {

    companion object {
        private const val TAG = "TextTranslator"
    }

    // Variables
    private lateinit var chineseOptions : TranslatorOptions
    private lateinit var chineseTranslator : com.google.mlkit.nl.translate.Translator

    private val remoteModelManager = RemoteModelManager.getInstance()

    // AlertDialog to show download progress
    private var progressDialog: AlertDialog? = null


    init {
        Log.d(TAG, "Initializing TextTranslator")

        // Initialize the translator for English to Chinese
        chineseOptions = TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.ENGLISH)
            .setTargetLanguage(TranslateLanguage.CHINESE)
            .build()

        chineseTranslator = com.google.mlkit.nl.translate.Translation.getClient(chineseOptions)
        Log.d(TAG, "TextTranslator initialized successfully")

    }


    // Create a function to translate text
    // sourceLanguageCode is the language code - expecting "en" for English
    private fun translateTextToChinese(text: String, sourceLanguageCode: String): String {
        Log.d(TAG, "translateTextToChinese called with sourceLanguageCode: $sourceLanguageCode, text length: ${text.length}")

        // Check if the source language code is "en" (English), if not return the text
        if (sourceLanguageCode != "en") {
            Log.d(TAG, "Source language is not English, returning original text")
            return text
        }

        try {
            // Check if the translation model is downloaded and available
            Log.d(TAG, "Checking if model is downloaded")
            if (!isModelDownloaded("zh")) {
                Log.d(TAG, "Model not downloaded, downloading...")
                // Model not downloaded, download it and wait for completion
                downloadModel("zh")
            } else {
                Log.d(TAG, "Model already downloaded")
            }

            // Translate the text to Simplified Chinese using the translator
            Log.d(TAG, "Starting translation to Simplified Chinese")
            val task: Task<String> = chineseTranslator.translate(text)
            val simplifiedChinese = Tasks.await(task)
            Log.d(TAG, "Translation to Simplified Chinese completed: $simplifiedChinese")

            // Convert Simplified Chinese to Traditional Chinese
            Log.d(TAG, "Converting to Traditional Chinese")
            val traditionalChinese = ChineseConverter.toTraditional(simplifiedChinese)
            Log.d(TAG, "Conversion to Traditional Chinese completed: $traditionalChinese")

            return traditionalChinese

        } catch (e: Exception) {
            Log.e(TAG, "Error during translation: ${e.message}", e)
            e.printStackTrace()
            return text
        }

    }



    // Check if the translation model for the given language code is downloaded and available
    private fun isModelDownloaded(languageCode: String): Boolean {
        Log.d(TAG, "Checking if model for language $languageCode is downloaded")
        try {
            val model = TranslateRemoteModel.Builder(languageCode).build()
            val task = remoteModelManager.isModelDownloaded(model)
            val isDownloaded = Tasks.await(task)
            Log.d(TAG, "Model for $languageCode is downloaded: $isDownloaded")
            return isDownloaded
        } catch (e: Exception) {
            Log.e(TAG, "Error checking if model is downloaded: ${e.message}", e)
            return false
        }

    }

    // Download the translation model for the given language code
    private fun downloadModel(languageCode: String) {
        Log.d(TAG, "Starting download for language: $languageCode")

        // Create a progress dialog
        val progressBar = ProgressBar(context).apply {
            isIndeterminate = true
        }

        Handler(Looper.getMainLooper()).post {
            progressDialog = AlertDialog.Builder(context)
                .setTitle("Downloading Translation Model (Please Be on Wifi)")
                .setCancelable(false)
                .setView(progressBar)
                .show()
        }



        val conditions = DownloadConditions.Builder()
            .requireWifi()
            .build()
        val model = TranslateRemoteModel.Builder(languageCode).build()
        val downloadTask = remoteModelManager.download(model, conditions)

        try {
            Tasks.await(downloadTask)
            Log.d(TAG, "Model download completed successfully")
            Handler(Looper.getMainLooper()).post {
                progressDialog?.dismiss()
                progressDialog = null
            }

            // Show a toast indicating successful download
            showDownloadToast("Translation Model Downloaded Successfully")

        } catch (e: Exception) {
            Log.e(TAG, "Model download failed: ${e.message}", e)

            // Dismiss the progress dialog on download failure
            Handler(Looper.getMainLooper()).post {
                progressDialog?.dismiss()
                progressDialog = null
            }

            // Show a toast indicating download failure
            showDownloadToast("Translation Model Download Failed: ${e.message}")

        }
    }

    // Show toast on the main UI thread
    private fun showDownloadToast(message: String) {
        (context as? Activity)?.runOnUiThread {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    // Create a function to translate ocr result
    fun translateOcrResult(ocrResult: Map<Rect, Text.TextBlock>, languageCode: String ): Map<Rect, String> {
        Log.d(TAG, "translateOcrResult called with ${ocrResult.size} text blocks, languageCode: $languageCode")

        // Create a map to store the translated result
        val translatedResult = mutableMapOf<Rect, String>()

        try {
            // Iterate through the ocr result
            var blockIndex = 0
            for ((rect, textBlock) in ocrResult) {
                blockIndex++
                Log.d(TAG, "Translating block $blockIndex/${ocrResult.size}: '${textBlock.text}'")

                // Translate the textBlock text to Chinese
                val translatedText = translateTextToChinese(textBlock.text, languageCode)
                Log.d(TAG, "Block $blockIndex translated: '$translatedText'")

                // Add the translated text to the map
                translatedResult[rect] = translatedText
            }

            Log.d(TAG, "All blocks translated successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error during OCR result translation: ${e.message}", e)
            e.printStackTrace()
        }

        return translatedResult

    }
}