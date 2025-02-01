package com.example.firebaseauth

import PermissionUtils
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.os.Build
import android.os.Environment
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.firebaseauth.navigation.AppNavigation
import com.example.firebaseauth.ui.theme.FirebaseAuthTheme
import androidx.fragment.app.FragmentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts


class MainActivity : FragmentActivity() {

    private lateinit var permissionUtils: PermissionUtils
    private lateinit var storagePermissionLauncher: ActivityResultLauncher<Intent>

    companion object {
        private const val REQUEST_CODE_STORAGE_PERMISSION = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        permissionUtils = PermissionUtils(this)


        storagePermissionLauncher =
            this.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    if (Environment.isExternalStorageManager()) {
                        permissionUtils.onPermissionGranted()
                    } else {
                        permissionUtils.onPermissionDenied()
                    }
                }
            }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                permissionUtils.onPermissionGranted()
            } else {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                storagePermissionLauncher.launch(intent)
            }
        } else {
            if (permissionUtils.isReadStoragePermissionGranted()) {
                permissionUtils.onPermissionGranted()
            } else {
                permissionUtils.requestReadStoragePermission(this)
            }
        }

        setContent {
            FirebaseAuthTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AppNavigation()
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionUtils.handlePermissionResult(requestCode, grantResults)
    }
}
