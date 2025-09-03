@file:OptIn(PublicPreviewAPI::class)

package com.example.cameraapp.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.graphics.Bitmap
import android.util.Size
import androidx.annotation.RequiresPermission
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceRequest
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Content
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.LiveSession
import com.google.firebase.ai.type.PublicPreviewAPI
import com.google.firebase.ai.type.ResponseModality
import com.google.firebase.ai.type.SpeechConfig
import com.google.firebase.ai.type.Voice
import com.google.firebase.ai.type.liveGenerationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@androidx.annotation.OptIn(ExperimentalGetImage::class)
class CameraViewModel
    (
    private val application: Application
) : AndroidViewModel(application) {

    private val _surfaceRequest = MutableStateFlow<SurfaceRequest?>(null)
    val surfaceRequest = _surfaceRequest.asStateFlow()

    val imageAnalysis = ImageAnalysis.Builder()
        // enable the following line if RGBA output is needed.
        // .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
        .setTargetResolution(Size(1280, 720))
        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
        .build()

    private val cameraPreviewUsecase = Preview.Builder().build().apply {
        setSurfaceProvider { newSurfaceRequest ->
            _surfaceRequest.update { newSurfaceRequest }
        }
    }

    val model by lazy {
        Firebase.ai(backend = GenerativeBackend.googleAI()).liveModel(
            modelName = "gemini-live-2.5-flash-preview",
            generationConfig = liveGenerationConfig {
                responseModality = ResponseModality.AUDIO
                speechConfig = SpeechConfig(voice = Voice("FENRIR"))
            }
        )
    }

    private var session: LiveSession? = null

    init {
        FirebaseApp.initializeApp(application)

        var analyzed = false
        imageAnalysis.setAnalyzer(Dispatchers.Default.asExecutor(), ImageAnalysis.Analyzer { imageProxy ->
            val bitmap: Bitmap? = imageProxy.toBitmap()
            if (!analyzed) {
                if (bitmap != null) {
                    viewModelScope.launch {
                        delay(2000)
                        session?.let {
                            it.send(Content.Builder().text("Describe this image").image(bitmap).build())
                            analyzed = true
                        }
                    }
                }
            }
            imageProxy.close()
        })
    }

    suspend fun bind(lifecycleOwner: LifecycleOwner) {
        val processCameraProvider = ProcessCameraProvider.awaitInstance(application)
        processCameraProvider.bindToLifecycle(
            lifecycleOwner,
            CameraSelector.DEFAULT_BACK_CAMERA,
            cameraPreviewUsecase,
            imageAnalysis
        )

        try {
            awaitCancellation()
        } finally {
            processCameraProvider.unbindAll()
        }
    }

    @SuppressLint("MissingPermission")
    fun startGeminiSession() {
        viewModelScope.launch {
            session = model.connect()
//            session?.startAudioConversation()
        }
    }
}