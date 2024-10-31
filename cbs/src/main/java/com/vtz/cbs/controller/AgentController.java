package com.vtz.cbs.controller;

import com.vtz.cbs.Environment;
import com.vtz.cbs.State;
import com.vtz.cbs.service.EnvironmentService;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/agents")
public class AgentController {

    private final EnvironmentService environmentService;

    @PostMapping("/add")
    public ResponseEntity<?> addAgent(@RequestBody AgentRequest agentRequest) {
        log.info("Add agent: {}", agentRequest);
        boolean success = environmentService.addAgentAndComputePath(agentRequest);
        if (success) {
            return ResponseEntity.ok("Agent added and path computed successfully.");
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to compute path for the agent.");
        }
    }

    @GetMapping("/solution")
    public ResponseEntity<?> getSolution() {
        Map<String, List<State>> solution = environmentService.getLatestSolution();
        return ResponseEntity.ok(solution);
    }
}
