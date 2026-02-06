# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is an Android application that uses OCR (Optical Character Recognition) to capture and translate English text from images into Mandarin (Traditional Chinese). It uses Google ML Kit for OCR, language identification, and translation, combined with CameraX for camera functionality.

## Build and Development Commands

```bash
# Build the project
./gradlew build

# Build APK
./gradlew assembleDebug
./gradlew assembleRelease

# Install app on connected device
./gradlew installDebug

# Run unit tests
./gradlew test

# Run instrumented tests (requires emulator or device)
./gradlew connectedAndroidTest

# Clean build
./gradlew clean
```

## Architecture

### Data Flow Pipeline

The app follows a sequential 5-step pipeline executed across two activities:

1. **MainActivity**: Image capture using CameraX
   - Saves captured image to app's external media directory as `translateocrapp.jpg`
   - Launches PreviewActivity with image path

2. **PreviewActivity**: Orchestrates the OCR → Language Detection → Translation → Annotation pipeline
   - Uses coroutines to chain asynchronous operations
   - Each step waits for the previous one via `invokeOnCompletion`

### Core Helper Classes

All helper classes are stateless processors that work with `Map<Rect, Text.TextBlock>` or `Map<Rect, String>`:

- **OcrHelper**: Extracts text blocks from bitmap using ML Kit's Latin text recognizer
  - Returns: `Map<Rect, Text.TextBlock>` mapping bounding boxes to recognized text blocks
  - Configured for English text recognition

- **LanguageRecognizer**: Identifies language per text block
  - Detects English ("en")
  - Returns "und" (undetermined) if English is not found

- **TextTranslator**: Downloads translation models on-demand and translates text
  - Manages English to Chinese ML Kit translation model
  - Translates English text to Simplified Chinese, then converts to Traditional Chinese
  - Uses ChineseConverter for Simplified → Traditional conversion
  - Downloads models on first use (requires WiFi)
  - Returns: `Map<Rect, String>` mapping bounding boxes to translated Traditional Chinese text

- **ChineseConverter**: Converts Simplified Chinese to Traditional Chinese
  - Uses Android's built-in ICU Transliterator (API 24+)
  - More reliable than external libraries, no resource loading issues
  - Handles common and rare character variants

- **BitmapAnnotator**: Overlays translated text on the original image
  - Draws filled rectangles using average color of text region
  - Renders translated text with contrasting color (black/white)
  - Automatically sizes text to fit within original text bounds

### Key Technical Details

- **OCR Processing**: Uses `Map<Rect, Text.TextBlock>` throughout the pipeline to preserve spatial information from OCR to final rendering
- **Model Management**: Translation models are downloaded once and cached locally. The app shows progress dialogs during download.
- **Thread Safety**: OCR, language detection, and translation all run on `Dispatchers.Default`, with UI updates marshaled to `Dispatchers.Main`
- **Image Rotation**: Reads EXIF orientation data and rotates bitmaps accordingly before OCR processing

## Project Structure

```
app/src/main/java/com/example/translateocrapp/
├── MainActivity.kt           # Camera capture with CameraX
├── PreviewActivity.kt        # Pipeline orchestration with coroutines
├── OcrHelper.kt              # Text recognition
├── LanguageRecognizer.kt     # Language identification
├── TextTranslator.kt         # Translation with model management
├── ChineseConverter.kt       # Simplified to Traditional Chinese conversion
└── BitmapAnnotator.kt        # Image annotation
```

## Dependencies

- **Google ML Kit**: Text recognition, language ID, and translation
- **CameraX**: Camera functionality (version 1.2.2)
- **Kotlin Coroutines**: Asynchronous OCR/translation pipeline
- View Binding enabled for layouts

## Translation Direction

- **OCR**: Uses ML Kit's Latin text recognizer for English text
- **Language Detection**: Detects English ("en") in captured text
- **Translation**: Two-step process:
  1. ML Kit translates English → Simplified Chinese
  2. Android ICU Transliterator converts Simplified → Traditional Chinese
- **Why Two Steps**: ML Kit only supports Simplified Chinese translation, so we convert to Traditional as a post-processing step using Android's built-in ICU library
- **Model Size**: Chinese translation models may be larger than European language models
- **Output**: Traditional Chinese characters are rendered over the original English text in the annotated image

## Debugging and Testing

- **Logging**: TextTranslator includes comprehensive logging with tag "TextTranslator"
- **Error Handling**: All translation steps include try-catch blocks to prevent crashes
- **Unit Tests**: Run `./gradlew test` for basic unit tests
- **Instrumented Tests**: Run `./gradlew connectedAndroidTest` for on-device tests including ChineseConverter
- **View Logs**: Use `adb logcat | grep -E "(TextTranslator|ChineseConverter|OCR|Language|Translation)"` to see detailed logs

## Important Notes

- First run requires WiFi to download translation models
- Images are saved to external media directory with fixed filename `translateocrapp.jpg`
- The sequential pipeline means total processing time is sum of: OCR + Language Detection + Translation + Annotation
