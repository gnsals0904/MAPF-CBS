package com.vtz.cbsbasic;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WareHouseEnvironment {
    public int[] dimension;
    public Set<Location> obstacles;
    public List<Map<String, Object>> agents;
    public Map<String, Map<String, Object>> agentDict;

    public Constraints constraints;
    public Map<String, Constraints> constraintDict;

    private ConcurrentMap<Integer, Map<Location, String>> reservationTable = new ConcurrentHashMap<>();

    public AStar aStar;

    public GlobalClock globalClock;

    public WareHouseEnvironment(int[] dimension, List<Map<String, Object>> agents, Set<Location> obstacles, GlobalClock globalClock) {
        this.dimension = dimension;
        this.obstacles = obstacles;
        this.globalClock = globalClock;
        this.agents = agents;
        this.agentDict = new HashMap<>();

        makeAgentDict();

        this.constraints = new Constraints();
        this.constraintDict = new HashMap<>();

        this.aStar = new AStar(this);
    }

    public List<Map<String, Object>> getAgents() {
        return new ArrayList<>(agentDict.values());
    }

    public void makeAgentDict() {
        for (Map<String, Object> agent : agents) {
            String agentName = (String) agent.get("name");
            List<Integer> startCoords = (List<Integer>) agent.get("start");
            List<Integer> goalCoords = (List<Integer>) agent.get("goal");
            int startTime = (int) agent.getOrDefault("startTime", 0);

            State startState = new State(startTime, new Location(startCoords.get(0), startCoords.get(1)));
            State goalState = new State(0, new Location(goalCoords.get(0), goalCoords.get(1)));

            Map<String, Object> agentInfo = new HashMap<>();
            agentInfo.put("start", startState);
            agentInfo.put("goal", goalState);
            agentInfo.put("startTime", startTime);

            agentDict.put(agentName, agentInfo);
        }
    }


    public List<State> getNeighbors(State state) {
        List<State> neighbors = new ArrayList<>();

        List<Location> possibleMoves = Arrays.asList(
                state.location, // Wait
                new Location(state.location.x, state.location.y + 1), // Up
                new Location(state.location.x, state.location.y - 1), // Down
                new Location(state.location.x - 1, state.location.y), // Left
                new Location(state.location.x + 1, state.location.y)  // Right
        );

        for (Location loc : possibleMoves) {
            State neighbor = new State(state.time + 1, loc);
            if (stateValid(neighbor) && transitionValid(state, neighbor)) {
                neighbors.add(neighbor);
            }
        }
        return neighbors;
    }

    public Conflict getFirstConflict(Map<String, List<State>> solution) {
        int maxT = solution.values().stream()
                .mapToInt(path -> path.get(path.size() - 1).time)
                .max()
                .orElse(0);

        for (int t = 0; t <= maxT; t++) {
            List<String> agentNames = new ArrayList<>(solution.keySet());
            for (int i = 0; i < agentNames.size(); i++) {
                for (int j = i + 1; j < agentNames.size(); j++) {
                    String agent1 = agentNames.get(i);
                    String agent2 = agentNames.get(j);
                    State state1 = getState(agent1, solution, t);
                    State state2 = getState(agent2, solution, t);
                    if (state1.isEqualExceptTime(state2)) {
                        log.info("Vertex collision detected at time {} between {} and {} at location ({}, {})", t,
                                agent1, agent2, state1.location.x, state1.location.y);
                        Conflict conflict = new Conflict();
                        conflict.time = t;
                        conflict.type = Conflict.VERTEX;
                        conflict.location1 = state1.location;
                        conflict.agent1 = agent1;
                        conflict.agent2 = agent2;
                        return conflict;
                    }

                    // Edge collision 체크
                    if (t < maxT) {
                        State state1Next = getState(agent1, solution, t + 1);
                        State state2Next = getState(agent2, solution, t + 1);
                        if (state1.location.equals(state2Next.location) && state1Next.location.equals(state2.location)) {
                            log.info("Edge collision detected at time {} between {} and {}", t, agent1, agent2);
                            Conflict conflict = new Conflict();
                            conflict.time = t;
                            conflict.type = Conflict.EDGE;
                            conflict.agent1 = agent1;
                            conflict.agent2 = agent2;
                            conflict.location1 = state1.location;
                            conflict.location2 = state1Next.location;
                            return conflict;
                        }
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
        boolean valid = state.location.x >= 0 && state.location.x < dimension[0]
                && state.location.y >= 0 && state.location.y < dimension[1]
                && !obstacles.contains(state.location);

        valid &= !constraints.vertexConstraints.contains(new VertexConstraint(state.time, state.location));

        Map<Location, String> occupiedLocations = reservationTable.getOrDefault(state.time, new HashMap<>());
        valid &= !occupiedLocations.containsKey(state.location);

        return valid;
    }

    public boolean transitionValid(State state1, State state2) {
        boolean valid = !constraints.edgeConstraints.contains(new EdgeConstraint(state1.time, state1.location, state2.location));

        Map<Location, String> occupiedNextTime = reservationTable.getOrDefault(state2.time, new HashMap<>());
        valid &= !occupiedNextTime.containsKey(state2.location);

        return valid;
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

            updateReservationTable(agent, localSolution);
        }
        return solution;
    }

    public int computeSolutionCost(Map<String, List<State>> solution) {
        return solution.values().stream().mapToInt(List::size).sum();
    }

    public synchronized void updateReservationTable(String agentName, List<State> path) {
        for (State state : path) {
            if (state.time >= globalClock.getCurrentTime()) {
                reservationTable
                        .computeIfAbsent(state.time, k -> new HashMap<>())
                        .put(state.location, agentName);
            }
        }
    }

}

