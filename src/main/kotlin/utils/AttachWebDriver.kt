package utils

import org.openqa.selenium.remote.DesiredCapabilities
import org.openqa.selenium.remote.RemoteWebDriver
import org.openqa.selenium.remote.SessionId
import java.net.URL

class AttachWebDriver(
    sessionId: SessionId,
    commandExecutor: URL
) : RemoteWebDriver(AttachCommandExecutor(sessionId, commandExecutor), DesiredCapabilities())