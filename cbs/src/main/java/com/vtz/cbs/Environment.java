package com.vtz.cbs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

public class Environment {
    private final int[] dimension;
    private final Set<Location> obstacles;
    private final List<Map<String, Object>> agents;
    @Getter
    private Map<String, Map<String, State>> agentDict;
    private Constraints constraints;
    @Setter
    private Map<String, Constraints> constraintDict;
    private final AStar aStar;

    public Environment(int[] dimension, List<Map<String, Object>> agents, Set<Location> obstacles) {
        this.dimension = dimension;
        this.obstacles = obstacles;
        this.agents = agents;
        this.agentDict = new HashMap<>();
        this.constraints = new Constraints();
        this.constraintDict = new HashMap<>();
        makeAgentDict();
        this.aStar = new AStar(this);
    }

    public List<State> getNeighbors(State state) {
        List<State> neighbors = new ArrayList<>();
        int currentTime = state.time;

        State waitState = new State(currentTime + 1, state.location);
        if (stateValid(waitState)) {
            neighbors.add(waitState);
        }

        int[][] moves = {{0, 1}, {0, -1}, {-1, 0}, {1, 0}};
        for (int[] move : moves) {
            Location newLocation = new Location(state.location.x + move[0], state.location.y + move[1]);
            State newState = new State(currentTime + 1, newLocation);
            if (stateValid(newState) && transitionValid(state, newState)) {
                neighbors.add(newState);
            }
        }
        return neighbors;
    }

    public boolean stateValid(State state) {
        if (state.location.x < 0 || state.location.x >= dimension[0] ||
                state.location.y < 0 || state.location.y >= dimension[1]) {
            return false;
        }

        if (obstacles.contains(state.location)) {
            return false;
        }

        VertexConstraint vc = new VertexConstraint(state.time, state.location);
        return !constraints.vertexConstraints.contains(vc);
    }

    public boolean transitionValid(State state1, State state2) {
        EdgeConstraint ec = new EdgeConstraint(state1.time, state1.location, state2.location);
        return !constraints.edgeConstraints.contains(ec);
    }

    public int admissibleHeuristic(State state, String agentName) {
        State goal = agentDict.get(agentName).get("goal");
        return Math.abs(state.location.x - goal.location.x) + Math.abs(state.location.y - goal.location.y);
    }

    public boolean isAtGoal(State state, String agentName) {
        State goalState = agentDict.get(agentName).get("goal");
        return state.isEqualExceptTime(goalState);
    }

    public void makeAgentDict() {
        for (Map<String, Object> agent : agents) {
            String name = (String) agent.get("name");
            List<Integer> startCoords = (List<Integer>) agent.get("start");
            List<Integer> goalCoords = (List<Integer>) agent.get("goal");
            int startTime = (int) agent.getOrDefault("start_time", 0);

            State startState = new State(startTime, new Location(startCoords.get(0), startCoords.get(1)));
            State goalState = new State(0, new Location(goalCoords.get(0), goalCoords.get(1)));

            Map<String, State> stateMap = new HashMap<>();
            stateMap.put("start", startState);
            stateMap.put("goal", goalState);

            agentDict.put(name, stateMap);
        }
    }

    public Map<String, List<State>> computeSolution() {
        Map<String, List<State>> solution = new HashMap<>();
        for (String agent : agentDict.keySet()) {
            this.constraints = this.constraintDict.getOrDefault(agent, new Constraints());
            List<State> localSolution = aStar.search(agent);
            if (localSolution == null) {
                return null;
            }
            solution.put(agent, localSolution);
        }
        return solution;
    }

    public int computeSolutionCost(Map<String, List<State>> solution) {
        int cost = 0;
        for (List<State> path : solution.values()) {
            cost += path.size();
        }
        return cost;
    }

    public Conflict getFirstConflict(Map<String, List<State>> solution) {
        int maxTime = 0;
        for (List<State> path : solution.values()) {
            maxTime = Math.max(maxTime, path.size());
        }

        for (int t = 0; t < maxTime; t++) {
            for (String agent1 : solution.keySet()) {
                for (String agent2 : solution.keySet()) {
                    if (agent1.equals(agent2)) continue;

                    State state1 = getState(agent1, solution, t);
                    State state2 = getState(agent2, solution, t);

                    // Vertex conflict
                    if (state1.isEqualExceptTime(state2)) {
                        Conflict conflict = new Conflict();
                        conflict.time = t;
                        conflict.type = Conflict.VERTEX;
                        conflict.agent1 = agent1;
                        conflict.agent2 = agent2;
                        conflict.location1 = state1.location;
                        return conflict;
                    }

                    State state1Prev = getState(agent1, solution, t - 1);
                    State state2Prev = getState(agent2, solution, t - 1);

                    if (state1Prev != null && state2Prev != null) {
                        if (state1Prev.isEqualExceptTime(state2) && state2Prev.isEqualExceptTime(state1)) {
                            Conflict conflict = new Conflict();
                            conflict.time = t;
                            conflict.type = Conflict.EDGE;
                            conflict.agent1 = agent1;
                            conflict.agent2 = agent2;
                            conflict.location1 = state1Prev.location;
                            conflict.location2 = state1.location;
                            return conflict;
                        }
                    }
                }
            }
        }
        return null; // 충돌이 없음
    }

    public State getState(String agentName, Map<String, List<State>> solution, int time) {
        List<State> path = solution.get(agentName);
        if (time >= 0 && time < path.size()) {
            return path.get(time);
        } else if (time >= path.size()) {
            return path.get(path.size() - 1);
        } else {
            return null;
        }
    }

    public Map<String, Constraints> createConstraintsFromConflict(Conflict conflict) {
        Map<String, Constraints> constraintDict = new HashMap<>();

        if (conflict.type == Conflict.VERTEX) {
            // Vertex constraint를 각 에이전트에 추가
            VertexConstraint vc = new VertexConstraint(conflict.time, conflict.location1);

            // 에이전트 1에 제약 추가
            Constraints constraints1 = new Constraints();
            constraints1.vertexConstraints.add(vc);
            constraintDict.put(conflict.agent1, constraints1);

            // 에이전트 2에 동일한 제약 추가
            Constraints constraints2 = new Constraints();
            constraints2.vertexConstraints.add(vc);
            constraintDict.put(conflict.agent2, constraints2);

        } else if (conflict.type == Conflict.EDGE) {
            // Edge constraint를 각 에이전트에 추가
            EdgeConstraint ec1 = new EdgeConstraint(conflict.time, conflict.location1, conflict.location2);

            Constraints constraints1 = new Constraints();
            constraints1.edgeConstraints.add(ec1);
            constraintDict.put(conflict.agent1, constraints1);

            // 반대 방향의 EdgeConstraint 생성
            EdgeConstraint ec2 = new EdgeConstraint(conflict.time, conflict.location2, conflict.location1);

            Constraints constraints2 = new Constraints();
            constraints2.edgeConstraints.add(ec2);
            constraintDict.put(conflict.agent2, constraints2);
        }

        return constraintDict;
    }



}