package com.example.erezd.animative.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.erezd.animative.R;
import com.example.erezd.animative.utilities.Stroke;
import com.wacom.ink.boundary.Boundary;
import com.wacom.ink.boundary.BoundaryBuilder;
import com.wacom.ink.path.PathBuilder.PropertyFunction;
import com.wacom.ink.path.PathBuilder.PropertyName;
import com.wacom.ink.path.PathUtils;
import com.wacom.ink.path.PathUtils.Phase;
import com.wacom.ink.path.SpeedPathBuilder;
import com.wacom.ink.rasterization.BlendMode;
import com.wacom.ink.rasterization.InkCanvas;
import com.wacom.ink.rasterization.Layer;
import com.wacom.ink.rasterization.ParticleBrush;
import com.wacom.ink.rasterization.SolidColorBrush;
import com.wacom.ink.rasterization.StrokePaint;
import com.wacom.ink.rasterization.StrokeRenderer;
import com.wacom.ink.rendering.EGLRenderingContext;
import com.wacom.ink.rendering.EGLRenderingContext.EGLConfiguration;
import com.wacom.ink.smooth.MultiChannelSmoothener;
import com.wacom.ink.smooth.MultiChannelSmoothener.SmoothingResult;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.LinkedList;

public class MainActivity extends AppCompatActivity {

    private SurfaceView SV;
    private InkCanvas m_Canvas;
    private Layer m_ViewLayer, m_StrokesLayer, m_CurrentFrameLayer;
    private SpeedPathBuilder m_PathBuilder;
    private int m_PathStride;
    private SolidColorBrush m_SolidBrush;
    private ParticleBrush m_ParticleBrush;
    private StrokePaint m_Paint;
    private StrokeRenderer m_StrokeRenderer;
    private boolean m_IsButtonClicked = false;
    private MultiChannelSmoothener m_Smoothener;

    private BoundaryBuilder boundaryBuilder;
    private ArrayList<Path> boundaryPaths;
    private BoundaryView boundaryView;
    private LinkedList<Stroke> m_Strokes = new LinkedList<>();

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        createPathBuilder();

        SV = findViewById(R.id.surfaceView);
        //m_CopyButton = findViewById(R.id.buttonCopy);

        SV.getHolder().addCallback(new SurfaceHolder.Callback(){
            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                if (m_Canvas != null && !m_Canvas.isDisposed()){
                    releaseResources();
                }

                m_Canvas = InkCanvas.create(holder, new EGLRenderingContext.EGLConfiguration());

                m_ViewLayer = m_Canvas.createViewLayer(width, height);
                m_StrokesLayer = m_Canvas.createLayer(width, height);
                m_CurrentFrameLayer = m_Canvas.createLayer(width, height);
                m_Canvas.clearLayer(m_CurrentFrameLayer, Color.WHITE);

                m_SolidBrush = new SolidColorBrush();
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

        SV.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                boolean returnedVal = false;

                if(m_IsButtonClicked) {
                    boolean isStrokeDrawingFinished = buildPath(event);
                    drawStroke(event);
                    renderView();

                    if(isStrokeDrawingFinished){
                        Stroke stroke = new Stroke();
                        stroke.CopyPoints(m_PathBuilder.getPathBuffer(), 0, m_PathBuilder.getPathSize());
                        stroke.SetStride(m_PathBuilder.getStride());
                        stroke.SetColor(m_Paint.getColor());
                        stroke.SetInterval(0.0f, 1.0f);
                        stroke.SetBlendMode(BlendMode.BLENDMODE_NORMAL);
                        m_Strokes.add(stroke);
                        Toast.makeText(MainActivity.this, "We have " + m_Strokes.size() + " strokes in the list.", Toast.LENGTH_SHORT).show();

                    }
                    returnedVal = true;
                }

                return returnedVal;
            }
        });

        boundaryBuilder = new BoundaryBuilder();
        boundaryPaths = new ArrayList<Path>();
        boundaryView = new BoundaryView(this);
        //((RelativeLayout)findViewById(R.id.contentview)).addView(boundaryView,
          //      new RelativeLayout.LayoutParams(
            //            RelativeLayout.LayoutParams.MATCH_PARENT,
              //          RelativeLayout.LayoutParams.MATCH_PARENT));

    }

    public void buttonDraw_OnClick(View view)
    {
        if(!m_IsButtonClicked)
        {
            m_IsButtonClicked = true;
            view.setBackgroundColor(Color.RED);
        }
        else
        {
            m_IsButtonClicked = false;
            view.setBackgroundColor(Color.BLUE);
        }
    }

    public void buttonCopy_OnClick(View view)
    {
        FloatBuffer buffer = m_PathBuilder.getPreliminaryPathBuffer();


    }

    private boolean buildPath(MotionEvent event) {
        if (event.getAction() != MotionEvent.ACTION_DOWN &&
                event.getAction() != MotionEvent.ACTION_MOVE &&
                event.getAction() != MotionEvent.ACTION_UP) {
            return false;
        }

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            // Reset the smoothener instance when starting to generate a new path.
            m_Smoothener.reset();
        }

        PathUtils.Phase phase = PathUtils.getPhaseFromMotionEvent(event);
        FloatBuffer part = m_PathBuilder.addPoint(phase, event.getX(), event.getY(), event.getEventTime());
        MultiChannelSmoothener.SmoothingResult smoothingResult;
        int partSize = m_PathBuilder.getPathPartSize();
        if (partSize > 0) {
            smoothingResult = m_Smoothener.smooth(part, partSize, (phase == PathUtils.Phase.END));
            // Add the smoothed control points to the path builder.
            m_PathBuilder.addPathPart(smoothingResult.getSmoothedPoints(), smoothingResult.getSize());
        }

        // Create a preliminary path.
        FloatBuffer preliminaryPath = m_PathBuilder.createPreliminaryPath();
        // Smoothen the preliminary path's control points (return inform of a path part).
        smoothingResult = m_Smoothener.smooth(preliminaryPath, m_PathBuilder.getPreliminaryPathSize(), true);
        // Add the smoothed preliminary path to the path builder.
        m_PathBuilder.finishPreliminaryPath(smoothingResult.getSmoothedPoints(), smoothingResult.getSize());


        return (event.getAction() == MotionEvent.ACTION_UP && m_PathBuilder.hasFinished());
    }

    private void drawStroke(MotionEvent event) {
        m_StrokeRenderer.drawPoints(m_PathBuilder.getPathBuffer(), m_PathBuilder.getPathLastUpdatePosition(), m_PathBuilder.getAddedPointsSize(), event.getAction()==MotionEvent.ACTION_UP);

        if (event.getAction()!=MotionEvent.ACTION_UP){
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
        m_PathBuilder.setMovementThreshold(2.0f);
        m_PathBuilder.setPropertyConfig(PropertyName.Width, 10f, 40f, Float.NaN, Float.NaN, PropertyFunction.Power, 1.0f, false);
        m_PathStride  = m_PathBuilder.getStride();
    }

    private void createStrokePaint()
    {
        m_Paint = new StrokePaint();
        m_Paint.setStrokeBrush(m_SolidBrush);
        m_Paint.setColor(Color.BLUE);// Particle brush.
        m_Paint.setWidth(Float.NaN);
    }

    private class BoundaryView extends View {
        private Paint paint;

        public BoundaryView(Context context) {
            super(context);

            paint = new Paint();
            paint.setAntiAlias(true);
            paint.setColor(Color.RED);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            for(Path path : boundaryPaths) {
                canvas.drawPath(path, paint);
            }
        }
    }
}
