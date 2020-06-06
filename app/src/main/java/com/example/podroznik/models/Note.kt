package com.example.podroznik.models

data class Note (
    var name: String = "",
    var description: String = "",
    var diameterCircle: Double = 0.0,
    var userId: String = "",
    var noteId: String? = ""
)