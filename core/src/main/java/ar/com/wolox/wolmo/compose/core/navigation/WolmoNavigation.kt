package ar.com.wolox.wolmo.compose.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost

@Composable
fun WolmoNavGraph(
    vararg routes: Route,
    startDestination: String,
    navController: NavHostController
) {
    NavHost(navController = navController, startDestination = startDestination) {
        routes.forEach {
            it.createNavigation(this)
        }
    }
}
