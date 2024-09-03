package com.example.findyou

import androidx.navigation.NavController
import okhttp3.Route

fun navigateTo(navController: NavController,route: String){
    navController.navigate(route){
        popUpTo(route)
        launchSingleTop = true
    }
}