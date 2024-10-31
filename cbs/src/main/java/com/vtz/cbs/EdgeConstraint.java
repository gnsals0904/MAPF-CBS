package com.vtz.cbs;

import java.util.Objects;

public class EdgeConstraint {
    public int time;
    public Location location1;
    public Location location2;

    public EdgeConstraint(int time, Location location1, Location location2) {
        this.time = time;
        this.location1 = location1;
        this.location2 = location2;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof EdgeConstraint)) return false;
        EdgeConstraint other = (EdgeConstraint) obj;
        return this.time == other.time && this.location1.equals(other.location1) &&
                this.location2.equals(other.location2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(time, location1, location2);
    }
}
