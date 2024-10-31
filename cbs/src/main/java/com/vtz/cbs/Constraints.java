package com.vtz.cbs;

import java.util.HashSet;
import java.util.Set;

public class Constraints {
    public Set<VertexConstraint> vertexConstraints;
    public Set<EdgeConstraint> edgeConstraints;

    public Constraints() {
        this.vertexConstraints = new HashSet<>();
        this.edgeConstraints = new HashSet<>();
    }

    public void addConstraints(Constraints other) {
        this.vertexConstraints.addAll(other.vertexConstraints);
        this.edgeConstraints.addAll(other.edgeConstraints);
    }
}
