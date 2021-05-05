package com.example.capstone2021

import android.R.array
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {

    private val PICK_IMAGE_REQUEST = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun loadImagefromGallery(v: View) {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST)
    }

    //버전분기 필요함
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val intent = Intent(applicationContext, ExifActivity::class.java)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && null != data) {
            try{
                val uri: Uri? = data.data
                intent.putExtra("uri", uri.toString())
                startActivity(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            Log.d("Activity Result", "something wrong")
        }

    }

}

