package com.example.erezd.animative.utilities.tasks;


import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.example.erezd.animative.utilities.serialization.Stroke;
import com.wacom.ink.geometry.WRect;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 * Created by aviv_ams on 04/05/2018.
 */

public class AnimationManager {

    private Queue<Runnable> savingTaskQueue = new ArrayDeque<>();
    private Runnable active = null;
    private Handler handler;
    private SavingTasksHandler bgHandler;
    private SaveListenerActivity saveListener;
    private static final AnimationManager manager = new AnimationManager();


    private AnimationManager() {
        Log.d("ThisThread", Thread.currentThread().getName());

        handler = new Handler(Looper.getMainLooper()) {

            @Override
            public void handleMessage(Message inputMessage) {
                // Gets the task from the incoming Message object.
                SavingTask saveFramTask = (SavingTask) inputMessage.obj;

                switch (inputMessage.what) {

                    case SavingTask.START_TASK:
                        preSaveTaskExecute(saveFramTask);
                        startExecTask(inputMessage, false);
                        break;

                    case SavingTask.LAST_TASK:
                        preSaveTaskExecute(saveFramTask);
                        startExecTask(inputMessage, true);
                        break;

                    // The decoding is done
                    case SavingTask.TASK_SAVE_COMPLETE:
                        //update progress
                        active = null;
                        scheduleNext();
                        break;

                    case SavingTask.ALL_SAVE_TASK_COMPLETE:
                        //end the saving process, and create the gif file
                        SavingTask.count = 0;
                        saveListener.finishedSavingAnimation();
                        Log.d("savingFrame", "finished all saving tasks");
                        active = null;
                        scheduleNext();

                    default:
                           //  Pass along other messages from the UI
                        super.handleMessage(inputMessage);
                }
            }

        };

        bgHandler = new SavingTasksHandler(handler);
        new Thread(bgHandler).start();
        active = null;

    }

    public static AnimationManager getManager(){
        return manager;
    }

    public synchronized void executeSaveTask(final SavingTask task, final boolean isLast){
        savingTaskQueue.offer(new Runnable(){
            @Override
            public void run() {
                Log.d("running", " run on ui thread");
                Message message;
                if(isLast)
                    message = handler.obtainMessage(SavingTask.LAST_TASK, task);
                else
                    message = handler.obtainMessage(SavingTask.START_TASK, task);

                handler.sendMessage(message);
            }
        });

        if(active == null)
            scheduleNext();
    }

    //activate the nest task
    private synchronized void scheduleNext() {
        //if no saving frame task is running, than execute it immediately
        if ((active = savingTaskQueue.poll()) != null) {
            //handler.post(active);
            active.run();
        }
    }


    //Copying the bitmap before execution, on the UI thread.
    private synchronized void preSaveTaskExecute(SavingTask task){
        //imageScaled=getResizedBitmap(b, m_ViewLayer.getWidth()/3);
        //SavingTask.incrementNum();

        saveListener.drawStrokesOfSaveFrame(task.getTargetToUpdate(), task.getUpdatedPoints());

        WRect rect = task.getRenderer().getStrokeUpdatedArea();
        Log.d("savingFrame", "start reading pixels from canvas");

        task.setFrame(Bitmap.createBitmap(task.getLayer().getWidth(), task.getLayer().getHeight(), Bitmap.Config.ARGB_8888));
        //task.getCanvas().readPixels(task.getLayer(), task.getFrame(), rect.getX(),rect.getY(), rect.getX(),rect.getY(), rect.getWidth(), rect.getHeight());
        task.getCanvas().readPixels(task.getLayer(), task.getFrame(), 0,0,
                0,0, task.getLayer().getWidth(), task.getLayer().getHeight());
    }


    public void registerSaveListenerActivity(SaveListenerActivity activity) {
        saveListener = activity;
    }

    public void unregisterOnSaveListenerActivity() {
        saveListener = null;
    }


    private synchronized void startExecTask(Message task, boolean isLastSaveTask){
        Message msg;
        if(isLastSaveTask)
            msg = bgHandler.getBgHandler().obtainMessage(SavingTask.LAST_TASK, task.obj);
        else
            msg = bgHandler.getBgHandler().obtainMessage(SavingTask.START_TASK, task.obj);

        bgHandler.getBgHandler().sendMessage(msg);
    }


    public void stopBgHandler(){
        bgHandler.stopHandler();
    }



    public interface SaveListenerActivity{
       void drawStrokesOfSaveFrame(Stroke target, Stroke copy);
       void finishedSavingAnimation();
    }
}
