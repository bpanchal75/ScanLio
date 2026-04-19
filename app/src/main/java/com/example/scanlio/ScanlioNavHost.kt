package com.example.scanlio

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

private const val ROUTE_SPLASH = "splash"
private const val ROUTE_HOME = "home"
private const val ROUTE_SETTINGS = "settings"
private const val ROUTE_SCAN = "scan"
private const val ROUTE_RESULT = "result"

@Composable
fun ScanlioNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    scanResultViewModel: ScanResultViewModel = viewModel(),
) {
    NavHost(
        navController = navController,
        startDestination = ROUTE_SPLASH,
        modifier = modifier,
    ) {
        composable(ROUTE_SPLASH) {
            SplashScreen(
                onFinished = {
                    navController.navigate(ROUTE_HOME) {
                        popUpTo(ROUTE_SPLASH) { inclusive = true }
                    }
                },
            )
        }
        composable(ROUTE_HOME) {
            HomeScreen(
                onScanQr = { navController.navigate("$ROUTE_SCAN/${ScanMode.Qr.toRouteSegment()}") },
                onScanBarcode = { navController.navigate("$ROUTE_SCAN/${ScanMode.Barcode.toRouteSegment()}") },
                onOpenSettings = { navController.navigate(ROUTE_SETTINGS) },
            )
        }
        composable(ROUTE_SETTINGS) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
        composable(
            route = "$ROUTE_SCAN/{mode}",
            arguments = listOf(
                navArgument("mode") { type = NavType.StringType },
            ),
        ) { entry ->
            val mode = (entry.arguments?.getString("mode") ?: "qr").toScanModeOrQr()
            ScannerScreen(
                scanMode = mode,
                onScanComplete = { payload, format ->
                    scanResultViewModel.setResult(
                        payload = payload,
                        formatLabel = format,
                        richPayloadActions = mode == ScanMode.Qr,
                    )
                    navController.navigate(ROUTE_RESULT)
                },
                onBack = { navController.popBackStack() },
            )
        }
        composable(ROUTE_RESULT) {
            ScanResultScreen(
                viewModel = scanResultViewModel,
                onBack = { navController.popBackStack() },
                onScanAgainToHome = {
                    navController.popBackStack(ROUTE_HOME, inclusive = false)
                },
            )
        }
    }
}
