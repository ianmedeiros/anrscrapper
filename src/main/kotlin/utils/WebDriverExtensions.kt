import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.remote.RemoteWebDriver
import java.lang.Exception

fun WebElement.attributes(driver: RemoteWebDriver): String {
    val script = """
        var items = {}; 
        for (index = 0; index < arguments[0].attributes.length; ++index) { 
            items[arguments[0].attributes[index].name] = arguments[0].attributes[index].value 
        };
        return items;
    """.trimIndent()

    return driver.executeScript(script, this).toString()
}

fun WebDriver.isRunning() =
    try {
        windowHandle
        true
    } catch (e: Exception) {
        false
    }