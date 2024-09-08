package com.example.findyou

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.findyou.ui.ChatListScreen
import com.example.findyou.ui.LoginScreen
import com.example.findyou.ui.ProfileScreen
import com.example.findyou.ui.SignupScreen
import com.example.findyou.ui.SingleChatScreen
import com.example.findyou.ui.SwipeScreen
import com.example.findyou.ui.theme.FindYouTheme
import dagger.hilt.android.AndroidEntryPoint


sealed class DestinationScreen(val route : String){
    object Signup : DestinationScreen("signup")
    object Login : DestinationScreen("login")
    object Profile : DestinationScreen("profile")
    object Swipe : DestinationScreen("swipe")
    object ChatList : DestinationScreen("chatList")
    object SingleChat : DestinationScreen("singleChat/{chatId}"){
        fun crateRoute(id : String) = "singleChat/$id"
    }

}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FindYouTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SwipeAppNavigation()
                }
            }
        }
    }
}

@Composable
fun SwipeAppNavigation(){
    val navController = rememberNavController()
    val viewModel = hiltViewModel<FindYouViewModel>()
    
    NotificationMessage(findYouViewModel = viewModel)

    NavHost(navController = navController, startDestination = DestinationScreen.Signup.route){
        composable(DestinationScreen.Signup.route){
            SignupScreen(navController,viewModel)
        }
        composable(DestinationScreen.Login.route){
            LoginScreen(navController,viewModel)
        }
        composable(DestinationScreen.Swipe.route){
            SwipeScreen(navController, viewModel)
        }
        composable(DestinationScreen.Profile.route){
            ProfileScreen(navController,viewModel)
        }
        composable(DestinationScreen.ChatList.route){
            ChatListScreen(navController,viewModel)
        }
        composable(DestinationScreen.SingleChat.route){
            SingleChatScreen(chatId = "123")
        }
    }
}