package ir.fallahpoor.eks

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import ir.fallahpoor.eks.theme.EksTheme

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (!isGranted) {
            showPermissionDeniedDialog()
        }
    }

    private fun showPermissionDeniedDialog() {
        AlertDialog.Builder(this)
            .setMessage(getString(R.string.notification_permission_denied_message))
            .setPositiveButton(getString(android.R.string.ok)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EksTheme {
                Eks()
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPostNotificationsPermissionIfNotGranted()
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun requestPostNotificationsPermissionIfNotGranted() {
        if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            return
        }
        if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
            showPermissionRationaleDialog()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun showPermissionRationaleDialog() {
        AlertDialog.Builder(this)
            .setMessage(getString(R.string.notification_permission_rationale_message))
            .setNegativeButton(getString(android.R.string.cancel)) { dialog, _ -> dialog.dismiss() }
            .setPositiveButton(getString(R.string.grant_access)) { dialog, _ ->
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                dialog.dismiss()
            }
            .show()
    }

}