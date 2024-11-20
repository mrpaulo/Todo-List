package com.todolist.service;

import com.todolist.model.Task;
import com.todolist.model.TaskStatus;
import com.todolist.repository.TaskRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TaskService {
    private static final List<Task> taskList = new ArrayList<>();

    // Método para pegar todas as tarefas
    public List<Task> getAllTasks() {
        return TaskRepository.getAllTasks();
    }

    // Método para pegar uma tarefa por ID
    public Optional<Task> getTaskById(int id) {
        return TaskRepository.getTaskById(id);
    }

    // Método para adicionar uma nova tarefa
    public void addTask(Task task) {
        if(task != null) {
            System.out.println("Adicionando tarefa ao banco: " + task.getDescription());
            TaskRepository.saveTask(task);
        }
    }

    // Método para alterar o status da tarefa
    public boolean changeTaskStatus(int id, TaskStatus newStatus) {
        return TaskRepository.updateTaskStatus(id, newStatus);
    }

    // Método para deletar uma tarefa
    public boolean deleteTask(int id) {
        return TaskRepository.deleteTask(id);
    }
}
