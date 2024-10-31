package com.vtz.cbs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Getter
public class Environment {
    private final int[] dimension = {9, 9};;
    private final Set<Location> obstacles;
    private final List<Map<String, Object>> agents;
    private Map<String, Map<String, State>> agentDict;
    @Setter
    private Map<String, Constraints> constraintDict;
    private final AStar aStar;
    private int globalTime;
    private Map<String, List<State>> latestSolution;

    public Environment(List<Map<String, Object>> agents, Set<Location> obstacles) {
        this.obstacles = obstacles;
        this.agents = agents;
        this.agentDict = new HashMap<>();
        this.constraintDict = new HashMap<>();
        makeAgentDict();
        this.aStar = new AStar(this);
        this.globalTime = 0;
        this.latestSolution = new HashMap<>();
    }

    public List<State> getNeighbors(State state, String agentName, Constraints constraints) {
        List<State> neighbors = new ArrayList<>();
        int currentTime = state.time;
        int maxTime = 100;
        int[][] moves = {{0, 1}, {0, -1}, {-1, 0}, {1, 0}};

        if (currentTime >= maxTime) {
            return neighbors; // 무한루프처리
        }

        for (int[] move : moves) {
            // x=8 동서 방향 이동 막기
            if (state.location.x == 8 && move[0] != 0) {
                continue;
            }

            Location newLocation = new Location(state.location.x + move[0], state.location.y + move[1]);
            State newState = new State(currentTime + 1, newLocation);
            if (stateValid(newState, agentName, constraints) && transitionValid(state, newState, agentName, constraints)) {
                neighbors.add(newState);
            }
        }

        // 대기 액션
        State waitState = new State(currentTime + 1, state.location);
        if (stateValid(waitState, agentName, constraints)) {
            neighbors.add(waitState);
        }

        return neighbors;
    }

    public boolean stateValid(State state, String agentName, Constraints constraints) {
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

    public boolean transitionValid(State state1, State state2, String agentName, Constraints constraints) {
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
            Constraints agentConstraints = constraintDict.getOrDefault(agent, new Constraints());
            List<State> localSolution = aStar.search(agent, agentConstraints);
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
        return null; // 충돌이 없는 상황
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

    public void incrementGlobalTime() {
        this.globalTime++;
    }

    public void addAgent(Map<String, Object> agentInfo) {
        String name = (String) agentInfo.get("name");
        List<Integer> startCoords = (List<Integer>) agentInfo.get("start");
        List<Integer> goalCoords = (List<Integer>) agentInfo.get("goal");
        int startTime = this.globalTime; // 현재 글로벌 시간을 시작 시간으로 설정

        State startState = new State(startTime, new Location(startCoords.get(0), startCoords.get(1)));
        State goalState = new State(0, new Location(goalCoords.get(0), goalCoords.get(1)));

        Map<String, State> stateMap = new HashMap<>();
        stateMap.put("start", startState);
        stateMap.put("goal", goalState);

        agentDict.put(name, stateMap);
        Constraints newConstraints = new Constraints();
        constraintDict.put(name, newConstraints);
    }

    public void addExistingAgentConstraints(String newAgentName) {
        Constraints newAgentConstraints = constraintDict.get(newAgentName);
        for (String agentName : agentDict.keySet()) {
            if (agentName.equals(newAgentName)) continue;

            List<State> agentPath = latestSolution.get(agentName);
            if (agentPath == null) continue;

            for (State state : agentPath) {
                // Vertex Constraint 추가
                VertexConstraint vc = new VertexConstraint(state.time, state.location);
                newAgentConstraints.vertexConstraints.add(vc);
            }

            for (int i = 1; i < agentPath.size(); i++) {
                State prevState = agentPath.get(i - 1);
                State currState = agentPath.get(i);
                // Edge Constraint 추가
                EdgeConstraint ec = new EdgeConstraint(prevState.time, prevState.location, currState.location);
                newAgentConstraints.edgeConstraints.add(ec);
            }
        }
    }

    public boolean computePathForNewAgent(String agentName) {
        addExistingAgentConstraints(agentName);
        Constraints agentConstraints = this.constraintDict.get(agentName);
        List<State> path = aStar.search(agentName, agentConstraints);
        if (path == null) {
            return false; // 경로를 찾을 수 없음
        }
        latestSolution.put(agentName, path);
        return true;
    }

    public void updateAgents() {
        for (String agentName : agentDict.keySet()) {
            List<State> path = latestSolution.get(agentName);
            if (path == null) continue;

            int timeIndex = globalTime - agentDict.get(agentName).get("start").time;
            if (timeIndex >= 0 && timeIndex < path.size()) {
                State currentState = path.get(timeIndex);
                // 에이전트의 위치를 currentState.location으로 업데이트
                // TODO : 실제 에이전트 이동 로직 적용
            }
        }
    }


}