package com.example.erezd.animative.utilities.animation;

import android.graphics.Bitmap;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.util.Log;
import android.view.SurfaceView;

import com.example.erezd.animative.activities.MainActivity;
import com.example.erezd.animative.utilities.serialization.Stroke;

import java.nio.FloatBuffer;
import java.util.LinkedList;

/**
 * Created by aviv_ams on 07/05/2018.
 */

public class AnimationPlay extends AsyncTask<LinkedList<Stroke>[], Boolean, Void> {

    private Stroke objectToMove, pathTarget;
    private RectF currentPoint;
    private int speed = 5;
    private FloatBuffer objectPoints;
    private AnimeOnePointTask.AnimeResultListener activity;
    private Stroke updatedPoints;


    public AnimationPlay(AnimeOnePointTask.AnimeResultListener activity, int speed){
        this.activity = activity;
        this.speed = speed;
    }


    @Override
    protected Void doInBackground(LinkedList<Stroke>[]... strokes) {

        Stroke[] objects = new Stroke[strokes[0].length];
        LinkedList<Stroke>[] paths = new LinkedList[objects.length];
        //init the list
        for (int i = 0; i < paths.length; i++) {
            paths[i] = new LinkedList<>();
        }

        for (int i = 0; i < strokes[0].length; i++) {
            //get the object
            objects[i] = strokes[0][i].poll();
            //then add its paths to its paths list
            for(Stroke path: strokes[0][i]) {
                paths[i].add(path);
            }
        }

        //check if every object has paths, if not then there is an error
        for(LinkedList<Stroke>list : paths){
            if(list.isEmpty())
                return null; //each object should have an array of paths
        }

        /*
        objectToMove = strokes[0][0];
        pathTarget = strokes[1][0];
*/
        PointCalc startPoint = new PointCalc(0,0),  direction = new PointCalc(0,0);
        float distanceEnd;
        int stepsToRender = speed < 6 ? 2 : 1;

       //for each object in objects
        for (int indexObj = 0; indexObj < objects.length && !isCancelled(); indexObj++) {
            objectToMove = objects[indexObj]; //in object array, there is only one object per array
            objectPoints = objectToMove.getPoints().duplicate();
            currentPoint = objectToMove.getBounds();

            //for each object there is an array of paths to move onto
            for(Stroke path : paths[indexObj]){
                pathTarget = path;
                int jump = pathTarget.getStride();

                //each point on the path should be a target. we are moving 2 points each time
                for(int indexPoint = 0; indexPoint < pathTarget.getSize() && !isCancelled(); indexPoint+=jump*2) {

                    //initialize new target, new start point and direction for each new target point in the path
                    distanceEnd = initPointsAndDirection(indexPoint, startPoint, direction);
                    boolean hasNotReachedTarget = PointCalc.Distance(currentPoint.centerX(), currentPoint.centerY(), startPoint.x, startPoint.y) < distanceEnd;//distanceEnd-1;

                    //move to target point
                    while (hasNotReachedTarget) {

                        moveToTarget(objectPoints, direction);

                        Log.d("loopStroke", "" + objectPoints.position());//for debug location

                        stepsToRender--;
                        try {
                            //to be able to delete this stroke and calc the new distance
                            objectToMove.calculateBounds();

                            //the current distance from where we begun and where we are now. check if it is like the distance to the end.
                            hasNotReachedTarget = PointCalc.Distance(startPoint.x, startPoint.y, currentPoint.centerX(), currentPoint.centerY()) < distanceEnd;//distanceEnd-1;

                            if (stepsToRender == 0) {
                                // updatedPoints = new Stroke(startStroke);
                                Log.d("showHasNotReachedTarget", "" + !hasNotReachedTarget);
                                publishProgress(!hasNotReachedTarget);

                                stepsToRender = speed < 6 ? 2 : 1;
                            }
                            //for fps use
                            Thread.sleep(40);//tested 70

                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        //if the thread is canceled
                        if(isCancelled())
                            break;
                    }

                    //if we didn't render the last point in target
                    //could happen if stepsToRender was less than 2 in last point
                    if (stepsToRender != (speed < 6 ? 2 : 1)) {
                        publishProgress(true);
                    }
                }
            }
        }

        return null;
    }


    @Override
    protected void onProgressUpdate(Boolean... values) {
        if(MainActivity.IsVisible()) {
            // Log.d("loopStroke", "onProgressUpdate");
             activity.RenderOnAnime();

            // images.add(activity.getBitmap());

            //saves the frame
           /* if(values.length > 0) {
                activity.saveBitmap(updatedPoints, startStroke, values[0]);
            }*/
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


    /**
     * move the object to the target point via direction
     */
    private void moveToTarget(FloatBuffer object, PointCalc direction){
        int i=1, jump = objectToMove.getStride();
        //update values by direction
        // int d = startPoints.capacity();

        while(i < object.capacity()){
            object.put(i-1, object.get(i-1) + (direction.x*speed));
            object.put(i, object.get(i) + (direction.y*speed));

            //  Log.d("loopStroke", i-1 + "/" + d + "and " + i + "/" + d);
            i+=jump;
        }
    }


    private float initPointsAndDirection(int indexPoint, PointCalc startPoint, PointCalc direction){
        float targetX = pathTarget.getPoints().get(indexPoint),
        targetY = pathTarget.getPoints().get(indexPoint+1);

        startPoint.x = currentPoint.centerX();
        startPoint.y = currentPoint.centerY();

        PointCalc direc= PointCalc.Direction(targetX - currentPoint.centerX(),targetY - currentPoint.centerY());
        direction.x = direc.x;
        direction.y =  direc.y;
        return PointCalc.Distance(currentPoint.centerX(), currentPoint.centerY(), targetX, targetY);
    }
}
