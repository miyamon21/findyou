package com.example.findyou.data

data class UserData(
    var userId : String? = "",
    var name : String? = "",
    var userName : String? = "",
    var imageUrl : String? = "",
    var bio : String? = "",
    var gender : String? = "",
    var genderPreference : String? = ""
){
    fun toMap()= mapOf(
        "userId" to userId,
        "name" to name,
        "username" to userName,
        "imageUrl" to imageUrl,
        "bio" to bio,
        "gender" to gender,
        "genderPreference" to genderPreference
    )
}