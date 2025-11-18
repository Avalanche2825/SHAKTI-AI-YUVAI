# Download ML Models for ShaktiAI

This guide will help you download and set up the required TensorFlow Lite models for the ShaktiAI
application.

## Quick Start - Download Pre-trained Models

### Option 1: Use YAMNet for Audio Detection (Recommended for Testing)

YAMNet is a pre-trained audio classification model from Google.

**Download Link:** https://tfhub.dev/google/lite-model/yamnet/classification/tflite/1

**Steps:**

1. Visit the link above
2. Click "Download" to get `yamnet.tflite`
3. Rename it to `audio_threat_detector.tflite`
4. Place it in `app/src/main/assets/models/`

**Alternative Direct Download:**

```bash
# Using PowerShell
Invoke-WebRequest -Uri "https://tfhub.dev/google/lite-model/yamnet/classification/tflite/1?lite-format=tflite" -OutFile "app/src/main/assets/models/audio_threat_detector.tflite"
```

### Option 2: Use MobileNet for Video/Image Detection (Optional)

MobileNet is a lightweight image classification model.

**Download Link:
** https://storage.googleapis.com/download.tensorflow.org/models/tflite/mobilenet_v1_1.0_224_quant_and_labels.zip

**Steps:**

1. Download the ZIP file
2. Extract `mobilenet_v1_1.0_224.tflite`
3. Rename it to `violence_detector.tflite`
4. Place it in `app/src/main/assets/models/`

**PowerShell Command:**

```powershell
# Download and extract
Invoke-WebRequest -Uri "https://storage.googleapis.com/download.tensorflow.org/models/tflite/mobilenet_v1_1.0_224_quant_and_labels.zip" -OutFile "mobilenet.zip"
Expand-Archive -Path "mobilenet.zip" -DestinationPath "temp_models"
Copy-Item "temp_models/mobilenet_v1_1.0_224.tflite" -Destination "app/src/main/assets/models/violence_detector.tflite"
Remove-Item "mobilenet.zip", "temp_models" -Recurse -Force
```

## Advanced - Train Custom Models

### Audio Threat Detection Model

For a custom audio threat detection model, you'll need:

1. **Dataset:** Audio samples of threatening situations (screams, violence, aggressive speech)
2. **Training:** Use TensorFlow/Keras with audio preprocessing
3. **Conversion:** Convert to TFLite format

**Sample Training Code:**

```python
import tensorflow as tf
from tensorflow import keras
import librosa
import numpy as np

# 1. Prepare your dataset
def extract_features(audio_path):
    y, sr = librosa.load(audio_path, sr=16000)
    mfcc = librosa.feature.mfcc(y=y, sr=sr, n_mfcc=40)
    return np.mean(mfcc.T, axis=0)

# 2. Build model
model = keras.Sequential([
    keras.layers.Dense(256, activation='relu', input_shape=(40,)),
    keras.layers.Dropout(0.3),
    keras.layers.Dense(128, activation='relu'),
    keras.layers.Dropout(0.3),
    keras.layers.Dense(64, activation='relu'),
    keras.layers.Dense(1, activation='sigmoid')  # Binary: threat or not
])

model.compile(optimizer='adam', loss='binary_crossentropy', metrics=['accuracy'])

# 3. Train (replace with your data)
# model.fit(X_train, y_train, epochs=50, validation_data=(X_val, y_val))

# 4. Convert to TFLite
converter = tf.lite.TFLiteConverter.from_keras_model(model)
converter.optimizations = [tf.lite.Optimize.DEFAULT]
tflite_model = converter.convert()

# 5. Save
with open('app/src/main/assets/models/audio_threat_detector.tflite', 'wb') as f:
    f.write(tflite_model)
```

## Using Pre-built Models from TensorFlow Hub

### Audio Classification Models

- **YAMNet**: https://tfhub.dev/google/yamnet/1
- **Audio Event Detection**: https://tfhub.dev/google/audioset-base/1

### Image/Video Models

- **MobileNet V2**: https://tfhub.dev/google/imagenet/mobilenet_v2_100_224/classification/5
- **EfficientDet**: https://tfhub.dev/tensorflow/efficientdet/lite0/detection/1

### Emotion Recognition

- **FER+ Model**: Search for "FER emotion recognition tflite" or train custom

## Verifying Models

After placing models in `app/src/main/assets/models/`, verify:

```bash
# Check if models exist
cd "app/src/main/assets/models"
dir
```

Expected files:

- ✅ `audio_threat_detector.tflite` (Required)
- ✅ `violence_detector.tflite` (Optional)
- ✅ `emotion_classifier.tflite` (Optional)

## Testing Models in Android

The app will automatically load models from the assets folder. Check logs:

```kotlin
// In AudioDetectionService.kt
try {
    val model = loadModelFile("models/audio_threat_detector.tflite")
    Log.d("ShaktiAI", "Audio model loaded successfully")
} catch (e: Exception) {
    Log.e("ShaktiAI", "Failed to load model: ${e.message}")
}
```

## Important Notes

1. **Model Compatibility**: Ensure models are TFLite format (.tflite extension)
2. **Input/Output Shape**: Document your model's expected input/output shapes
3. **Testing**: Always test with sample audio/video before production use
4. **Privacy**: Use on-device models only (no cloud inference)
5. **Size**: Keep models under 50MB for reasonable app size

## Troubleshooting

**Issue:** Model not found

- **Solution:** Ensure file is in `app/src/main/assets/models/` directory
- **Solution:** Clean and rebuild project (`./gradlew clean build`)

**Issue:** Model loading error

- **Solution:** Verify the model is a valid TFLite file
- **Solution:** Check model compatibility with TFLite runtime version

**Issue:** Poor accuracy

- **Solution:** Use domain-specific models or train custom models
- **Solution:** Ensure proper preprocessing of input data

## Resources

- **TensorFlow Lite Guide**: https://www.tensorflow.org/lite
- **Model Maker**: https://www.tensorflow.org/lite/models/modify/model_maker
- **Pre-trained Models**: https://www.tensorflow.org/lite/models
- **Audio ML**: https://www.tensorflow.org/tutorials/audio/simple_audio

---

**For Development/Testing:** Start with YAMNet and MobileNet, then replace with custom trained
models later.
