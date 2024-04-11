package com.example.indianarmyrkpuram

//sealed class ExtractionResult {
//    data class Success(val numbers: String, val C: String, val R: String, val S: String) : ExtractionResult()
//    data class Failure(val errorMessage: String) : ExtractionResult()
//}
sealed class ExtractionResult {
    data class Success(
        val numbers: String,
        val compactor: String?,
        val room: String?,
        val shelf: String?
    ) : ExtractionResult()

    data class Failure(val errorMessage: String) : ExtractionResult()
}