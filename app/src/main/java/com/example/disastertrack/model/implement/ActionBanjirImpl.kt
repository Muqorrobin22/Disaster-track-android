package com.example.disastertrack.model.implement

import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.disastertrack.model.data.ReportsData
import com.example.disastertrack.model.service.ReportApiService
import com.example.disastertrack.utils.BaseURL
import com.example.disastertrack.utils.ButtonAction
import com.example.disastertrack.view.adapter.ReportAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class ActionBanjirImpl : ButtonAction {


    override fun performAction() {
        // Implement action 1 logic here

        Log.d("MainActivity", "Perform Banjir button")
    }

    override fun getName(): String {
        return "Banjir"
    }
}