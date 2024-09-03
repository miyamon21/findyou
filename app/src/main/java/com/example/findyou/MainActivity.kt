package com.example.findyou

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.findyou.ui.ChatListScreen
import com.example.findyou.ui.LoginScreen
import com.example.findyou.ui.ProfileScreen
import com.example.findyou.ui.SignupScreen
import com.example.findyou.ui.SingleChatScreen
import com.example.findyou.ui.SwipeCards
import com.example.findyou.ui.theme.FindYouTheme


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
    NavHost(navController = navController, startDestination = DestinationScreen.Swipe.route){
        composable(DestinationScreen.Signup.route){
            SignupScreen()
        }
        composable(DestinationScreen.Login.route){
            LoginScreen()
        }
        composable(DestinationScreen.Swipe.route){
            SwipeCards(navController)
        }
        composable(DestinationScreen.Profile.route){
            ProfileScreen(navController)
        }
        composable(DestinationScreen.ChatList.route){
            ChatListScreen(navController)
        }
        composable(DestinationScreen.SingleChat.route){
            SingleChatScreen(chatId = "123")
        }
    }
}

//@Preview(showSystemUi = true)
//@Composable
//fun PreviewSwipeCards(){
//    FindYouTheme {
//        SwipeCards()
//    }
//}