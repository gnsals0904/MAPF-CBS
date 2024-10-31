package com.vtz.cbs.service;

import com.vtz.cbs.CBS;
import com.vtz.cbs.Environment;
import com.vtz.cbs.Location;
import com.vtz.cbs.State;
import com.vtz.cbs.controller.AgentRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Getter;
import org.springframework.stereotype.Service;

@Service
@Getter
public class EnvironmentService {

    private Environment environment;
    private CBS cbs;

    public EnvironmentService() {
        Set<Location> obstacles = new HashSet<>();
        for (int y = 0; y <= 8; y++) {
            obstacles.add(new Location(8, y));
        }
        environment = new Environment(new ArrayList<>(), obstacles);
        cbs = new CBS(environment);
    }

    public boolean addAgentAndComputePath(AgentRequest agentRequest) {
        Map<String, Object> agentInfo = new HashMap<>();
        agentInfo.put("name", agentRequest.name());
        agentInfo.put("start", agentRequest.start());
        agentInfo.put("goal", agentRequest.goal());
        agentInfo.put("start_time", environment.getGlobalTime());

        environment.addAgent(agentInfo);
        boolean pathFound = environment.computePathForNewAgent(agentRequest.name());

        return pathFound;
    }

    public Map<String, List<State>> getLatestSolution() {
        return environment.getLatestSolution();
    }

}
