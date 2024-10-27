package com.example.simplewallet

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatTimestamp(timestamp: Long): String {
    val date = Date(timestamp)
    val formatter = SimpleDateFormat("MMM d 'at' h:mm a", Locale.getDefault())
    return formatter.format(date)
}