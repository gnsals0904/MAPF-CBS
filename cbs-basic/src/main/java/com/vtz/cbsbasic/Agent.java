package com.vtz.cbsbasic;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Agent {
    private String name;
    private Location currentLocation;
    private Location goalLocation;
    private AgentStatus status;
    private List<State> currentPath;
}
