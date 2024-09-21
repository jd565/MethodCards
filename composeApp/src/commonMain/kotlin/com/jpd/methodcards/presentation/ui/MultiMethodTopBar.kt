package com.jpd.methodcards.presentation.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MultiMethodTopBar(
    description: String?,
    explainerClicked: () -> Unit,
    settingsClicked: () -> Unit,
    navigateToMultiMethodSelection: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().heightIn(56.dp).padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = explainerClicked) {
            Icon(Icons.Outlined.Info, contentDescription = "Explainer")
        }
        if (description != null) {
            Row(
                modifier = Modifier.weight(1f).heightIn(56.dp)
                    .clickable(onClick = navigateToMultiMethodSelection),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Text(description)
                Spacer(Modifier.width(8.dp))
                Icon(Icons.AutoMirrored.Default.KeyboardArrowRight, contentDescription = "Method Selection")
            }
        } else {
            Box(modifier = Modifier.weight(1f))
        }
        IconButton(onClick = settingsClicked) {
            Icon(Icons.Default.Settings, contentDescription = "Settings")
        }
    }
}
