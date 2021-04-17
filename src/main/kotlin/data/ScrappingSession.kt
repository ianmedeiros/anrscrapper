package data

import attributes
import kotlinx.coroutines.*
import org.openqa.selenium.By
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.remote.RemoteWebDriver
import org.openqa.selenium.support.ui.WebDriverWait
import java.io.File

class ScrappingSession(
    private val dataSource: DataSource
) {

    private val drivers = mutableListOf<RemoteWebDriver>()

    suspend fun startScrapping() = withContext(Dispatchers.IO) {
        val anrListDriver = createDriver()
        anrListDriver.get(dataSource.getURL())

        if (anrListDriver.waitForANRListPage()) {
            //for some reason, loading too much url's at the same time give a lot of errors
            val anrLinks = anrListDriver.getANRLinks(take = 10)
            anrListDriver.close() //So we can copy session cookies/etc

            val detailDrivers = startANRDetailsDrivers(anrLinks)

            detailDrivers.forEachIndexed { detailPageIndex, detailPageDriver ->
                launch {
                    var anrOnPageCount = 0
                    while (true) {
                        val anrStack = detailPageDriver.getANRStack()
                        println("ANR Stack: $anrStack")

                        if (anrStack != null)
                            saveANR("session$detailPageIndex-$anrOnPageCount.txt", anrStack)

                        detailPageDriver.goToNextANR()
                        anrOnPageCount++
                        delay(500)
                    }
                }
            }
        }
    }

    fun closeAll() {
        drivers.forEach {
            try {
                it.quit()
            } catch (ignored: Exception) {
            }
        }
    }

    private fun saveANR(filename: String, content: String) {
        val dir = File("ANR")
        if (!dir.exists()) dir.mkdirs()
        File("ANR${File.separatorChar}$filename").writeText(content)
    }

    private suspend fun startANRDetailsDrivers(anrLinks: List<String>): List<RemoteWebDriver> =
        anrLinks.mapIndexed { index, link ->
            val userDataFolderName = copyLoggedInUserData(index)
            val driver = createDriver(userDataFolderName)
            driver.get(link)

            driver.waitForANRDetailsPage()
            driver
        }

    private fun copyLoggedInUserData(index: Int): String {
        val sessionName = "selenium$index"
        val sessionFolder = File(sessionName)
        if (!sessionFolder.exists()) {
            println("Coppying session($index) folder.")
            File("selenium").copyRecursively(File(sessionName), overwrite = true)
            println("Session($index) copy finished")
        } else {
            println("Session($index) already copied")
        }
        return sessionName
    }

    private fun RemoteWebDriver.waitForANRListPage(): Boolean {
        return try {
            val element = WebDriverWait(this, 20).until {
                findElement(By.cssSelector("button[aria-label='View details']"))
            }
            println("'View Details' Button found. Attributes: ${element.attributes(this)}")
            true
        } catch (e: Exception) {
            println(e)
            false
        }
    }

    private fun RemoteWebDriver.getANRLinks(take: Int): List<String> =
        try {
            findElements(By.xpath("//a[@href]"))
                ?.map { element -> element.getAttribute("href") }
                ?.filter { link -> link.contains("play.google.com/console/u") }
                ?.take(take) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }

    private suspend fun RemoteWebDriver.waitForANRDetailsPage() {
        var found = false
        while (!found) {
            try {
                val element = WebDriverWait(this, 5).until { driver ->
                    driver.findElement(By.cssSelector("button[aria-label='Show more button to show the exception']"))
                }
                println("'Show more' button found. Attributes: ${element.attributes(this)}")
                found = true
            } catch (e: Exception) {
                println(e)
                delay(1000)
                navigate().refresh()
            }
        }
    }


    private fun RemoteWebDriver.getANRStack(): String? =
        try {
            val showMoreButton = findElement(By.cssSelector("button[aria-label='Show more button to show the exception']"))
            showMoreButton.click()

            WebDriverWait(this, 5).until { driver ->
                val snippet = driver.findElement(By.cssSelector("snippet[debug-id='expanded-snippet']"))
                if (!snippet.text.isNullOrEmpty())
                    snippet
                else
                    null
            }?.text
        } catch (e: Exception) {
            println(e)
            null
        }

    private suspend fun RemoteWebDriver.goToNextANR() {
        while (!clickNextANR()) {
            refreshANRList()
            delay(500)
        }
    }

    private suspend fun RemoteWebDriver.refreshANRList() {
        clickPreviewsANR()
        delay(500)
        clickNextANR()
    }

    private fun RemoteWebDriver.clickNextANR(): Boolean =
        try {
            findElementByCssSelector("button[aria-label='Next occurrence of this ANR']").click()
            true
        } catch (e: Exception) {
            println(e)
            false
        }

    private fun RemoteWebDriver.clickPreviewsANR(): Boolean =
        try {
            findElementByCssSelector("button[aria-label='Previous occurrence of this ANR']").click()
            true
        } catch (e: Exception) {
            println(e)
            false
        }

    private suspend fun createDriver(userData: String = "selenium"): RemoteWebDriver = withContext(Dispatchers.IO) {
        val options = ChromeOptions()
        options.addArguments("user-data-dir=$userData")
        ChromeDriver(options).also {
            drivers.add(it)
        }
    }
}



