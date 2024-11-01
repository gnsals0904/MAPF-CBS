package com.vtz.cbsbasic;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TimeAdvancer {
    private final GlobalClock globalClock;

    public TimeAdvancer(GlobalClock globalClock) {
        this.globalClock = globalClock;
    }

    @Scheduled(fixedRate = 1000)
    public void advanceTime() {
        synchronized (globalClock) {
            globalClock.advanceTime();
            log.info("Global Time : {}", globalClock.getCurrentTime());
        }
    }
}
