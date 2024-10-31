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

    public List<State> search(String agentName) {
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

            List<State> neighborList = env.getNeighbors(current);

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
