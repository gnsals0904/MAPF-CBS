package com.vtz.cbs;

import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.ToString;

@ToString
@AllArgsConstructor
public class Location {
    public int x;
    public int y;

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Location))
            return false;
        Location other = (Location) obj;
        return this.x == other.x && this.y == other.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
}
