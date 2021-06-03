package com.example.capstone2021

import android.content.Intent
import android.net.Uri
import android.net.Uri.parse
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.exifinterface.media.ExifInterface


class ExifActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exif)
        supportActionBar!!.setIcon(R.drawable.banner)
        supportActionBar!!.setDisplayUseLogoEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)

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
        if(lat != null || lng != null) {
            textview.setText("위치정보 탐지 : \n $lat | $lng")
        }else
            textview.setText("위치 정보가 없습니다.")
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

//        exif.saveAttributes()

        val lat = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE) //위도
        val lng = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE) //경도

        val textview : TextView = findViewById(R.id.textview)
        textview.setText("제거된 위치정보 : $lat | $lng")
        Log.d("test_after_delete", "$lat | $lng")
    }

}
