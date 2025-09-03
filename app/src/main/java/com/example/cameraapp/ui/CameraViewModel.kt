package com.example.cameraapp.ui

import android.app.Application
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceRequest
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class CameraViewModel(
    private val application: Application
) : AndroidViewModel(application) {

    private val _surfaceRequest = MutableStateFlow<SurfaceRequest?>(null)
    val surfaceRequest = _surfaceRequest.asStateFlow()

    private val cameraPreviewUsecase = Preview.Builder().build().apply {
        setSurfaceProvider { newSurfaceRequest ->
            _surfaceRequest.update { newSurfaceRequest }
        }
    }

    suspend fun bind(lifecycleOwner: LifecycleOwner) {
        val processCameraProvider = ProcessCameraProvider.awaitInstance(application)
        processCameraProvider.bindToLifecycle(
            lifecycleOwner,
            CameraSelector.DEFAULT_BACK_CAMERA,
            cameraPreviewUsecase
        )

        try {
            awaitCancellation()
        } finally {
            processCameraProvider.unbindAll()
        }
    }
}