package com.well.androidgnews.data.auth

/** The signed-in person. [id] is the Firebase Auth uid, which also keys their favorites. */
data class User(
    val id: String,
    val name: String?,
    val email: String?,
)
