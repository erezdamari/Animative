package com.example.erezd.animative.utilities.animation;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.View;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.erezd.animative.R;

import java.io.IOException;

import static android.app.Activity.RESULT_OK;

/**
 * Created by aviva on 06/05/2018.
 */

public class AnimeRecorder {


    private static final String TAG = "AnimeRecorder";
    private int mScreenDensity;
    private MediaProjectionManager mProjectionManager;
    private static final int DISPLAY_WIDTH = 720;
    private static final int DISPLAY_HEIGHT = 1280;
    private MediaProjection mMediaProjection;
    private VirtualDisplay mVirtualDisplay;
    private MediaProjectionCallback mMediaProjectionCallback;
    private ToggleButton mToggleButton;
    private MediaRecorder mMediaRecorder;
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    public static final int REQUEST_PERMISSIONS = 10;
    public static final int REQUEST_CODE = 1000;
    private final Activity activity;

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    public AnimeRecorder(Activity mainActivity) {
        activity = mainActivity;

        DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        mScreenDensity = metrics.densityDpi;

        mMediaRecorder = new MediaRecorder();

        mProjectionManager = (MediaProjectionManager) activity.getSystemService(Context.MEDIA_PROJECTION_SERVICE);

        mToggleButton = (ToggleButton) activity.findViewById(R.id.record);
        mToggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (ContextCompat.checkSelfPermission(activity,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                    if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) ) {
                        mToggleButton.setChecked(false);
                        Snackbar.make(activity.findViewById(android.R.id.content), "permit recording",
                                Snackbar.LENGTH_INDEFINITE).setAction("ENABLE",
                                new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        ActivityCompat.requestPermissions(activity,
                                                new String[]{Manifest.permission
                                                        .WRITE_EXTERNAL_STORAGE},
                                                REQUEST_PERMISSIONS);
                                    }
                                }).show();
                    } else {
                        ActivityCompat.requestPermissions(activity,
                                new String[]{Manifest.permission
                                        .WRITE_EXTERNAL_STORAGE},
                                REQUEST_PERMISSIONS);
                    }
                } else {
                    onToggleScreenShare(v);
                }
            }
        });
    }



    public void onToggleScreenShare(View view) {
        if (((ToggleButton) view).isChecked()) {
            initRecorder();
            shareScreen();
        } else {
            mMediaRecorder.stop();
            mMediaRecorder.reset();
            Log.v(TAG, "Stopping Recording");
            stopScreenSharing();
        }
    }

    private void shareScreen() {
        if (mMediaProjection == null) {
            activity.startActivityForResult(mProjectionManager.createScreenCaptureIntent(), REQUEST_CODE);
            return;
        }
        mVirtualDisplay = createVirtualDisplay();
        mMediaRecorder.start();
        Log.d(TAG, "started recording");
    }


    private VirtualDisplay createVirtualDisplay() {
        return mMediaProjection.createVirtualDisplay("MainActivity",
                DISPLAY_WIDTH, DISPLAY_HEIGHT, mScreenDensity,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mMediaRecorder.getSurface(), null /*Callbacks*/, null
                /*Handler*/);
    }


    private void initRecorder() {
        try {
            mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mMediaRecorder.setOutputFile(Environment
                    .getExternalStoragePublicDirectory(Environment
                            .DIRECTORY_DOWNLOADS) + "/draw_animation.mp4");
            mMediaRecorder.setVideoSize(DISPLAY_WIDTH, DISPLAY_HEIGHT);
            mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
            mMediaRecorder.setVideoEncodingBitRate(512 * 1000);
            mMediaRecorder.setVideoFrameRate(30);
            int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
            int orientation = ORIENTATIONS.get(rotation);// + 90);
            mMediaRecorder.setOrientationHint(orientation);
            mMediaRecorder.prepare();
            Log.d(TAG, Environment
                    .getExternalStoragePublicDirectory(Environment
                            .DIRECTORY_DOWNLOADS) + "/draw_animation.mp4");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class MediaProjectionCallback extends MediaProjection.Callback {
        @Override
        public void onStop() {
            if (mToggleButton.isChecked()) {
                mToggleButton.setChecked(false);
                mMediaRecorder.stop();
                mMediaRecorder.reset();
                Log.v(TAG, "Recording Stopped");
            }
            mMediaProjection = null;
            stopScreenSharing();
        }
    }

    private void stopScreenSharing() {
        if (mVirtualDisplay == null) {
            return;
        }
        mVirtualDisplay.release();
        //mMediaRecorder.release(); //If used: mMediaRecorder object cannot
        // be reused again
        destroyMediaProjection();
    }


    public void destroyMediaProjection() {
        if (mMediaProjection != null) {
            mMediaProjection.unregisterCallback(mMediaProjectionCallback);
            mMediaProjection.stop();
            mMediaProjection = null;
        }
        Log.i(TAG, "MediaProjection Stopped");
    }


    public void startAfterResult(int resultCode, Intent data){

        mMediaProjectionCallback = new AnimeRecorder.MediaProjectionCallback();
        mMediaProjection = mProjectionManager.getMediaProjection(resultCode, data);
        mMediaProjection.registerCallback(mMediaProjectionCallback, null);
        mVirtualDisplay = createVirtualDisplay();
        mMediaRecorder.start();
    }
}

