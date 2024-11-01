package com.vtz.cbsbasic;

import java.util.*;

public class AStar {
    private Map<String, Map<String, Object>> agentDict;
    private Environment env;

    public AStar(Environment env) {
        this.env = env;
        this.agentDict = env.agentDict;
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
        State initialState = (State) agentDict.get(agentName).get("start");
        int stepCost = 1;

        Set<State> closedSet = new HashSet<>();
        Set<State> openSet = new HashSet<>();
        openSet.add(initialState);

        Map<State, State> cameFrom = new HashMap<>();

        Map<State, Double> gScore = new HashMap<>();
        gScore.put(initialState, 0.0);

        Map<State, Double> fScore = new HashMap<>();
        fScore.put(initialState, env.admissibleHeuristic(initialState, agentName));

        while (!openSet.isEmpty()) {
            State current = null;
            double minFScore = Double.POSITIVE_INFINITY;
            for (State state : openSet) {
                double f = fScore.getOrDefault(state, Double.POSITIVE_INFINITY);
                if (f < minFScore) {
                    minFScore = f;
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

                double tentativeGScore = gScore.getOrDefault(current, Double.POSITIVE_INFINITY) + stepCost;

                if (!openSet.contains(neighbor)) {
                    openSet.add(neighbor);
                } else if (tentativeGScore >= gScore.getOrDefault(neighbor, Double.POSITIVE_INFINITY)) {
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

