package com.example.disastertrack.model.data

data class Properties(
    val pkey: String,
    val created_at: String,
    val source: String,
    val status: String,
    val url: String,
    val image_url: String?,
    val disaster_type: String,
    val report_data: ReportData,
    val tags: Tags,
    val title: String?,
    val text: String?,
    val partner_code: String?,
    val partner_icon: String?
)
