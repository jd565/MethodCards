package com.jpd.methodcards.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.PermanentDrawerSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.window.core.layout.WindowWidthSizeClass
import com.jpd.methodcards.presentation.blueline.BlueLineMethodListScreen
import com.jpd.methodcards.presentation.blueline.BlueLineScreen
import com.jpd.methodcards.presentation.blueline.BlueLineTopBar
import com.jpd.methodcards.presentation.blueline.DarkBlueLineColors
import com.jpd.methodcards.presentation.blueline.LightBlueLineColors
import com.jpd.methodcards.presentation.blueline.blueLineColors
import com.jpd.methodcards.presentation.composer.CompositionScreen
import com.jpd.methodcards.presentation.flashcard.FlashCardScreen
import com.jpd.methodcards.presentation.flashcard.FlashCardTopBar
import com.jpd.methodcards.presentation.hearing.HearingTrainerScreen
import com.jpd.methodcards.presentation.listener.AudioFFTScreen
import com.jpd.methodcards.presentation.methodbuilder.MethodBuilderScreen
import com.jpd.methodcards.presentation.overunder.OverUnderScreen
import com.jpd.methodcards.presentation.overunder.OverUnderTopBar
import com.jpd.methodcards.presentation.settings.AddMethodScreen
import com.jpd.methodcards.presentation.settings.SettingsScreen
import com.jpd.methodcards.presentation.settings.SettingsTopBar
import com.jpd.methodcards.presentation.simulator.SimulatorScreen
import com.jpd.methodcards.presentation.simulator.SimulatorSettingsSheet
import com.jpd.methodcards.presentation.simulator.SimulatorTopBar
import com.jpd.methodcards.presentation.ui.MultiMethodSelectionScreen
import com.jpd.methodcards.presentation.ui.MultiMethodSelectionTopBar
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun App(navController: NavHostController = rememberNavController()) {
    MethodCardTheme {
        var showBottomSheet by rememberSaveable { mutableStateOf(false) }
        val bottomSheetState = rememberModalBottomSheetState(
            skipPartiallyExpanded = true,
        )
        val bottomSheetRoute = remember { mutableStateOf<MethodCardScreen?>(null) }
        val navigateToAppSettings: () -> Unit = { navController.navigateTo(MethodCardScreen.Settings) }
        val navigateToMultiMethodSelection: () -> Unit =
            { navController.navigate(MethodCardScreen.MultiMethodSelection) }
        val navigateToAddMethod: () -> Unit =
            { navController.navigate(MethodCardScreen.AddMethod) }
        val navigateToBlueline: (MethodCardScreen.SingleMethodBlueLine) -> Unit = {
            navController.navigate(route = it)
        }

        val drawerState = methodCardDrawerState()
        val scope = rememberCoroutineScope()

        MethodCardDrawer(navController, drawerState) {
            Scaffold(
                topBar = {
                    val navBackStackEntry = navController.currentBackStackEntryAsState().value
                    val d = navBackStackEntry?.destination
                    val navigationIcon: @Composable () -> Unit = {
                        val bse = navController.currentBackStackEntryAsState()
                        val canGoBack by remember(bse) {
                            derivedStateOf {
                                val destination = bse.value?.destination
                                val isTopLevel = MethodCardScreen.TopLevel.entries.any {
                                    destination?.hasRoute(it::class) == true
                                }
                                destination != null && !isTopLevel
                            }
                        }
                        IconButton(
                            onClick = {
                                if (canGoBack) {
                                    navController.popBackStack()
                                } else {
                                    scope.launch {
                                        if (drawerState.isClosed) {
                                            drawerState.open()
                                        } else {
                                            drawerState.close()
                                        }
                                    }
                                }
                            },
                        ) {
                            if (canGoBack) {
                                Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Back")
                            } else {
                                Icon(Icons.Default.Menu, contentDescription = "Menu")
                            }
                        }
                    }
                    if (d != null) {
                        when {
                            d.hasRoute<MethodCardScreen.SingleMethodBlueLine>() ||
                                d.hasRoute<MethodCardScreen.BlueLine>() -> {
                                BlueLineTopBar(
                                    navBackStackEntry,
                                    { navController.navigate(MethodCardScreen.BlueLineMethodList) },
                                    navigationIcon,
                                    { navController.navigate(it) },
                                )
                            }

                            d.hasRoute<MethodCardScreen.FlashCard>() -> {
                                FlashCardTopBar(
                                    navBackStackEntry,
                                    navigateToMultiMethodSelection,
                                    navigationIcon,
                                )
                            }

                            d.hasRoute<MethodCardScreen.OverUnder>() -> {
                                OverUnderTopBar(navBackStackEntry, navigationIcon)
                            }

                            d.hasRoute<MethodCardScreen.SingleMethodSimulator>() ||
                                d.hasRoute<MethodCardScreen.Simulator>() -> {
                                SimulatorTopBar(
                                    navBackStackEntry,
                                    navigateToMultiMethodSelection,
                                    {
                                        bottomSheetRoute.value = MethodCardScreen.Simulator
                                        showBottomSheet = true
                                    },
                                    navigationIcon,
                                )
                            }

                            d.hasRoute<MethodCardScreen.Settings>() -> {
                                SettingsTopBar(navBackStackEntry, navigateToAddMethod, navigationIcon)
                            }

                            d.hasRoute<MethodCardScreen.MultiMethodSelection>() -> {
                                MultiMethodSelectionTopBar(navBackStackEntry, navigationIcon)
                            }

                            else -> {
                                val screenKlass = MethodCardScreen.entries.firstOrNull {
                                    d.hasRoute(it)
                                }
                                TopAppBar(
                                    title = { Text(screenKlass.title) },
                                    navigationIcon = navigationIcon,
                                )
                            }
                        }
                    }
                },
            ) { innerPadding ->
                NavHost(
                    navController = navController,
                    startDestination = MethodCardScreen.BlueLine,
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                ) {
                    composable<MethodCardScreen.FlashCard> {
                        FlashCardScreen(
                            modifier = Modifier.fillMaxSize(),
                            navigateToAppSettings = navigateToAppSettings,
                        )
                    }
                    composable<MethodCardScreen.OverUnder> {
                        OverUnderScreen(
                            modifier = Modifier.fillMaxSize(),
                            navigateToBlueLine = {
                                navController.navigate(route = it)
                            },
                        )
                    }
                    composable<MethodCardScreen.Simulator> {
                        SimulatorScreen(
                            modifier = Modifier.fillMaxSize(),
                            navigateToAppSettings = navigateToAppSettings,
                        )
                    }
                    composable<MethodCardScreen.SingleMethodSimulator> {
                        SimulatorScreen(
                            modifier = Modifier.fillMaxSize(),
                            navigateToAppSettings = navigateToAppSettings,
                        )
                    }
                    composable<MethodCardScreen.BlueLine> {
                        BlueLineScreen(
                            modifier = Modifier.fillMaxHeight(),
                            navigateToAppSettings = navigateToAppSettings,
                        )
                    }
                    composable<MethodCardScreen.SingleMethodBlueLine> {
                        BlueLineScreen(
                            modifier = Modifier.fillMaxHeight(),
                            navigateToAppSettings = navigateToAppSettings,
                        )
                    }
                    composable<MethodCardScreen.BlueLineMethodList> {
                        BlueLineMethodListScreen(
                            modifier = Modifier.fillMaxSize(),
                            navigateBack = {
                                navController.popBackStack()
                            },
                        )
                    }
                    composable<MethodCardScreen.Settings> {
                        SettingsScreen(
                            modifier = Modifier.fillMaxSize(),
                            navigateToBlueline = navigateToBlueline,
                        )
                    }
                    composable<MethodCardScreen.MultiMethodSelection> {
                        MultiMethodSelectionScreen(
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                    composable<MethodCardScreen.AddMethod> {
                        AddMethodScreen(
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                    composable<MethodCardScreen.Compose> {
                        CompositionScreen(modifier = Modifier.fillMaxSize())
                    }
                    composable<MethodCardScreen.MethodBuilder> {
                        MethodBuilderScreen(modifier = Modifier.fillMaxSize())
                    }
                    composable<MethodCardScreen.RingingListener> {
                        AudioFFTScreen(modifier = Modifier.fillMaxSize())
                    }
                    composable<MethodCardScreen.HearingTrainer> {
                        HearingTrainerScreen(modifier = Modifier.fillMaxSize())
                    }
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
                    MethodCardScreen.Simulator -> {
                        SimulatorSettingsSheet()
                    }

                    else -> Unit
                }
                // MethodCardBottomSheet()
            }
        }
    }
}

@Composable
private fun methodCardDrawerState(): DrawerState {
    val adaptiveInfo = currentWindowAdaptiveInfo()
    val isExpanded = adaptiveInfo.windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.EXPANDED
    val initial = if (isExpanded) {
        DrawerValue.Open
    } else {
        DrawerValue.Closed
    }
    val confirmStateChange: (DrawerValue) -> Boolean = { true }
    return rememberSaveable(isExpanded, saver = DrawerState.Saver(confirmStateChange)) {
        DrawerState(initial, confirmStateChange)
    }
}

@Composable
private fun MethodCardDrawer(
    navController: NavController,
    drawerState: DrawerState,
    content: @Composable () -> Unit,
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val scope = rememberCoroutineScope()
    val adaptiveInfo = currentWindowAdaptiveInfo()
    val isExpanded = adaptiveInfo.windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.EXPANDED

    val drawerContent: @Composable ColumnScope.() -> Unit = {
        Column(modifier = Modifier.padding(horizontal = 16.dp).verticalScroll(rememberScrollState())) {
            Spacer(Modifier.height(12.dp))
            MethodCardScreen.TopLevel.entries.forEach { screen ->
                NavigationDrawerItem(
                    label = { Text(screen::class.title) },
                    selected = currentDestination?.hierarchy?.any {
                        it.hasRoute(screen::class)
                    } == true,
                    onClick = {
                        navController.navigateTo(screen)
                        if (!isExpanded) {
                            scope.launch {
                                drawerState.close()
                            }
                        }
                    },
                    icon = { Icon(screen.icon, contentDescription = null) },
                )

            }
            Spacer(Modifier.height(12.dp))
        }
    }

    if (isExpanded) {
        Row {
            AnimatedVisibility(
                drawerState.isOpen,
                enter = expandHorizontally(),
                exit = shrinkHorizontally(),
            ) {
                PermanentDrawerSheet(content = drawerContent)
            }
            Box(Modifier.weight(1f), content = { content() })
        }
    } else {
        ModalNavigationDrawer(
            drawerContent = {
                ModalDrawerSheet(
                    drawerState = drawerState,
                    content = drawerContent,
                )
            },
            modifier = Modifier.fillMaxSize(),
            drawerState = drawerState,
            content = content,
        )
    }
}

private fun NavController.navigateTo(screen: MethodCardScreen) {
    navigate(screen) {
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
    LaunchedEffect(darkTheme) {
        blueLineColors = if (darkTheme) DarkBlueLineColors else LightBlueLineColors
    }
    MaterialTheme(
        colorScheme = if (darkTheme) darkMethodCardColors() else lightMethodCardColors(),
        content = content,
    )
}

private fun lightMethodCardColors(): ColorScheme = lightColorScheme(
    surface = Color(0xFFE0E0E0),
)

private fun darkMethodCardColors(): ColorScheme = darkColorScheme(
    surface = Color(0xFF212121),
)

internal val LocalKeyEvents = staticCompositionLocalOf {
    mutableListOf<(KeyDirection, KeyEvent) -> Boolean>()
}

sealed class KeyDirection {
    data object Left : KeyDirection()
    data object Down : KeyDirection()
    data object Right : KeyDirection()
    data object Up : KeyDirection()
    data object A : KeyDirection()
    data object S : KeyDirection()
    data object D : KeyDirection()
    data class Bell(val bellChar: String) : KeyDirection()
    data object Undo : KeyDirection()
    data object Delete : KeyDirection()
}

enum class KeyEvent {
    Down, Up
}

