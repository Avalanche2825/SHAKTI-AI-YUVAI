package com.shakti.ai.services

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "FCMService"
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "FCM Token: $token")

        // TODO: Send token to server
        // Store token in SharedPreferences or send to backend
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d(TAG, "Message received from: ${message.from}")

        // TODO: Handle incoming messages
        // - Display notification
        // - Update app state
        // - Trigger alerts if needed

        message.notification?.let {
            Log.d(TAG, "Notification Title: ${it.title}")
            Log.d(TAG, "Notification Body: ${it.body}")
        }

        message.data.isNotEmpty().let {
            Log.d(TAG, "Message data payload: ${message.data}")
        }
    }
}
