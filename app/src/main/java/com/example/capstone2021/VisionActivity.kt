/*
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.capstone2021

//import androidx.appcompat.app.AppCompatActivity;
import android.Manifest
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.design.widget.FloatingActionButton
import android.support.v4.content.FileProvider
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.json.GoogleJsonResponseException
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.vision.v1.Vision
import com.google.api.services.vision.v1.VisionRequest
import com.google.api.services.vision.v1.VisionRequestInitializer
import com.google.api.services.vision.v1.model.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.lang.ref.WeakReference
import java.util.*

class VisionActivity : AppCompatActivity() {
    private var mImageDetails: TextView? = null
    private var mMainImage: ImageView? = null

    //Main
    protected fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        val fab: FloatingActionButton = findViewById(R.id.fab)
        fab.setOnClickListener({ view ->
            val builder: AlertDialog.Builder = Builder(this@MainActivity)
            builder
                .setMessage(R.string.dialog_select_prompt)
                .setPositiveButton(
                    R.string.dialog_select_gallery,
                    { dialog, which -> startGalleryChooser() })
            //                    .setNegativeButton(R.string.dialog_select_tessract, (dialog, which) -> copyFiles());
            builder.create().show()
        })
        mImageDetails = findViewById(R.id.image_details)
        mMainImage = findViewById(R.id.main_image)
    }

    fun startGalleryChooser() {
        if (PermissionUtils.requestPermission(
                this,
                GALLERY_PERMISSIONS_REQUEST,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        ) {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(
                Intent.createChooser(intent, "Select a photo"),
                GALLERY_IMAGE_REQUEST
            )
        }
    }

    fun startCamera() {
        if (PermissionUtils.requestPermission(
                this,
                CAMERA_PERMISSIONS_REQUEST,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
            )
        ) {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            val photoUri: Uri = FileProvider.getUriForFile(
                this, getApplicationContext().getPackageName().toString() + ".provider",
                cameraFile
            )
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            startActivityForResult(intent, CAMERA_IMAGE_REQUEST)
        }
    }

    val cameraFile: File
        get() {
            val dir: File = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            return File(dir, FILE_NAME)
        }

    protected fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GALLERY_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            uploadImage(data.data)
        } else if (requestCode == CAMERA_IMAGE_REQUEST && resultCode == RESULT_OK) {
            val photoUri: Uri = FileProvider.getUriForFile(
                this, getApplicationContext().getPackageName().toString() + ".provider",
                cameraFile
            )
            uploadImage(photoUri)
        }
    }

    fun onRequestPermissionsResult(
        requestCode: Int, @NonNull permissions: Array<String?>?, @NonNull grantResults: IntArray?
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CAMERA_PERMISSIONS_REQUEST -> if (PermissionUtils.permissionGranted(
                    requestCode,
                    CAMERA_PERMISSIONS_REQUEST,
                    grantResults
                )
            ) {
                startCamera()
            }
            GALLERY_PERMISSIONS_REQUEST -> if (PermissionUtils.permissionGranted(
                    requestCode,
                    GALLERY_PERMISSIONS_REQUEST,
                    grantResults
                )
            ) {
                startGalleryChooser()
            }
        }
    }

    //start vision API
    fun uploadImage(uri: Uri?) {
        if (uri != null) {
            try {
                // scale the image to save on bandwidth
                val bitmap = scaleBitmapDown(
                    MediaStore.Images.Media.getBitmap(getContentResolver(), uri),
                    MAX_DIMENSION
                )
                callCloudVision(bitmap)
                mMainImage!!.setImageBitmap(bitmap)
            } catch (e: IOException) {
                Log.d(TAG, "Image picking failed because " + e.message)
                Toast.makeText(this, R.string.image_picker_error, Toast.LENGTH_LONG).show()
            }
        } else {
            Log.d(TAG, "Image picker gave us a null image.")
            Toast.makeText(this, R.string.image_picker_error, Toast.LENGTH_LONG).show()
        }
    }

    @Throws(IOException::class)
    private fun prepareAnnotationRequest(bitmap: Bitmap): Vision.Images.Annotate {
        val httpTransport = AndroidHttp.newCompatibleTransport()
        val jsonFactory: JsonFactory = GsonFactory.getDefaultInstance()
        val requestInitializer: VisionRequestInitializer = object : VisionRequestInitializer(
            CLOUD_VISION_API_KEY
        ) {
            /**
             * We override this so we can inject important identifying fields into the HTTP
             * headers. This enables use of a restricted cloud platform API key.
             */
            @Throws(IOException::class)
            override fun initializeVisionRequest(visionRequest: VisionRequest<*>) {
                super.initializeVisionRequest(visionRequest)
                val packageName: String = getPackageName()
                visionRequest.requestHeaders[ANDROID_PACKAGE_HEADER] =
                    packageName
                val sig: String = PackageManagerUtils.getSignature(getPackageManager(), packageName)
                visionRequest.requestHeaders[ANDROID_CERT_HEADER] =
                    sig
            }
        }
        val builder = Vision.Builder(httpTransport, jsonFactory, null)
        builder.setVisionRequestInitializer(requestInitializer)
        val vision = builder.build()
        val batchAnnotateImagesRequest = BatchAnnotateImagesRequest()
        batchAnnotateImagesRequest.requests = object : ArrayList<AnnotateImageRequest?>() {
            init {
                val annotateImageRequest = AnnotateImageRequest()

                // Add the image
                val base64EncodedImage = Image()
                // Convert the bitmap to a JPEG
                // Just in case it's a format that Android understands but Cloud Vision
                val byteArrayOutputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream)
                val imageBytes = byteArrayOutputStream.toByteArray()

                // Base64 encode the JPEG
                base64EncodedImage.encodeContent(imageBytes)
                annotateImageRequest.image = base64EncodedImage

                // add the features we want
                annotateImageRequest.features = object : ArrayList<Feature?>() {
                    init {
                        val textDetection = Feature()
                        textDetection.type = "TEXT_DETECTION"
                        textDetection.maxResults = MAX_LABEL_RESULTS
                        add(textDetection)
                    }
                }

                // Add the list of one thing to the request
                add(annotateImageRequest)
            }
        }
        val annotateRequest = vision.images().annotate(batchAnnotateImagesRequest)
        // Due to a bug: requests to Vision API containing large images fail when GZipped.
        annotateRequest.disableGZipContent = true
        Log.d(TAG, "created Cloud Vision request object, sending request")
        return annotateRequest
    }

    private class LableDetectionTask internal constructor(
        activity: MainActivity,
        annotate: Vision.Images.Annotate
    ) :
        AsyncTask<Any?, Void?, String>() {
        private val mActivityWeakReference: WeakReference<MainActivity>
        private val mRequest: Vision.Images.Annotate
        protected override fun doInBackground(vararg params: Any): String {
            try {
                Log.d(TAG, "created Cloud Vision request object, sending request")
                val response = mRequest.execute()
                return convertResponseToString(response)
            } catch (e: GoogleJsonResponseException) {
                Log.d(TAG, "failed to make API request because " + e.content)
            } catch (e: IOException) {
                Log.d(
                    TAG, "failed to make API request because of other IOException " +
                            e.message
                )
            }
            return "Cloud Vision API request failed. Check logs for details."
        }

        override fun onPostExecute(result: String) {
            val activity = mActivityWeakReference.get()
            if (activity != null && !activity.isFinishing()) {
                val imageDetail: TextView = activity.findViewById(R.id.image_details)
                imageDetail.text = result
            }
        }

        init {
            mActivityWeakReference = WeakReference(activity)
            mRequest = annotate
        }
    }

    private fun callCloudVision(bitmap: Bitmap) {
        // Switch text to loading
        mImageDetails.setText(R.string.loading_message)

        // Do the real work in an async task, because we need to use the network anyway
        try {
            val labelDetectionTask: AsyncTask<Any, Void, String> =
                LableDetectionTask(this, prepareAnnotationRequest(bitmap))
            labelDetectionTask.execute()
        } catch (e: IOException) {
            Log.d(
                TAG, "failed to make API request because of other IOException " +
                        e.message
            )
        }
    }

    private fun scaleBitmapDown(bitmap: Bitmap, maxDimension: Int): Bitmap {
        val originalWidth = bitmap.width
        val originalHeight = bitmap.height
        var resizedWidth = maxDimension
        var resizedHeight = maxDimension
        if (originalHeight > originalWidth) {
            resizedHeight = maxDimension
            resizedWidth =
                (resizedHeight * originalWidth.toFloat() / originalHeight.toFloat()).toInt()
        } else if (originalWidth > originalHeight) {
            resizedWidth = maxDimension
            resizedHeight =
                (resizedWidth * originalHeight.toFloat() / originalWidth.toFloat()).toInt()
        } else if (originalHeight == originalWidth) {
            resizedHeight = maxDimension
            resizedWidth = maxDimension
        }
        return Bitmap.createScaledBitmap(bitmap, resizedWidth, resizedHeight, false)
    }

    companion object {
        private val CLOUD_VISION_API_KEY: String = BuildConfig.API_KEY
        const val FILE_NAME = "temp.jpg"
        private const val ANDROID_CERT_HEADER = "X-Android-Cert"
        private const val ANDROID_PACKAGE_HEADER = "X-Android-Package"
        private const val MAX_LABEL_RESULTS = 10
        private const val MAX_DIMENSION = 1200
        private val TAG = MainActivity::class.java.simpleName
        private const val GALLERY_PERMISSIONS_REQUEST = 0
        private const val GALLERY_IMAGE_REQUEST = 1
        const val CAMERA_PERMISSIONS_REQUEST = 2
        const val CAMERA_IMAGE_REQUEST = 3
        private fun convertResponseToString(response: BatchAnnotateImagesResponse): String {
            val message = StringBuilder("I found these things:\n\n")
            val text = response.responses[0].textAnnotations
            if (text != null) {
                for (label in text) {
                    message.append(String.format(Locale.US, "%s", label.description))
                    message.append("\n")
                }
            } else {
                message.append("nothing")
            }
            return message.toString()
        }
    }
}