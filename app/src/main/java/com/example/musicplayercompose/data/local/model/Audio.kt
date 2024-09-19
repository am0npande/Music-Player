package com.example.musicplayercompose.data.local.model

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Audio(
    val uri:Uri,
    val name: String,
    val id:Long,
    val artist:String,
    val duration: Long,
    val title:String,
    val data:String,
    val artWork:String
): Parcelable