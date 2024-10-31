package com.vtz.cbs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AStar {
    private Map<String, Map<String, State>> agentDict;
    private Environment env;

    public AStar(Environment env) {
        this.env = env;
        this.agentDict = env.getAgentDict();
    }

    public List<State> reconstructPath(Map<State, State> cameFrom, State current) {
        List<State> totalPath = new ArrayList<>();
        totalPath.add(current);
        while (cameFrom.containsKey(current)) {
            current = cameFrom.get(current);
            totalPath.add(current);
        }
        Collections.reverse(totalPath);
        return totalPath;
    }

    public List<State> search(String agentName, Constraints constraints) {
        State initialState = agentDict.get(agentName).get("start");
        int stepCost = 1;

        Set<State> closedSet = new HashSet<>();
        Set<State> openSet = new HashSet<>();
        openSet.add(initialState);

        Map<State, State> cameFrom = new HashMap<>();

        Map<State, Integer> gScore = new HashMap<>();
        gScore.put(initialState, 0);

        Map<State, Integer> fScore = new HashMap<>();
        fScore.put(initialState, env.admissibleHeuristic(initialState, agentName));

        int maxTime = 100; // TODO: 최대 시간 설정 추후 변경 필요

        while (!openSet.isEmpty()) {
            State current = null;
            int minFScore = Integer.MAX_VALUE;
            for (State state : openSet) {
                int score = fScore.getOrDefault(state, Integer.MAX_VALUE);
                if (score < minFScore) {
                    minFScore = score;
                    current = state;
                }
            }

            if (env.isAtGoal(current, agentName)) {
                return reconstructPath(cameFrom, current);
            }

            openSet.remove(current);
            closedSet.add(current);

            // 현재 시간이 최대 시간을 초과하면 탐색 중지
            if (current.time >= maxTime) {
                continue;
            }

            List<State> neighborList = env.getNeighbors(current, agentName, constraints);

            for (State neighbor : neighborList) {
                if (closedSet.contains(neighbor)) {
                    continue;
                }

                int tentativeGScore = gScore.getOrDefault(current, Integer.MAX_VALUE) + stepCost;

                if (!openSet.contains(neighbor)) {
                    openSet.add(neighbor);
                } else if (tentativeGScore >= gScore.getOrDefault(neighbor, Integer.MAX_VALUE)) {
                    continue;
                }

                cameFrom.put(neighbor, current);
                gScore.put(neighbor, tentativeGScore);
                fScore.put(neighbor, tentativeGScore + env.admissibleHeuristic(neighbor, agentName));
            }
        }
        return null;
    }
}
