package com.vtz.cbsbasic;

import lombok.AllArgsConstructor;
import lombok.ToString;

@ToString
@AllArgsConstructor
public class VertexConstraint {
    public int time;
    public Location location;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        VertexConstraint that = (VertexConstraint) o;

        if (time != that.time) return false;
        return location.equals(that.location);
    }

    @Override
    public int hashCode() {
        return 31 * time + location.hashCode();
    }
}

