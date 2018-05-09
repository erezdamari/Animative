package com.example.erezd.animative.utilities.tasks;

import android.graphics.Bitmap;
import android.util.Log;

import com.example.erezd.animative.utilities.serialization.Stroke;
import com.wacom.ink.geometry.WRect;
import com.wacom.ink.rasterization.InkCanvas;
import com.wacom.ink.rasterization.Layer;
import com.wacom.ink.rasterization.StrokeRenderer;

import java.io.File;
import java.io.FileOutputStream;

/**
 * Created by aviv_ams on 04/05/2018.
 */

public class SavingTask implements Runnable {

    public final static int START_TASK = 123;
    public final static int LAST_TASK = 124;
    public final static int TASK_SAVE_COMPLETE = 125;
    public final static int ALL_SAVE_TASK_COMPLETE = 126;

    public static int count = 0;
    private String path;
    private InkCanvas canvas;
    private Layer layer;
    private StrokeRenderer renderer;
    private Bitmap frame;
    private Stroke updatedPoints;
    private Stroke targetToUpdate;


    public SavingTask(String path, InkCanvas canvas, Layer layer, StrokeRenderer renderer, Stroke updatedPoints, Stroke targetToUpdate) {
        this.path = path;
        this.canvas = canvas;
        this.layer = layer;
        this.renderer = renderer;
        this.updatedPoints = updatedPoints;
        this.targetToUpdate = targetToUpdate;
    }

    public static void incrementNum(){
        count++;
    }

    @Override
    public void run() {

        String file_path = path;
        //"/PhysicsSketchpad";
        File dir = new File(file_path);
        if (!dir.exists())
            dir.mkdirs();
        try {

            File file = new File(dir, "sketchpad" + count + ".png");
            FileOutputStream outStream = new FileOutputStream(file);
            // outStream = new FileOutputStream(file);
            frame.compress(Bitmap.CompressFormat.PNG, 100, outStream);
            outStream.flush();
            outStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d("savingFrameOrder", "saved frame number" + count);
        Log.d("fileFrame", "" + file_path);
        frame.recycle();
        frame = null;
        count++;
    }

   public void setFrame(Bitmap frame){
        this.frame = frame;
   }

    public InkCanvas getCanvas() {
        return canvas;
    }

    public Layer getLayer() {
        return layer;
    }

    public StrokeRenderer getRenderer() {
        return renderer;
    }

    public Bitmap getFrame() {
        return frame;
    }

    public Stroke getTargetToUpdate(){
       return targetToUpdate;
    }
    public Stroke getUpdatedPoints(){
        return updatedPoints;
    }

   /*  public void onPreExec(){
        //imageScaled=getResizedBitmap(b, m_ViewLayer.getWidth()/3);
        WRect rect = m_StrokeRenderer.getStrokeUpdatedArea();
        Log.d("xyrect", rect.getX() + " "+rect.getY());
        imageScaled = Bitmap.createBitmap(m_ViewLayer.getWidth(), m_ViewLayer.getHeight(), Bitmap.Config.ARGB_8888);
        m_Canvas.readPixels(m_ViewLayer, imageScaled, rect.getX(),rect.getY(), rect.getX(),rect.getY(), rect.getWidth(), rect.getHeight());


    }*/
}
