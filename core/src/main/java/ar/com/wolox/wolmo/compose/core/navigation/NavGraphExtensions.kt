package ar.com.wolox.wolmo.compose.core.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.navigation

fun NavGraphBuilder.compoundNavigation(compoundRoute: CompoundRoute) {
    compoundRoute.startDestination?.let {
        this.navigation(
            startDestination = compoundRoute.startDestination,
            route = compoundRoute.route
        ) {
            compoundRoute.internalRoutes?.forEach {
                it.createNavigation(this)
            }
        }
    }
}
