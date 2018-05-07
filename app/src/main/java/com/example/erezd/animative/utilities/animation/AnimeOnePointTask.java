package com.example.erezd.animative.utilities.animation;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.view.SurfaceView;

import com.example.erezd.animative.activities.MainActivity;
import com.example.erezd.animative.utilities.serialization.Stroke;

import java.nio.FloatBuffer;
import java.util.ArrayList;

/**
 * Created by avivam on 01/05/2018.
 */

public class AnimeOnePointTask extends AsyncTask<Stroke, Boolean, Void> {

        private Stroke startStroke, targetStroke;
        private float xStart, yStart;
        private int speed = 2;
        private FloatBuffer startPoints;
        private AnimeResultListener activity;
        private Stroke updatedPoints;
        private SurfaceView surfaceView;
        private ArrayList<Bitmap> images;

        public AnimeOnePointTask(AnimeResultListener activity, int speed){
            this.activity = activity;
            this.speed = speed;
            images = new ArrayList<>();

        }

        @Override
        protected Void doInBackground(Stroke... strokes) {


            startStroke = strokes[0];
            targetStroke = strokes[1];
            startPoints = startStroke.getPoints().duplicate();
            xStart = startPoints.get(0);
            yStart = startPoints.get(1);

            float distanceEnd = PointCalc.Distance(xStart, yStart, targetStroke.getPoints().get(0), targetStroke.getPoints().get(1));
            PointCalc direction = PointCalc.Direction(targetStroke.getPoints().get(0) - xStart, targetStroke.getPoints().get(1) - yStart);

            boolean hasNotReachedTarget = PointCalc.Distance(xStart, yStart, startPoints.get(0), startPoints.get(1)) < distanceEnd-1;
            int stepsToRender = 2;

           while(hasNotReachedTarget) {
               int i=1, jump = startStroke.getStride();
               //update values by direction
               int d = startPoints.capacity();

               while(i < startPoints.capacity()){
                   startPoints.put(i-1, startPoints.get(i-1) + (direction.x*speed));
                   startPoints.put(i, startPoints.get(i) + (direction.y*speed));

                   Log.d("loopStroke", i-1 + "/" + d + "and " + i + "/" + d);
                   i+=jump;
               }
               Log.d("loopStroke", ""+startPoints.position());

                stepsToRender--;
                try {
                    //to be able to delete this stroke
                    startStroke.calculateBounds();

                    hasNotReachedTarget = PointCalc.Distance(xStart, yStart, startPoints.get(0), startPoints.get(1)) < distanceEnd-1;

                    if(stepsToRender == 0) {
                        updatedPoints = new Stroke(startStroke);
                        Log.d("showHasNotReachedTarget", ""+!hasNotReachedTarget);
                        publishProgress(!hasNotReachedTarget);
                        stepsToRender = 2;
                    }
                    Thread.sleep(40);//tested 70

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }catch (Exception e){
                    e.printStackTrace();
                }

            }

            //if we didnt render the last point in target
            //could happen if stepsToRender was less than 2 in last point
            if(stepsToRender != 2) {
                publishProgress(true);
            }

            return null;
        }


        @Override
        protected void onProgressUpdate(Boolean... values) {
            if(MainActivity.IsVisible()) {
               // Log.d("loopStroke", "onProgressUpdate");
               // activity.RenderOnAnime();

               // images.add(activity.getBitmap());
                if(values.length > 0) {
                    Log.d("showHasNotReachedTarget", "onProgressUpdate "+values[0]);
                    activity.saveBitmap(updatedPoints, startStroke, values[0]);
                }
            }
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(Void aVoid) {

          //  activity.RenderOnAnime();
            //activity.AnimeEnded(images);
            activity.AnimeEnded();
            super.onPostExecute(aVoid);
        }

        public interface AnimeResultListener{
            void RenderOnAnime();
          //  void AnimeEnded(ArrayList<Bitmap> images);
            void AnimeEnded();
            void saveBitmap(Stroke copy, Stroke target, boolean isLastFrame);
        }


   /* private File savebitmap(String filename) {


        String file_path = path;//  +
                //"/PhysicsSketchpad";
        File dir = new File(file_path);
        if(!dir.exists())
            dir.mkdirs();
        try {

            File file = new File(dir, "sketchpad" +count + ".png");
            FileOutputStream outStream = new FileOutputStream(file);
           // outStream = new FileOutputStream(file);
            images[count].compress(Bitmap.CompressFormat.PNG, 100, outStream);
            outStream.flush();
            outStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.e("file", "" + path);
        return dir;

    }*/
}
