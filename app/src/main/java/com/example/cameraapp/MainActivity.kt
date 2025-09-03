package com.example.cameraapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.camera.compose.CameraXViewfinder
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cameraapp.ui.CameraViewModel
import com.example.cameraapp.ui.Permission
import com.example.cameraapp.ui.theme.CameraAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CameraAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        Permission(
            permissionNotAvailable = {
                Text("Oh noes!")
            }
        ) {
            CameraPreview()
        }
    }
}

@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    viewModel: CameraViewModel = viewModel()
) {
    val surfaceRequest by viewModel.surfaceRequest.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(Unit) {
        viewModel.bind(lifecycleOwner)
    }

    surfaceRequest?.let {
        CameraXViewfinder(
            surfaceRequest = it,
            modifier = modifier
        )
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    CameraAppTheme {
        MainScreen()
    }
}