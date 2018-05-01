package com.example.erezd.animative.utilities;

import android.app.Activity;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.erezd.animative.activities.MainActivity;
import com.example.erezd.animative.utilities.serialization.Stroke;
import com.wacom.ink.utils.Utils;

import java.nio.FloatBuffer;

/**
 * Created by avivam on 01/05/2018.
 */

public class AnimeTask extends AsyncTask<Stroke, Void, Void> {

        private Stroke startStroke, targetStroke;
        float xStart, yStart;
        int speed = 2;
        FloatBuffer startPoints;
        AnimeResultListener activity;


        public AnimeTask(AnimeResultListener activity, int speed){
            this.activity = activity;
            this.speed = speed;
        }

        @Override
        protected Void doInBackground(Stroke... strokes) {

            startStroke = strokes[0];
            targetStroke = strokes[1];
            startPoints = startStroke.getPoints().duplicate();
            xStart = startPoints.get(0);
            yStart = startPoints.get(1);

            float distanceEnd = PointCalc.Distance(xStart, yStart, targetStroke.getPoints().get(0), targetStroke.getPoints().get(1));
            float[] direction = PointCalc.Direction(targetStroke.getPoints().get(0) - xStart, targetStroke.getPoints().get(1) - yStart);

            boolean hasReachedTarget = PointCalc.Distance(xStart, yStart, startPoints.get(0), startPoints.get(1)) < distanceEnd-1;
            int stepsToRender = 2;

           while(hasReachedTarget) {
               int i=1, jump = startStroke.getStride();
               //update values by direction
               int d = startPoints.capacity();

               while(i < startPoints.capacity()){
                   startPoints.put(i-1, startPoints.get(i-1) + (direction[0]*speed));
                   startPoints.put(i, startPoints.get(i) + (direction[1]*speed));

                   Log.d("loopStroke", i-1 + "/" + d + "and " + i + "/" + d);
                   i+=jump;
               }
               Log.d("loopStroke", ""+startPoints.position());

                stepsToRender--;
                try {
                    //to be able to delete this stroke
                    startStroke.calculateBounds();
                    if(stepsToRender == 0) {
                        publishProgress(null);
                        stepsToRender = 2;
                    }
                    Thread.sleep(40);//tested 70

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }catch (Exception e){
                    e.printStackTrace();
                }
                hasReachedTarget = PointCalc.Distance(xStart, yStart, startPoints.get(0), startPoints.get(1)) < distanceEnd-1;
            }

            //if we didnt render the last point in target
            if(stepsToRender != 2)
                publishProgress();

            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            if(MainActivity.IsVisible()) {
                Log.d("loopStroke", "onProgressUpdate");
                activity.RenderOnAnime();
            }
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(Void aVoid) {

          //  activity.RenderOnAnime();
            activity.AnimeEnded();
            super.onPostExecute(aVoid);
        }

        public interface AnimeResultListener{
            void RenderOnAnime();
            void AnimeEnded();
        }
}
