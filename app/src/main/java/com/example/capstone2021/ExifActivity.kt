package com.example.capstone2021

import androidx.exifinterface.media.ExifInterface //여기 수정
import android.net.Uri
import android.net.Uri.parse
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.capstone2021.R
import java.io.File
import java.io.IOException


class ExifActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exif)

        var intent = getIntent()
        var uristr = intent.extras!!.getString("uri")

        if( uristr != null){
            val uri: Uri = parse(uristr)
            this.loadExifFromImage(uri)
        }
        else{
            Log.d("uristr", "uristr is null")
        }
        val delete_button : Button = findViewById(R.id.delete_button);
        delete_button.setOnClickListener{
            if( uristr != null){
                val uri: Uri = parse(uristr)
                this.deleteExifData(uri)
            }
        }

    }

    fun loadExifFromImage(uri: Uri?) {
        val imgView : ImageView = findViewById(R.id.showImageView)
        imgView.setImageURI(uri)

        val path : String = getPathFromUri(uri)

        var exif = ExifInterface(path)

        val lat = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE) //위도
        val lng = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE) //경도
        Log.d("test_before_delete", "$lat, $lng")

        val textview : TextView = findViewById(R.id.textview)
        textview.setText("$lat | $lng")
    }

    fun getPathFromUri(uri: Uri?): String {
        val cursor = contentResolver.query(uri!!, null, null, null, null)
        cursor!!.moveToNext()
        val path = cursor.getString(cursor.getColumnIndex("_data"))
        cursor.close()
        return path
    }

    fun deleteExifData(uri: Uri?) {
        val path : String = getPathFromUri(uri)

        var exif = ExifInterface(path)

        exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE, null)
        exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, null)

        exif.saveAttributes();

        val lat = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE) //위도
        val lng = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE) //경도

        val textview : TextView = findViewById(R.id.textview)
        textview.setText("$lat | $lng")
        Log.d("test_after_delete", "$lat | $lng")
    }

}

