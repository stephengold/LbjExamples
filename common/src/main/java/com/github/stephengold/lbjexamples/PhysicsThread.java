package com.github.stephengold.lbjexamples;

import com.jme3.bullet.PhysicsSpace;

public class PhysicsThread implements Runnable {
    public Thread thread;
    public PhysicsSpace space;
    private boolean running;

    public PhysicsThread(PhysicsSpace space) {
        this.space = space;
    }

    public void start() {
        if (thread == null) {
            thread = new Thread(this, "Physics Thread");
            thread.start();
            running = true;
        }
    }

    @Override
    public void run() {
        while (running) {
            try {
                space.update(0.02f, 0);
                Thread.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void stop() {
        running = false;
    }
}
