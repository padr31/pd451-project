package uk.ac.cam.pd451.dissertation.utils;

/**
 * A custom timer class that is able to time multiple segments of execution.
 */
public class Timer {

    private long time;
    private long lastCheckpoint;

    public Timer() {
        this.time = System.currentTimeMillis();
        this.lastCheckpoint = time;
        System.out.println("New timer started.");
    }

    public void printTime() {
        System.out.println("Timer time: " + time);
    }
    public void printTimeFromStart() {
        System.out.println("Duration from timer start: " + (System.currentTimeMillis() - time));
    }

    public void printLastTimeSegment() {
        long currentTime = System.currentTimeMillis();
        System.out.println("Duration of last segment: " + (currentTime - lastCheckpoint));
        this.lastCheckpoint = currentTime;
    }

    public void printLastTimeSegment(String text) {
        long currentTime = System.currentTimeMillis();
        System.out.println("Duration of segment (" + text + "): " + (currentTime - lastCheckpoint));
        this.lastCheckpoint = currentTime;
    }
}
