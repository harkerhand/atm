package com.harkerhand.backend.server;

import java.net.*;
import java.util.concurrent.*;
import java.util.logging.*;

import com.harkerhand.backend.log.LoggerConfig;
import com.harkerhand.backend.log.UserActivityLogger;
import com.harkerhand.backend.storage.UserStorage;
import com.harkerhand.backend.service.InterestService;

public class ATMServer {
    public static final int PORT = 8888;
    private static final Logger logger = Logger.getLogger("ATMServer");
    private static final ConcurrentHashMap<String, Socket> onlineUsers = new ConcurrentHashMap<>();
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public static void main(String[] args) throws Exception {
        // 设置系统日志
        LoggerConfig.setup();
        // 初始化用户活动日志
        UserActivityLogger.initialize();

        logger.info("ATM Server starting...");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            logger.info("ATM Server started on port " + PORT);

            // 加载用户数据
            UserStorage.loadUsers();
            logger.info("User data loaded successfully");

            // 启动利息计算服务，每10秒执行一次
            InterestService interestService = new InterestService();
            scheduler.scheduleAtFixedRate(interestService, 0, 10, TimeUnit.SECONDS);
            logger.info("Interest calculation service started");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                String clientAddress = clientSocket.getInetAddress().getHostAddress();
                logger.info("New client connected: " + clientAddress);
                new Thread(new ClientHandler(clientSocket, onlineUsers)).start();
            }
        } catch (Exception e) {
            logger.severe("Server error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            logger.info("ATM Server shutting down");
            UserActivityLogger.shutdown();
            scheduler.shutdown();
        }
    }
}
