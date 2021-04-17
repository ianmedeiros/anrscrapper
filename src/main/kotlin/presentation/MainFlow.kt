import androidx.compose.runtime.Composable
import presentation.MainFlowState
import presentation.MainFlowViewModel
import presentation.ui.ScrapScreen
import utils.exhaustive

@Composable
fun MainFlow(viewModel: MainFlowViewModel) {
    when(val state = viewModel.state) {
        is MainFlowState.WaitingLogIn -> LogInScreen()
        is MainFlowState.LogInFailed -> LoginFailedScreen(onLoginClick = viewModel::onLogInClick)
        is MainFlowState.LoggedIn -> ScrapScreen(state, viewModel::onScrapClick, viewModel::onClassifyClick)
    }.exhaustive
}