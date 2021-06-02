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

package com.example.capstone2021;

import android.Manifest;
import android.content.ClipData;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequest;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.content.Intent.ACTION_PICK;

public class MainActivity extends AppCompatActivity {
    private static final String CLOUD_VISION_API_KEY = BuildConfig.API_KEY;
    public static final String FILE_NAME = "temp.jpg";
    private static final String ANDROID_CERT_HEADER = "X-Android-Cert";
    private static final String ANDROID_PACKAGE_HEADER = "X-Android-Package";
    private static final int MAX_LABEL_RESULTS = 10;
    private static final int MAX_DIMENSION = 1200;
    //EXIF 변수
    private static final int OCR_IMAGE_REQUEST = 1;
    private static final int EXIF_IMAGE_REQUEST = 2;
    private static final int OCR_EXIF_IMAGE_REQUEST = 3;


    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int GALLERY_PERMISSIONS_REQUEST = 0;

    public static final int SELECT_NULL = 0;
    public static final int SELECT_EXIF = 1;
    public static final int SELECT_OCR = 2;
    public static final int SELECT_EXIF_OCR = 3;
    public static String TEXT = "null";
    int select = SELECT_NULL;


    private TextView mImageDetails;
    private ImageView mMainImage;

    class DataBox implements Serializable{
        private static final long serialVersionUID = -4865946674835353945L;
        private String result; //직렬화 대상
        private int num; //직렬화 대상
        private URI uri;
        DataBox() { //생성자는 직렬화 대상이 아니다.

        }
        DataBox(int num, String result,URI uri ) { //생성자는 직렬화 대상이 아니다.
            this.num = num;
            this.result = result;
            this.uri = uri;
        }
    }

    //Main
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.SetButton();

        mImageDetails = findViewById(R.id.image_details);
        mMainImage = findViewById(R.id.main_image);

    }

    public void SetButton() {
        Button btn = findViewById(R.id.button);
        btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view)
            {
                CheckBox checkBox1 = (CheckBox) findViewById(R.id.checkBox1) ;
                CheckBox checkBox2 = (CheckBox) findViewById(R.id.checkBox2) ;
                if (checkBox1.isChecked() && checkBox2.isChecked()) {
                    select = SELECT_EXIF_OCR;
                    Log.d(TAG, "EXIF AND OCR");
//                    Intent intent = new Intent(ACTION_PICK);
//                    intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
//                    startActivityForResult(intent, OCR_EXIF_IMAGE_REQUEST);

                } else if(checkBox1.isChecked()) {
                    select = SELECT_EXIF;
                    Log.d(TAG, "EXIF");
                    Intent intent = new Intent(ACTION_PICK);
                    intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                    startActivityForResult(intent, EXIF_IMAGE_REQUEST);
                } else if(checkBox2.isChecked()){
                    select = SELECT_OCR;
                    Log.d(TAG, "OCR");
                    startGalleryChooser();
                } else{
                    Log.d(TAG, "NOTHING");
                }
            }
        });
    }
    public void startGalleryChooser() {
        if (PermissionUtils.requestPermission(this, GALLERY_PERMISSIONS_REQUEST, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            Intent intent = new Intent(ACTION_PICK);
            intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
            //이미지 다중선택
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
//            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(intent,OCR_IMAGE_REQUEST);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == EXIF_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            Log.d(TAG, "start EXIF");
            Intent intent = new Intent(getApplicationContext(), ExifActivity.class);
            try{
                Uri uri= data.getData();
                intent.putExtra("uri", uri.toString());
                intent.putExtra("img",data);
                startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else if (requestCode == OCR_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            Log.d(TAG, "start OCR");
            uploadImage(data.getData());
            Uri uri = data.getData();
            final ArrayList<String> list = new ArrayList<>();
            Intent intent = new Intent(getApplicationContext(), RecieverActivity.class);
            ClipData clipdata = data.getClipData();
            for(int i=0; i < clipdata.getItemCount(); i++){
                uri = clipdata.getItemAt(i).getUri();
                uploadImage(uri);

                list.add(String.format("no.%d -> uri : %s\n", i , TEXT));
            }
            Log.d(TAG, "test : " + TEXT);
            intent.putExtra("list",list);
//            startActivity(intent);
//                mMainImage.setImageURI(data.getData());
        }else if (requestCode == OCR_EXIF_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            Log.d(TAG, "start OCR and EXIF");
            uploadImage(data.getData());
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case GALLERY_PERMISSIONS_REQUEST:
                if (PermissionUtils.permissionGranted(requestCode, GALLERY_PERMISSIONS_REQUEST, grantResults)) {
                    startGalleryChooser();
                }
                break;
        }
    }

    //start vision API
    public void uploadImage(Uri uri) {
        if (uri != null) {
            try {
                // scale the image to save on bandwidth
                Bitmap bitmap =
                        scaleBitmapDown(
                                MediaStore.Images.Media.getBitmap(getContentResolver(), uri),
                                MAX_DIMENSION);
                callCloudVision(bitmap);
                mMainImage.setImageBitmap(bitmap);
            } catch (IOException e) {
                Log.d(TAG, "Image picking failed because " + e.getMessage());
                Toast.makeText(this, R.string.image_picker_error, Toast.LENGTH_LONG).show();
            }
        } else {
            Log.d(TAG, "Image picker gave us a null image.");
            Toast.makeText(this, R.string.image_picker_error, Toast.LENGTH_LONG).show();
        }
    }



    private Vision.Images.Annotate prepareAnnotationRequest(Bitmap bitmap) throws IOException {
        HttpTransport httpTransport = new NetHttpTransport();
        JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

        VisionRequestInitializer requestInitializer =
                new VisionRequestInitializer(CLOUD_VISION_API_KEY) {
                    /**
                     * We override this so we can inject important identifying fields into the HTTP
                     * headers. This enables use of a restricted cloud platform API key.
                     */
                    @Override
                    protected void initializeVisionRequest(VisionRequest<?> visionRequest)
                            throws IOException {
                        super.initializeVisionRequest(visionRequest);

                        String packageName = getPackageName();
                        visionRequest.getRequestHeaders().set(ANDROID_PACKAGE_HEADER, packageName);

                        String sig = PackageManagerUtils.getSignature(getPackageManager(), packageName);

                        visionRequest.getRequestHeaders().set(ANDROID_CERT_HEADER, sig);
                    }
                };

        Vision.Builder builder = new Vision.Builder(httpTransport, jsonFactory, null);
        builder.setVisionRequestInitializer(requestInitializer);

        Vision vision = builder.build();

        BatchAnnotateImagesRequest batchAnnotateImagesRequest =
                new BatchAnnotateImagesRequest();
        batchAnnotateImagesRequest.setRequests(new ArrayList<AnnotateImageRequest>() {{
            AnnotateImageRequest annotateImageRequest = new AnnotateImageRequest();

            // Add the image
            Image base64EncodedImage = new Image();
            // Convert the bitmap to a JPEG
            // Just in case it's a format that Android understands but Cloud Vision
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream);
            byte[] imageBytes = byteArrayOutputStream.toByteArray();

            // Base64 encode the JPEG
            base64EncodedImage.encodeContent(imageBytes);
            annotateImageRequest.setImage(base64EncodedImage);

            // add the features we want
            annotateImageRequest.setFeatures(new ArrayList<Feature>() {{
                Feature textDetection = new Feature();
                textDetection.setType("TEXT_DETECTION");
                textDetection.setMaxResults(MAX_LABEL_RESULTS);
                add(textDetection);
            }});

            // Add the list of one thing to the request
            add(annotateImageRequest);
        }});

        Vision.Images.Annotate annotateRequest =
                vision.images().annotate(batchAnnotateImagesRequest);
        // Due to a bug: requests to Vision API containing large images fail when GZipped.
        annotateRequest.setDisableGZipContent(true);
        Log.d(TAG, "created Cloud Vision request object, sending request");

        return annotateRequest;
    }


    private class LableDetectionTask extends AsyncTask<Object, Void, String> {
        private final WeakReference<MainActivity> mActivityWeakReference;
        private Vision.Images.Annotate mRequest;

        LableDetectionTask(MainActivity activity, Vision.Images.Annotate annotate) {
            mActivityWeakReference = new WeakReference<>(activity);
            mRequest = annotate;
        }

        @Override
        protected String doInBackground(Object... params) {
            try {
                Log.d(TAG, "created Cloud Vision request object, sending request");
                BatchAnnotateImagesResponse response = mRequest.execute();
                return convertResponseToString(response);
//                Log.d(TAG,TEXT);
            } catch (GoogleJsonResponseException e) {
                Log.d(TAG, "failed to make API request because " + e.getContent());
            } catch (IOException e) {
                Log.d(TAG, "failed to make API request because of other IOException " +
                        e.getMessage());
            }

            return TEXT;
        }

        protected void onPostExecute(String result) {
            MainActivity activity = mActivityWeakReference.get();
            if (activity != null && !activity.isFinishing()) {
                TextView imageDetail = activity.findViewById(R.id.image_details);
//                imageDetail.setText(result);
                resultOCR(result,imageDetail);
//                Log.d(TAG,TEXT);
//                TEXT = result;

            }
        }
    }
    private void resultOCR(String requestOCR,TextView imageDetail) {
        if (requestOCR != null) {

            //주민 번호 탐지
            String regex = "\\b(?:[0-9]{2}(?:0[1-9]|1[0-2])(?:0[1-9]|[1,2][0-9]|3[0,1]))-[1-4][0-9]{6}\\b";
            Matcher matcher = Pattern.compile(regex).matcher(requestOCR);
            if (matcher.find())
                imageDetail.setText("주민 번호 탐지 : "+ matcher.group());
            //핸드폰 번호 탐지
            regex = "\\b01(?:0|1|[6-9])-(?:\\d{3}|\\d{4})-\\d{4}\\b";
            matcher = Pattern.compile(regex).matcher(requestOCR);
            if (matcher.find())
                imageDetail.setText("핸드폰 번호 탐지 : "+ matcher.group());

            //이메일 탐지
            regex = "\\b[a-zA-Z0-9]+@[a-zA-Z0-9]+.[a-zA-Z]+\\b";
            matcher = Pattern.compile(regex).matcher(requestOCR);
            if (matcher.find())
                imageDetail.setText("이메일 탐지 : "+ matcher.group());

        }else
            imageDetail.setText("개인정보가 없습니다.");
    }
    private void callCloudVision(final Bitmap bitmap) {
        // Switch text to loading
        mImageDetails.setText(R.string.loading_message);

        // Do the real work in an async task, because we need to use the network anyway
        try {
            AsyncTask<Object, Void, String> labelDetectionTask = new LableDetectionTask(this, prepareAnnotationRequest(bitmap));
            labelDetectionTask.execute();
        } catch (IOException e) {
            Log.d(TAG, "failed to make API request because of other IOException " +
                    e.getMessage());
        }
    }

    private Bitmap scaleBitmapDown(Bitmap bitmap, int maxDimension) {

        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();
        int resizedWidth = maxDimension;
        int resizedHeight = maxDimension;

        if (originalHeight > originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = (int) (resizedHeight * (float) originalWidth / (float) originalHeight);
        } else if (originalWidth > originalHeight) {
            resizedWidth = maxDimension;
            resizedHeight = (int) (resizedWidth * (float) originalHeight / (float) originalWidth);
        } else if (originalHeight == originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = maxDimension;
        }
        return Bitmap.createScaledBitmap(bitmap, resizedWidth, resizedHeight, false);
    }

    private static String convertResponseToString(BatchAnnotateImagesResponse response) {
        StringBuilder message = new StringBuilder("");

        List<EntityAnnotation> text = response.getResponses().get(0).getTextAnnotations();
        if (text != null) {
            for (EntityAnnotation label : text) {
                message.append(String.format(Locale.US, "%s",  label.getDescription()));
                message.append("\n");
            }
        } else {
            message.append("nothing");
        }
        return message.toString();
    }

}



//package com.example.capstone2021
//
////import com.google.api.client.extensions.android.http.AndroidHttp 사용불가
//import android.Manifest
////import androidx.constraintlayout.motion.utils.Oscillator.TAG
////import androidx.constraintlayout.motion.widget.MotionScene.TAG
////import androidx.constraintlayout.widget.ConstraintLayoutStates.TAG
////import androidx.constraintlayout.widget.Constraints.TAG
////import androidx.constraintlayout.widget.StateSet.TAG
////import android.content.ContentValues.TAG
//import android.content.Intent
//import android.graphics.Bitmap
//import android.net.Uri
//import android.os.AsyncTask
//import android.os.Bundle
//import android.os.Environment
//import android.provider.MediaStore
//import android.util.Log
//import android.widget.ImageView
//import android.widget.TextView
//import android.widget.Toast
//import androidx.appcompat.app.AlertDialog
//import androidx.appcompat.app.AppCompatActivity
//
//import com.google.android.material.floatingactionbutton.FloatingActionButton
//import com.google.api.client.googleapis.json.GoogleJsonResponseException
//import com.google.api.client.http.HttpTransport
//import com.google.api.client.http.javanet.NetHttpTransport
//import com.google.api.client.json.JsonFactory
//import com.google.api.client.json.gson.GsonFactory
//import com.google.api.services.vision.v1.Vision
//import com.google.api.services.vision.v1.VisionRequest
//import com.google.api.services.vision.v1.VisionRequestInitializer
//import com.google.api.services.vision.v1.model.*
//import java.io.ByteArrayOutputStream
//import java.io.File
//import java.io.IOException
//import java.lang.ref.WeakReference
//import java.util.*
//
//
//class MainActivity : AppCompatActivity() {
//
//    private val PICK_IMAGE_REQUEST = 1
//    private val CLOUD_VISION_API_KEY = BuildConfig.API_KEY
//    val FILE_NAME = "temp.jpg"
//    private val ANDROID_CERT_HEADER = "X-Android-Cert"
//    private val ANDROID_PACKAGE_HEADER = "X-Android-Package"
//    private val MAX_LABEL_RESULTS = 10
//    private val MAX_DIMENSION = 1200
//
//    private val TAG = MainActivity::class.java.simpleName
//    private val GALLERY_PERMISSIONS_REQUEST = 0
//    private val GALLERY_IMAGE_REQUEST = 1
//    val CAMERA_PERMISSIONS_REQUEST = 2
//    val CAMERA_IMAGE_REQUEST = 3
//
//    private var mImageDetails: TextView? = null
//    private var mMainImage: ImageView? = null
//
//    //Main
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
//        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
//        setSupportActionBar(toolbar)
//        val fab: FloatingActionButton = findViewById(R.id.fab)
//        fab.setOnClickListener { view ->
//            val builder: AlertDialog.Builder = AlertDialog.Builder(this@MainActivity)
//            builder
//                .setMessage(R.string.dialog_select_prompt)
//                .setPositiveButton(R.string.dialog_select_OCR,{ dialog, which -> startGalleryChooser() })
////                .setNegativeButton(R.string.dialog_select_Exif,{ dialog, which -> copyFiles() })
//            builder.create().show()
//        }
//        mImageDetails = findViewById(R.id.image_details)
//        mMainImage = findViewById(R.id.main_image)
//    }
//
//    fun startGalleryChooser() {
//        if (PermissionUtils.requestPermission(
//                this,
//                GALLERY_PERMISSIONS_REQUEST,
//                Manifest.permission.READ_EXTERNAL_STORAGE
//            )
//        ) {
//            val intent = Intent()
//            intent.type = "image/*"
//            intent.action = Intent.ACTION_GET_CONTENT
//            startActivityForResult(
//                Intent.createChooser(intent, "Select a photo"),
//                GALLERY_IMAGE_REQUEST
//            )
//        }
//    }
//
//
//    fun getCameraFile(): File? {
//        val dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
//        return File(dir, FILE_NAME)
//    }
//
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if (requestCode == GALLERY_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
//            uploadImage(data.data)
//        }
//    }
//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<out String>,
//        grantResults: IntArray
//    ) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        when (requestCode) {
//                    GALLERY_PERMISSIONS_REQUEST -> if (PermissionUtils.permissionGranted(
//                    requestCode,
//                    GALLERY_PERMISSIONS_REQUEST,
//                    grantResults
//                )
//            )
//                startGalleryChooser()
//            }
//        }
//    //start vision API
//    fun uploadImage(uri: Uri?) {
//        if (uri != null) {
//            try {
//                // scale the image to save on bandwidth
//                val bitmap = scaleBitmapDown(
//                    MediaStore.Images.Media.getBitmap(contentResolver, uri),
//                    MAX_DIMENSION
//                )
//                callCloudVision(bitmap)
//                mMainImage!!.setImageBitmap(bitmap)
//            } catch (e: IOException) {
//                Log.d(TAG, "Image picking failed because " + e.message)
//                Toast.makeText(this, R.string.image_picker_error, Toast.LENGTH_LONG).show()
//            }
//        } else {
//            Log.d(TAG, "Image picker gave us a null image.")
//            Toast.makeText(this, R.string.image_picker_error, Toast.LENGTH_LONG).show()
//        }
//    }
//
//    @Throws(IOException::class)
//    private fun prepareAnnotationRequest(bitmap: Bitmap): Vision.Images.Annotate {
//        val httpTransport: HttpTransport = NetHttpTransport()
//        val jsonFactory: JsonFactory = GsonFactory.getDefaultInstance()
//        val requestInitializer: VisionRequestInitializer =
//            object : VisionRequestInitializer(CLOUD_VISION_API_KEY) {
//                /**
//                 * We override this so we can inject important identifying fields into the HTTP
//                 * headers. This enables use of a restricted cloud platform API key.
//                 */
//                @Throws(IOException::class)
//                override fun initializeVisionRequest(visionRequest: VisionRequest<*>) {
//                    super.initializeVisionRequest(visionRequest)
//                    val packageName = packageName
//                    visionRequest.requestHeaders[ANDROID_PACKAGE_HEADER] = packageName
////                  getSignature의 리턴 값을 찾기위해 ?추가
//                    val sig: String? = PackageManagerUtils.getSignature(packageManager, packageName)
//                    visionRequest.requestHeaders[ANDROID_CERT_HEADER] = sig
//                }
//            }
//        val builder = Vision.Builder(httpTransport, jsonFactory, null)
//        builder.setVisionRequestInitializer(requestInitializer)
//        val vision = builder.build()
//        val batchAnnotateImagesRequest = BatchAnnotateImagesRequest()
//        batchAnnotateImagesRequest.requests = object : ArrayList<AnnotateImageRequest?>() {
//            init {
//                val annotateImageRequest = AnnotateImageRequest()
//
//                // Add the image
//                val base64EncodedImage = Image()
//                // Convert the bitmap to a JPEG
//                // Just in case it's a format that Android understands but Cloud Vision
//                val byteArrayOutputStream = ByteArrayOutputStream()
//                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream)
//                val imageBytes = byteArrayOutputStream.toByteArray()
//
//                // Base64 encode the JPEG
//                base64EncodedImage.encodeContent(imageBytes)
//                annotateImageRequest.image = base64EncodedImage
//
//                // add the features we want
//                annotateImageRequest.features = object : ArrayList<Feature?>() {
//                    init {
//                        val textDetection = Feature()
//                        textDetection.type = "TEXT_DETECTION"
//                        textDetection.maxResults = MAX_LABEL_RESULTS
//                        add(textDetection)
//                    }
//                }
//
//                // Add the list of one thing to the request
//                add(annotateImageRequest)
//            }
//        }
//        val annotateRequest = vision.images().annotate(batchAnnotateImagesRequest)
//        // Due to a bug: requests to Vision API containing large images fail when GZipped.
//        annotateRequest.disableGZipContent = true
//        Log.d(TAG, "created Cloud Vision request object, sending request")
//        return annotateRequest
//    }
//    fun convertResponseToString(response: BatchAnnotateImagesResponse): String? {
//        val message = StringBuilder("I found these things:\n\n")
//        val text = response.responses[0].textAnnotations
//        if (text != null) {
//            for (label in text) {
//                message.append(String.format(Locale.US, "%s", label.description))
//                message.append("\n")
//            }
//        } else {
//            message.append("nothing")
//        }
//        return message.toString()
//    }
//
//    abstract class LableDetectionTask internal constructor(
//        activity: MainActivity,
//        annotate: Vision.Images.Annotate
//    ) :
//        AsyncTask<Any?, Void?, String>() {
//        private val mActivityWeakReference: WeakReference<MainActivity> = WeakReference(activity)
//        private val mRequest: Vision.Images.Annotate = annotate
//        protected fun doInBackground(vararg params: Any): String {
//            try {
//                Log.d("MainActivity", "created Cloud Vision request object, sending request")
//                val response = mRequest.execute()
//                return MainActivity.convertResponseToString(response)
//            } catch (e: GoogleJsonResponseException) {
//                Log.d("MainActivity", "failed to make API request because " + e.content)
//            } catch (e: IOException) {
//                Log.d(
//                    "MainActivity", "failed to make API request because of other IOException " +
//                            e.message
//                )
//            }
//            return "Cloud Vision API request failed. Check logs for details."
//        }
//
//        override fun onPostExecute(result: String) {
//            val activity = mActivityWeakReference.get()
//            if (activity != null && !activity.isFinishing) {
//                val imageDetail: TextView = activity.findViewById(R.id.image_details)
//                imageDetail.text = result
//            }
//        }
//
//    }
//    private fun callCloudVision(bitmap: Bitmap) {
//        // Switch text to loading
//        mImageDetails?.setText(R.string.loading_message)
//
//        // Do the real work in an async task, because we need to use the network anyway
//        try {
//            val labelDetectionTask: AsyncTask<Any, Void, String> =
//
//                LableDetectionTask(this, prepareAnnotationRequest(bitmap))
//            labelDetectionTask.execute()
//        } catch (e: IOException) {
//            Log.d(
//                TAG, "failed to make API request because of other IOException " +
//                        e.message
//            )
//        }
//    }
//
//    private fun scaleBitmapDown(bitmap: Bitmap, maxDimension: Int): Bitmap {
//        val originalWidth = bitmap.width
//        val originalHeight = bitmap.height
//        var resizedWidth = maxDimension
//        var resizedHeight = maxDimension
//        if (originalHeight > originalWidth) {
//            resizedHeight = maxDimension
//            resizedWidth =
//                (resizedHeight * originalWidth.toFloat() / originalHeight.toFloat()).toInt()
//        } else if (originalWidth > originalHeight) {
//            resizedWidth = maxDimension
//            resizedHeight =
//                (resizedWidth * originalHeight.toFloat() / originalWidth.toFloat()).toInt()
//        } else if (originalHeight == originalWidth) {
//            resizedHeight = maxDimension
//            resizedWidth = maxDimension
//        }
//        return Bitmap.createScaledBitmap(bitmap, resizedWidth, resizedHeight, false)
//    }
//
//
//}
////
////

//
//
