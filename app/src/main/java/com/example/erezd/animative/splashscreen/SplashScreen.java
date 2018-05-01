package com.example.erezd.animative.splashscreen;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.erezd.animative.R;
import com.example.erezd.animative.activities.MainActivity;

public class SplashScreen extends AppCompatActivity {

    private final int SPLASH_DELAY = 1000;
    private final Handler mHandler = new Handler();
    private final Launcher mLauncher = new Launcher();
    private boolean visible = true, forceStart=false;

    @Override
    protected void onStart() {
        super.onStart();

        mHandler.postDelayed(mLauncher, SPLASH_DELAY);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_splash_screen);
    }

    @Override
    protected void onPause() {
        visible = false;
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        visible = true;
        //checks if we returned from background and the handler timer has finished
        if(forceStart){
            launch();
        }
    }

    private void launch(){
        if(!isFinishing()){
            //checks if the app on the foreground
            if(visible) {
                startActivity(new Intent(this, MainActivity.class));
                overridePendingTransition(R.anim.splashscreen_fade_in, R.anim.splashscreen_fade_out);
                finish();
            }
            else{
                //here the app is in the background but the handler finished
                forceStart = true;
            }
        }
    }

    @Override
    protected void onStop() {
        mHandler.removeCallbacks(mLauncher);
        super.onStop();
    }

    private class Launcher implements Runnable{
        @Override
        public void run() {
            launch();
        }
    }
}
