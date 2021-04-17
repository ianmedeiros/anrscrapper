package presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import presentation.MainFlowState

@Composable
fun ScrapScreen(
    state: MainFlowState.LoggedIn,
    onScrapClick: (String) -> Unit, onClassifyClick: (String) -> Unit
) {
    var scrapTextField by remember { mutableStateOf(state.scrapURL) }
    var classifyTextField by remember { mutableStateOf(state.classifyFilter) }

    MaterialTheme {
        Column(
            modifier = Modifier.fillMaxSize().background(MaterialTheme.colors.background),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {

            Text(state.description)

            OutlinedTextField(
                value = scrapTextField,
                onValueChange = { scrapTextField = it },
                label = { Text("Play console URL") },
                singleLine = true
            )
            Button(
                onClick = { onScrapClick(scrapTextField) },
                enabled = !state.isScrapping,
            ) {
                Text("Scrap")
            }

            OutlinedTextField(
                value = classifyTextField,
                onValueChange = { classifyTextField = it },
                label = { Text("Classification filter ") }
            )
            Button(
                onClick = { onClassifyClick(classifyTextField) },
                enabled = !state.isClassifying
            ) {
                Text("Classify")
            }
        }
    }
}