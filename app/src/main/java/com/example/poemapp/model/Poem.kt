package com.example.poemapp.model

data class Poem(
    val id: String,
    val title: String,
    val author: String,
    val dynasty: String,
    val content: String,
    val famousLine: String,
    //下面的只有详情页用得上
    val genre: String = "",
    val format: String = "",
    val collection: String = "",
    val theme: String = "",
    val translate: String = "",
    val notes: String = "",
    val appreciation: String = "",
    val authorBio: String = ""
)
