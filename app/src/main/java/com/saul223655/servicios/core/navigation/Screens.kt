package com.saul223655.servicios.core.navigation

sealed class Screen(val route: String) {
    object Music : Screen("music_screen")
    object StoragePermission : Screen("storage_permission_screen")
}
