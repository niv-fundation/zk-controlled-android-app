package com.example.simplewallet

enum class SelectedNavigationElement {
    Home,
    Send,
    Settings
}

data class Transaction(
    val title: String,
    val amount: String,
    val status: String,
    val date: Long
)