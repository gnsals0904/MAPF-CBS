package com.vtz.cbsbasic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CbsBasicApplication {

    public static void main(String[] args) {
        int[] dimension = {9, 9};
        Set<Location> obstacles = new HashSet<>();
        List<Map<String, Object>> agents = new ArrayList<>();

        // 에이전트 및 장애물 설정
        Map<String, Object> agent1 = new HashMap<>();
        agent1.put("name", "agent1");
        agent1.put("start", Arrays.asList(0, 0));
        agent1.put("goal", Arrays.asList(5, 5));
        agents.add(agent1);

        // 에이전트 2 설정
        Map<String, Object> agent2 = new HashMap<>();
        agent2.put("name", "agent2");
        agent2.put("start", Arrays.asList(5, 5));
        agent2.put("goal", Arrays.asList(0, 0));
        agents.add(agent2);

        // 환경 설정
        Environment env = new Environment(dimension, agents, obstacles);

        // CBS 알고리즘 실행
        CBS cbs = new CBS(env);
        Map<String, List<Map<String, Object>>> solution = cbs.search();
        if (solution == null) {
            System.out.println("Solution not found");
            return;
        }

        // 결과 출력
        System.out.println("Schedule:");
        for (String agentName : solution.keySet()) {
            System.out.println("Agent: " + agentName);
            for (Map<String, Object> step : solution.get(agentName)) {
                System.out.println(step);
            }
        }
        System.out.println("Cost: " + env.computeSolutionCost(env.computeSolution()));
    }

}
