package com.listenandwalk;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.Image;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.util.Size;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.speech.tts.TextToSpeech;
import android.speech.SpeechRecognizer;


import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Queue;

import com.google.common.util.concurrent.ListenableFuture;
import androidx.camera.lifecycle.ProcessCameraProvider;
import com.google.mlkit.common.model.LocalModel;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.objects.ObjectDetection;
import com.google.mlkit.vision.objects.ObjectDetector;
import com.google.mlkit.vision.objects.custom.CustomObjectDetectorOptions;


public class MainActivity extends AppCompatActivity implements LocationListener
{
    Button start;
    TextView textView_location;
    LocationManager locationManager;
    TextToSpeech textToSpeech;
    public String address;
    private SpeechRecognizer speechRecognizer;
    private  Intent intentRecognizer;

    private HashMap<String, String> utterParam;
    double startlat,startlon;

    Location l1 = new Location("");
    Location l2 = new Location("");
    int i=0,once=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ContextCompat.checkSelfPermission( MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
        != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(MainActivity.this,new  String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION
            },100);
        }


        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {

                // if No error is found then only it will run
                if(i!=TextToSpeech.ERROR){
                    // To Choose language of speech
                    textToSpeech.setLanguage(new Locale("en"));
                    textToSpeech.setSpeechRate((float)0.8);
                    if(once==0)
                    {
                        speakOut("WELCOME TO LISTEN AND WALK");
                        once++;
                    }

                }
            }
        });

        start = (Button) findViewById(R.id.start);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    if(i==0)
                    {
                        getLocation();
                        ++i;
                    }
                    else if(i==1)
                    {
                        speakOut("PLEASE SPEAK YOUR DESTINATION AFTER THE BEEP SOUND");
                        Thread.sleep(5050);
                        {
                            beep();
                            getSpeech();
                        }
                        i++;
                    }
                    else if(i==2)
                    {
                        speakOut("NAVIGATION MODE TURNED ON FOLLOW THE COMMANDS");
                        //intent.setAction(MediaStore.ACTION_VIDEO_CAPTURE);
                        //startActivity(intent);
                        Intent intent = new Intent(MainActivity.this,MainActivity2.class);
                        startActivity(intent);
                        finish();

                    }

                }catch (Exception e)
                {
                    e.printStackTrace();
                }

            }
        });
    }

    public  void getSpeech() {
        intentRecognizer = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intentRecognizer.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

        if (intentRecognizer.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intentRecognizer, 10);
        } else {
            Toast.makeText(this, "THIS FEATURE IS NOT SUPPORTED", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    protected void onActivityResult(int requestcode,int resultcode,Intent data)
    {
        super.onActivityResult(requestcode,resultcode,data);

        switch (requestcode)
        {
            case 10:
                if(resultcode==RESULT_OK && data != null)
                {
                    ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    String destination = "";
                    if(matches != null)
                    {
                        destination = matches.get(0);
                        //Toast.makeText(this,""+destination,Toast.LENGTH_SHORT).show();
                        giveLocation(destination);
                    }
                }
                break;
        }

    }

    @SuppressLint("MissingPermission")
    private void getLocation()
    {
        try{
        locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5, MainActivity.this);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void onLocationChanged(Location location)
    {
        Toast.makeText(this,""+location.getLatitude()+","+location.getLongitude(), Toast.LENGTH_SHORT).show();
        try {
            Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(),location.getLongitude(),1);
            address = addresses.get(0).getAddressLine(0);

            address = "YOUR CURRENT LOCATION IS "+address;
            l1.setLatitude(addresses.get(0).getLatitude());
            l1.setLongitude(addresses.get(0).getLongitude());

            //startlat = addresses.get(0).getLatitude();
            //startlon = addresses.get(0).getLongitude();
            speakOut(address);

            //MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.beep);     //define a new media player with your local wav file
           // mediaPlayer.start();         //plays the beep sound for indication.

            //speechRecognizer.startListening(intentRecognizer);
            //speechRecognizer.wait();
            //speechRecognizer.stopListening();

            //textView_location.setText(address);
        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void giveLocation(String desLoc)
    {
        try {
            Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
            List<Address> destination = geocoder.getFromLocationName(desLoc, 1);
            //double endlat = destination.get(0).getLatitude();
            //double endlon = destination.get(0).getLongitude();
            l2.setLatitude(destination.get(0).getLatitude());
            l2.setLongitude(destination.get(0).getLongitude());
            float[] result = new float[1];
            //Location.distanceBetween(startlat,startlon,endlat,endlon,result);

            float diskm = (l1.distanceTo(l2))/1000;
            //df2.format(diskm);
            //diskm = Math.round(diskm);

            String dis = "DISTANCE TO WALK IS "+String.valueOf(diskm)+" KILOMETERS";
            speakOut(dis);

        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    public void speakOut(String str)            // speak function
    {
        textToSpeech.speak(str,TextToSpeech.QUEUE_FLUSH,null);
    }

    public void beep()
    {
        MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.beep);     //define a new media player with your local wav file
        mediaPlayer.start();
       // getSpeech();
    }
}

