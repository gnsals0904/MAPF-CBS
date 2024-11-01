package com.vtz.cbsbasic;

import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public class TaskProcessor implements Runnable {
    private final BlockingQueue<Task> taskQueue;
    private final WareHouseEnvironment env;
    private final GlobalClock globalClock;
    private final ConcurrentMap<String, Boolean> agentStatus;
    private final Object monitor;
    private final ScheduledExecutorService executorService;


    public TaskProcessor(BlockingQueue<Task> taskQueue, WareHouseEnvironment env, GlobalClock globalClock) {
        this.taskQueue = taskQueue;
        this.env = env;
        this.globalClock = globalClock;
        this.agentStatus = new ConcurrentHashMap<>();
        this.monitor = new Object();
        for (String agentName : env.agentDict.keySet()) {
            agentStatus.put(agentName, true);
        }
        this.executorService = Executors.newScheduledThreadPool(2);
    }

    @Override
    public void run() {
        while (true) {
            try {
                synchronized (monitor) {
                    while (findIdleAgent() == null || taskQueue.isEmpty()) {
                        monitor.wait();
                    }

                    String agentName = findIdleAgent();
                    Task task = taskQueue.poll();
                    assignTaskToAgent(agentName, task, getNextAvailableTimeslot());
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    public void notifyAgentAvailable() {
        synchronized (monitor) {
            monitor.notifyAll();
        }
    }

    public Task waitForNextTask() throws InterruptedException {
        synchronized (monitor) {
            while (taskQueue.isEmpty()) {
                monitor.wait();
            }
            return taskQueue.poll();
        }
    }

    public void notifyNewTaskAdded() {
        synchronized (monitor) {
            monitor.notifyAll();
        }
    }

    private boolean hasIdleAgent() {
        return agentStatus.values().stream().anyMatch(isIdle -> isIdle);
    }

    private String findIdleAgent() {
        for (Map.Entry<String, Boolean> entry : agentStatus.entrySet()) {
            if (entry.getValue()) {
                return entry.getKey();
            }
        }
        return null;
    }

    private int getNextAvailableTimeslot() {
        return globalClock.getCurrentTime() + 1;
    }

    private void assignTaskToAgent(String agentName, Task task, int startTime) {
        agentStatus.put(agentName, false);

        Map<String, Object> agentInfo = env.agentDict.get(agentName);
        agentInfo.put("start", new State(startTime, task.getStartLocation()));
        agentInfo.put("goal", new State(0, task.getGoalLocation()));
        agentInfo.put("startTime", startTime);

        env.constraintDict.putIfAbsent(agentName, new Constraints());
        env.constraints = env.constraintDict.get(agentName);

        List<State> localSolution = env.aStar.search(agentName);

        if (localSolution != null) {
            env.updateReservationTable(agentName, localSolution);

            env.agentDict.get(agentName).put("solution", localSolution);
            logAgentPath(agentName, localSolution);
            scheduleAgentIdle(agentName, localSolution);
        } else {
            log.info("No path found for agent {}", agentName);
            agentStatus.put(agentName, true);
            notifyAgentAvailable();
            synchronized (monitor) {
                taskQueue.offer(task);
                notifyNewTaskAdded();
            }
        }
    }

    private void scheduleAgentIdle(String agentName, List<State> path) {
        executorService.execute(() -> {
            try {
                for (State state : path) {
                    int waitTime = (state.time - globalClock.getCurrentTime()) * 1000;
                    if (waitTime > 0) {
                        Thread.sleep(waitTime);
                    }
                    log.info("Agent {} moved to ({}, {}) at time {}", agentName, state.location.x, state.location.y,
                            state.time);
                }
                agentStatus.put(agentName, true);
                log.info("Agent {} has completed the task and is now idle.", agentName);
                notifyAgentAvailable();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }


    private void logAgentPath(String agentName, List<State> path) {
        log.info("Agent {} path:", agentName);
        for (State state : path) {
            log.info("Time: {}, Location: ({}, {})", state.time, state.location.x, state.location.y);
        }
    }

    private void assignTasksToIdleAgents() {
        while (hasIdleAgent() && !taskQueue.isEmpty()) {
            String agentName = findIdleAgent();
            if (agentName != null) {
                Task task = taskQueue.poll();
                if (task != null) {
                    log.info("Assigning task {} to agent {}", task.getName(), agentName);
                    int startTime = getNextAvailableTimeslot();
                    assignTaskToAgent(agentName, task, startTime);
                }
            } else {
                break;
            }
        }
    }

    private void waitForNextTimeslot() throws InterruptedException {
        synchronized (globalClock) {
            int currentTime = globalClock.getCurrentTime();
            globalClock.wait();
        }
    }

    private void processTask(Task task) {
        String agentName = findIdleAgent();
        if (agentName != null) {
            int startTime = getNextAvailableTimeslot();
            assignTaskToAgent(agentName, task, startTime);
        } else {
            taskQueue.offer(task);
        }
    }

    public void shutdown() {
        executorService.shutdown();
    }

}

