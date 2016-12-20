package com.hack3r77.ticfit;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        TextView textView = (TextView) findViewById(R.id.text1);
        textView.setText("Welcome to the prototype app!\n" +
                "Test watch with PHONE GPS:\n" +
                "1. Connect phoine and watch\n" +
                "2. turn on phone's GPS\n" +
                "3. Start watch app and verify that gps location is shown \n" +
                "\n" +
                "Test watch with WATCH GPS:\n" +
                "1. DISconnect phone and watch\n" +
                "2. Start watch app and verify that gps location is shown\n" +
                "3. It will turn from 'Loading' to displaying gps location\n");
    }
}
