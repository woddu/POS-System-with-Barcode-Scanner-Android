package com.example.firebaseapptest.ui.view.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.firebaseapptest.R
import com.example.firebaseapptest.ui.event.InventoryEvent
import com.example.firebaseapptest.ui.state.InventoryState
import com.example.firebaseapptest.ui.theme.FirebaseApptestTheme
import com.example.firebaseapptest.ui.event.AppEvent
import com.example.firebaseapptest.ui.state.AppState
import com.example.firebaseapptest.ui.view.screen.inventory.ItemDetails
import com.example.firebaseapptest.ui.view.screen.inventory.Inventory
import com.example.firebaseapptest.ui.view.screen.sale.Sale
import com.example.firebaseapptest.ui.view.screen.sale.SaleDetails

data class BottomNavItems(
    val name: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val route: String
)

sealed class Route(val path: String) {
    object Home : Route("home")
    object Inventory : Route("inventory")
    object InventoryDetails : Route("inventoryDetails")
    object Sale : Route("sale")
    object CaptureTransaction : Route("captureTransaction")
    object SaleDetails : Route("saleDetails")
    object Scanner : Route("scanner")
    object Report : Route("report")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScreen(
    state: AppState,
    inventoryState: InventoryState,
    onEvent: (AppEvent) -> Unit,
    onInventoryEvent: (InventoryEvent) -> Unit
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
            "Report",
            Icons.Default.Email,
            Icons.Outlined.Email,
            Route.Report.path
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

        val snackbarHostState = remember { SnackbarHostState() }
        Box {
            Scaffold(
                bottomBar = {
                    NavigationBar(

                    ) {
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
                snackbarHost = {
                    SnackbarHost(
                        hostState = snackbarHostState
                    )
                }
            ) { innerPadding ->
                NavHost(
                    navController = navController,
                    startDestination = Route.Home.path,
                    modifier = Modifier.padding(innerPadding)
                ) {
                    composable(Route.Home.path) {
                        Home(state, onEvent, navController, snackbarHostState)
                    }

                    composable(Route.Sale.path) {
                        Sale(state, onEvent, navController)
                    }

                    composable(Route.SaleDetails.path) {
                        SaleDetails(state)
                    }

                    composable(Route.Inventory.path) {
                        Inventory(
                            state.navigateToScanner,
                            inventoryState,
                            navController,
                            onInventoryEvent,
                        )
                    }

                    composable(Route.InventoryDetails.path) {
                        ItemDetails(inventoryState, onInventoryEvent) {
                            navController.navigate(Route.Inventory.path)
                        }
                    }

                    composable(Route.Scanner.path) { backStackEntry ->
                        Scanner(backStackEntry, onEvent) {
                            navController.navigate(state.navigateBackTo)
                        }
                    }

                    composable(Route.CaptureTransaction.path) {
                        CaptureTransactionAndCrop(state, onEvent, navController)
                    }

                    composable(Route.Report.path) {
                        Report(state, onEvent, snackbarHostState)
                    }
                }
            }

            if (state.isLoading) {
                BasicAlertDialog(onDismissRequest = { /* block dismiss */ }) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            // intercept all clicks
                            .clickable(enabled = true, onClick = { })
                            .background(Color.Black.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}

//fun NavGraphBuilder.authComposable(
//    route: String,
//    isLoggedIn: Boolean,
//    navController: NavController,
//    content: @Composable (NavBackStackEntry) -> Unit
//) {
//    composable(route) { backStackEntry ->
//        if (!isLoggedIn) {
//            LaunchedEffect(Unit) {
//                navController.navigate(Route.Login.path) {
//                    popUpTo(0) { inclusive = true }
//                }
//            }
//        } else {
//            content(backStackEntry)
//        }
//    }
//}