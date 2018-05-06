package com.example.erezd.animative.utilities.tasks;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 * Created by aviv_ams on 05/05/2018.
 */

public class SavingTasksHandler implements Runnable{

    private static Handler bgHandler;
    private Handler mainUIHandler;
    private Queue<Runnable> queue = new ArrayDeque<>();
    private Runnable active = null;

    public SavingTasksHandler(Handler mainUIHandler){
        this.mainUIHandler = mainUIHandler;
        if(bgHandler != null){
            bgHandler.getLooper().quit();
        }
    }

    public Handler getBgHandler(){
        return bgHandler;
    }


    @Override
    public void run() {
/*
        this.setName(SavingTasksHandler.class.getName());
        this.setPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
*/

        Looper.prepare();

        bgHandler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                Log.d("msg", "handle msg");
                if (msg.what == SavingTask.START_TASK) {
                    // Gets the task from the incoming Message object.
                    SavingTask task = (SavingTask) msg.obj;
                    execute(task, false);
                }
                else if(msg.what == SavingTask.LAST_TASK){
                    SavingTask task = (SavingTask) msg.obj;
                    execute(task, true);
                }

                Log.d("bla", "blalbaslafalfa");
            }
        };

        active = null;

        // Run the message queue in this thread call Looper.loop()
        Looper.loop();
    }


    private synchronized void execute(final Runnable task, final boolean lastTask){

        Log.d("msg", "adding msg");
        //add Runnable to the queue
        queue.offer(new Runnable() {
            public void run() {
                try {
                    Log.d("msg", "running msg");
                    task.run();
                    // Perform some task that need to be updated to UI
                    // thread after completion
                    Thread.sleep(100);

                    //inform the UIHandler on completion
                    if (mainUIHandler != null) {
                        if(lastTask){
                            Message complete = mainUIHandler.obtainMessage(SavingTask.ALL_SAVE_TASK_COMPLETE);
                            mainUIHandler.sendMessage(complete);
                        }
                        else {
                            Message complete = mainUIHandler.obtainMessage(SavingTask.TASK_SAVE_COMPLETE);
                            mainUIHandler.sendMessage(complete);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    scheduleNext();
                }
            }
        });

        //if no one is active then run this task
        if (active == null ) {
            scheduleNext();
        }
    }

    //activate the nest task
    private synchronized void scheduleNext() {
        //if no runnabel is running, than execute it immediately
        if ((active = queue.poll()) != null) {
           bgHandler.post(active);
        }
    }


    //call this from onDestroy
    public void stopHandler(){
        if(bgHandler != null)
            bgHandler.getLooper().quit();
    }
}
