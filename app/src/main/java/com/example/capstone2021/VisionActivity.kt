package com.example.capstone2021

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.cloud.vision.v1.AnnotateImageRequest
import com.google.cloud.vision.v1.AnnotateImageResponse
import com.google.cloud.vision.v1.BatchAnnotateImagesResponse
import com.google.cloud.vision.v1.Feature
import com.google.cloud.vision.v1.Image
import com.google.cloud.vision.v1.ImageAnnotatorClient
import com.google.protobuf.ByteString
import java.io.FileInputStream
import java.io.IOException
import java.util.*


class VisionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vision)
        object DetectText {
            @Throws(IOException::class)
            fun detectText() {
                // TODO(developer): Replace these variables before running the sample.
                val filePath = "path/to/your/image/file.jpg"
                detectText(filePath)
            }

            // Detects text in the specified image.
            @Throws(IOException::class)
            fun detectText(filePath: String?) {
                val requests: MutableList<AnnotateImageRequest> = ArrayList<AnnotateImageRequest>()
                val imgBytes: ByteString = ByteString.readFrom(FileInputStream(filePath))
                val img: Image = Image.newBuilder().setContent(imgBytes).build()
                val feat: Feature = Feature.newBuilder().setType(Feature.Type.TEXT_DETECTION).build()
                val request: AnnotateImageRequest = AnnotateImageRequest.newBuilder().addFeatures(feat).setImage(img).build()
                requests.add(request)
                ImageAnnotatorClient.create().use({ client ->
                    val response: BatchAnnotateImagesResponse = client.batchAnnotateImages(requests)
                    val responses: kotlin.collections.List<AnnotateImageResponse> = response.getResponsesList()
                    for (res in responses) {
                        if (res.hasError()) {
                            System.out.format("Error: %s%n", res.getError().getMessage())
                            return
                        }

                        // For full list of available annotations, see http://g.co/cloud/vision/docs
                        for (annotation in res.getTextAnnotationsList()) {
                            System.out.format("Text: %s%n", annotation.getDescription())
                            System.out.format("Position : %s%n", annotation.getBoundingPoly())
                        }
                    }
                })
            }
        }
    }
}