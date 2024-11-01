package com.vtz.cbsbasic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

@Slf4j
@EnableScheduling
@SpringBootApplication
public class CbsBasicApplication {

    public static void main(String[] args) {
        SpringApplication.run(CbsBasicApplication.class, args);
    }

    @Bean
    public GlobalClock globalClock() {
        return new GlobalClock();
    }

    @Bean
    public WareHouseEnvironment wareHouseEnvironment(GlobalClock globalClock) {
        int[] dimension = {9, 9};
        Set<Location> obstacles = new HashSet<>();
        List<Map<String, Object>> agents = new ArrayList<>();

        // 에이전트 초기화
        Map<String, Object> agent1 = new HashMap<>();
        agent1.put("name", "agent1");
        agent1.put("start", Arrays.asList(0, 0));
        agent1.put("goal", Arrays.asList(0, 0));
        agents.add(agent1);

        Map<String, Object> agent2 = new HashMap<>();
        agent2.put("name", "agent2");
        agent2.put("start", Arrays.asList(5, 5));
        agent2.put("goal", Arrays.asList(5, 5));
        agents.add(agent2);

        return new WareHouseEnvironment(dimension, agents, obstacles, globalClock);
    }

    @Bean
    public BlockingQueue<Task> taskQueue() {
        return new LinkedBlockingQueue<>();
    }

    @Bean
    public TaskProcessor taskProcessor(BlockingQueue<Task> taskQueue, WareHouseEnvironment env, GlobalClock globalClock) {
        TaskProcessor taskProcessor = new TaskProcessor(taskQueue, env, globalClock);
        new Thread(taskProcessor).start();
        return taskProcessor;
    }

    @Bean
    public CommandLineRunner commandLineRunner(BlockingQueue<Task> taskQueue, TaskProcessor taskProcessor) {
        return args -> {
            List<Task> tasks = TaskLoader.loadTasksFromJson();
            simulatePredefinedTasks(taskQueue, taskProcessor, tasks);
        };
    }

    @Bean
    public TimeAdvancer timeAdvancer(GlobalClock globalClock) {
        return new TimeAdvancer(globalClock);
    }

    @Bean
    public SimulationLogger simulationLogger(GlobalClock globalClock, WareHouseEnvironment env) {
        return new SimulationLogger(globalClock, env);
    }

    private void simulatePredefinedTasks(BlockingQueue<Task> taskQueue, TaskProcessor taskProcessor, List<Task> tasks) {
        new Thread(() -> {
            try {
                for (Task task : tasks) {
                    Thread.sleep(5000);
                    synchronized (taskProcessor.getMonitor()) {
                        taskQueue.offer(task);
                        log.info("New task added: {}", task.getName());
                        taskProcessor.getMonitor().notifyAll();
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }


}
