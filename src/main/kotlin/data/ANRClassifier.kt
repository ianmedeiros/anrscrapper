package data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.File

class ANRClassifier(
    private val filterDataSource: DataSource
) {

    suspend fun classify() = withContext(Dispatchers.IO) {
        val summary = generateSummaryMap()
        generateSummaryReport(summary)
        summary
    }

    private suspend fun generateSummaryMap(): Map<String, List<ANRData>> {
        println("Generating summary map")
        val filters = filterDataSource.getFilterList()
        return File("ANR").walkBottomUp()
            .filter { it.isFile }
            .map { file ->
                val snippetLines = file.readLines()

                //Notice that we initiate the line data and it only changes if we find a better classification
                var classifiedLineData = ANRLine("unclassified", snippetLines[0])
                classifiedLineData = findFirstLineStartingWithCom(snippetLines) ?: classifiedLineData
                runBlocking {
                    classifiedLineData = findLastLineContainerAnyFilterString(snippetLines, filters) ?: classifiedLineData
                }
                println("Grouping: ${classifiedLineData.errorLine}")
                ANRData(
                    classifiedLineData.errorClassLine,
                    classifiedLineData.errorLine,
                    snippetLines,
                    file
                )

            }
            .groupBy { snippetData -> snippetData.errorClassLine }
            .toList()
            .sortedByDescending { (_, value) -> value.size } //We are ordering by the amount of SnippetData in the same group
            .toMap().also {
                println("Summary map generated!")
            }
    }

    private fun findLastLineContainerAnyFilterString(snippetLines: List<String>, filters: List<String>): ANRLine? {
        for (filter in filters) {
            val lineWithFilter = snippetLines.firstOrNull { snippetLine ->
                filter in snippetLine
            }

            if (lineWithFilter != null) {
                return ANRLine(
                    lineWithFilter.substringAfter("(").substringBefore(")"),
                    lineWithFilter
                )
            }
        }

        return null
    }

    private fun generateSummaryReport(summary: Map<String, List<ANRData>>) {
        println("Generating summary report.")
        File("summaryFolder").mkdirs()
        File("Summary.txt").printWriter().use { writer ->
            for ((group, anrList) in summary) {
                storeFilesOnSummaryFolder(group, anrList)

                //Write summary section header
                writer.println("$group - ${anrList.size}")
                println("$group - ${anrList.size}")

                //Write the first stack into the summary file
                anrList[0].stack.forEach {
                    writer.println(it)
                    println(it)
                }
                writer.println("\n")
                println("\n")
            }
        }
        println("Summary report generated!")
    }

    @Suppress("RegExpRedundantEscape")
    private fun storeFilesOnSummaryFolder(group: String, anrList: List<ANRData>) {
        //Create the group folder
        val groupFolder =
            "summaryFolder${File.separatorChar}${group.replace(Regex("[^a-zA-Z0-9\\.\\-]"), "_")}" //replace : for a valid windows folder char

        println("Saving on $groupFolder")
        File(groupFolder).mkdirs()

        //copy files to summaryFolder
        anrList.map { snippet -> snippet.file }.forEach { file ->
            println("Copying ${file.name}")
            file.copyTo(File("$groupFolder${File.separatorChar}${file.name}"), true)
        }
    }

    private fun findFirstLineStartingWithCom(
        snippetLines: List<String>
    ): ANRLine? {
        val comLine = snippetLines.firstOrNull { line ->
            "at com." in line
        }
        if (comLine != null) {
            return ANRLine(
                errorClassLine = comLine.substringAfter("(").substringBefore(")"),
                errorLine = comLine
            )
        }
        return null
    }

}