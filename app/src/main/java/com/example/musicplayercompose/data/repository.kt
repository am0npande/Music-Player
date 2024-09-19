package com.example.musicplayercompose.data

import androidx.coordinatorlayout.widget.CoordinatorLayout.DispatchChangeEvent
import com.example.musicplayercompose.data.local.ContentResolverHelpher
import com.example.musicplayercompose.data.local.model.Audio
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class repository @Inject constructor(
    private val contentResolver:ContentResolverHelpher
){
    suspend fun getAudioDataa():List<Audio> =
        // '='  function return last item directly ..., like getAudioData() list<Audio>
        //making a inline function with "="
        withContext(Dispatchers.IO){
            contentResolver.getAudioData()
        }

}