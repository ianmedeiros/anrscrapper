import androidx.compose.desktop.AppManager
import androidx.compose.desktop.Window
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import data.ANRClassifier
import data.DataSource
import data.LoginSession
import data.ScrappingSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import presentation.MainFlowViewModel

fun main() = Window(title = "ANR Scrapper", size = IntSize(300,300), location = IntOffset(0, 0), centered = false) {

    val scope = CoroutineScope(Dispatchers.Main)

    val login = LoginSession()

    val dataSource = DataSource()

    val scrapping = ScrappingSession(dataSource)

    val classifier = ANRClassifier(dataSource)

    val viewModel = MainFlowViewModel(scope, login, scrapping, classifier, dataSource)

    AppManager.setEvents(onAppExit = {
        login.closeDriver()
        scrapping.closeAll()
    })

    MainFlow(viewModel)
}