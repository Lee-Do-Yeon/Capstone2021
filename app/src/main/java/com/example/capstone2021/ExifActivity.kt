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

        var intent = getIntent()
        var uristr = intent.extras!!.getString("uri")
        val byteArray = getIntent().getByteArrayExtra("img")

        if( uristr != null){
            val uri: Uri = parse(uristr)
            this.loadExifFromImage(uri,byteArray)
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

    fun loadExifFromImage(uri: Uri?, byteArray: ByteArray?) {
        val imgView : ImageView = findViewById(R.id.showImageView)
        imgView.setImageURI(uri)

        val path : String = getPathFromUri(uri)

        var exif = ExifInterface(path)

        val lat = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE) //위도
        val lng = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE) //경도
        Log.d("test_before_delete", "$lat, $lng")

//        val textview : TextView = findViewById(R.id.textview)
//        textview.setText("$lat | $lng")
        //05.28 데이터 전송하기
        if(lat == null && lng == null){
            Log.d("test", "$lat, $lng")
            val intent = Intent(applicationContext, RecieverActivity::class.java)
//            intent.putExtra("data", null)
            intent.putExtra("image",byteArray)
            startActivity(intent)
        }else {
            val data = lat + lng
            val intent = Intent(applicationContext, RecieverActivity::class.java)
            intent.putExtra("data", data)
            intent.putExtra("image",byteArray)
            startActivity(intent)
        }
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

