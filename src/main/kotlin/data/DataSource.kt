package data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.lang.Exception

class DataSource {

    private val filterFile = File("filters.txt")

    private val urlFile = File("url.txt")

    suspend fun getFilter() = withContext(Dispatchers.IO) {
        try {
            filterFile.readText()
        } catch (e: Exception) {
            ""
        }
    }

    suspend fun getFilterList() = withContext(Dispatchers.IO) {
        try {
            filterFile.readText().split(";")
        } catch (e: Exception) {
            listOf()
        }
    }

    suspend fun getURL() = withContext(Dispatchers.IO) {
        try {
            urlFile.readText()
        } catch (e: Exception) {
            ""
        }
    }

    suspend fun saveFilter(filter: String) = withContext(Dispatchers.IO) {
        filterFile.writeText(filter)
    }

    suspend fun saveUrl(url: String) = withContext(Dispatchers.IO) {
        urlFile.writeText(url)
    }

}