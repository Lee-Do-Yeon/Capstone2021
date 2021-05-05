package com.example.capstone2021

import android.media.ExifInterface
import android.net.Uri
import android.net.Uri.parse
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import java.io.IOException



class ExifActivity : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exif)

        var intent = getIntent()
        var str = intent.extras!!.getString("uri")
        if( str != null){
            Log.d("test", "test : $str")
            val uri: Uri = parse(str)
            this.loadExifFromImage(uri)
        }
        else{
            Log.d("test", "test : null")
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun loadExifFromImage(uri: Uri?) {
        val imgView : ImageView = findViewById(R.id.showImageView)
        imgView.setImageURI(uri)

        var exif : ExifInterface? = null
        try{
            val `in` = contentResolver.openInputStream(uri!!)!!
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                exif = ExifInterface(`in`)
            }
        } catch (e: IOException)
        {
            Log.d("error", "catch error")
            e.printStackTrace()
        }

        val lat = exif?.getAttribute(ExifInterface.TAG_GPS_LATITUDE); //위도
        val lat_ref = exif?.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF); //위도가 북위인지 남위인지
        val lng = exif?.getAttribute(ExifInterface.TAG_GPS_LONGITUDE); //경도
        val lng_ref = exif?.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF); //경도가 북위인지 남위인지

        val textview : TextView = findViewById(R.id.textview)
        textview.setText("$lat | $lat_ref | $lng | $lng_ref")
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun deleteExifdata(uri: Uri?) {
        var exif : ExifInterface? = null
        try{
            val `in` = contentResolver.openInputStream(uri!!)!!
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                exif = ExifInterface(`in`)
            }
        } catch (e: IOException)
        {
            Log.d("error", "catch error")
            e.printStackTrace()
        }

        exif?.setAttribute(ExifInterface.TAG_GPS_LATITUDE, null)
        exif?.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, null)
        exif?.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, null)
        exif?.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, null)

        val lat = exif?.getAttribute(ExifInterface.TAG_GPS_LATITUDE); //위도
        val lat_ref = exif?.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF); //위도가 북위인지 남위인지
        val lng = exif?.getAttribute(ExifInterface.TAG_GPS_LONGITUDE); //경도
        val lng_ref = exif?.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF); //경도가 북위인지 남위인지

        val textview : TextView = findViewById(R.id.textview)
        textview.setText("$lat | $lat_ref | $lng | $lng_ref")
        //exif?.saveAttributes()
    }


}