package data

import isRunning
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.remote.RemoteWebDriver

class LoginSession {

    private var driver: RemoteWebDriver? = null

    suspend fun logIn(): Boolean = withContext(Dispatchers.IO) {
        val driver = loadDriver()
        driver.get("https://accounts.google.com/ServiceLogin")
        driver.waitForLoginOrClose()
    }

    suspend fun isLoggedIn(): Boolean = withContext(Dispatchers.IO) {
        try {
            loadDriver().manage().cookies.any { it.domain == ".google.com" && it.name == "SSID" }
        } catch (e: Exception) {
            false
        }
    }

    fun closeDriver() {
        try {
            driver?.quit()
        } catch (e: Exception) {
        }
    }

    private suspend fun loadDriver(): RemoteWebDriver = withContext(Dispatchers.IO) {
        driver ?: with(ChromeOptions()) {
            addArguments("user-data-dir=selenium")
            val chromeDriver = ChromeDriver(this)
            driver = chromeDriver
            chromeDriver
        }
    }

    private suspend fun RemoteWebDriver.waitForLoginOrClose(): Boolean {
        val isLoggedIn: Boolean
        while (true) {
            if (isLoggedIn()) {
                isLoggedIn = true
                closeDriver()
                break
            } else if (!isRunning()) {
                driver = null
                isLoggedIn = false
                break
            }
            delay(10)
        }
        return isLoggedIn
    }

}