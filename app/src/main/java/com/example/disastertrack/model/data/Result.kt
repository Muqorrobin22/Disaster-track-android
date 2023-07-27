package com.example.disastertrack.model.data

data class Result(
    val type: String,
    val objects: Objects,
    val arcs: List<Any>,
    val bbox: List<Double>
)
