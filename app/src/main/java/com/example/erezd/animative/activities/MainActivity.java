package com.example.erezd.animative.activities;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;

import com.example.erezd.animative.R;
import com.example.erezd.animative.utilities.serialization.Stroke;
import com.example.erezd.animative.utilities.serialization.StrokeSerializer;
import com.wacom.ink.boundary.BoundaryBuilder;
import com.wacom.ink.path.PathBuilder.PropertyFunction;
import com.wacom.ink.path.PathBuilder.PropertyName;
import com.wacom.ink.path.PathUtils;
import com.wacom.ink.path.SpeedPathBuilder;
import com.wacom.ink.rasterization.BlendMode;
import com.wacom.ink.rasterization.InkCanvas;
import com.wacom.ink.rasterization.Layer;
import com.wacom.ink.rasterization.ParticleBrush;
import com.wacom.ink.rasterization.SolidColorBrush;
import com.wacom.ink.rasterization.StrokePaint;
import com.wacom.ink.rasterization.StrokeRenderer;
import com.wacom.ink.rendering.EGLRenderingContext;
import com.wacom.ink.smooth.MultiChannelSmoothener;

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
    private ArrayList<FloatBuffer> paths=null;
    private BoundaryBuilder boundaryBuilder;
    private ArrayList<Path> boundaryPaths;
    private BoundaryView boundaryView;

    //a list of strokes to save
    private LinkedList<Stroke> strokesList = new LinkedList<Stroke>();
    //the encode/decode instance
    private StrokeSerializer serializer;

    void drawthem(){
        FloatBuffer path = paths.get(0);

        //path.flip();

        MultiChannelSmoothener.SmoothingResult smoothingResult = m_Smoothener.smooth(path, path.array().length, true);
        // Add the smoothed control points to the path builder.
        m_PathBuilder.addPathPart(smoothingResult.getSmoothedPoints(), smoothingResult.getSize());
        m_StrokeRenderer.drawPoints(m_PathBuilder.getPathBuffer(), m_PathBuilder.getPathLastUpdatePosition(), m_PathBuilder.getAddedPointsSize(),true);
        m_Canvas.setTarget(m_CurrentFrameLayer, m_StrokeRenderer.getStrokeUpdatedArea());
        m_Canvas.clearColor(Color.WHITE);
        m_Canvas.drawLayer(m_StrokesLayer, BlendMode.BLENDMODE_NORMAL);
        m_StrokeRenderer.blendStrokeUpdatedArea(m_CurrentFrameLayer, BlendMode.BLENDMODE_NORMAL);
      //  renderView();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        //outState.putSerializable("paths", boundaryPaths);
        if(strokesList != null && strokesList.size() > 0)
            serializer.serialize(Uri.fromFile(getFileStreamPath(getString(R.string.FILE_SAVE_NAME))), strokesList);
        super.onSaveInstanceState(outState);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(serializer == null)
            serializer = new StrokeSerializer();

        //saves previous paths
        if(savedInstanceState!=null && !savedInstanceState.isEmpty()) {
            Object tmp = savedInstanceState.getSerializable("paths");
            try {
                boundaryPaths = (ArrayList<Path>) tmp;
            }catch(Exception e){e.printStackTrace();}
        }

        createPathBuilder();

        if(paths == null)
            paths = new ArrayList<>();

        SV = findViewById(R.id.surfaceView);

        SV.getHolder().addCallback(new SurfaceHolder.Callback(){
            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                if (m_Canvas != null && !m_Canvas.isDisposed()){
                    releaseResources();
                }

                m_Canvas = InkCanvas.create(holder, new EGLRenderingContext.EGLConfiguration());


                m_ViewLayer = m_Canvas.createViewLayer(width, height);//everything in this layer is drawn to screen (it is the target layer)
                m_StrokesLayer = m_Canvas.createLayer(width, height);//this layer draws all strokes
                m_CurrentFrameLayer = m_Canvas.createLayer(width, height);//this layer contains all the drawings on screen.
                //to add more drawings, we add them to this layer so on renderView() will update it.

                m_Canvas.clearLayer(m_CurrentFrameLayer, Color.WHITE);

                m_SolidBrush = new SolidColorBrush();
                createStrokePaint();

                m_Smoothener = new MultiChannelSmoothener(m_PathStride);
                m_Smoothener.enableChannel(2);

                m_StrokeRenderer = new StrokeRenderer(m_Canvas, m_Paint, m_PathStride, width, height);

               /* if(paths != null && paths.size()>0){
                    drawthem();
                }
*/
               //USE A THREAD HERE
                strokesList = serializer.deserialize(Uri.fromFile(getFileStreamPath(getString(R.string.FILE_SAVE_NAME))));

                drawStrokes(strokesList);
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
                if(m_IsButtonClicked) {
                    boolean bFinished = buildPath(event);
                    drawStroke(event);
                    renderView();

                    if (bFinished){
                        Stroke stroke = new Stroke();
                        stroke.copyPoints(m_PathBuilder.getPathBuffer(), 0, m_PathBuilder.getPathSize());
                        stroke.setStride(m_PathBuilder.getStride());
                        stroke.setWidth(Float.NaN);
                        stroke.setColor(m_Paint.getColor());
                        stroke.setInterval(0.0f, 1.0f);
                        stroke.setBlendMode(BlendMode.BLENDMODE_NORMAL);

                        strokesList.add(stroke);

                        Toast.makeText(MainActivity.this, "We have " + strokesList.size()
                                + " strokes in the list.", Toast.LENGTH_SHORT).show();
                    }

                    return true;
                }

                return false;
            }

        });




    /*   if(boundaryBuilder == null) {
            boundaryBuilder = new BoundaryBuilder();
            if (boundaryPaths == null) {
                boundaryPaths = new ArrayList<Path>();
            }
        }
        boundaryView = new BoundaryView(this);
        ((RelativeLayout)findViewById(R.id.contentview)).addView(boundaryView,
                new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.MATCH_PARENT,
                        RelativeLayout.LayoutParams.MATCH_PARENT));
*/

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
        // Add the current input point to the path builder.
        FloatBuffer part = m_PathBuilder.addPoint(phase, event.getX(), event.getY(), event.getEventTime());

        MultiChannelSmoothener.SmoothingResult smoothingResult;

        int partSize = m_PathBuilder.getPathPartSize();
        if (partSize > 0) {
            // Smooth the returned control points (aka partial path).
            smoothingResult = m_Smoothener.smooth(part, partSize, (phase == PathUtils.Phase.END));
            // Add the smoothed control points to the path builder.
            m_PathBuilder.addPathPart(smoothingResult.getSmoothedPoints(), smoothingResult.getSize());
        }

      /*  if(event.getAction() == MotionEvent.ACTION_UP) {
            /*if(partSize>0) {
                FloatBuffer t = m_PathBuilder.getPathBuffer();
                if (t != null) {

                    float[] f = copyfloats(t, m_PathBuilder.getPathSize());
                    FloatBuffer tmp = FloatBuffer.allocate(f.length);
                    tmp.put(f.clone());
                    paths.add(tmp);
                } else
                    Log.d("bla", "shit happens1");
            }else Log.d("bla", "shit happens2");
            */
           /* boundaryBuilder.addPath(m_PathBuilder.getPathBuffer(), m_PathBuilder.getPathSize(), m_PathBuilder.getStride());

            Boundary boundary = boundaryBuilder.getBoundary();
            boundaryPaths.add(boundary.createPath());
            boundaryView.invalidate();

        }*/
        // Create a preliminary path.
        FloatBuffer preliminaryPath = m_PathBuilder.createPreliminaryPath();
        // Smoothen the preliminary path's control points (return inform of a partial path).
        smoothingResult = m_Smoothener.smooth(preliminaryPath, m_PathBuilder.getPreliminaryPathSize(), true);
        // Add the smoothed preliminary path to the path builder.
        m_PathBuilder.finishPreliminaryPath(smoothingResult.getSmoothedPoints(), smoothingResult.getSize());

        return (event.getAction()==MotionEvent.ACTION_UP && m_PathBuilder.hasFinished());
    }


    float[] copyfloats(FloatBuffer fs, int size){
        float[] tmp = new float[size];
        for (int i = 0; i < size; i++) {
            tmp[i] = fs.get(i);
        }
        return tmp;
    }

    //draw a stroke from onTouch
    private void drawStroke(MotionEvent event) {
        m_StrokeRenderer.drawPoints(m_PathBuilder.getPathBuffer(), m_PathBuilder.getPathLastUpdatePosition(), m_PathBuilder.getAddedPointsSize(), event.getAction()==MotionEvent.ACTION_UP);
        m_StrokeRenderer.drawPrelimPoints(m_PathBuilder.getPreliminaryPathBuffer(), 0, m_PathBuilder.getFinishedPreliminaryPathSize());

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

    //draw the strokes from the list from surfaceChanged
    private void drawStrokes(LinkedList<Stroke> strokesList) {
        m_Canvas.setTarget(m_StrokesLayer);
        m_Canvas.clearColor();

        for (Stroke stroke: strokesList){
            m_Paint.setColor(stroke.getColor());
            m_StrokeRenderer.setStrokePaint(m_Paint);
            m_StrokeRenderer.drawPoints(stroke.getPoints(), 0, stroke.getSize(),
                    stroke.getStartValue(), stroke.getEndValue(), true);
            m_StrokeRenderer.blendStroke(m_StrokesLayer, BlendMode.BLENDMODE_NORMAL);
        }

        m_Canvas.setTarget(m_CurrentFrameLayer);
        m_Canvas.clearColor(Color.WHITE);
        m_Canvas.drawLayer(m_StrokesLayer, BlendMode.BLENDMODE_NORMAL);
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
        m_PathBuilder = new SpeedPathBuilder();
        m_PathBuilder.setNormalizationConfig(100.0f, 4000.0f);
        m_PathBuilder.setMovementThreshold(2.0f);
        //the PropertyName.Width means that each point on the path is defined by a set of x,y,width
        //so each point has three values
        m_PathBuilder.setPropertyConfig(PropertyName.Width, 10f, 40f, Float.NaN, Float.NaN, PropertyFunction.Power, 1.0f, false);
        m_PathStride  = m_PathBuilder.getStride(); //how much of values defines each point
    }

    /**
     * specifies how to draw each stroke
     */
    private void createStrokePaint()
    {
        m_Paint = new StrokePaint();
        m_Paint.setStrokeBrush(m_SolidBrush);
        m_Paint.setColor(Color.BLUE);// Particle brush.
        m_Paint.setWidth(Float.NaN);//draw it with width
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

          /*  for(Path path : boundaryPaths) {
                    canvas.drawPath(path, paint);
            }*/
        }
    }
}
