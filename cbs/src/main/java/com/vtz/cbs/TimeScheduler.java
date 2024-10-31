package com.vtz.cbs;

import com.vtz.cbs.service.EnvironmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TimeScheduler {

    private final EnvironmentService environmentService;

    @Scheduled(fixedRate = 1000)
    public void updateTime() {
        Environment env = environmentService.getEnvironment();
        env.incrementGlobalTime();
        env.updateAgents();
    }
}
