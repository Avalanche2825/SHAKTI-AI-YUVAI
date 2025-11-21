package com.shakti.ai.services

import android.content.Context
import android.speech.tts.TextToSpeech
import com.shakti.ai.models.ChatContext
import com.shakti.ai.models.ChatMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

/**
 * AI Chat Service - Intelligent conversational assistant
 *
 * Features:
 * - Context-aware responses (Legal, Escape Planning, General)
 * - Natural language processing
 * - Text-to-speech support
 * - Domain-specific knowledge bases
 */
class AIChatService(private val context: Context) {

    private var textToSpeech: TextToSpeech? = null
    private var isInitialized = false
    private val conversationHistory = mutableListOf<ChatMessage>()

    /**
     * Initialize the AI service
     */
    suspend fun initialize() = withContext(Dispatchers.IO) {
        try {
            // Initialize Text-to-Speech
            textToSpeech = TextToSpeech(context) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    textToSpeech?.language = Locale.US
                    isInitialized = true
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Generate response to user message
     */
    suspend fun generateResponse(
        userMessage: String,
        chatContext: ChatContext
    ): String = withContext(Dispatchers.IO) {
        try {
            // Add to conversation history
            conversationHistory.add(ChatMessage(text = userMessage, isUser = true))

            // Process message and generate response
            val response = when (chatContext) {
                ChatContext.LEGAL -> generateLegalResponse(userMessage)
                ChatContext.ESCAPE -> generateEscapeResponse(userMessage)
                ChatContext.GENERAL -> generateGeneralResponse(userMessage)
            }

            // Add response to history
            conversationHistory.add(ChatMessage(text = response, isUser = false))

            response
        } catch (e: Exception) {
            "I'm sorry, I encountered an error. Please try again."
        }
    }

    /**
     * Speak response using Text-to-Speech
     */
    fun speakResponse(text: String) {
        if (isInitialized) {
            textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    /**
     * Stop speaking
     */
    fun stopSpeaking() {
        textToSpeech?.stop()
    }

    /**
     * Generate Legal domain responses
     */
    private fun generateLegalResponse(message: String): String {
        val lowerMessage = message.toLowerCase(Locale.getDefault())

        return when {
            // FIR related
            lowerMessage.contains("fir") || lowerMessage.contains("first information report") -> {
                """
                I can help you with FIR (First Information Report):
                
                📋 To file an FIR:
                1. Go to the nearest police station
                2. You have the right to file an FIR free of charge
                3. You can file it orally or in writing
                4. Police MUST register it (Section 154 CrPC)
                5. Get a free copy of the FIR
                
                💡 If police refuse, you can:
                - File online FIR (eCops portal)
                - Approach Superintendent of Police
                - Send complaint by post (registered)
                - File before Judicial Magistrate
                
                Would you like help with what to include in your FIR?
                """.trimIndent()
            }

            // IPC sections related
            lowerMessage.contains("ipc") || lowerMessage.contains("section") -> {
                """
                Key IPC Sections for Women's Safety:
                
                🔒 Harassment & Assault:
                • Section 354: Assault on woman with intent to outrage modesty
                • Section 354A: Sexual harassment
                • Section 354B: Assault with intent to disrobe
                • Section 354C: Voyeurism
                • Section 354D: Stalking
                
                💔 Domestic Violence:
                • Section 498A: Cruelty by husband or relatives
                • Protection of Women from Domestic Violence Act, 2005
                
                🚫 Sexual Offenses:
                • Section 375-376: Rape and punishment
                • Section 376A-376E: Various sexual assault offenses
                
                Which section would you like to know more about?
                """.trimIndent()
            }

            // Evidence related
            lowerMessage.contains("evidence") || lowerMessage.contains("proof") -> {
                """
                Strong Evidence Can Make Your Case:
                
                ✅ What You Should Collect:
                1. **Audio/Video Recordings** (like SHAKTI AI captures)
                2. Text messages, emails, call logs
                3. Photos of injuries or scene
                4. Witness statements
                5. Medical reports
                6. CCTV footage
                
                📱 Your SHAKTI Evidence:
                - All recordings are timestamped
                - Location data is captured
                - Files are encrypted and secure
                - Admissible in court
                
                💡 Tip: Don't delete anything! Even if you think it's not important.
                
                Need help organizing your evidence?
                """.trimIndent()
            }

            // Lawyer/advocate related
            lowerMessage.contains("lawyer") || lowerMessage.contains("advocate") -> {
                """
                Finding Legal Help:
                
                👨⚖️ Free Legal Aid:
                • National Legal Services Authority (NALSA)
                • State Legal Services Authority
                • District Legal Services Committee
                • Eligibility: Women are prioritized
                
                🏛️ Where to Find Lawyers:
                • Your local Bar Association
                • State Bar Council
                • Legal aid clinics at courts
                • NGOs providing legal support
                
                📞 Important Helplines:
                • Women Helpline: 181
                • Legal Services Authority: 15100
                • National Commission for Women: 011-26944680
                
                Would you like me to show recommended lawyers in your area?
                """.trimIndent()
            }

            // Rights related
            lowerMessage.contains("rights") || lowerMessage.contains("what can i do") -> {
                """
                Your Rights as a Woman:
                
                🛡️ Fundamental Rights:
                1. Right to file FIR at ANY police station
                2. Right to Zero FIR (file where you are, transfer to jurisdiction)
                3. Right to record your statement before female officer
                4. Right to medical examination by female doctor
                5. Right to NOT answer questions in public
                
                ⚖️ During Investigation:
                • Right to know case status
                • Right to bail consideration
                • Right to free legal aid
                • Right to witness protection if needed
                
                🏥 Medical Rights:
                • Free medical examination
                • Copy of medical reports
                • Privacy during examination
                
                What specific right would you like to know more about?
                """.trimIndent()
            }

            // Complaint/report related
            lowerMessage.contains("how to file") || lowerMessage.contains("complaint") -> {
                """
                Filing a Complaint Step-by-Step:
                
                📝 Option 1: Police Station
                1. Visit nearest police station
                2. State you want to file a complaint
                3. Give facts clearly and chronologically
                4. Include all evidence you have
                5. Get FIR copy with your signature
                
                💻 Option 2: Online
                1. Visit your state police website
                2. Look for "e-FIR" or "Online Complaint"
                3. Fill the form with incident details
                4. Upload evidence (SHAKTI videos/audio)
                5. Note down acknowledgment number
                
                📧 Option 3: Email/Post
                1. Write complaint addressed to local SP
                2. Send via registered post with A.D.
                3. Keep receipt as proof
                
                🎯 In Your SHAKTI App:
                • Go to "NYAY Legal"
                • Tap "Generate FIR"
                • Fill details
                • App will create formatted FIR
                
                Ready to file your complaint?
                """.trimIndent()
            }

            // General legal help
            else -> {
                """
                I'm your Legal AI Assistant. I can help with:
                
                📋 FIR & Complaints
                • How to file FIR
                • What to include
                • Your rights during filing
                
                ⚖️ Legal Provisions
                • IPC sections for women
                • Domestic violence laws
                • Sexual harassment laws
                
                👨⚖️ Legal Aid
                • Finding lawyers
                • Free legal services
                • Court procedures
                
                📱 Evidence
                • What counts as evidence
                • How to preserve evidence
                • Using SHAKTI recordings
                
                What would you like to know? Just ask in your own words!
                """.trimIndent()
            }
        }
    }

    /**
     * Generate Escape Planning responses
     */
    private fun generateEscapeResponse(message: String): String {
        val lowerMessage = message.toLowerCase(Locale.getDefault())

        return when {
            // Financial planning
            lowerMessage.contains("money") || lowerMessage.contains("financial") || lowerMessage.contains(
                "save"
            ) -> {
                """
                Financial Planning for Safety:
                
                💰 How Much You Need:
                • 1 Month: ₹15,000-25,000
                • 3 Months: ₹45,000-75,000
                • 6 Months: ₹90,000-1,50,000
                
                💡 How to Save Secretly:
                1. **Bank Account** (in your name only)
                   - Open at different branch
                   - Use office/friend's address
                   - Paperless statements only
                
                2. **Hidden Cash** (small amounts)
                   - Women's clothing pockets
                   - Sanitary product boxes
                   - Book pages
                   - With trusted friend
                
                3. **Digital Savings**
                   - UPI accounts (Paytm, PhonePe)
                   - Digital gold
                   - Prepaid cards
                
                🎯 In SHAKTI App:
                • Go to "Escape Planner"
                • Tap "Calculate Financial Needs"
                • Enter your situation
                • Get personalized plan
                
                Need a detailed financial breakdown?
                """.trimIndent()
            }

            // Safe houses/shelters
            lowerMessage.contains("safe house") || lowerMessage.contains("shelter") || lowerMessage.contains(
                "where to go"
            ) -> {
                """
                Safe Houses & Shelters:
                
                🏠 Types of Safe Accommodation:
                
                1. **Government Shelters** (Free)
                   • Swadhar Greh (long-term stay)
                   • Short Stay Homes
                   • One Stop Centers
                   • Ujjawala Homes
                
                2. **NGO Shelters**
                   • Many provide free shelter
                   • Counseling included
                   • Legal aid available
                   • Children accepted
                
                3. **Working Women's Hostels**
                   • Government subsidized
                   • Safe & affordable
                   • For employed women
                
                📞 How to Access:
                • Women Helpline: 181
                • Call and they'll arrange
                • Can pick you up if needed
                • 24/7 service
                
                🎯 In SHAKTI App:
                • Go to "Escape Planner"
                • Tap "Find Safe Houses"
                • See nearest shelters with distance
                • Get contact numbers
                
                Would you like me to show shelters near you?
                """.trimIndent()
            }

            // Children related
            lowerMessage.contains("children") || lowerMessage.contains("kids") -> {
                """
                Planning with Children:
                
                👶 What to Consider:
                
                1. **Documents to Take**
                   • Birth certificates
                   • School records
                   • Medical records
                   • Immunization cards
                   • Aadhaar cards
                
                2. **School Arrangements**
                   • Transfer certificate (TC)
                   • School leaving certificate
                   • Progress reports
                   • List: TC can be obtained later if emergency
                
                3. **Custody Planning**
                   • You have equal custody rights
                   • Can take children to safety
                   • File for custody if needed
                   • Child welfare is priority
                
                4. **Financial Support**
                   • Maintenance under Section 125 CrPC
                   • Child support from father
                   • Government schemes
                
                💡 Many shelters accept children:
                • Provide schooling facilities
                • Daycare available
                • Child counseling
                
                How many children do you need to plan for?
                """.trimIndent()
            }

            // Job/employment
            lowerMessage.contains("job") || lowerMessage.contains("work") || lowerMessage.contains("employment") -> {
                """
                Employment & Financial Independence:
                
                💼 Finding Work:
                
                1. **Immediate Options**
                   • Home-based: Stitching, tiffin service, tutoring
                   • Online: Data entry, content writing, virtual assistance
                   • Traditional: Domestic work, cooking, childcare
                
                2. **Government Schemes**
                   • Pradhan Mantri Kaushal Vikas Yojana (PMKVY)
                   • Skill India Mission
                   • National Rural Livelihood Mission
                   • Free training + placement
                
                3. **NGO Support**
                   • Vocational training
                   • Microfinance for business
                   • Job placement assistance
                   • Self-employment support
                
                4. **Self-Employment**
                   • MUDRA loan: Up to ₹10 lakh
                   • Stand Up India: For women entrepreneurs
                   • Low interest, easy terms
                
                📚 Free Skill Training:
                • Beauty & wellness
                • Computer basics
                • Tailoring & embroidery
                • Food processing
                • Nursing & healthcare
                
                What skills do you have or want to learn?
                """.trimIndent()
            }

            // Documents
            lowerMessage.contains("document") || lowerMessage.contains("papers") -> {
                """
                Important Documents to Take:
                
                🆔 Identity Documents:
                • Aadhaar card
                • PAN card
                • Voter ID
                • Driving license
                • Passport (if any)
                
                📄 Legal Documents:
                • Marriage certificate
                • Property documents
                • Bank papers
                • Insurance policies
                • Will/inheritance papers
                
                👶 Children's Documents:
                • Birth certificates
                • School records
                • Medical records
                • Aadhaar cards
                
                💊 Health Documents:
                • Prescriptions
                • Medical history
                • Health insurance cards
                • Vaccination records
                
                💡 Tips:
                • Make photocopies/scans
                • Upload to cloud storage
                • Email to yourself
                • Keep copies with trusted friend
                
                ⚠️ If you can't get originals:
                • Duplicates can be obtained
                • Affidavit can substitute temporarily
                • Many services accept Aadhaar only
                
                Which documents do you need help obtaining?
                """.trimIndent()
            }

            // General escape planning
            else -> {
                """
                I'm your Escape Planning AI Assistant. I can help with:
                
                💰 Financial Planning
                • How much money you need
                • How to save secretly
                • Funding sources
                • Microfinance options
                
                🏠 Safe Accommodation
                • Government shelters
                • NGO safe houses
                • Working women's hostels
                • Emergency accommodation
                
                📋 Legal & Documents
                • Important papers to take
                • How to get duplicates
                • Your legal rights
                • Custody matters
                
                💼 Employment & Skills
                • Finding work
                • Skill training programs
                • Self-employment
                • Government schemes
                
                👶 Planning with Children
                • What to arrange
                • Custody rights
                • Child support
                • Education continuity
                
                What aspect of your escape plan would you like help with?
                """.trimIndent()
            }
        }
    }

    /**
     * Generate General safety responses
     */
    private fun generateGeneralResponse(message: String): String {
        val lowerMessage = message.toLowerCase(Locale.getDefault())

        return when {
            lowerMessage.contains("help") || lowerMessage.contains("emergency") -> {
                """
                Emergency Help Available 24/7:
                
                🚨 Helpline Numbers:
                • Women Helpline: 181
                • Police: 100
                • Ambulance: 108
                • National Commission for Women: 7827-170-170
                
                📱 Your SHAKTI App:
                • Long-press % button = Instant SOS
                • Say "HELP" 3 times = Auto-record
                • Records video + audio + location
                
                Stay safe! I'm here to help.
                """.trimIndent()
            }

            lowerMessage.contains("features") || lowerMessage.contains("how to use") -> {
                """
                SHAKTI AI Features:
                
                🔐 Protection:
                • Voice-activated: Say "HELP" 3x
                • Panic button: Long-press %
                • Auto-recording when triggered
                
                📹 Evidence:
                • Dual camera recording
                • Audio capture
                • GPS location tracking
                • Secure hidden storage
                
                ⚖️ Legal:
                • Auto-generate FIR
                • Find lawyers
                • Know your rights
                
                🏠 Escape Planner:
                • Financial calculator
                • Safe house finder
                • Step-by-step timeline
                
                What would you like to explore?
                """.trimIndent()
            }

            else -> {
                """
                Hello! I'm SHAKTI AI Assistant. I can help you with:
                
                ⚖️ Legal Matters (Say "Legal Help")
                🏠 Escape Planning (Say "Escape Plan")
                🆘 Emergency Assistance
                📱 App Features
                
                Just ask me anything in your own words!
                """.trimIndent()
            }
        }
    }

    /**
     * Clear conversation history
     */
    fun clearHistory() {
        conversationHistory.clear()
    }

    /**
     * Cleanup resources
     */
    fun shutdown() {
        textToSpeech?.stop()
        textToSpeech?.shutdown()
    }
}
