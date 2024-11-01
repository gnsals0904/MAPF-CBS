package com.vtz.cbsbasic;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SimulationLogger {
    private final GlobalClock globalClock;
    private final WareHouseEnvironment env;

    public SimulationLogger(GlobalClock globalClock, WareHouseEnvironment env) {
        this.globalClock = globalClock;
        this.env = env;
    }

    @Scheduled(fixedRate = 1000)
    public void logSimulationState() {
        int currentTime = globalClock.getCurrentTime();
        System.out.println("Time: " + currentTime);
        for (String agentName : env.agentDict.keySet()) {
            List<State> path = (List<State>) env.agentDict.get(agentName).get("solution");
            if (path != null) {
                State currentState = getCurrentState(path, currentTime);
                log.debug("Agent {} is at ({}, {})", agentName, currentState.location.x, currentState.location.y);
            } else {
                log.debug("Agent {} has no assigned path.", agentName);
            }
        }
    }

    private State getCurrentState(List<State> path, int currentTime) {
        for (State state : path) {
            if (state.time == currentTime) {
                return state;
            }
        }
        return path.get(path.size() - 1);
    }
}
