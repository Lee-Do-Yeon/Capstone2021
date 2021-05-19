package com.example.capstone2021

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.CompoundButton
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    private val PICK_IMAGE_REQUEST = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val sw1 = findViewById<Switch>(R.id.LoadExif)
        val sw2 = findViewById<Switch>(R.id.LoadVision)
        val textview : TextView = findViewById(R.id.textview)

        sw1.setOnCheckedChangeListener { buttonView, isChecked ->
            sw1.toggle()
            textview.text = "select sw1"
        }
        sw2.setOnCheckedChangeListener { buttonView, isChecked ->
            if(sw2.isChecked == true){
                sw2.toggle()
                sw2.isChecked = false
                textview.text = "sw2 true to false"
            }
            else{
                sw2.toggle()
                sw2.isChecked = true
                textview.text = "sw2 false to true"
            }
        }

    }
    inner class checkboxListener : CompoundButton.OnCheckedChangeListener{
        override fun onCheckedChanged(p0: CompoundButton?, p1: Boolean) {
            TODO("Not yet implemented")
        }
    }

    fun loadImagefromGallery(v: View) {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = MediaStore.Images.Media.CONTENT_TYPE
        startActivityForResult(intent, PICK_IMAGE_REQUEST)

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

