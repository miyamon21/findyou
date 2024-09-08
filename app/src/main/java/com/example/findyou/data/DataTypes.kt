package com.example.findyou.data

data class UserData(
    var userId : String? = "",
    var name : String? = "",
    var username : String? = "",
    var imageUrl : String? = "",
    var bio : String? = "",
    var gender : String? = "",
    var genderPreference : String? = "",
    var swipeLeft : List<String> = listOf(),
    var swipeRight : List<String> = listOf(),
    var matches : List<String> = listOf()
){
    fun toMap()= mapOf(
        "userId" to userId,
        "name" to name,
        "username" to username,
        "imageUrl" to imageUrl,
        "bio" to bio,
        "gender" to gender,
        "genderPreference" to genderPreference,
        "swipeLeft" to swipeLeft,
        "swipeRight" to swipeRight,
        "matches" to matches
    )
}

data class ChatData(
    var userId : String? = null,
    var user1 : ChatUser = ChatUser(),
    var user2 : ChatUser = ChatUser(),
)

data class ChatUser(
    var userId : String? = null,
    var name: String? = null ,
    var imageUrl: String? =null,
)

data class Messages(
    val sendBy : String? = null,
    val messages : String? = null,
    val timeStamp : String? = null
)