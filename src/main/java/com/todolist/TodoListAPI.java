package com.todolist;

import com.todolist.controller.TaskController;

import java.io.*;
import java.net.*;

public class TodoListAPI {

    public static void main(String[] args) throws IOException {
        int port = 8080;
        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("Servidor rodando na porta " + port);

        // Instanciando o controller
        TaskController taskController = new TaskController();

        // Loop para aceitar conexões de clientes
        while (true) {
            try {
                Socket socket = serverSocket.accept();
                // Para cada conexão, iniciar um novo thread
                new Thread(new RequestHandler(socket, taskController)).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static class RequestHandler implements Runnable {
        private final Socket socket;
        private final TaskController taskController;

        public RequestHandler(Socket socket, TaskController taskController) {
            this.socket = socket;
            this.taskController = taskController;
        }

        @Override
        public void run() {
            try {
                InputStream inputStream = socket.getInputStream();
                OutputStream outputStream = socket.getOutputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

                // Ler a primeira linha do request HTTP
                String requestLine = reader.readLine();
                if (requestLine != null) {
                    String[] requestParts = requestLine.split(" ");
                    String method = requestParts[0];
                    String path = requestParts[1];

                    if (path.equals("/tasks") && method.equals("GET")) {
                        taskController.getAllTasks(outputStream);
                    } else if (path.equals("/tasks") && method.equals("POST")) {
                        taskController.addTask(reader, outputStream);
                    } else if (path.matches("/tasks/[0-9]+") && method.equals("GET")) {
                        taskController.getTaskById(path, outputStream);
                    } else if (path.matches("/tasks/[0-9]+/status/[a-zA-Z]+") && method.equals("PUT")) {
                        taskController.changeTaskStatus(path, outputStream);
                    } else if (path.matches("/tasks/[0-9]+") && method.equals("DELETE")) {
                        taskController.deleteTask(path, outputStream);
                    } else {
                        sendResponse(outputStream, 404, "Not Found", "Rota não encontrada");
                    }
                }
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

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
}