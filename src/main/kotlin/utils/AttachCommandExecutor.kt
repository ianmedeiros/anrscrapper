package utils

import org.openqa.selenium.remote.Command
import org.openqa.selenium.remote.HttpCommandExecutor
import org.openqa.selenium.remote.Response
import org.openqa.selenium.remote.SessionId
import org.openqa.selenium.remote.http.W3CHttpCommandCodec
import org.openqa.selenium.remote.http.W3CHttpResponseCodec
import java.io.IOException
import java.net.URL

class AttachCommandExecutor(
    private val sessionId: SessionId,
    commandExecutor: URL
) : HttpCommandExecutor(commandExecutor) {

    @Throws(IOException::class)
    override fun execute(command: Command): Response {
        val response = Response()
        if (command.name === "newSession") {
            response.sessionId = sessionId.toString()
            response.status = 0
            response.value = emptyMap<String, String>()
            try {
                val commandCodec = this.javaClass.superclass.getDeclaredField("commandCodec")
                commandCodec.isAccessible = true
                commandCodec.set(this, W3CHttpCommandCodec())

                val responseCodec = this.javaClass.superclass.getDeclaredField("responseCodec")
                responseCodec.isAccessible = true
                responseCodec.set(this, W3CHttpResponseCodec())
            } catch (e: NoSuchFieldException) {
                e.printStackTrace()
            } catch (e: IllegalAccessException) {
                e.printStackTrace()
            }
        } else {
            return super.execute(command)
        }
        return response
    }
}