package com.example.disastertrack.model.implement

import android.util.Log
import com.example.disastertrack.utils.ButtonAction

class ActionGunungImpl : ButtonAction {
    override fun performAction() {
        // Implement action 1 logic here

        Log.d("MainActivity", "Perform Gunung Meletus button")
    }

    override fun getName(): String {
        return "Gunung Meletus"
    }
}