@file:OptIn(ExperimentalPermissionsApi::class)

package com.example.cameraapp.ui

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale

@Composable
fun Permission(
    permission: String = android.Manifest.permission.CAMERA,
    rationale: String = "We need this permission to display the camera feed",
    permissionNotAvailable: @Composable () -> Unit = {},
    content: @Composable () -> Unit = {}
) {
    val permissionState = rememberPermissionState(permission)

    LaunchedEffect(Unit) {
        if (!permissionState.status.isGranted) {
            permissionState.launchPermissionRequest()
        }
    }

    if (permissionState.status.isGranted) {
        content()
    } else {
        if (permissionState.status.shouldShowRationale) {
            Rationale(rationale) {
                permissionState.launchPermissionRequest()
            }
        } else {
            permissionNotAvailable()
        }
    }
}

@Composable
fun Rationale(
    text: String,
    onRequestPermission: () -> Unit
) {
    AlertDialog(
        onDismissRequest = {},
        confirmButton = {
            Button(
                onClick = onRequestPermission
            ) {
                Text("OK")
            }
        },
        title = {
            Text("Permission request")
        },
        text = {
            Text(text)
        }
    )
}
