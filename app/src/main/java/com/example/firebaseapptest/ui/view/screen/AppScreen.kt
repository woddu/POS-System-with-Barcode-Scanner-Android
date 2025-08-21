package com.example.firebaseapptest.ui.view.screen

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.firebaseapptest.R
import com.example.firebaseapptest.ui.theme.FirebaseApptestTheme
import com.example.firebaseapptest.ui.view.AppEvent
import com.example.firebaseapptest.ui.view.AppState

data class BottomNavItems(
    val name: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val route: String
)

sealed class Route(val path: String) {
    object Home : Route("home")
    object Inventory : Route("inventory")
    object Sale : Route("sale")
    object Scanner : Route("scanner")
}

@Composable
fun AppScreen(
    state: AppState,
    onEvent: (AppEvent) -> Unit
) {
    val navItems = listOf(
        BottomNavItems(
            "Home",
            Icons.Filled.Home,
            Icons.Outlined.Home,
            Route.Home.path
        ),
        BottomNavItems(
            "Sale",
            Icons.Filled.ShoppingCart,
            Icons.Outlined.ShoppingCart,
            Route.Sale.path
        ),
        BottomNavItems(
            "Inventory",
            ImageVector.vectorResource(R.drawable.inventory_icon),
            ImageVector.vectorResource(R.drawable.inventory_icon),
            Route.Inventory.path
        ),
    )
    val navController = rememberNavController()
    FirebaseApptestTheme {
        val selectedItemIndex = rememberSaveable {
            mutableIntStateOf(0)
        }
        val navBackStackEntry = navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry.value?.destination?.route
        Scaffold(
            bottomBar = {
                NavigationBar {
                    navItems.forEachIndexed { index, item ->
                        NavigationBarItem(

                            selected = selectedItemIndex.intValue == index,
                            icon = {
                                Icon(
                                    imageVector = if (selectedItemIndex.intValue == index) {
                                        item.selectedIcon
                                    } else item.unselectedIcon,
                                    contentDescription = item.name
                                )
                            },
                            label = {
                                Text(item.name)
                            },
                            onClick = {
                                selectedItemIndex.intValue = index
                                navController.navigate(item.route)
                            }
                        )
                    }
                }
            },
            floatingActionButton = {
                when (currentRoute) {
                    Route.Home.path -> {
                        FloatingActionButton(onClick = { /* action for Home */ }) {
                            Icon(Icons.Default.Add, contentDescription = "Add")
                        }
                    }
                    Route.Inventory.path -> {
                        FloatingActionButton(onClick = { onEvent(AppEvent.OnInventoryAddButtonClicked) }) {
                            Icon(Icons.Default.Add, contentDescription = "Add")
                        }
                    }
                    // If route not matched, show nothing
                    else -> {}
                }
            },
            floatingActionButtonPosition = FabPosition.End
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Route.Home.path,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(Route.Home.path) {
                    Home(state.scannedText, state.navigateToScanner, onEvent){
                        navController.navigate(Route.Scanner.path)
                    }
                }

                composable(Route.Sale.path) {
                    Text("Sale")
                }

                composable(Route.Inventory.path) {
                    Text("Inventory")
                }

                composable(Route.Scanner.path) { backStackEntry ->
                    Scanner(backStackEntry, onEvent){
                        navController.navigate(Route.Home.path)
                    }
                }

            }
        }
    }
}

