package com.example.indianarmyrkpuram

data class Quad<out A, out B, out C, out D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)

data class ExtractedValues(
    val numbers: String,
    val compactor: String,
    val room: String,
    val shelf: String
)
