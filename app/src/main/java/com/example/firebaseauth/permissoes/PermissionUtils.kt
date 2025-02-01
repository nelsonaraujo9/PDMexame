import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import android.provider.Settings

class PermissionUtils(private val context: Context) {

    companion object {
        const val REQUEST_CODE_READ_STORAGE = 1001
    }

    fun isReadStoragePermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }

    fun requestReadStoragePermission(activity: FragmentActivity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (isReadStoragePermissionGranted()) {
                onPermissionGranted()
            } else {
                ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_CODE_READ_STORAGE)
            }
        } else {
            onPermissionGranted()
        }
    }

    fun handlePermissionResult(requestCode: Int, grantResults: IntArray) {
        if (requestCode == REQUEST_CODE_READ_STORAGE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                onPermissionGranted()
            } else {
                onPermissionDenied()
            }
        }
    }

    fun onPermissionGranted() {
        Toast.makeText(context, "Permissão concedida!", Toast.LENGTH_SHORT).show()
    }

    fun onPermissionDenied() {
        Toast.makeText(context, "Permissão negada. Não é possível acessar o armazenamento.", Toast.LENGTH_SHORT).show()
    }

    fun getImagesFromExternalStorage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            val projection = arrayOf(MediaStore.Images.Media._ID, MediaStore.Images.Media.DISPLAY_NAME)
            val cursor = context.contentResolver.query(collection, projection, null, null, null)

            cursor?.use {
                while (it.moveToNext()) {
                    val imageId = it.getLong(it.getColumnIndexOrThrow(MediaStore.Images.Media._ID))
                    val imageName = it.getString(it.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME))
                }
            }
        }
    }


    fun requestManageExternalStoragePermission(activity: FragmentActivity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                onPermissionGranted()
            } else {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                activity.startActivityForResult(intent, REQUEST_CODE_READ_STORAGE)
            }
        }
    }
}
