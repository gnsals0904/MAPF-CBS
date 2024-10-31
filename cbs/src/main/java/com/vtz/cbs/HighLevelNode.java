package com.vtz.cbs;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class HighLevelNode {
    public Map<String, List<State>> solution;
    public Map<String, Constraints> constraintDict;
    public int cost;

    public HighLevelNode() {
        this.solution = new HashMap<>();
        this.constraintDict = new HashMap<>();
        this.cost = 0;
    }

    public HighLevelNode(HighLevelNode other) {
        this.solution = new HashMap<>(other.solution);
        this.constraintDict = new HashMap<>();
        for (String agent : other.constraintDict.keySet()) {
            this.constraintDict.put(agent, new Constraints());
            this.constraintDict.get(agent).vertexConstraints.addAll(other.constraintDict.get(agent).vertexConstraints);
            this.constraintDict.get(agent).edgeConstraints.addAll(other.constraintDict.get(agent).edgeConstraints);
        }
        this.cost = other.cost;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof HighLevelNode)) return false;
        HighLevelNode other = (HighLevelNode) obj;
        return this.solution.equals(other.solution) && this.cost == other.cost;
    }

    @Override
    public int hashCode() {
        return Objects.hash(solution, cost);
    }
}
