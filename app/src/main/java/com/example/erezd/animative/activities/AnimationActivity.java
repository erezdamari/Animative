package com.example.erezd.animative.activities;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.example.erezd.animative.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class AnimationActivity extends AppCompatActivity {

    private WebView m_WebView;
    private static ArrayList<Float> m_PathPoint;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_animation);

        m_WebView = findViewById(R.id.webViewAnimation);
        m_WebView.getSettings().setJavaScriptEnabled(true);


        m_WebView.setWebViewClient(new WebViewClient(){
            public void onPageFinished(WebView view, String url){
                m_PathPoint.set(0, (float)m_PathPoint.size());
                JSONArray jsArray = new JSONArray(m_PathPoint);
                JSONObject testObj = null;
                try {
                    testObj = new JSONObject ("test");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                m_WebView.loadUrl("javascript:CreatePointsArray('" + jsArray  + "')");

            }
        });
        m_WebView.loadUrl("file:///android_asset/animation.html");
    }

    public static void SetPoints(ArrayList<Float> i_List){
        m_PathPoint = i_List;
    }
}
