package presentation

import data.ANRClassifier
import data.ScrappingSession
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import data.DataSource
import data.LoginSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


sealed class MainFlowState {
    object WaitingLogIn : MainFlowState()
    object LogInFailed : MainFlowState()

    class LoggedIn(
        val description: String,
        val scrapURL: String,
        val classifyFilter: String,
        val isScrapping: Boolean,
        val isClassifying: Boolean,
    ) : MainFlowState()
}

class MainFlowViewModel(
    private val scope: CoroutineScope,
    private val loginSession: LoginSession,
    private val scrapSession: ScrappingSession,
    private val classifier: ANRClassifier,
    private val dataSource: DataSource
) {
    var state by mutableStateOf<MainFlowState>(MainFlowState.WaitingLogIn)
        private set

    var isScrapping = false
    var isClassifying = false

    init {
        scope.launch {
            if (loginSession.isLoggedIn()) {
                emmitLoggedInState("Log in successful!")
            } else {
                login()
            }
        }
    }

    fun onLogInClick() = scope.launch {
        login()
    }

    fun onScrapClick(url: String) = scope.launch {
        emmitLoggedInState("Started scrapping", isScrapping = true)
        dataSource.saveUrl(url)
        scrapSession.startScrapping()
        emmitLoggedInState("Finished scrapping", isScrapping = false)
    }

    fun onClassifyClick(filter: String) = scope.launch {
        emmitLoggedInState("Started classify", isClassifying = true)
        dataSource.saveFilter(filter)
        classifier.classify()
        emmitLoggedInState("Finished classify", isClassifying = false)
    }

    private suspend fun login() {
        state = MainFlowState.WaitingLogIn
        if (loginSession.logIn()) {
            emmitLoggedInState("Log in successful!")
        } else {
            state = MainFlowState.LogInFailed
        }
    }

    private suspend fun emmitLoggedInState(description: String, isScrapping: Boolean? = null, isClassifying: Boolean? = null) {
        this.isScrapping = isScrapping ?: this.isScrapping
        this.isClassifying = isClassifying ?: this.isClassifying
        state = MainFlowState.LoggedIn(
            description,
            dataSource.getURL(),
            dataSource.getFilter(),
            this.isScrapping,
            this.isClassifying
        )
    }
}