package com.vtz.cbs;

import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.ToString;

@ToString
@AllArgsConstructor
public class State {
    public int time;
    public Location location;


    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof State))
            return false;
        State other = (State) obj;
        return this.time == other.time && this.location.equals(other.location);
    }

    @Override
    public int hashCode() {
        return Objects.hash(time, location.x, location.y);
    }

    public boolean isEqualExceptTime(State other) {
        return this.location.equals(other.location);
    }

}
