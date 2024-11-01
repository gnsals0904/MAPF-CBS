package com.vtz.cbsbasic;


import java.util.HashSet;
import java.util.Set;
import lombok.ToString;

@ToString
public class Constraints {
    public Set<VertexConstraint> vertexConstraints;
    public Set<EdgeConstraint> edgeConstraints;

    public Constraints() {
        vertexConstraints = new HashSet<>();
        edgeConstraints = new HashSet<>();
    }

    public void addConstraint(Constraints other) {
        vertexConstraints.addAll(other.vertexConstraints);
        edgeConstraints.addAll(other.edgeConstraints);
    }
}

