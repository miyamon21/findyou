package com.example.findyou.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.findyou.CommonDivider
import com.example.findyou.CommonImage
import com.example.findyou.FindYouViewModel
import com.example.findyou.data.Messages

@Composable
fun SingleChatScreen(
    navController: NavController, viewModel: FindYouViewModel, chatId: String
) {
    LaunchedEffect(key1 = Unit) {
        viewModel.populateChat(chatId)
    }
    BackHandler {
        viewModel.depopulateChat()
    }

    var reply by rememberSaveable() { mutableStateOf("") }
    val currentChat = viewModel.chats.value.first { it.userId == chatId }
    val myId = viewModel.userData.value
    val chatUser =
        if (myId?.userId == currentChat.user1.userId) currentChat.user2 else currentChat.user1
    val onSendReply = {
        viewModel.onSendReply(chatId, reply)
        reply = ""
    }
    val chatMessage = viewModel.chatMessages

    Column(modifier = Modifier.fillMaxSize()) {
        //chat header
        ChatHeader(name = chatUser.name ?: "", imageUrl = chatUser.imageUrl ?: "") {
            navController.popBackStack()
            viewModel.depopulateChat()
        }
        //chat message
        ChatMessages(
            modifier = Modifier.weight(1f),
            chatMessages = chatMessage.value,
            currentUserId = myId?.userId ?: ""
        )

        //reply box
        ChatReplyBox(reply = reply, onReplyChange = {reply = it},onSendReply)
    }
}

@Composable
fun ChatHeader(
    name: String, imageUrl: String, onBackClicked: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.AutoMirrored.Rounded.ArrowBack,
            contentDescription = null,
            modifier = Modifier
                .clickable {
                    onBackClicked.invoke()
                }
                .padding(8.dp))
        CommonImage(
            data = imageUrl, modifier = Modifier
                .padding(8.dp)
                .size(50.dp)
                .clip(CircleShape)
        )
        Text(text = name, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 4.dp))
    }
    CommonDivider()
}

@Composable
fun ChatMessages(modifier: Modifier, chatMessages: List<Messages>, currentUserId: String) {
    LazyColumn(modifier = modifier) {
        items(chatMessages) { chatMsg ->
            chatMsg.messages?.let {
                val alignment =
                    if (chatMsg.sendBy == currentUserId) Alignment.End else Alignment.Start
                val color = if (chatMsg.sendBy == currentUserId) Color(0xFF68C400)
                else Color(0xFFc0c0c0)


                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalAlignment = alignment
                ) {
                    Text(
                        text = chatMsg.messages,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(color)
                            .padding(12.dp),
                        color= Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun ChatReplyBox(
    reply: String, onReplyChange: (String) -> Unit, onSendReply: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        CommonDivider()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextField(value = reply, onValueChange = onReplyChange, maxLines = 3)
            Button(onClick = { onSendReply() }) {
                Text(text = "Send")
            }
        }
    }
}