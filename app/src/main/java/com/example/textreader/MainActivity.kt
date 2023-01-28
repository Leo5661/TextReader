package com.example.textreader

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.textreader.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    private val permissionsList = Manifest.permission.READ_EXTERNAL_STORAGE
    private val REQUEST_CODE = 1


    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)

    }

    private val requestPermissionLauncher = registerForActivityResult(RequestPermission()) { isGranted: Boolean ->
        when(isGranted){
            true -> {
                openFileManager()
            }
            else -> {
                getPermission()
            }
        }
    }

    private fun getPermission(){
        when{
            ContextCompat.checkSelfPermission(this, permissionsList) == PackageManager.PERMISSION_GRANTED -> {
                openFileManager()
            }
            shouldShowRequestPermissionRationale(permissionsList) -> {

            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_CODE)
            }

        }
    }

    private val startActivityForPDF = registerForActivityResult(ActivityResultContracts.StartActivityForResult() ) {result ->
        when(result.resultCode){
            Activity.RESULT_OK -> {
                val data = result.data
                getData(data)
            }
            else -> {

            }
        }
    }

    private fun openFileManager(){
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "application/pdf"
        startActivityForPDF.launch(intent)
    }

    private fun getData(data: Intent?){
        if (data != null){
            var uri = data.data
            var url = ""
            Log.d("MainActivityLogs", "URI is ============>  $uri" )
            val bundle = Bundle()
            bundle.putString("Uri", uri.toString())

            uri?.path.let { url = it!! }
            Log.d("MainActivityLogs", "URI is ============>  $url" )
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            REQUEST_CODE -> {
                if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    openFileManager()
                }
                else {

                }
                return
            }
        }
    }



    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }
}