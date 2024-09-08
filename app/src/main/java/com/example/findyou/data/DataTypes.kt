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