package com.listenandwalk;


import androidx.fragment.app.FragmentActivity;
import android.speech.tts.TextToSpeech;

import java.util.Locale;

public class speaking extends FragmentActivity
{
    TextToSpeech textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
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
    public void speakOut(String str)
    {
        textToSpeech.speak(str,TextToSpeech.QUEUE_FLUSH,null);
    }
}
