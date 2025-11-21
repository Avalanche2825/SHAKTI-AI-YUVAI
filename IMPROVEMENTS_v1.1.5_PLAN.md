# 🚀 SHAKTI AI v1.1.5 - Improvements Plan

## ✅ COMPLETED IN THIS UPDATE

### **1. Evidence Viewer - Show ALL Evidence ✅**

**Problem:** Evidence viewer only showing current incident evidence

**Fix Applied:**

- Modified `EvidenceViewerActivity` to load ALL evidence by default
- Group evidence by incident with headers
- Added filter chips (All, Videos, Audio)
- Added statistics card showing total evidence count
- Improved UI with better organization

**Changes:**

- `EvidenceViewerActivity.kt` - Completely refactored
- Added `EvidenceGroup` data class for grouping
- New `EvidenceGroupAdapter` with header and item view types
- `activity_evidence_viewer.xml` - Added stats card and filter chips
- `item_evidence_header.xml` - NEW layout for incident headers
- `item_evidence.xml` - Updated with play button

**Features:**

- ✅ Shows ALL incidents and their evidence
- ✅ Groups evidence by incident with headers
- ✅ Shows incident time, trigger type, location
- ✅ Filter by type (All/Videos/Audio)
- ✅ Statistics: "X files from Y incidents"
- ✅ Improved card design with play buttons
- ✅ Better timestamps and file size formatting

---

## 🔄 IN PROGRESS / TODO

### **2. AI Chatbot Integration** 🤖

**Requirements:**

1. Add AI chatbot to NYAY Legal Activity
2. Add AI chatbot to Escape Planner Activity
3. Support both text messaging and voice input
4. Use on-device AI model (no internet required)
5. Context-aware responses

**Recommended Approach:**

#### **Model Selection:**

Use **Gemini Nano** or **MediaPipe LLM** for on-device inference:

1. **Gemini Nano** (Recommended)
    - Size: ~1.8 GB (quantized)
    - Best for conversational AI
    - Integrated with Android AICore
    - Requires Android 14+

2. **MediaPipe LLM**
    - Size: ~500 MB - 2 GB (depending on model)
    - Good for specific tasks
    - Works on Android 8.0+
    - Better compatibility

3. **TensorFlow Lite**
    - Use BERT/DistilBERT for Q&A
    - Size: ~200-400 MB
    - Faster inference
    - Works offline

**Implementation Steps:**

#### **Step 1: Add Dependencies**

```gradle
dependencies {
    // Gemini Nano (if available)
    implementation 'com.google.android.gms:play-services-ai-core:16.0.0'
    
    // OR MediaPipe
    implementation 'com.google.mediapipe:tasks-text:0.10.8'
    
    // OR TensorFlow Lite
    implementation 'org.tensorflow:tensorflow-lite:2.14.0'
    implementation 'org.tensorflow:tensorflow-lite-support:0.4.4'
    implementation 'org.tensorflow:tensorflow-lite-gpu:2.14.0'
    
    // Speech recognition
    implementation 'androidx.speech:speech:1.0.0'
    
    // Text-to-Speech
    // (Already included in Android SDK)
}
```

#### **Step 2: Create AI Chat Service**

```kotlin
// File: app/src/main/java/com/shakti/ai/services/AIChatService.kt

class AIChatService(private val context: Context) {
    
    private var model: LLMModel? = null
    
    suspend fun initialize() {
        // Load model from assets
        model = loadModel()
    }
    
    suspend fun chat(message: String, context: ChatContext): String {
        return model?.generate(message, context) ?: "I'm sorry, I couldn't process that."
    }
    
    fun chatWithVoice(audioData: ByteArray): String {
        // Convert speech to text
        val text = speechToText(audioData)
        // Generate response
        return chat(text)
    }
    
    enum class ChatContext {
        LEGAL,      // For NYAY Legal
        ESCAPE,     // For Escape Planner
        GENERAL     // Generic assistance
    }
}
```

#### **Step 3: Create Chat UI Component**

```kotlin
// File: app/src/main/java/com/shakti/ai/ui/components/AIChatFragment.kt

class AIChatFragment : Fragment() {
    private lateinit var chatService: AIChatService
    private val messages = mutableListOf<ChatMessage>()
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupRecyclerView()
        setupInputField()
        setupVoiceButton()
        
        // Initialize AI
        lifecycleScope.launch {
            chatService.initialize()
        }
    }
    
    private fun sendMessage(text: String) {
        messages.add(ChatMessage(text, isUser = true))
        
        lifecycleScope.launch {
            val response = chatService.chat(text, getContext())
            messages.add(ChatMessage(response, isUser = false))
            adapter.notifyDataSetChanged()
        }
    }
}
```

#### **Step 4: Update NYAY Legal Activity**

```kotlin
// Add chat fragment to NyayLegalActivity

class NyayLegalActivity : AppCompatActivity() {
    
    private lateinit var chatFragment: AIChatFragment
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Add floating action button for chat
        binding.fabChat.setOnClickListener {
            showChatDialog()
        }
    }
    
    private fun showChatDialog() {
        chatFragment = AIChatFragment.newInstance(ChatContext.LEGAL)
        chatFragment.show(supportFragmentManager, "legal_chat")
    }
}
```

#### **Step 5: Add Voice Input**

```kotlin
// File: app/src/main/java/com/shakti/ai/utils/VoiceInputHelper.kt

class VoiceInputHelper(private val context: Context) {
    
    private val speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
    
    fun startListening(onResult: (String) -> Unit) {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, 
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }
        
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(
                    SpeechRecognizer.RESULTS_RECOGNITION)
                matches?.firstOrNull()?.let { onResult(it) }
            }
            // ... other callbacks
        })
        
        speechRecognizer.startListening(intent)
    }
}
```

---

### **3. Evidence Recording Improvements** 📹

**Current Issues:**

- Video quality could be better
- Audio synchronization needs improvement
- File sizes are large

**Improvements Needed:**

#### **A. Video Recording:**

```kotlin
// Update VideoRecorderService.kt

private fun setupVideoEncoder() {
    // Use H.265 (HEVC) for better compression
    val mediaFormat = MediaFormat.createVideoFormat(
        MediaFormat.MIMETYPE_VIDEO_HEVC, // Better compression
        width,
        height
    ).apply {
        setInteger(MediaFormat.KEY_BIT_RATE, 2_000_000) // 2 Mbps
        setInteger(MediaFormat.KEY_FRAME_RATE, 30)
        setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
        setInteger(MediaFormat.KEY_PROFILE, 
            MediaCodecInfo.CodecProfileLevel.HEVCProfileMain)
    }
}
```

#### **B. Audio Recording:**

```kotlin
// Update AudioDetectionService.kt

private fun setupAudioRecorder() {
    mediaRecorder = MediaRecorder().apply {
        setAudioSource(MediaRecorder.AudioSource.MIC)
        setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        setAudioEncoder(MediaRecorder.AudioEncoder.AAC) // Better quality
        setAudioEncodingBitRate(128000) // 128 kbps
        setAudioSamplingRate(44100) // CD quality
        setOutputFile(outputFile)
    }
}
```

#### **C. File Compression:**

```kotlin
// Add compression after recording

suspend fun compressVideo(inputPath: String): String {
    return withContext(Dispatchers.IO) {
        val outputPath = inputPath.replace(".mp4", "_compressed.mp4")
        
        // Use MediaCodec for compression
        val compressor = VideoCompressor()
        compressor.compress(inputPath, outputPath, 
            bitrate = 1_500_000,
            quality = VideoQuality.MEDIUM
        )
        
        // Delete original if compression successful
        File(inputPath).delete()
        outputPath
    }
}
```

---

### **4. UI Improvements** 🎨

**Completed:**

- ✅ Evidence Viewer with grouping
- ✅ Filter chips
- ✅ Statistics card
- ✅ Improved item cards with play buttons

**Still Needed:**

#### **A. Dashboard Improvements:**

- Better stats visualization
- Charts for incident trends
- Quick action tiles

#### **B. Incident Report Improvements:**

- Map view for location
- Timeline visualization
- Share options

#### **C. Dark Mode:**

- Full dark mode support
- Theme switching

---

## 📁 FILES TO CREATE/MODIFY

### **New Files:**

1. **AI Chat Service:**
    - `app/src/main/java/com/shakti/ai/services/AIChatService.kt`
    - `app/src/main/java/com/shakti/ai/models/ChatMessage.kt`
    - `app/src/main/java/com/shakti/ai/models/LLMModel.kt`

2. **Chat UI:**
    - `app/src/main/java/com/shakti/ai/ui/components/AIChatFragment.kt`
    - `app/src/main/res/layout/fragment_ai_chat.xml`
    - `app/src/main/res/layout/item_chat_message.xml`

3. **Voice Input:**
    - `app/src/main/java/com/shakti/ai/utils/VoiceInputHelper.kt`
    - `app/src/main/java/com/shakti/ai/utils/TextToSpeechHelper.kt`

4. **Model Assets:**
    - `app/src/main/assets/models/legal_assistant.tflite` (~500 MB)
    - `app/src/main/assets/models/escape_planner.tflite` (~500 MB)

### **Modified Files:**

1. **Activities:**
    - `NyayLegalActivity.kt` - Add chat button and integration
    - `EscapePlannerActivity.kt` - Add chat button and integration
    - `EvidenceViewerActivity.kt` - ✅ DONE

2. **Services:**
    - `VideoRecorderService.kt` - Improve encoding
    - `AudioDetectionService.kt` - Improve audio quality

3. **Layouts:**
    - `activity_nyay_legal.xml` - Add FAB for chat
    - `activity_escape_planner.xml` - Add FAB for chat
    - `activity_evidence_viewer.xml` - ✅ DONE
    - `item_evidence.xml` - ✅ DONE
    - `item_evidence_header.xml` - ✅ NEW

4. **Gradle:**
    - `app/build.gradle` - Add AI/ML dependencies

---

## 🔧 IMPLEMENTATION PRIORITY

### **Phase 1 (Current - v1.1.5):**

- ✅ Evidence Viewer improvements
- ✅ Show ALL evidence with grouping
- ✅ UI improvements

### **Phase 2 (Next - v1.2.0):**

- 🔄 Download and integrate AI model
- 🔄 Implement AI Chat Service
- 🔄 Add chat UI components
- 🔄 Integrate with NYAY Legal
- 🔄 Integrate with Escape Planner

### **Phase 3 (Future - v1.2.1):**

- 🔄 Voice input integration
- 🔄 Text-to-speech responses
- 🔄 Multilingual support

### **Phase 4 (Future - v1.3.0):**

- 🔄 Video/audio quality improvements
- 🔄 File compression
- 🔄 Dark mode
- 🔄 Advanced analytics

---

## 📊 AI MODEL RECOMMENDATIONS

### **Option 1: Gemini Nano (Best)**

- **Size:** 1.8 GB
- **Pros:** Best quality, conversational
- **Cons:** Android 14+ only, larger size
- **Use Case:** All features

### **Option 2: DistilBERT (Balanced)**

- **Size:** 250 MB
- **Pros:** Good quality, smaller, faster
- **Cons:** Less conversational
- **Use Case:** Q&A, legal advice

### **Option 3: MobileBERT (Fastest)**

- **Size:** 100 MB
- **Pros:** Very fast, small
- **Cons:** Limited context
- **Use Case:** Quick responses

**Recommendation:** Start with **DistilBERT** for legal Q&A, then upgrade to Gemini Nano for full
conversational AI.

---

## 🎯 NEXT STEPS

1. ✅ **Build and test evidence viewer improvements**
2. 🔄 **Download DistilBERT model** (~250 MB)
3. 🔄 **Create AIChatService**
4. 🔄 **Implement chat UI**
5. 🔄 **Add to NYAY Legal and Escape Planner**
6. 🔄 **Test voice input**
7. 🔄 **Improve video/audio quality**

---

## ⚠️ IMPORTANT NOTES

### **Model Download:**

- AI models are large (250 MB - 2 GB)
- Should be downloaded separately or on first run
- Consider app size limits (< 100 MB for Play Store)

### **Permissions Needed:**

- `RECORD_AUDIO` - For voice input
- `INTERNET` - For initial model download (optional)

### **Performance:**

- On-device inference is slower than cloud
- Consider caching responses
- Show loading indicators

---

**STATUS:** Evidence Viewer improvements COMPLETE ✅  
**NEXT:** AI Chatbot integration (requires model download)  
**VERSION:** 1.1.5  
**DATE:** November 21, 2025
