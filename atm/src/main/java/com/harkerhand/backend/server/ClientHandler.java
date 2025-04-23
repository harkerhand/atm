package com.harkerhand.backend.server;

import com.google.gson.*;
import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import java.util.logging.*;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final Logger logger = Logger.getLogger("ATMHandler");
    private final ConcurrentHashMap<String, Socket> onlineUsers;

    public ClientHandler(Socket socket, ConcurrentHashMap<String, Socket> onlineUsers) {
        this.socket = socket;
        this.onlineUsers = onlineUsers;
    }

    public void run() {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
            String input;
            while ((input = in.readLine()) != null) {
                JsonObject request = JsonParser.parseString(input).getAsJsonObject();
                JsonObject response = new JsonObject();

                String action = request.get("action").getAsString();
                switch (action) {
                    case "register":
                        response = UserService.register(request);
                        break;
                    case "login":
                        response = UserService.login(request);
                        if (response.get("status").getAsString().equals("success")) {
                            onlineUsers.put(request.get("username").getAsString(), socket);
                        }
                        break;
                    case "query":
                        response = UserService.query(request);
                        break;
                    case "withdraw":
                        response = UserService.withdraw(request);
                        break;
                    case "deposit":
                        response = UserService.deposit(request);
                        break;
                    case "logout":
                        onlineUsers.remove(request.get("username").getAsString());
                        response.addProperty("status", "success");
                        break;
                    default:
                        response.addProperty("status", "error");
                        response.addProperty("message", "Invalid action");
                }
                out.println(response.toString());
            }
        } catch (IOException e) {
            logger.warning("连接异常: " + e.getMessage());
        }
    }
}