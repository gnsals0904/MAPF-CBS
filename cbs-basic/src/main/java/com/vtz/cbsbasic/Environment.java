package com.vtz.cbsbasic;

import java.util.*;

public class Environment {
    public int[] dimension;
    public Set<Location> obstacles;

    public List<Map<String, Object>> agents;
    public Map<String, Map<String, Object>> agentDict;

    public Constraints constraints;
    public Map<String, Constraints> constraintDict;

    public AStar aStar;

    public Environment(int[] dimension, List<Map<String, Object>> agents, Set<Location> obstacles) {
        this.dimension = dimension;
        this.obstacles = obstacles;

        this.agents = agents;
        this.agentDict = new HashMap<>();

        makeAgentDict();

        this.constraints = new Constraints();
        this.constraintDict = new HashMap<>();

        this.aStar = new AStar(this);
    }

    public void makeAgentDict() {
        for (Map<String, Object> agent : agents) {
            String agentName = (String) agent.get("name");
            List<Integer> startCoords = (List<Integer>) agent.get("start");
            List<Integer> goalCoords = (List<Integer>) agent.get("goal");

            State startState = new State(0, new Location(startCoords.get(0), startCoords.get(1)));
            State goalState = new State(0, new Location(goalCoords.get(0), goalCoords.get(1)));

            Map<String, Object> agentInfo = new HashMap<>();
            agentInfo.put("start", startState);
            agentInfo.put("goal", goalState);

            agentDict.put(agentName, agentInfo);
        }
    }


    public List<State> getNeighbors(State state) {
        List<State> neighbors = new ArrayList<>();

        // Wait action
        State n = new State(state.time + 1, state.location);
        if (stateValid(n)) {
            neighbors.add(n);
        }
        // Up action
        n = new State(state.time + 1, new Location(state.location.x, state.location.y + 1));
        if (stateValid(n) && transitionValid(state, n)) {
            neighbors.add(n);
        }
        // Down action
        n = new State(state.time + 1, new Location(state.location.x, state.location.y - 1));
        if (stateValid(n) && transitionValid(state, n)) {
            neighbors.add(n);
        }
        // Left action
        n = new State(state.time + 1, new Location(state.location.x - 1, state.location.y));
        if (stateValid(n) && transitionValid(state, n)) {
            neighbors.add(n);
        }
        // Right action
        n = new State(state.time + 1, new Location(state.location.x + 1, state.location.y));
        if (stateValid(n) && transitionValid(state, n)) {
            neighbors.add(n);
        }
        return neighbors;
    }

    public Conflict getFirstConflict(Map<String, List<State>> solution) {
        int maxT = solution.values().stream().mapToInt(List::size).max().orElse(0);
        Conflict result = new Conflict();

        for (int t = 0; t < maxT; t++) {
            List<String> agentNames = new ArrayList<>(solution.keySet());
            for (int i = 0; i < agentNames.size(); i++) {
                for (int j = i + 1; j < agentNames.size(); j++) {
                    String agent1 = agentNames.get(i);
                    String agent2 = agentNames.get(j);
                    State state1 = getState(agent1, solution, t);
                    State state2 = getState(agent2, solution, t);
                    if (state1.isEqualExceptTime(state2)) {
                        result.time = t;
                        result.type = Conflict.VERTEX;
                        result.location1 = state1.location;
                        result.agent1 = agent1;
                        result.agent2 = agent2;
                        return result;
                    }
                }
            }
            for (int i = 0; i < agentNames.size(); i++) {
                for (int j = i + 1; j < agentNames.size(); j++) {
                    String agent1 = agentNames.get(i);
                    String agent2 = agentNames.get(j);
                    State state1a = getState(agent1, solution, t);
                    State state1b = getState(agent1, solution, t + 1);

                    State state2a = getState(agent2, solution, t);
                    State state2b = getState(agent2, solution, t + 1);

                    if (state1a.isEqualExceptTime(state2b) && state1b.isEqualExceptTime(state2a)) {
                        result.time = t;
                        result.type = Conflict.EDGE;
                        result.agent1 = agent1;
                        result.agent2 = agent2;
                        result.location1 = state1a.location;
                        result.location2 = state1b.location;
                        return result;
                    }
                }
            }
        }
        return null;
    }

    public Map<String, Constraints> createConstraintsFromConflict(Conflict conflict) {
        Map<String, Constraints> constraintDict = new HashMap<>();
        if (conflict.type == Conflict.VERTEX) {
            VertexConstraint vConstraint = new VertexConstraint(conflict.time, conflict.location1);
            Constraints constraint = new Constraints();
            constraint.vertexConstraints.add(vConstraint);
            constraintDict.put(conflict.agent1, constraint);
            constraintDict.put(conflict.agent2, constraint);

        } else if (conflict.type == Conflict.EDGE) {
            Constraints constraint1 = new Constraints();
            Constraints constraint2 = new Constraints();

            EdgeConstraint eConstraint1 = new EdgeConstraint(conflict.time, conflict.location1, conflict.location2);
            EdgeConstraint eConstraint2 = new EdgeConstraint(conflict.time, conflict.location2, conflict.location1);

            constraint1.edgeConstraints.add(eConstraint1);
            constraint2.edgeConstraints.add(eConstraint2);

            constraintDict.put(conflict.agent1, constraint1);
            constraintDict.put(conflict.agent2, constraint2);
        }
        return constraintDict;
    }

    public State getState(String agentName, Map<String, List<State>> solution, int t) {
        List<State> plan = solution.get(agentName);
        if (t < plan.size()) {
            return plan.get(t);
        } else {
            return plan.get(plan.size() - 1);
        }
    }

    public boolean stateValid(State state) {
        return state.location.x >= 0 && state.location.x < dimension[0]
                && state.location.y >= 0 && state.location.y < dimension[1]
                && !constraints.vertexConstraints.contains(new VertexConstraint(state.time, state.location))
                && !obstacles.contains(state.location);
    }

    public boolean transitionValid(State state1, State state2) {
        return !constraints.edgeConstraints.contains(new EdgeConstraint(state1.time, state1.location, state2.location));
    }

    public double admissibleHeuristic(State state, String agentName) {
        State goal = (State) agentDict.get(agentName).get("goal");
        return Math.abs(state.location.x - goal.location.x) + Math.abs(state.location.y - goal.location.y);
    }

    public boolean isAtGoal(State state, String agentName) {
        State goalState = (State) agentDict.get(agentName).get("goal");
        return state.isEqualExceptTime(goalState);
    }

    public Map<String, List<State>> computeSolution() {
        Map<String, List<State>> solution = new HashMap<>();
        for (String agent : agentDict.keySet()) {
            constraints = constraintDict.getOrDefault(agent, new Constraints());
            List<State> localSolution = aStar.search(agent);
            if (localSolution == null) {
                return null;
            }
            solution.put(agent, localSolution);
        }
        return solution;
    }

    public int computeSolutionCost(Map<String, List<State>> solution) {
        return solution.values().stream().mapToInt(List::size).sum();
    }
}

