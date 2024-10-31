package com.vtz.cbs;

public class Conflict {
    public static final int VERTEX = 1;
    public static final int EDGE = 2;

    public int time;
    public int type;
    public String agent1;
    public String agent2;
    public Location location1;
    public Location location2;

    public Conflict() {
        this.time = -1;
        this.type = -1;
        this.agent1 = "";
        this.agent2 = "";
        this.location1 = null;
        this.location2 = null;
    }
}
