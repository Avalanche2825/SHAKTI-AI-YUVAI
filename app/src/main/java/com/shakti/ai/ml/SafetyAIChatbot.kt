package com.shakti.ai.ml

/**
 * Safety AI Chatbot - Women's Safety Focused Assistant
 *
 * Features:
 * - Pattern-based response system
 * - Comprehensive knowledge about:
 *   • Indian legal rights (IPC sections)
 *   • Emergency procedures
 *   • Domestic violence support
 *   • Sexual harassment laws
 *   • Safety tips
 *   • Emotional support
 */
class SafetyAIChatbot {

    /**
     * Get AI response based on user query
     */
    fun getResponse(query: String): String {
        val normalizedQuery = query.lowercase().trim()

        return when {
            // Legal rights and IPC sections
            matchesAny(
                normalizedQuery,
                "legal",
                "rights",
                "ipc",
                "section",
                "law"
            ) -> getLegalResponse(normalizedQuery)

            // FIR and police procedures
            matchesAny(normalizedQuery, "fir", "police", "complaint", "report") -> getFIRResponse(
                normalizedQuery
            )

            // Domestic violence
            matchesAny(
                normalizedQuery,
                "domestic",
                "husband",
                "violence",
                "abuse",
                "dowry"
            ) -> getDomesticViolenceResponse(normalizedQuery)

            // Sexual harassment
            matchesAny(
                normalizedQuery,
                "harassment",
                "harass",
                "sexual",
                "stalking",
                "eve teasing"
            ) -> getHarassmentResponse(normalizedQuery)

            // Emergency and safety
            matchesAny(
                normalizedQuery,
                "emergency",
                "danger",
                "help",
                "unsafe",
                "threat"
            ) -> getEmergencyResponse(normalizedQuery)

            // Escape planning
            matchesAny(
                normalizedQuery,
                "escape",
                "leave",
                "run away",
                "shelter",
                "safe house"
            ) -> getEscapeResponse(normalizedQuery)

            // Emotional support
            matchesAny(
                normalizedQuery,
                "scared",
                "afraid",
                "depressed",
                "suicide",
                "emotional",
                "support"
            ) -> getEmotionalSupportResponse(normalizedQuery)

            // Evidence collection
            matchesAny(
                normalizedQuery,
                "evidence",
                "proof",
                "record",
                "document"
            ) -> getEvidenceResponse(normalizedQuery)

            // App features
            matchesAny(
                normalizedQuery,
                "feature",
                "how to use",
                "app",
                "calculator",
                "voice command"
            ) -> getAppFeaturesResponse(normalizedQuery)

            // Greeting
            matchesAny(normalizedQuery, "hi", "hello", "hey", "namaste") -> getGreetingResponse()

            // Thank you
            matchesAny(normalizedQuery, "thank", "thanks", "grateful") -> getThankYouResponse()

            // Default response
            else -> getDefaultResponse()
        }
    }

    private fun matchesAny(query: String, vararg keywords: String): Boolean {
        return keywords.any { query.contains(it) }
    }

    private fun getLegalResponse(query: String): String {
        return when {
            query.contains("domestic") || query.contains("dowry") -> {
                "**Your Legal Rights Against Domestic Violence:**\n\n" +
                        "**IPC Section 498A:** Cruelty by husband or relatives\n" +
                        "**IPC Section 304B:** Dowry death\n" +
                        "**IPC Section 406:** Criminal breach of trust\n" +
                        "**IPC Section 323:** Voluntarily causing hurt\n\n" +
                        "**Protection of Women from Domestic Violence Act, 2005:**\n" +
                        "• Right to residence\n" +
                        "• Right to maintenance\n" +
                        "• Protection orders\n" +
                        "• Custody orders\n\n" +
                        "You can file a complaint at any police station or approach a magistrate directly.\n\n" +
                        "Use our **NYAY Legal Assistant** to auto-generate your FIR! 📋"
            }

            query.contains("harassment") || query.contains("sexual") -> {
                "**Legal Protection Against Sexual Harassment:**\n\n" +
                        "**IPC Section 354:** Assault with intent to outrage modesty\n" +
                        "**IPC Section 354A:** Sexual harassment\n" +
                        "**IPC Section 354B:** Disrobing\n" +
                        "**IPC Section 354C:** Voyeurism\n" +
                        "**IPC Section 354D:** Stalking\n" +
                        "**IPC Section 509:** Insulting modesty\n\n" +
                        "**Workplace Harassment:**\n" +
                        "POSH Act (Prevention of Sexual Harassment at Workplace, 2013)\n" +
                        "• Every workplace must have ICC (Internal Complaints Committee)\n" +
                        "• File complaint within 3 months\n\n" +
                        "Need help filing FIR? Use NYAY Legal Assistant! 📋"
            }

            query.contains("rape") || query.contains("assault") -> {
                "**Legal Protection Against Sexual Assault:**\n\n" +
                        "**IPC Section 375-376:** Rape and punishment\n" +
                        "**IPC Section 376A:** Causing death or vegetative state\n" +
                        "**IPC Section 376B:** Intercourse by husband during separation\n" +
                        "**IPC Section 376D:** Gang rape\n\n" +
                        "**Important:**\n" +
                        "• Immediate medical examination\n" +
                        "• Zero FIR can be filed at any police station\n" +
                        "• Statement recorded by female officer\n" +
                        "• Free legal aid available\n\n" +
                        "**National Helplines:**\n" +
                        "Women Helpline: 1091\n" +
                        "Police: 100\n" +
                        "Emergency: 112"
            }

            else -> {
                "**Key Legal Rights for Women in India:**\n\n" +
                        "1. **Right to Live with Dignity** (Article 21)\n" +
                        "2. **Right to Equality** (Article 14)\n" +
                        "3. **Right Against Exploitation** (Article 23)\n\n" +
                        "**Major Legal Protections:**\n" +
                        "• Domestic Violence Act, 2005\n" +
                        "• Sexual Harassment Act (POSH), 2013\n" +
                        "• Dowry Prohibition Act, 1961\n" +
                        "• Immoral Traffic Prevention Act, 1956\n\n" +
                        "**Criminal Laws:**\n" +
                        "• IPC 354: Assault on women\n" +
                        "• IPC 375-376: Sexual assault\n" +
                        "• IPC 498A: Domestic cruelty\n" +
                        "• IPC 509: Insulting modesty\n\n" +
                        "What specific situation would you like to know about?"
            }
        }
    }

    private fun getFIRResponse(query: String): String {
        return "**How to File an FIR (First Information Report):**\n\n" +
                "**Step 1:** Go to nearest police station\n" +
                "**Step 2:** Provide details of the incident\n" +
                "**Step 3:** Get FIR copy with diary number\n\n" +
                "**Important Rights:**\n" +
                "• FIR must be registered (mandatory)\n" +
                "• Zero FIR: Can be filed at any police station\n" +
                "• Online FIR available in many states\n" +
                "• Free copy of FIR is your right\n" +
                "• If police refuse, approach SP/DGP\n\n" +
                "**For Sexual Offenses:**\n" +
                "• Statement recorded by female officer\n" +
                "• Medical examination at government hospital\n" +
                "• Dress/items kept as evidence\n\n" +
                "**SHAKTI Feature:** Use our **NYAY Legal Assistant** to auto-generate your FIR with correct IPC sections! 📋✨\n\n" +
                "It will:\n" +
                "• Identify applicable IPC sections\n" +
                "• Format the complaint properly\n" +
                "• Include all necessary details\n" +
                "• Ready to submit at police station"
    }

    private fun getDomesticViolenceResponse(query: String): String {
        return "**Protection from Domestic Violence:**\n\n" +
                "Domestic violence includes:\n" +
                "• Physical abuse\n" +
                "• Sexual abuse\n" +
                "• Verbal/emotional abuse\n" +
                "• Economic abuse\n\n" +
                "**Immediate Actions:**\n" +
                "1. **Document everything** (photos, medical records, messages)\n" +
                "2. **File complaint** at police station\n" +
                "3. **Get Protection Order** from magistrate\n" +
                "4. **Seek medical attention** (important for evidence)\n\n" +
                "**Legal Remedies:**\n" +
                "• Protection order\n" +
                "• Residence order\n" +
                "• Monetary relief\n" +
                "• Custody of children\n" +
                "• Compensation\n\n" +
                "**Where to Approach:**\n" +
                "• Police Station\n" +
                "• Protection Officer\n" +
                "• Magistrate\n" +
                "• Women's Cell\n\n" +
                "**SHAKTI Features:**\n" +
                "• **Voice Command:** Say 'HELP' 3 times for emergency\n" +
                "• **Auto Recording:** Evidence captured automatically\n" +
                "• **Escape Planner:** Plan your safe exit\n" +
                "• **NYAY Legal:** Auto-generate FIR\n\n" +
                "You deserve to be safe. We're here to help. 💜"
    }

    private fun getHarassmentResponse(query: String): String {
        return "**Protection Against Harassment:**\n\n" +
                "**Workplace Harassment:**\n" +
                "• File complaint with ICC (Internal Complaints Committee)\n" +
                "• Complaint must be filed within 3 months\n" +
                "• Inquiry completed in 90 days\n" +
                "• IPC Section 354A applies\n\n" +
                "**Street/Public Harassment:**\n" +
                "• IPC Section 509: Insulting modesty\n" +
                "• IPC Section 354D: Stalking\n" +
                "• File FIR immediately\n\n" +
                "**Online Harassment:**\n" +
                "• IT Act Section 67: Publishing obscene content\n" +
                "• IPC Section 507: Criminal intimidation\n" +
                "• Save all evidence (screenshots, messages)\n" +
                "• Report to Cyber Crime Cell\n\n" +
                "**Stalking:**\n" +
                "• IPC Section 354D\n" +
                "• Punishment: Up to 5 years imprisonment\n" +
                "• First offense: 3 years\n" +
                "• Subsequent offense: 5 years\n\n" +
                "**Safety Tips:**\n" +
                "• Keep SHAKTI app active (long-press AC button)\n" +
                "• Share location with trusted contacts\n" +
                "• Voice command: Say 'HELP' 3 times for emergency\n" +
                "• Auto-recording will capture evidence\n\n" +
                "Stay safe! Use our monitoring features. 🛡️"
    }

    private fun getEmergencyResponse(query: String): String {
        return "**🚨 EMERGENCY PROCEDURES:**\n\n" +
                "**Immediate Danger:**\n" +
                "1. Call **100** (Police) or **112** (Emergency)\n" +
                "2. Women Helpline: **1091**\n" +
                "3. Domestic Violence Helpline: **181**\n\n" +
                "**Using SHAKTI App:**\n\n" +
                "**Method 1: Voice Command**\n" +
                "• Say **'HELP'** 3 times within 8 seconds\n" +
                "• Emergency automatically triggered!\n" +
                "• Recording starts\n" +
                "• Contacts alerted\n\n" +
                "**Method 2: Secret Code**\n" +
                "• Open calculator\n" +
                "• Type **911=**\n" +
                "• Instant SOS activated\n\n" +
                "**Method 3: Long Press**\n" +
                "• Long-press AC button (enable monitoring)\n" +
                "• Green dot = Protection active\n\n" +
                "**What Happens:**\n" +
                "✅ Video recording (front + back cameras)\n" +
                "✅ Audio recording\n" +
                "✅ Location tracking\n" +
                "✅ Emergency contacts notified\n" +
                "✅ Evidence saved securely\n\n" +
                "**All evidence saved in hidden location on your device!**\n\n" +
                "**Safe Houses:** Check Escape Planner for nearby shelters 🏠\n\n" +
                "You're not alone. Help is available. 💜"
    }

    private fun getEscapeResponse(query: String): String {
        return "**Escape Planning Guide:**\n\n" +
                "**SHAKTI Escape Planner** will help you:\n\n" +
                "**Financial Planning:**\n" +
                "• Estimated costs: ₹90,000 minimum\n" +
                "• Transport: ₹15,000\n" +
                "• Shelter (3 months): ₹30,000\n" +
                "• Legal fees: ₹20,000\n" +
                "• Emergency buffer: ₹15,000\n" +
                "• Documents: ₹10,000\n\n" +
                "**Safe Houses Near You:**\n" +
                "• Shakti Foundation, Delhi\n" +
                "• ARIVAA, Ghaziabad\n" +
                "• Breakthrough India, NCR\n" +
                "• Many more...\n\n" +
                "**Documents to Take:**\n" +
                "✓ Aadhar Card\n" +
                "✓ PAN Card\n" +
                "✓ Bank passbook\n" +
                "✓ Marriage certificate\n" +
                "✓ Medical records\n" +
                "✓ Children's documents\n" +
                "✓ Property papers (if any)\n\n" +
                "**Safety Checklist:**\n" +
                "1. Save money secretly\n" +
                "2. Keep bag packed\n" +
                "3. Inform trusted friend\n" +
                "4. Have escape route planned\n" +
                "5. Keep phone charged\n\n" +
                "**Use Escape Planner feature in app for detailed roadmap!** 🗺️\n\n" +
                "Remember: Your safety comes first. 💜"
    }

    private fun getEmotionalSupportResponse(query: String): String {
        return "**You Are Not Alone 💜**\n\n" +
                "I'm sorry you're going through this. What you're feeling is valid.\n\n" +
                "**Immediate Support:**\n" +
                "• Mental Health Helpline: **KIRAN (1800-599-0019)**\n" +
                "• Women's Helpline: **1091**\n" +
                "• Childline (if minor): **1098**\n\n" +
                "**Remember:**\n" +
                "• This is NOT your fault\n" +
                "• You deserve safety and respect\n" +
                "• Your feelings are valid\n" +
                "• Seeking help is strength, not weakness\n" +
                "• Recovery is possible\n\n" +
                "**Coping Strategies:**\n" +
                "1. Talk to someone you trust\n" +
                "2. Journal your feelings\n" +
                "3. Practice deep breathing\n" +
                "4. Seek professional counseling\n" +
                "5. Join support groups\n\n" +
                "**If you're having suicidal thoughts:**\n" +
                "Please call **AASRA: 9820466726** (24/7)\n" +
                "or visit nearest hospital emergency\n\n" +
                "**Resources:**\n" +
                "• iCall (TISS): 9152987821\n" +
                "• Vandrevala Foundation: 9999666555\n" +
                "• Snehi: 91-22-27546669\n\n" +
                "You are stronger than you know. Better days are ahead. 🌟\n\n" +
                "How can I help you right now?"
    }

    private fun getEvidenceResponse(query: String): String {
        return "**Collecting Evidence for Your Case:**\n\n" +
                "**Types of Evidence:**\n" +
                "1. **Physical Evidence**\n" +
                "   • Photographs of injuries\n" +
                "   • Medical reports\n" +
                "   • Torn clothes\n" +
                "   • Weapons used\n\n" +
                "2. **Documentary Evidence**\n" +
                "   • WhatsApp/SMS messages\n" +
                "   • Emails\n" +
                "   • Voice recordings\n" +
                "   • Bank statements\n" +
                "   • Property documents\n\n" +
                "3. **Digital Evidence**\n" +
                "   • CCTV footage\n" +
                "   • Call recordings\n" +
                "   • Social media posts\n" +
                "   • GPS location data\n\n" +
                "**SHAKTI Auto-Evidence Collection:**\n\n" +
                "When emergency triggered:\n" +
                "✅ **Dual camera recording** (front + back)\n" +
                "✅ **Audio recording**\n" +
                "✅ **GPS location** tracking\n" +
                "✅ **Timestamp** on all evidence\n" +
                "✅ **Saved in hidden location** on device\n\n" +
                "**How to Access:**\n" +
                "• Open Dashboard (type 999=)\n" +
                "• Go to Incident Reports\n" +
                "• View and export evidence\n\n" +
                "**Important:**\n" +
                "• Keep original evidence safe\n" +
                "• Make multiple copies\n" +
                "• Store in cloud/email\n" +
                "• Never edit original evidence\n\n" +
                "**All SHAKTI recordings are court-admissible!** ⚖️"
    }

    private fun getAppFeaturesResponse(query: String): String {
        return "**🌟 SHAKTI App Features:**\n\n" +
                "**1. Calculator Disguise**\n" +
                "• App looks like calculator\n" +
                "• Maintains your privacy\n" +
                "• Works as real calculator too!\n\n" +
                "**2. Secret Codes**\n" +
                "• **999=** Open Dashboard\n" +
                "• **911=** Emergency SOS\n" +
                "• **777=** Settings\n\n" +
                "**3. Voice Commands** 🎤\n" +
                "• Say **'HELP'** 3 times (within 8 seconds)\n" +
                "• Hands-free emergency activation\n" +
                "• Works in background\n\n" +
                "**4. Auto-Recording** 📹\n" +
                "• Dual camera (front + back)\n" +
                "• Audio capture\n" +
                "• Saved in hidden location\n\n" +
                "**5. NYAY Legal Assistant** ⚖️\n" +
                "• Auto-generate FIR\n" +
                "• IPC section identification\n" +
                "• Legal advice\n\n" +
                "**6. Escape Planner** 🗺️\n" +
                "• Financial planning\n" +
                "• Safe house locations\n" +
                "• Document checklist\n\n" +
                "**7. AI Monitoring** 🤖\n" +
                "• Real-time threat detection\n" +
                "• Audio analysis\n" +
                "• Live visualization\n\n" +
                "**8. Community Network** 💜\n" +
                "• Nearby SHAKTI users\n" +
                "• Bluetooth SOS alerts\n" +
                "• Women helping women\n\n" +
                "**How to Enable Protection:**\n" +
                "• Long-press **AC** button in calculator\n" +
                "• Green dot = Active\n" +
                "• Voice commands enabled\n\n" +
                "What feature would you like to know more about?"
    }

    private fun getGreetingResponse(): String {
        val greetings = listOf(
            "Hello! I'm your SHAKTI AI Assistant. How can I help you today? 💜",
            "Hi there! I'm here to support you with legal advice, safety tips, and emotional support. What do you need?",
            "Namaste! I'm SHAKTI AI, your safety companion. Ask me anything about your rights, legal protection, or emergency procedures.",
            "Hey! Need information about women's safety, legal rights, or emergency help? I'm here for you! 🌟"
        )
        return greetings.random()
    }

    private fun getThankYouResponse(): String {
        val responses = listOf(
            "You're welcome! Stay safe and remember - you're never alone. 💜",
            "Glad I could help! If you need anything else, I'm always here. 🌟",
            "My pleasure! Your safety is my priority. Take care! 💜",
            "Anytime! Remember to keep the protection active (long-press AC button). Stay safe! 🛡️"
        )
        return responses.random()
    }

    private fun getDefaultResponse(): String {
        return "I understand you're asking about something. I can help you with:\n\n" +
                "• **Legal Rights** - IPC sections, laws, rights\n" +
                "• **FIR Filing** - How to file complaint\n" +
                "• **Domestic Violence** - Protection and support\n" +
                "• **Sexual Harassment** - Laws and remedies\n" +
                "• **Emergency** - Immediate help procedures\n" +
                "• **Escape Planning** - Safe exit strategies\n" +
                "• **Emotional Support** - Coping and counseling\n" +
                "• **Evidence** - Collecting and preserving proof\n" +
                "• **App Features** - How to use SHAKTI\n\n" +
                "What specific topic would you like to know about?"
    }
}
