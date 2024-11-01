package com.vtz.cbsbasic;

import java.util.*;

public class CBS {
    private Environment env;
    private Set<HighLevelNode> openSet;
    private Set<HighLevelNode> closedSet;

    public CBS(Environment environment) {
        this.env = environment;
        this.openSet = new HashSet<>();
        this.closedSet = new HashSet<>();
    }

    public Map<String, List<Map<String, Object>>> search() {
        HighLevelNode start = new HighLevelNode();

        for (String agent : env.agentDict.keySet()) {
            start.constraintDict.put(agent, new Constraints());
        }
        start.solution = env.computeSolution();
        if (start.solution == null) {
            return null;
        }
        start.cost = env.computeSolutionCost(start.solution);

        openSet.add(start);

        while (!openSet.isEmpty()) {
            HighLevelNode P = Collections.min(openSet);
            openSet.remove(P);
            closedSet.add(P);

            env.constraintDict = P.constraintDict;
            Conflict conflict = env.getFirstConflict(P.solution);
            if (conflict == null) {
                System.out.println("Solution found");
                return generatePlan(P.solution);
            }

            Map<String, Constraints> constraintDict = env.createConstraintsFromConflict(conflict);

            for (String agent : constraintDict.keySet()) {
                HighLevelNode newNode = new HighLevelNode();
                newNode.constraintDict = new HashMap<>(P.constraintDict);
                newNode.constraintDict.get(agent).addConstraint(constraintDict.get(agent));

                env.constraintDict = newNode.constraintDict;
                newNode.solution = env.computeSolution();
                if (newNode.solution == null) {
                    continue;
                }
                newNode.cost = env.computeSolutionCost(newNode.solution);

                if (!closedSet.contains(newNode)) {
                    openSet.add(newNode);
                }
            }
        }
        return null;
    }

    public Map<String, List<Map<String, Object>>> generatePlan(Map<String, List<State>> solution) {
        Map<String, List<Map<String, Object>>> plan = new HashMap<>();
        for (Map.Entry<String, List<State>> entry : solution.entrySet()) {
            String agent = entry.getKey();
            List<State> path = entry.getValue();
            List<Map<String, Object>> pathList = new ArrayList<>();
            for (State state : path) {
                Map<String, Object> stateMap = new HashMap<>();
                stateMap.put("t", state.time);
                stateMap.put("x", state.location.x);
                stateMap.put("y", state.location.y);
                pathList.add(stateMap);
            }
            plan.put(agent, pathList);
        }
        return plan;
    }
}

