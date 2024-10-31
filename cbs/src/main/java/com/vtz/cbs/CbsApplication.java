package com.vtz.cbs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CbsApplication {

	public static void main(String[] args) {
		SpringApplication.run(CbsApplication.class, args);

		List<Map<String, Object>> agents = new ArrayList<>();

		Map<String, Object> agent1 = new HashMap<>();
		agent1.put("name", "agent1");
		agent1.put("start", Arrays.asList(1, 0));
		agent1.put("goal", Arrays.asList(8, 7));
		agent1.put("start_time", 0);

		Map<String, Object> agent2 = new HashMap<>();
		agent2.put("name", "agent2");
		agent2.put("start", Arrays.asList(0, 1));
		agent2.put("goal", Arrays.asList(7, 8));
		agent2.put("start_time", 3);

		agents.add(agent1);
		agents.add(agent2);

		int[] dimension = {10, 10};
		Set<Location> obstacles = new HashSet<>();

		Environment env = new Environment(dimension, agents, obstacles);
		CBS cbs = new CBS(env);
		Map<String, List<State>> solution = cbs.search();

		if (solution == null) {
			System.out.println("Solution not found");
		} else {
			for (String agent : solution.keySet()) {
				System.out.println("Agent: " + agent);
				for (State state : solution.get(agent)) {
					System.out.println("Time: " + state.time + ", Location: (" + state.location.x + ", " + state.location.y + ")");
				}
			}
		}
	}

}
