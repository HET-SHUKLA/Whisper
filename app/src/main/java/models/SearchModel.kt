package models

data class SearchModel (
    val userName: String,
    val userImg: String, // Url for the image
    val userUid: String,
    val userKey: String,
    val fcm: String
)