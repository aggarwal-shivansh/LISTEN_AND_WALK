package com.listenandwalk;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import android.annotation.SuppressLint;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Size;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.objects.DetectedObject;
import com.listenandwalk.databinding.ActivityMain2Binding;
import com.listenandwalk.databinding.ActivityMain2Binding;

import com.google.mlkit.common.model.LocalModel;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.objects.ObjectDetection;
import com.google.mlkit.vision.objects.ObjectDetector;
import com.google.mlkit.vision.objects.custom.CustomObjectDetectorOptions;

import java.util.Locale;

public class MainActivity2 extends AppCompatActivity {

    ActivityMain2Binding binding;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private ObjectDetector objectDetector;
    TextToSpeech textToSpeech;
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main2);

        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {

                // if No error is found then only it will run
                if(i!=TextToSpeech.ERROR){
                    // To Choose language of speech
                    textToSpeech.setLanguage(new Locale("hi"));
                    textToSpeech.setSpeechRate((float)0.8);

                }
            }
        });

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main2);

        cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener( ()-> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider = cameraProvider);
            }
            catch (Exception e) {
                // No errors need to be handled for this Future.
                // This should never be reached.
            }
        }, ContextCompat.getMainExecutor(this));


        LocalModel localModel = new LocalModel.Builder().setAssetFilePath("object_detection.tflite").build();
        CustomObjectDetectorOptions customObjectDetectorOptions = new CustomObjectDetectorOptions.Builder(localModel)
                .setDetectorMode(CustomObjectDetectorOptions.STREAM_MODE)
                .enableClassification()
                .setClassificationConfidenceThreshold(0.5f).setMaxPerObjectLabelCount(1)
                .build();


        objectDetector = ObjectDetection.getClient(customObjectDetectorOptions);

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @SuppressLint("UnsafeExperimentalUsageError")
    private void bindPreview(ProcessCameraProvider cameraProvider)
    {
        Preview preview = new Preview.Builder().build();
        CameraSelector cameraSelector = new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build();

        preview.setSurfaceProvider(binding.previewView.getSurfaceProvider());

        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setTargetResolution(new Size(1280,720))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();

        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this),imageProxy->{
            // rotationDegrees = imageProxy.getImageInfo().getRotationDegrees();

            @SuppressLint("UnsafeOptInUsageError") Image image = imageProxy.getImage();
            if(image!=null)
            {
                InputImage processImage =
                        InputImage.fromMediaImage(image, imageProxy.getImageInfo().getRotationDegrees());

                objectDetector
                        .process(processImage)
                        .addOnSuccessListener(
                                objects ->{

                                    if(binding.parentLayout.getChildCount() >1)
                                        binding.parentLayout.removeViewAt(1);
                                    for(DetectedObject detectedObject : objects) {
                                        for (DetectedObject.Label label : detectedObject.getLabels()) {
                                            String text = label.getText();
                                            Draw element = new Draw(this,
                                                    detectedObject.getBoundingBox(),
                                                    text != null ? text : "Undefined");
                                            speakOut(text+"AHEAD");


                                            binding.parentLayout.addView(element);
                                        }
                                    }
                                    imageProxy.close();
                                }
                        );
            }
        });

        cameraProvider.bindToLifecycle(this,cameraSelector,preview,imageAnalysis);
    }

    public void speakOut(String str)            // speak function
    {
        textToSpeech.speak(str,TextToSpeech.QUEUE_FLUSH,null);
    }

}