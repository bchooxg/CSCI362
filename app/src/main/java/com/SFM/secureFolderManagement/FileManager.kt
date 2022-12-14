package com.SFM.secureFolderManagement

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager

import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi

import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File


class FileManager : AppCompatActivity() {



    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file_manager)

        // find button
        val btn = findViewById<Button>(R.id.btnCheckPermissions)
        // set onclick listener and request for permissions if check  permissions function returns false
        btn.setOnClickListener {

            if (Build.VERSION.SDK_INT >= 30) {
                if (!Environment.isExternalStorageManager()) {
                    val intent = Intent(android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                    intent.addCategory("android.intent.category.DEFAULT")
                    intent.data = Uri.parse(String.format("package:%s", applicationContext.packageName))
                    startActivity(intent)

                }
            }
            // Check if the app permssion to write to external storage
            if (!this.checkWritePermissions()) {
                Toast.makeText(this, "Requesting for Write Permissions", Toast.LENGTH_SHORT).show()
                this.requestWritePermissions()
            // Check if the app has manage all files permissions
            }else if(!this.checkManagePermisssions()){
                Toast.makeText(this, "Requesting for Manage Permissions", Toast.LENGTH_SHORT).show()
                this.requestManagePermissions()
            }else{
                // if permissions are already granted, make toast
                Toast.makeText(this, "Permissions already granted", Toast.LENGTH_SHORT).show()
                // Create intent to go to file list
                val intent = Intent(this, fileList::class.java)
                var path = Environment.getExternalStorageDirectory().path
                path += "/.Secure_File_Management"
                // Check if folder exists
                val folder = File(path)
                if (!folder.exists()) {
                    try{
                        Log.v("TEST", "Folder not found, attempting to create folder")
                        folder.mkdir()
                    }catch (e: Exception){
                        Toast.makeText(this, "Error creating folder", Toast.LENGTH_SHORT).show()
                    }
                }
                Log.v("TEST", "Folder found, moving to folder")
                intent.putExtra("path", path)
                startActivity(intent)
            }
        }

    }

    // Check permission to read and write to external storage
    @RequiresApi(Build.VERSION_CODES.R)
    private fun checkWritePermissions(): Boolean {
        val writePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        Log.v("TEST", writePermission.toString())
        return writePermission == PackageManager.PERMISSION_GRANTED
    }
    @RequiresApi(Build.VERSION_CODES.R)
    private fun checkManagePermisssions(): Boolean {
        // Call environment is external storage manager
        Log.v("TEST", Environment.isExternalStorageManager().toString())
        return Environment.isExternalStorageManager()
    }

    // Request permission to read and write to external storage
    @RequiresApi(Build.VERSION_CODES.R)
    private fun requestWritePermissions() {
        Log.v("TEST", "Requesting permissions")
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.MANAGE_EXTERNAL_STORAGE), 1)
     }
    @RequiresApi(Build.VERSION_CODES.R)
    private fun requestManagePermissions(){
        Log.v("TEST", "Requesting permissions")
        // create intent to open settings page
        val intent = Intent(android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
        intent.addCategory("android.intent.category.DEFAULT")
        intent.data = Uri.parse(String.format("package:%s", applicationContext.packageName))
        startActivity(intent)
        //ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.MANAGE_EXTERNAL_STORAGE), 1)
    }
}