package com.vtz.cbsbasic;

import lombok.AllArgsConstructor;
import lombok.ToString;

@ToString
@AllArgsConstructor
public class EdgeConstraint {
    public int time;
    public Location location1;
    public Location location2;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EdgeConstraint that = (EdgeConstraint) o;

        if (time != that.time) return false;
        if (!location1.equals(that.location1)) return false;
        return location2.equals(that.location2);
    }

    @Override
    public int hashCode() {
        int result = time;
        result = 31 * result + location1.hashCode();
        result = 31 * result + location2.hashCode();
        return result;
    }
}

