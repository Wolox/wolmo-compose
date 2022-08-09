package ar.com.wolox.wolmo.compose.core.navigation

import androidx.navigation.NavGraphBuilder

interface Route {
    fun createNavigation(navGraphBuilder: NavGraphBuilder)
}
