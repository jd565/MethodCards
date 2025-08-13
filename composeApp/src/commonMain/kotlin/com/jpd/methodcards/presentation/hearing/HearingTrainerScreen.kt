package com.jpd.methodcards.presentation.hearing

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jpd.methodcards.domain.toBellChar

@Composable
fun HearingTrainerScreen(
    modifier: Modifier = Modifier,
) {
    StrikingGuess(modifier = modifier)
    // Column(modifier) {
    //     val pagerState = rememberPagerState { 2 }
    //     TabRow(
    //         selectedTabIndex = pagerState.currentPage,
    //         modifier = Modifier.fillMaxWidth(),
    //         containerColor = Color.Transparent,
    //         tabs = {
    //             val scope = rememberCoroutineScope()
    //             Tab(
    //                 selected = pagerState.currentPage == 0,
    //                 onClick = { scope.launch { pagerState.animateScrollToPage(0) } },
    //                 text = { Text("Striking") },
    //             )
    //             Tab(
    //                 selected = pagerState.currentPage == 1,
    //                 onClick = { scope.launch { pagerState.animateScrollToPage(1) } },
    //                 text = { Text("Bell position") },
    //             )
    //         },
    //     )
    //     HorizontalPager(
    //         pagerState,
    //         modifier = Modifier.fillMaxWidth().weight(1f),
    //         userScrollEnabled = false,
    //     ) { page ->
    //         if (page == 0) {
    //             StrikingGuess(modifier = Modifier.fillMaxSize())
    //         } else {
    //             BellPositionGuess(modifier = Modifier.fillMaxSize())
    //         }
    //     }
    // }
}

@Composable
private fun StrikingGuess(
    modifier: Modifier = Modifier,
) {
    val viewModel: HearingTrainerViewModel = viewModel(factory = HearingTrainerViewModel.Factory)
    val uiState by viewModel.uiState.collectAsState()
    Column(
        modifier = modifier.fillMaxSize().padding(vertical = 16.dp, horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        IconButton(onClick = { viewModel.playPause() }) {
            Icon(
                imageVector = if (uiState.isPlaying) Icons.Default.Close else Icons.Default.PlayArrow,
                contentDescription = "Play/Pause",
            )
        }

        Spacer(modifier = Modifier.padding(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            repeat(uiState.stage) { idx ->
                val backgroundColor =
                    if (uiState.selectedBell == idx + 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                val borderColor =
                    if (uiState.selectedBell == idx + 1) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(48.dp)
                        .border(BorderStroke(1.dp, borderColor), MaterialTheme.shapes.medium)
                        .background(backgroundColor, shape = MaterialTheme.shapes.medium)
                        .clip(MaterialTheme.shapes.medium)
                        .clickable { viewModel.selectBell(idx + 1) },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = (idx + 1).toBellChar(),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.padding(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(
                "Fast" to { viewModel.selectFast() },
                "Slow" to { viewModel.selectSlow() },
            ).forEach { (text, onClick) ->
                val isFast = text == "Fast"
                val backgroundColor =
                    if (uiState.isBellFast == isFast) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                val borderColor =
                    if (uiState.isBellFast == isFast) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(48.dp)
                        .border(BorderStroke(1.dp, borderColor), MaterialTheme.shapes.medium)
                        .background(backgroundColor, shape = MaterialTheme.shapes.medium)
                        .clip(MaterialTheme.shapes.medium)
                        .clickable(onClick = onClick),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text = text)
                }
            }
        }

        Button(onClick = { viewModel.submitGuess() }) {
            Text(text = "Submit")
        }

        if (uiState.showFeedbackDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.dismissFeedback() },
                title = { Text(text = "Feedback") },
                text = { Text(text = uiState.feedbackMessage) },
                confirmButton = {
                    Button(onClick = { viewModel.dismissFeedback() }) {
                        Text(text = "OK")
                    }
                },
            )
        }
    }
}

@Composable
private fun BellPositionGuess(modifier: Modifier = Modifier) {
    val viewModel: HearingTrainerViewModel = viewModel(
        factory = HearingTrainerViewModel.PositionFactory,
        key = "Position",
    )
    val uiState by viewModel.uiState.collectAsState()
    Column(
        modifier = modifier.fillMaxSize().padding(vertical = 16.dp, horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        IconButton(onClick = { viewModel.playPause() }) {
            Icon(
                imageVector = if (uiState.isPlaying) Icons.Default.Close else Icons.Default.PlayArrow,
                contentDescription = "Play/Pause",
            )
        }

        Spacer(modifier = Modifier.padding(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            repeat(uiState.stage) { idx ->
                val backgroundColor =
                    if (uiState.selectedBell == idx + 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                val borderColor =
                    if (uiState.selectedBell == idx + 1) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(48.dp)
                        .border(BorderStroke(1.dp, borderColor), MaterialTheme.shapes.medium)
                        .background(backgroundColor, shape = MaterialTheme.shapes.medium)
                        .clip(MaterialTheme.shapes.medium)
                        .clickable { viewModel.selectBell(idx + 1) },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = (idx + 1).toBellChar(),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.padding(8.dp))

        Button(onClick = { viewModel.submitGuess() }) {
            Text(text = "Submit")
        }

        if (uiState.showFeedbackDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.dismissFeedback() },
                title = { Text(text = "Feedback") },
                text = { Text(text = uiState.feedbackMessage) },
                confirmButton = {
                    Button(onClick = { viewModel.dismissFeedback() }) {
                        Text(text = "OK")
                    }
                },
            )
        }
    }
}
