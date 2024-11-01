package com.vtz.cbsbasic;

import lombok.ToString;

@ToString
public class Location {
    public int x;
    public int y;

    public Location() {
        this.x = -1;
        this.y = -1;
    }

    public Location(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        Location location = (Location) o;

        if (x != location.x)
            return false;
        return y == location.y;
    }

    @Override
    public int hashCode() {
        return 31 * x + y;
    }
}
