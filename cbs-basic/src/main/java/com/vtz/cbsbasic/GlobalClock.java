package com.vtz.cbsbasic;

import org.springframework.stereotype.Component;

@Component
public class GlobalClock {
    private int currentTime = 0;

    public synchronized int getCurrentTime() {
        return currentTime;
    }

    public synchronized void advanceTime() {
        currentTime++;
        notifyAll();
    }
}
