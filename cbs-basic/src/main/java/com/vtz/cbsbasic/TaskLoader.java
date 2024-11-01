package com.vtz.cbsbasic;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

public class TaskLoader {
    public static List<Task> loadTasksFromJson() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            InputStream inputStream = TaskLoader.class.getClassLoader().getResourceAsStream("json/tasks.json");
            if (inputStream == null) {
                throw new FileNotFoundException("파일을 찾을 수 없습니다: tasks.json");
            }
            TasksWrapper tasksWrapper = mapper.readValue(inputStream, TasksWrapper.class);
            return tasksWrapper.tasks;
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public static class TasksWrapper {
        public List<Task> tasks;

        public TasksWrapper() {}

        public TasksWrapper(List<Task> tasks) {
            this.tasks = tasks;
        }
    }
}

