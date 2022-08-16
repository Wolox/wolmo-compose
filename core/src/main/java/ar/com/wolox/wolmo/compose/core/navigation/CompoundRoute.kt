package ar.com.wolox.wolmo.compose.core.navigation

import androidx.navigation.NavGraphBuilder

data class CompoundRoute(
    val route: String,
    val startDestination: String? = null,
    val internalRoutes: List<Route>? = null
) : Route {

    override fun createNavigation(navGraphBuilder: NavGraphBuilder) {
        navGraphBuilder.compoundNavigation(this)
    }
}