package com.vtz.cbs.controller;

import java.util.List;

public record AgentRequest(String name, List<Integer> start, List<Integer> goal) {
}
