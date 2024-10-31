package com.vtz.cbs;

import java.util.Objects;

public class VertexConstraint {
    public int time;
    public Location location;

    public VertexConstraint(int time, Location location) {
        this.time = time;
        this.location = location;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof VertexConstraint)) return false;
        VertexConstraint other = (VertexConstraint) obj;
        return this.time == other.time && this.location.equals(other.location);
    }

    @Override
    public int hashCode() {
        return Objects.hash(time, location);
    }
}
