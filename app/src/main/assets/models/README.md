# ML Models Directory

This directory contains the TensorFlow Lite models used by ShaktiAI for threat detection and
analysis.

## Required Models

### 1. Audio Threat Detection Model

**Filename:** `audio_threat_detector.tflite`

- **Purpose:** Detects threatening audio patterns (screaming, aggressive speech, violence sounds)
- **Input:** Audio spectrograms (Mel-frequency cepstral coefficients)
- **Output:** Threat probability score (0.0 to 1.0)
- **Size:** ~5-10 MB

### 2. Violence Detection Model (Optional)

**Filename:** `violence_detector.tflite`

- **Purpose:** Detects violent actions from video frames
- **Input:** Video frames (224x224 RGB images)
- **Output:** Violence classification with confidence scores
- **Size:** ~10-20 MB

### 3. Emotion Recognition Model (Optional)

**Filename:** `emotion_classifier.tflite`

- **Purpose:** Analyzes emotional state from facial expressions
- **Input:** Face crop images (96x96 or 224x224)
- **Output:** Emotion categories (anger, fear, distress, neutral, etc.)
- **Size:** ~3-8 MB

## Model Sources

You can obtain pre-trained TensorFlow Lite models from:

1. **TensorFlow Hub**: https://tfhub.dev/
2. **MediaPipe**: https://google.github.io/mediapipe/
3. **Train Custom Models**: Use TensorFlow/Keras and convert to TFLite format

## Converting Models to TFLite

If you have a TensorFlow/Keras model (.h5 or SavedModel format):

```python
import tensorflow as tf

# Load your model
model = tf.keras.models.load_model('your_model.h5')

# Convert to TensorFlow Lite
converter = tf.lite.TFLiteConverter.from_keras_model(model)
converter.optimizations = [tf.lite.Optimize.DEFAULT]
tflite_model = converter.convert()

# Save the model
with open('audio_threat_detector.tflite', 'wb') as f:
    f.write(tflite_model)
```

## Model Integration

Models in this directory are automatically loaded by:

- `AudioDetectionService` → loads `audio_threat_detector.tflite`
- `VideoRecorderService` → loads `violence_detector.tflite` (if available)

## File Structure

```
app/src/main/assets/models/
├── README.md (this file)
├── audio_threat_detector.tflite (REQUIRED)
├── violence_detector.tflite (Optional)
└── emotion_classifier.tflite (Optional)
```

## Important Notes

1. **Model Size**: Keep models under 50MB each for app size considerations
2. **Optimization**: Use TFLite optimization for faster inference on mobile devices
3. **Testing**: Test models thoroughly with various audio/video samples
4. **Privacy**: Ensure all processing happens on-device (no cloud inference)

## Placeholder Models

For development/testing, you can create placeholder models or use pre-trained models from:

- **Yamnet** (Audio classification): https://tfhub.dev/google/yamnet/1
- **MobileNet** (Image
  classification): https://tfhub.dev/google/imagenet/mobilenet_v2_100_224/classification/5

## Current Status

⚠️ **No models currently present**

Please add the required TensorFlow Lite models to this directory before running the audio/video
detection features.

---
**Last Updated:** November 2025
