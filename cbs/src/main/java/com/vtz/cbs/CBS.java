package com.vtz.cbs;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

public class CBS {
    private final Environment env;
    private final PriorityQueue<HighLevelNode> openSet;
    private final Set<HighLevelNode> closedSet;

    public CBS(Environment env) {
        this.env = env;
        this.openSet = new PriorityQueue<>(Comparator.comparingInt(node -> node.cost));
        this.closedSet = new HashSet<>();
    }

    public Map<String, List<State>> search() {
        HighLevelNode start = new HighLevelNode();
        start.constraintDict = new HashMap<>();

        for (String agent : env.getAgentDict().keySet()) {
            start.constraintDict.put(agent, new Constraints());
        }

        env.setConstraintDict(start.constraintDict);
        start.solution = env.computeSolution();
        if (start.solution == null) {
            return null;
        }
        start.cost = env.computeSolutionCost(start.solution);

        openSet.add(start);

        while (!openSet.isEmpty()) {
            HighLevelNode P = openSet.poll();
            closedSet.add(P);

            env.setConstraintDict(P.constraintDict);
            Conflict conflict = env.getFirstConflict(P.solution);
            if (conflict == null) {
                System.out.println("Solution found");
                return P.solution;
            }

            Map<String, Constraints> constraintDict = env.createConstraintsFromConflict(conflict);

            for (String agent : constraintDict.keySet()) {
                HighLevelNode newNode = new HighLevelNode(P);
                Constraints agentConstraints = newNode.constraintDict.get(agent);
                agentConstraints.addConstraints(constraintDict.get(agent));

                env.setConstraintDict(newNode.constraintDict);
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

    private Map<String, List<Map<String, Integer>>> generatePlan(Map<String, List<State>> solution) {
        Map<String, List<Map<String, Integer>>> plan = new HashMap<>();
        for (Map.Entry<String, List<State>> entry : solution.entrySet()) {
            String agent = entry.getKey();
            List<State> path = entry.getValue();
            List<Map<String, Integer>> pathDictList = new ArrayList<>();
            for (State state : path) {
                Map<String, Integer> stateMap = new HashMap<>();
                stateMap.put("t", state.time);
                stateMap.put("x", state.location.x);
                stateMap.put("y", state.location.y);
                pathDictList.add(stateMap);
            }
            plan.put(agent, pathDictList);
        }
        return plan;
    }
}