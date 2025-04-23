package com.harkerhand.backend.server;

import java.net.*;
import java.util.concurrent.*;
import java.util.logging.*;

import com.harkerhand.backend.log.LoggerConfig;
import com.harkerhand.backend.storage.UserStorage;

public class ATMServer {
    public static final int PORT = 8888;
    private static final Logger logger = Logger.getLogger("ATMServer");
    private static final ConcurrentHashMap<String, Socket> onlineUsers = new ConcurrentHashMap<>();

    public static void main(String[] args) throws Exception {
        LoggerConfig.setup();
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            logger.info("ATM Server started on port " + PORT);

            // 加载用户数据
            UserStorage.loadUsers();

            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(new ClientHandler(clientSocket, onlineUsers)).start();
            }
        }
    }
}
