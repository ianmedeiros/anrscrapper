package data

import java.io.File

data class ANRData(
    val errorClassLine: String,
    val errorLine: String,
    val stack: List<String>,
    val file: File
)