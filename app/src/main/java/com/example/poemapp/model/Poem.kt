package com.example.poemapp.model

data class Poem(
    val id: String,
    val title: String,
    val author: String,
    val dynasty: String,
    val content: String,
    val famousLine: String
)
