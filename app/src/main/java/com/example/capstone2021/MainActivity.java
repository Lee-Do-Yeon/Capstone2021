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
import android.annotation.SuppressLint;
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
import java.lang.ref.WeakReference;
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
    public static int select = SELECT_NULL;
    public static int count_OCR = 0;
    final ArrayList<String> list = new ArrayList<>();

    private TextView mImageDetails;
    private ImageView mMainImage;


    //Main
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setIcon(R.drawable.banner);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

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
//                    intent.setAction(Intent.ACTION_GET_CONTENT);
//                    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                    startActivityForResult(intent, EXIF_IMAGE_REQUEST);


                } else if(checkBox2.isChecked()){
                    select = SELECT_OCR;
                    count_OCR =0;
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
//            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
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
//                intent.putExtra("img",data);
                startActivity(intent);

            } catch (Exception e) {
                e.printStackTrace();
            }

        }else if (requestCode == OCR_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            Log.d(TAG, "start OCR");
//            uploadImage(data.getData());
//            Uri uri = data.getData();
//            Intent intent = new Intent(getApplicationContext(), RecieverActivity.class);
//            ClipData clipdata = data.getClipData();
//            for(int i=0; i < clipdata.getItemCount(); i++){
//                Uri uri = clipdata.getItemAt(i).getUri();
                uploadImage(data.getData());
//            }
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
                Log.d(TAG, "doInBackground");
                BatchAnnotateImagesResponse response = mRequest.execute();
                return convertResponseToString(response);
//                Log.d(TAG,TEXT);
            } catch (GoogleJsonResponseException e) {
                Log.d(TAG, "failed to make API request because " + e.getContent());
            } catch (IOException e) {
                Log.d(TAG, "failed to make API request because of other IOException " +
                        e.getMessage());
            }

            return "null";
        }

        protected void onPostExecute(String result) {
            MainActivity activity = mActivityWeakReference.get();
            if (activity != null && !activity.isFinishing()) {
                mImageDetails = activity.findViewById(R.id.image_details);
                resultOCR(result,mImageDetails);
            }
        }
    }
    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    private void resultOCR(String requestOCR, TextView imageDetail) {
        if (requestOCR != null) {
            //주민 번호 탐지
            String regex = "\\b(?:[0-9]{2}(?:0[1-9]|1[0-2])(?:0[1-9]|[1,2][0-9]|3[0,1]))-[1-4][0-9]{6}\\b";
            Matcher matcher = Pattern.compile(regex).matcher(requestOCR);
            imageDetail.setText("개인정보가 없습니다.");

            if (matcher.find()){
                imageDetail.setText("주민 번호 탐지 : "+ matcher.group());
                list.add(String.format("no.%d -> 주민 번호 탐지 : %s\n", count_OCR++ , matcher.group()));
            }
            //핸드폰 번호 탐지
            regex = "\\b01(?:0|1|[6-9])-(?:\\d{3}|\\d{4})-\\d{4}\\b";
            matcher = Pattern.compile(regex).matcher(requestOCR);
            if (matcher.find()) {
                imageDetail.setText("핸드폰 번호 탐지 : " + matcher.group());
                list.add(String.format("no.%d -> 핸드폰 번호 탐지 : %s\n", count_OCR++, matcher.group()));
            }
            //이메일 탐지
            regex = "\\b[a-zA-Z0-9]+@[a-zA-Z0-9]+.[a-zA-Z]+\\b";
            matcher = Pattern.compile(regex).matcher(requestOCR);
            if (matcher.find()){
                imageDetail.setText("이메일 탐지 : "+ matcher.group());
                list.add(String.format("no.%d -> 이메일 탐지 : %s\n", count_OCR++, matcher.group()));
            }
        }
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


