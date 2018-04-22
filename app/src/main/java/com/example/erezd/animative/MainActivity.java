package com.example.erezd.animative;

import android.content.res.Resources;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.Button;

import com.wacom.ink.path.PathBuilder;
import com.wacom.ink.path.PathUtils;
import com.wacom.ink.path.SpeedPathBuilder;
import com.wacom.ink.rasterization.BlendMode;
import com.wacom.ink.rasterization.InkCanvas;
import com.wacom.ink.rasterization.Layer;
import com.wacom.ink.rasterization.SolidColorBrush;
import com.wacom.ink.rasterization.StrokePaint;
import com.wacom.ink.rasterization.StrokeRenderer;
import com.wacom.ink.rendering.EGLRenderingContext;
import com.wacom.ink.smooth.MultiChannelSmoothener;

import java.nio.FloatBuffer;

public class MainActivity extends AppCompatActivity {

    private SurfaceView SV;
    private InkCanvas m_Canvas;
    private Layer m_ViewLayer, m_StrokesLayer, m_CurrentFrameLayer;
    private SpeedPathBuilder m_PathBuilder;
    private int m_PathStride;
    private SolidColorBrush m_Brush;
    private StrokePaint m_Paint;
    private StrokeRenderer m_StrokeRenderer;
    private Button m_DrawButton, m_CopyButton;
    private boolean m_IsButtonClicked = false;
    private MultiChannelSmoothener m_Smoothener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        createPathBuilder();
        SV = findViewById(R.id.surfaceView);
        m_CopyButton = findViewById(R.id.buttonCopy);
        m_DrawButton = findViewById(R.id.buttonDraw1);

        SV.getHolder().addCallback(new SurfaceHolder.Callback(){
            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                if (m_Canvas!=null && !m_Canvas.isDisposed()){
                    releaseResources();
                }

                m_Canvas = InkCanvas.create(holder, new EGLRenderingContext.EGLConfiguration());

                m_ViewLayer = m_Canvas.createViewLayer(width, height);
                m_StrokesLayer = m_Canvas.createLayer(width, height);
                m_CurrentFrameLayer = m_Canvas.createLayer(width, height);
                m_Canvas.clearLayer(m_CurrentFrameLayer, Color.WHITE);

                m_Brush = new SolidColorBrush();
                createStrokePaint();

                m_Smoothener = new MultiChannelSmoothener(m_PathStride);
                m_Smoothener.enableChannel(2);

                m_StrokeRenderer = new StrokeRenderer(m_Canvas, m_Paint, m_PathStride, width, height);

                renderView();
            }

            @Override
            public void surfaceCreated(SurfaceHolder holder) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                releaseResources();

            }

        });

        m_DrawButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                buttonDrawClicked();
            }
        });

        m_CopyButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                buttonCopyClicked();
            }
        });

        SV.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(m_IsButtonClicked) {
                    buildPath(event);
                    drawStroke(event);
                    renderView();
                    return true;
                }

                return false;
            }
        });
    }

    private void buttonDrawClicked()
    {
        if(!m_IsButtonClicked)
        {
            m_IsButtonClicked = true;
            m_DrawButton.setBackgroundColor(Color.RED);
        }
        else
        {
            m_IsButtonClicked = false;
            m_DrawButton.setBackgroundColor(Color.BLUE);
        }
    }

    private void buttonCopyClicked()
    {
        FloatBuffer buffer = m_PathBuilder.getPathBuffer();
    }

    private void buildPath(MotionEvent event)
    {
        if(event.getAction() != MotionEvent.ACTION_DOWN &&
           event.getAction() != MotionEvent.ACTION_MOVE &&
           event.getAction() != MotionEvent.ACTION_UP)
        {
            return;
        }

        if (event.getAction() == MotionEvent.ACTION_DOWN){
            // Reset the smoothener instance when starting to generate a new path.
            m_Smoothener.reset();
        }

        PathUtils.Phase phase = PathUtils.getPhaseFromMotionEvent(event);
        FloatBuffer part = m_PathBuilder.addPoint(phase, event.getX(), event.getY(), event.getEventTime());
        MultiChannelSmoothener.SmoothingResult smoothingResult;
        int partSize = m_PathBuilder.getPathPartSize();
        if(partSize > 0)
        {
            smoothingResult = m_Smoothener.smooth(part, partSize, (phase == PathUtils.Phase.END));
            // Add the smoothed control points to the path builder.
            m_PathBuilder.addPathPart(smoothingResult.getSmoothedPoints(), smoothingResult.getSize());
        }
        // Create a preliminary path.
        FloatBuffer preliminaryPath = m_PathBuilder.createPreliminaryPath();
        // Smoothen the preliminary path's control points (return inform of a partial path).
        smoothingResult = m_Smoothener.smooth(preliminaryPath, m_PathBuilder.getPreliminaryPathSize(), true);
        // Add the smoothed preliminary path to the path builder.
        m_PathBuilder.finishPreliminaryPath(smoothingResult.getSmoothedPoints(), smoothingResult.getSize());
    }

    private void drawStroke(MotionEvent event) {
        m_StrokeRenderer.drawPoints(
                m_PathBuilder.getPathBuffer(),
                m_PathBuilder.getPathLastUpdatePosition(),
                m_PathBuilder.getAddedPointsSize(),
                event.getAction() == MotionEvent.ACTION_UP);

        m_StrokeRenderer.drawPrelimPoints(m_PathBuilder.getPreliminaryPathBuffer(), 0, m_PathBuilder.getFinishedPreliminaryPathSize());
        //hello!!
        if (event.getAction() != MotionEvent.ACTION_UP) {
            m_Canvas.setTarget(m_CurrentFrameLayer, m_StrokeRenderer.getStrokeUpdatedArea());
            m_Canvas.clearColor(Color.WHITE);
            m_Canvas.drawLayer(m_StrokesLayer, BlendMode.BLENDMODE_NORMAL);
            m_StrokeRenderer.blendStrokeUpdatedArea(m_CurrentFrameLayer, BlendMode.BLENDMODE_NORMAL);
        } else {
            m_StrokeRenderer.blendStroke(m_StrokesLayer, BlendMode.BLENDMODE_NORMAL);
            m_Canvas.setTarget(m_CurrentFrameLayer);
            m_Canvas.clearColor(Color.WHITE);
            m_Canvas.drawLayer(m_StrokesLayer, BlendMode.BLENDMODE_NORMAL);

        }
    }

    private void renderView() {
        m_Canvas.setTarget(m_ViewLayer);
        m_Canvas.drawLayer(m_CurrentFrameLayer, BlendMode.BLENDMODE_OVERWRITE);
        m_Canvas.invalidate();
    }

    private void releaseResources(){
        m_StrokeRenderer.dispose();
        m_Canvas.dispose();
    }

    /*private void renderView() {
        m_Canvas.setTarget(m_ViewLayer);
        m_Canvas.clearColor(Color.RED);
        m_Canvas.invalidate();
    }*/

    private void createPathBuilder()
    {
        m_PathBuilder = new SpeedPathBuilder(getResources().getDisplayMetrics().density);
        m_PathBuilder.setNormalizationConfig(100.0f, 4000.0f);
        m_PathBuilder.setPropertyConfig(PathBuilder.PropertyName.Width, 5f, 10f, 5f, 10f, PathBuilder.PropertyFunction.Power, 1.0f, false);
        m_PathStride  = m_PathBuilder.getStride();
    }

    private void createStrokePaint()
    {
        m_Paint = new StrokePaint();
        m_Paint.setStrokeBrush(m_Brush);    // Solid color brush.
        m_Paint.setColor(Color.BLUE);     // Blue color.
        m_Paint.setWidth(Float.NaN);
    }
}
