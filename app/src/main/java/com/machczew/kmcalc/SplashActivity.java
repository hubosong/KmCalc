package com.machczew.kmcalc;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        startActivity(new Intent(getBaseContext(), MainActivity.class));
        finish();



        /*
        Thread th = new Thread() {
            @Override
            public void run() {
                try {
                    sleep(135);
                } catch (Exception e) {

                } finally {
                    startActivity(new Intent(getBaseContext(), MainActivity.class));
                    //overridePendingTransition(0, 0);
                    //finish();
                }
            }
        };
        th.start();

         */

    }
}
