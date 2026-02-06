# Testing Guide

This document explains how to test the TranslateOCRApp and debug any issues.

## Running Tests

### Unit Tests (No device required)
```bash
./gradlew test
```

These tests check basic logic and don't require ML Kit or Android runtime.

### Instrumented Tests (Requires device/emulator)
```bash
# Run all instrumented tests
./gradlew connectedAndroidTest

# Run specific test
./gradlew connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.translateocrapp.ChineseConverterInstrumentedTest
```

These tests run on a device and test actual Android functionality including:
- ChineseConverter with real ICU Transliterator
- Simplified to Traditional Chinese conversion accuracy

## Viewing Logs

### View all app logs
```bash
adb logcat | grep translateocrapp
```

### View translation-specific logs
```bash
adb logcat | grep TextTranslator
```

### View Chinese conversion logs
```bash
adb logcat | grep ChineseConverter
```

### View all OCR/translation pipeline logs
```bash
adb logcat | grep -E "(TextTranslator|ChineseConverter|OCR|Language|Translation)"
```

### Save logs to file
```bash
adb logcat > app_logs.txt
```

## Common Issues and Solutions

### Issue: App crashes during translation

**Symptoms**: App force stops when trying to translate

**Debugging steps**:
1. Check logs for the exact error:
   ```bash
   adb logcat | grep -E "(AndroidRuntime|FATAL|TextTranslator)"
   ```

2. Look for:
   - `NullPointerException`: Check if translator is initialized
   - `NetworkException`: Check WiFi connection for model download
   - `OutOfMemoryError`: Image might be too large

**Solutions**:
- Ensure device is on WiFi for first-time model download
- Check that minSdk is 24+ (required for ICU Transliterator)
- Verify ML Kit dependencies are properly included

### Issue: Chinese text is simplified instead of traditional

**Debugging steps**:
1. Run ChineseConverterInstrumentedTest:
   ```bash
   ./gradlew connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.translateocrapp.ChineseConverterInstrumentedTest
   ```

2. Check converter logs:
   ```bash
   adb logcat | grep ChineseConverter
   ```

**Solutions**:
- Verify ICU Transliterator is available (API 24+)
- Check test output to see if conversion is working
- Look for "Conversion failed" messages in logs

### Issue: Translation model won't download

**Symptoms**: Download dialog appears but never completes

**Debugging steps**:
1. Check network logs:
   ```bash
   adb logcat | grep -E "(TextTranslator|DownloadConditions)"
   ```

2. Verify:
   - Device is connected to WiFi (model download requires WiFi)
   - Device has internet access
   - Sufficient storage space

**Solutions**:
- Connect to WiFi network
- Clear app data and try again
- Check firewall/network restrictions

### Issue: OCR not recognizing text

**Symptoms**: Camera works but no translation happens

**Debugging steps**:
1. Check OCR logs:
   ```bash
   adb logcat | grep OCR
   ```

2. Check language detection:
   ```bash
   adb logcat | grep Language
   ```

**Solutions**:
- Ensure text is clear and well-lit
- Verify text is in English (app only translates English)
- Check if OCR result is empty (no text detected)

## Manual Testing Checklist

### First Run Test
- [ ] App launches without crash
- [ ] Camera permission is requested
- [ ] Camera preview shows correctly
- [ ] WiFi dialog appears for model download
- [ ] Model downloads successfully
- [ ] "Translation Model Downloaded Successfully" toast appears

### Translation Test
- [ ] Capture English text (try "Hello World")
- [ ] OCR processes the image (check logs)
- [ ] Language detection identifies English (check logs)
- [ ] Translation to Simplified Chinese completes (check logs)
- [ ] Conversion to Traditional Chinese completes (check logs)
- [ ] Traditional Chinese text appears on image
- [ ] Text is correctly positioned over original text

### Conversion Verification
Use these test phrases to verify Traditional Chinese output:

| English | Expected Traditional Chinese |
|---------|------------------------------|
| Hello | 你好 |
| School | 學校 |
| Library | 圖書館 |
| Computer | 電腦 |
| Learn | 學習 |

### Edge Cases
- [ ] Empty image (no text)
- [ ] Non-English text
- [ ] Mixed English and numbers
- [ ] Very long text
- [ ] Text with special characters

## Performance Testing

### Translation Speed
Expected times (varies by device):
- OCR: 1-3 seconds
- Language Detection: 0.5-1 second
- Translation: 1-2 seconds
- Conversion: < 0.1 seconds
- Total: ~3-7 seconds

If translation takes > 10 seconds, check:
- Device CPU usage
- Network speed (for model download)
- Image size

### Memory Usage
Monitor memory with:
```bash
adb shell dumpsys meminfo com.example.translateocrapp
```

Expected memory usage: 50-150 MB

## Test Data

### Good Test Images
- Simple text on contrasting background
- Clear fonts (Arial, Helvetica)
- Good lighting
- Minimal background clutter

### Sample English Phrases
- "Hello World"
- "Welcome to our restaurant"
- "Exit"
- "Open 9am to 5pm"
- "Please wash your hands"

## Reporting Issues

When reporting issues, include:
1. Device model and Android version
2. Full logcat output (use `adb logcat > logs.txt`)
3. Steps to reproduce
4. Screenshots of the error
5. Whether it's first run or subsequent run
6. Network status (WiFi/cellular/offline)
