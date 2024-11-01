package com.vtz.cbsbasic;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class HighLevelNode implements Comparable<HighLevelNode> {
    public Map<String, List<State>> solution;
    public Map<String, Constraints> constraintDict;
    public int cost;

    public HighLevelNode() {
        solution = new HashMap<>();
        constraintDict = new HashMap<>();
        cost = 0;
    }

    @Override
    public int compareTo(HighLevelNode other) {
        return Integer.compare(this.cost, other.cost);
    }

    @Override
    public int hashCode() {
        return Objects.hash(solution, cost);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof HighLevelNode)) return false;
        HighLevelNode other = (HighLevelNode) obj;
        return cost == other.cost && Objects.equals(solution, other.solution);
    }
}

