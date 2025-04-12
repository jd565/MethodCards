package com.jpd.methodcards.presentation.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiMethodTopBar(
    description: String?,
    explainerClicked: () -> Unit,
    settingsClicked: () -> Unit,
    navigateToMultiMethodSelection: () -> Unit,
    navigationIcon: @Composable () -> Unit,
    enabled: Boolean = true,
) {
    TopAppBar(
        navigationIcon = navigationIcon,
        title = {
            if (description != null) {
                Row(
                    modifier = Modifier.heightIn(48.dp)
                        .clickable(onClick = navigateToMultiMethodSelection, enabled = enabled),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Text(description)
                    if (enabled) {
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.AutoMirrored.Default.KeyboardArrowRight, contentDescription = "Method Selection")
                    }
                }
            }
        },
        actions = {
            IconButton(onClick = explainerClicked) {
                Icon(Icons.Outlined.Info, contentDescription = "Explainer")
            }
            IconButton(onClick = settingsClicked) {
                Icon(Icons.Default.Settings, contentDescription = "Settings")
            }
        }
    )
}
