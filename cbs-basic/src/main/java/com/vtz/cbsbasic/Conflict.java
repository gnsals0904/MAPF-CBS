package com.vtz.cbsbasic;

import lombok.ToString;

@ToString
public class Conflict {
    public static final int VERTEX = 1;
    public static final int EDGE = 2;

    public int time = -1;
    public int type = -1;

    public String agent1 = "";
    public String agent2 = "";

    public Location location1 = new Location();
    public Location location2 = new Location();
}

