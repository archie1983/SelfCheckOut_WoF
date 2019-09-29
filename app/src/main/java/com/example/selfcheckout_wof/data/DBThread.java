package com.example.selfcheckout_wof.data;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * A thread that we'll use to access the Room database functions. Room stipulates
 * that all db access has to happen from a separate (not main) thread and moreover
 * all db access has to come from one and the same thread. So a singleton instance
 * of this class will be it.
 */
public class DBThread extends Thread {
    private static DBThread instance = null;

    /**
     * Tasks to execute. If empty, then just make the thread yield.
     */
    private static ArrayList<Runnable> tasks = new ArrayList<>();

    /**
     * A private singleton style constructor
     */
    private DBThread() {
        super();
    }

    /**
     * If this thread is not yet running, then start it,
     * otherwise nothing.
     */
    private static void startThread() {
        if (instance == null) {
            instance = new DBThread();
            instance.start();
        }
    }

    /**
     * Adds a task for this thread to do.
     *
     * @param task
     */
    public static void addTask(Runnable task) {
        synchronized (tasks) {
            tasks.add(task);
        }

        startThread();
    }

    /**
     * A a flag of whether we want the thread to run.
     */
    private static boolean hasToRun = true;

    /**
     * End the execution of the thread - let it complete the overloaded
     * "run()" function.
     */
    public static void endThread() {
        hasToRun = false;
    }

    /**
     * While we need to run, take tasks out of the queue and execute one by one.
     */
    @Override
    public void run() {
        while (hasToRun) {
            synchronized (tasks) {
                while (!tasks.isEmpty()) {
                    Iterator<Runnable> it = tasks.iterator();
                    while (it.hasNext()) {
                        Runnable task_to_run = it.next();
                        task_to_run.run();
                        it.remove();
                    }
//                    for (Runnable r : tasks) {
//                        r.run();
//                        tasks.remove(r);
//                    }
                }
            }


            /*
             * yield
             */
            try {
                sleep(150);
            } catch (InterruptedException exc) {
                /*
                 * carry on working if interrupted
                 */
            }
        }
    }
}
