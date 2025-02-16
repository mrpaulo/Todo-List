package com.todolist.controller;

import com.todolist.model.Task;
import com.todolist.model.TaskStatus;
import com.todolist.service.TaskService;
import com.google.gson.Gson;
import java.io.*;
import java.util.List;
import java.util.Optional;

public class TaskController {

    private final TaskService taskService = new TaskService();

    // Método para pegar todas as tarefas
    public void getAllTasks(OutputStream outputStream) throws IOException {
        List<Task> tasks = taskService.getAllTasks();
        StringBuilder response = new StringBuilder("[");
        for (int i = 0; i < tasks.size(); i++) {
            Task task = tasks.get(i);
            response.append("{")
                    .append("\"id\": ").append(task.getId()).append(", ")
                    .append("\"description\": \"").append(task.getDescription()).append("\", ")
                    .append("\"status\": \"").append(task.getStatus()).append("\"")
                    .append("}");
            if (i < tasks.size() - 1) response.append(", ");
        }
        response.append("]");
        sendResponse(outputStream, 200, "OK", response.toString());
    }

    // Método para criar uma nova tarefa
    public void addTask(BufferedReader reader, OutputStream outputStream) throws IOException {
        String line;
        // Lê os cabeçalhos
        int contentLength = 0;
        while ((line = reader.readLine()) != null && !line.isEmpty()) {
            System.out.println("header: " + line);
            // Procura o cabeçalho Content-Length
            if (line.toLowerCase().startsWith("content-length:")) {
                contentLength = Integer.parseInt(line.split(":")[1].trim());
            }
        }
        // Verifica se o Content-Length foi encontrado
        if (contentLength <= 0) {
            sendResponse(outputStream, 400, "Bad Request", "Content-Length inválido ou ausente");
            return;
        }
        System.out.println("contentLength: " + contentLength);

        // Lê o corpo JSON com base no Content-Length
        char[] body = new char[contentLength];
        reader.read(body, 0, contentLength);

        String jsonString = new String(body).trim();
        System.out.println("JSON recebido: " + jsonString);

        if (!jsonString.isEmpty()) {
            try {
                if (jsonString.startsWith("[")) {
                    // Trata array de tarefas
                    Task[] tasks = new Gson().fromJson(jsonString, Task[].class);
                    for (Task task : tasks) {
                        System.out.println("[]Objeto Task criado: " + task);
                        taskService.addTask(task);
                    }
                } else {
                    // Trata objeto único
                    Task task = new Gson().fromJson(jsonString, Task.class);
                    System.out.println("Objeto Task criado: " + task);
                    taskService.addTask(task);
                }
            } catch (Exception e) {
                // Se houver erro de sintaxe no JSON, retorna um erro
                sendResponse(outputStream, 400, "Bad Request", "Erro no formato JSON: " + e.getMessage());
            }
        } else {
            sendResponse(outputStream, 400, "Bad Request", "Corpo da requisição está vazio");
        }
        sendResponse(outputStream, 201, "Created", "Tarefa criada com sucesso!");
    }

    // Método para pegar uma tarefa por ID
    public void getTaskById(String path, OutputStream outputStream) throws IOException {
        int id = Integer.parseInt(path.split("/")[2]);
        Optional<Task> task = taskService.getTaskById(id);
        if (task.isPresent()) {
            Task t = task.get();
            String response = "{"
                    + "\"id\": " + t.getId() + ", "
                    + "\"description\": \"" + t.getDescription() + "\", "
                    + "\"status\": \"" + t.getStatus() + "\""
                    + "}";
            sendResponse(outputStream, 200, "OK", response);
        } else {
            sendResponse(outputStream, 404, "Not Found", "Tarefa não encontrada");
        }
    }

    // Método para mudar o status de uma tarefa
    public void changeTaskStatus(String path, OutputStream outputStream) throws IOException {
        int id = Integer.parseInt(path.split("/")[2]);
        String statusString = path.split("/")[4];
        TaskStatus newStatus = TaskStatus.valueOf(statusString.toUpperCase());
        boolean success = taskService.changeTaskStatus(id, newStatus);
        if (success) {
            sendResponse(outputStream, 200, "OK", "Status alterado com sucesso");
        } else {
            sendResponse(outputStream, 404, "Not Found", "Tarefa não encontrada");
        }
    }

    // Método para deletar uma tarefa
    public void deleteTask(String path, OutputStream outputStream) throws IOException {
        int id = Integer.parseInt(path.split("/")[2]);
        boolean success = taskService.deleteTask(id);
        if (success) {
            sendResponse(outputStream, 200, "OK", "Tarefa deletada com sucesso");
        } else {
            sendResponse(outputStream, 404, "Not Found", "Tarefa não encontrada");
        }
    }

    // Método para enviar a resposta HTTP
    private void sendResponse(OutputStream outputStream, int statusCode, String statusMessage, String body) throws IOException {
        String response = "HTTP/1.1 " + statusCode + " " + statusMessage + "\r\n";
        response += "Content-Type: application/json\r\n";
        response += "Content-Length: " + body.length() + "\r\n";
        response += "\r\n";
        response += body;

        outputStream.write(response.getBytes());
        outputStream.flush();
    }
}