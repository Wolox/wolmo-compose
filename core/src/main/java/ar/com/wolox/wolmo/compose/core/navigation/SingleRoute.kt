package ar.com.wolox.wolmo.compose.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

class SingleRoute(
    private val route: String,
    private val destination: @Composable () -> Unit
) : Route {

    override fun createNavigation(navGraphBuilder: NavGraphBuilder) {
        navGraphBuilder.composable(route) {
            destination.invoke()
        }
    }
}
