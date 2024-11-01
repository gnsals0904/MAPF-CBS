package com.vtz.cbsbasic;


import lombok.AllArgsConstructor;
import lombok.ToString;

@ToString
@AllArgsConstructor
public class State {
    public int time;
    public Location location;

    public boolean isEqualExceptTime(State other) {
        return this.location.equals(other.location);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        State state = (State) o;

        if (time != state.time) return false;
        return location.equals(state.location);
    }

    @Override
    public int hashCode() {
        return 31 * time + location.hashCode();
    }
}

