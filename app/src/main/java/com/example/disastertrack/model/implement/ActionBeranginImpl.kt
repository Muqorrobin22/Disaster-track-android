package com.example.disastertrack.model.implement

import android.util.Log
import com.example.disastertrack.utils.ButtonAction

class ActionBeranginImpl: ButtonAction {

    override fun performAction() {
        // Implement action 1 logic here

        Log.d("MainActivity", "Perform Berangin button")
    }

    override fun getName(): String {
        return "Berangin"
    }
}