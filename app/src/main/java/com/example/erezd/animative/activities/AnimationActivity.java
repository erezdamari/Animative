package com.example.erezd.animative.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebView;

import com.example.erezd.animative.R;

public class AnimationActivity extends AppCompatActivity {

    private WebView m_WebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_animation);

        m_WebView = findViewById(R.id.webViewAnimation);
        m_WebView.getSettings().setJavaScriptEnabled(true);
        m_WebView.loadUrl("file:///android_asset/animation.html");
    }
}
