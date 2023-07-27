package com.example.disastertrack.model.implement

import android.util.Log
import com.example.disastertrack.utils.ButtonAction

class ActionGempaImpl : ButtonAction {
    override fun performAction() {
        // Implement action 1 logic here

        Log.d("MainActivity", "Perform Gempa button")
    }

    override fun getName(): String {
        return "Gempa"
    }
}