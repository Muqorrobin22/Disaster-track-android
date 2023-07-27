package com.example.disastertrack.model.implement

import android.util.Log
import com.example.disastertrack.utils.ButtonAction

class ActionKabutImpl : ButtonAction {
    override fun performAction() {
        // Implement action 1 logic here

        Log.d("MainActivity", "Perform Kabut button")
    }

    override fun getName(): String {
        return "Kabut"
    }
}