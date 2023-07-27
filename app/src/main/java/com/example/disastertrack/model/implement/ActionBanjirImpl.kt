package com.example.disastertrack.model.implement

import android.util.Log
import android.widget.Toast
import com.example.disastertrack.utils.ButtonAction

class ActionBanjirImpl : ButtonAction {


    override fun performAction() {
        // Implement action 1 logic here
        Log.d("MainActivity", "Perform Banjir button")
    }

    override fun getName(): String {
        return "Banjir"
    }
}