package com.example.capstone2021

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    private val PICK_IMAGE_REQUEST = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


    }


    fun loadImagefromGallery(v: View) {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = MediaStore.Images.Media.CONTENT_TYPE
        startActivityForResult(intent, PICK_IMAGE_REQUEST)

    }

    //버전분기 필요함
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val ExifSW = findViewById<Switch>(R.id.LoadExif)
        val VisionSW = findViewById<Switch>(R.id.LoadVision)
        val textview : TextView = findViewById(R.id.textview)
        textview.setText("$ExifSW | $VisionSW")
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

