package com.jpd.methodcards.presentation

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.jpd.methodcards.presentation.blueline.BlueLineScreen
import com.jpd.methodcards.presentation.composer.CompositionScreen
import com.jpd.methodcards.presentation.flashcard.FlashCardScreen
import com.jpd.methodcards.presentation.icons.Blueline
import com.jpd.methodcards.presentation.icons.Flashcard
import com.jpd.methodcards.presentation.icons.Simulator
import com.jpd.methodcards.presentation.settings.AddMethodScreen
import com.jpd.methodcards.presentation.settings.SettingsScreen
import com.jpd.methodcards.presentation.simulator.SimulatorScreen
import com.jpd.methodcards.presentation.simulator.SimulatorSettingsSheet
import com.jpd.methodcards.presentation.ui.MultiMethodSelectionScreen
import kotlinx.serialization.Serializable
import org.jetbrains.compose.ui.tooling.preview.Preview

enum class MethodCardScreen {
    BlueLine,
    FlashCard,
    Simulator,
    Settings,
    MultiMethodSelection,
    AddMethod,
    Composition,
}

@Serializable
data class MethodName(val name: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun App() {
    MethodCardTheme {
        val navController = rememberNavController()

        var showBottomSheet by rememberSaveable { mutableStateOf(false) }
        val bottomSheetState = rememberModalBottomSheetState(
            skipPartiallyExpanded = true,
        )
        val bottomSheetRoute = remember { mutableStateOf("") }
        val navigateToAppSettings: () -> Unit = { navController.navigateTo(MethodCardScreen.Settings) }
        val navigateToMultiMethodSelection: () -> Unit =
            { navController.navigate(MethodCardScreen.MultiMethodSelection.name) }
        val navigateToAddMethod: () -> Unit =
            { navController.navigate(MethodCardScreen.AddMethod.name) }
        val navigateToBlueline: (String) -> Unit = {
            navController.navigate(route = MethodName(it))
        }

            Scaffold(
                bottomBar = {
                    MethodCardBottomBar(navController)
                },
            ) { innerPadding ->
                NavHost(
                    navController = navController,
                    startDestination = MethodCardScreen.BlueLine.name,
                    modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                ) {
                    composable(route = MethodCardScreen.FlashCard.name) {
                        FlashCardScreen(
                            modifier = Modifier.fillMaxSize(),
                            navigateToAppSettings = navigateToAppSettings,
                            navigateToMultiMethodSelection = navigateToMultiMethodSelection,
                        )
                    }
                    composable(route = MethodCardScreen.Simulator.name) {
                        SimulatorScreen(
                            modifier = Modifier.fillMaxSize(),
                            {
                                bottomSheetRoute.value = MethodCardScreen.Simulator.name
                                showBottomSheet = true
                            },
                            navigateToAppSettings = navigateToAppSettings,
                            navigateToMultiMethodSelection = navigateToMultiMethodSelection,
                        )
                    }
                    composable(route = MethodCardScreen.BlueLine.name) {
                        BlueLineScreen(
                            modifier = Modifier.fillMaxHeight(),
                            {},
                            navigateToAppSettings = navigateToAppSettings,
                            navigateBack = { navController.popBackStack() },
                        )
                    }
                    composable<MethodName> { entry ->
                        val name = entry.toRoute<MethodName>()
                        BlueLineScreen(
                            modifier = Modifier.fillMaxHeight(),
                            { navigateToBlueline(name.name) },
                            navigateToAppSettings = navigateToAppSettings,
                            navigateBack = { navController.popBackStack() },
                            method = name.name,
                        )
                    }
                    composable(route = MethodCardScreen.Settings.name) {
                        SettingsScreen(
                            modifier = Modifier.fillMaxSize(),
                            navigateToAddMethod = navigateToAddMethod,
                            navigateToBlueline = navigateToBlueline,
                        )
                    }
                    composable(route = MethodCardScreen.MultiMethodSelection.name) {
                        MultiMethodSelectionScreen(
                            modifier = Modifier.fillMaxSize(),
                            onBack = { navController.popBackStack() },
                        )
                    }
                    composable(route = MethodCardScreen.AddMethod.name) {
                        AddMethodScreen(
                            modifier = Modifier.fillMaxSize(),
                            onBack = { navController.popBackStack() },
                        )
                    }
                    composable(route = MethodCardScreen.Composition.name) {
                        CompositionScreen(modifier = Modifier.fillMaxSize())
                    }
                }
            }

            if (showBottomSheet) {
                ModalBottomSheet(
                    sheetState = bottomSheetState,
                    onDismissRequest = {
                        showBottomSheet = false
                    },
                ) {
                    when (bottomSheetRoute.value) {
                        MethodCardScreen.Simulator.name -> {
                            SimulatorSettingsSheet()
                        }
                    }
                    // MethodCardBottomSheet()
                }
            }
    }
}

@Composable
private fun MethodCardBottomBar(navController: NavController, modifier: Modifier = Modifier) {
    NavigationBar(modifier = modifier) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination
        listOf(
            MethodCardScreen.BlueLine to Icons.Filled.Blueline,
            MethodCardScreen.FlashCard to Icons.Filled.Flashcard,
            MethodCardScreen.Simulator to Icons.Filled.Simulator,
            MethodCardScreen.Composition to Icons.Filled.Build,
            MethodCardScreen.Settings to Icons.Filled.Settings,
        ).forEach { (screen, icon) ->
            NavigationBarItem(
                icon = { Icon(icon, contentDescription = null) },
                label = { Text(screen.name) },
                selected = currentDestination?.hierarchy?.any { it.route == screen.name } == true,
                onClick = {
                    navController.navigateTo(screen)
                },
            )
        }
    }
}

private fun NavController.navigateTo(screen: MethodCardScreen) {
    navigate(screen.name) {
        // Pop up to the start destination of the graph to
        // avoid building up a large stack of destinations
        // on the back stack as users select items
        popUpTo(
            currentDestination?.route.orEmpty(),
        ) {
            inclusive = true
            saveState = true
        }
        // Avoid multiple copies of the same destination when
        // reselecting the same item
        launchSingleTop = true
        // Restore state when reselecting a previously selected item
        restoreState = true
    }
}

@Composable
private fun MethodCardTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(
//        colors = if (darkTheme) darkMethodCardColors() else lightMethodCardColors(),
        colorScheme = lightMethodCardColors(),
        content = content,
    )
}

private fun lightMethodCardColors(): ColorScheme = lightColorScheme(
    surface = Color(0xFFE0E0E0),
)

private fun darkMethodCardColors(): ColorScheme = darkColorScheme(
    surface = Color(0xFF212121),
)

internal val LocalKeyEvents = staticCompositionLocalOf { mutableListOf<(KeyDirection) -> Boolean>() }

enum class KeyDirection {
    Left, Down, Right
}

