package com.example.capstone2021


import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.googlecode.tesseract.android.TessBaseAPI
import java.io.*


class OcrActivity : AppCompatActivity() {
//    var image: Bitmap? = null
    private var mTess: TessBaseAPI? = null
    var datapath = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ocr)
        val image : ImageView = findViewById(R.id.showImageView)
        datapath = "$filesDir/tesseract/"
        checkFile(File(datapath + "tessdata/"))
        val lang = "kor"
        mTess = TessBaseAPI()
        mTess.init(datapath, lang)
        processImage(image)
    }

    private fun copyFiles() {
        try {
            //location we want the file to be at
            val filepath = "$datapath/tessdata/kor.traineddata"

            //get access to AssetManager
            val assetManager = assets

            //open byte streams for reading/writing
            val instream = assetManager.open("tessdata/kor.traineddata")
            val outstream: OutputStream = FileOutputStream(filepath)

            //copy the file to the location specified by filepath
            val buffer = ByteArray(1024)
            var read: Int
            while (instream.read(buffer).also { read = it } != -1) {
                outstream.write(buffer, 0, read)
            }
            outstream.flush()
            outstream.close()
            instream.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun checkFile(dir: File) {
        //directory does not exist, but we can successfully create it
        if (!dir.exists() && dir.mkdirs()) {
            copyFiles()
        }
        //The directory exists, but there is no data file in it
        if (dir.exists()) {
            val datafilepath = "$datapath/tessdata/kor.traineddata"
            val datafile = File(datafilepath)
            if (!datafile.exists()) {
                copyFiles()
            }
        }
    }

    fun processImage(view: Bitmap?) {
        var OCRresult: String? = null
        mTess.setImage(image)
        OCRresult = mTess.getUTF8Text()
        val OCRTextView = findViewById<View>(R.id.text) as TextView
        OCRTextView.text = OCRresult
    }
}

