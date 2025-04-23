package com.harkerhand.backend.server;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.harkerhand.backend.storage.UserStorage;
import com.harkerhand.backend.model.User;
import com.harkerhand.backend.log.UserActivityLogger;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private BufferedReader in;
    private PrintWriter out;
    private ObjectMapper mapper;
    private ConcurrentHashMap<String, Socket> onlineUsers;
    private static final Logger logger = Logger.getLogger("ClientHandler");

    public ClientHandler(Socket socket, ConcurrentHashMap<String, Socket> onlineUsers) {
        this.clientSocket = socket;
        this.onlineUsers = onlineUsers;
        this.mapper = new ObjectMapper();
        try {
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.out = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "无法创建客户端处理器", e);
        }
    }

    @Override
    public void run() {
        try {
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                JsonNode request = mapper.readTree(inputLine);
                String action = request.path("action").asText();
                String response;

                switch (action) {
                    case "register":
                        response = handleRegister(request);
                        break;
                    case "login":
                        response = handleLogin(request);
                        break;
                    case "query":
                        response = handleQuery(request);
                        break;
                    case "deposit":
                        response = handleDeposit(request);
                        break;
                    case "withdraw":
                        response = handleWithdraw(request);
                        break;
                    case "logout":
                        response = handleLogout(request);
                        break;
                    default:
                        ObjectNode errorResponse = mapper.createObjectNode();
                        errorResponse.put("status", "error");
                        errorResponse.put("message", "未知操作");
                        response = errorResponse.toString();
                }

                out.println(response);
            }
        } catch (IOException e) {
            logger.log(Level.WARNING, "客户端连接异常", e);
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                logger.log(Level.WARNING, "关闭客户端连接时出错", e);
            }
        }
    }

    private String handleRegister(JsonNode request) {
        String username = request.path("username").asText();
        String password = request.path("password").asText();

        ObjectNode response = mapper.createObjectNode();

        if (UserStorage.userExists(username)) {
            response.put("status", "error");
            response.put("message", "用户名已存在");
        } else {
            UserStorage.addUser(username, password);
            response.put("status", "success");
            response.put("message", "用户注册成功");
            logger.info("用户注册: " + username);
        }

        return response.toString();
    }

    private String handleLogin(JsonNode request) {
        String username = request.path("username").asText();
        String password = request.path("password").asText();

        ObjectNode response = mapper.createObjectNode();

        String ipAddress = clientSocket.getInetAddress().getHostAddress();

        if (onlineUsers.containsKey(username)) {
            response.put("status", "error");
            response.put("message", "用户已在其他地方登录");
            UserActivityLogger.logLogin(username, ipAddress, false);
        } else if (UserStorage.validateUser(username, password)) {
            onlineUsers.put(username, clientSocket);
            response.put("status", "success");
            response.put("message", "登录成功");
            logger.info("用户登录: " + username);
            UserActivityLogger.logLogin(username, ipAddress, true);
        } else {
            response.put("status", "error");
            response.put("message", "用户名或密码错误");
            UserActivityLogger.logLogin(username, ipAddress, false);
        }

        return response.toString();
    }

    private String handleQuery(JsonNode request) {
        String username = request.path("username").asText();
        ObjectNode response = mapper.createObjectNode();

        if (!onlineUsers.containsKey(username) || !isUserSessionValid(username)) {
            response.put("status", "error");
            response.put("message", "用户未登录");
            return response.toString();
        }

        User user = UserStorage.getUser(username);
        if (user != null) {
            response.put("status", "success");
            response.put("balance", user.getBalance());
            logger.info("用户查询余额: " + username + ", 余额: " + user.getBalance());
            UserActivityLogger.logBalanceCheck(username);
        } else {
            response.put("status", "error");
            response.put("message", "用户不存在");
        }

        return response.toString();
    }

    private String handleDeposit(JsonNode request) {
        String username = request.path("username").asText();
        double amount = request.path("amount").asDouble();

        ObjectNode response = mapper.createObjectNode();

        if (!onlineUsers.containsKey(username) || !isUserSessionValid(username)) {
            response.put("status", "error");
            response.put("message", "用户未登录");
            return response.toString();
        }

        if (amount <= 0) {
            response.put("status", "error");
            response.put("message", "存款金额必须大于零");
            return response.toString();
        }

        User user = UserStorage.getUser(username);
        if (user != null) {
            user.deposit(amount);
            UserStorage.updateUser(user);

            response.put("status", "success");
            response.put("amount", amount); // 添加存款金额到响应中
            response.put("balance", user.getBalance()); // 添加更新后的余额到响应中
            logger.info("用户存款: " + username + ", 金额: " + amount + ", 余额: " + user.getBalance());
            UserActivityLogger.logDeposit(username, amount);
        } else {
            response.put("status", "error");
            response.put("message", "用户不存在");
        }

        return response.toString();
    }

    private String handleWithdraw(JsonNode request) {
        String username = request.path("username").asText();
        double amount = request.path("amount").asDouble();

        ObjectNode response = mapper.createObjectNode();

        if (!onlineUsers.containsKey(username) || !isUserSessionValid(username)) {
            response.put("status", "error");
            response.put("message", "用户未登录");
            return response.toString();
        }

        if (amount <= 0) {
            response.put("status", "error");
            response.put("message", "取款金额必须大于零");
            return response.toString();
        }

        User user = UserStorage.getUser(username);
        if (user != null) {
            if (user.getBalance() >= amount) {
                user.withdraw(amount);
                UserStorage.updateUser(user);

                response.put("status", "success");
                response.put("amount", amount); // 添加取款金额到响应中
                response.put("balance", user.getBalance()); // 添加更新后的余额到响应中
                logger.info("用户取款: " + username + ", 金额: " + amount + ", 余额: " + user.getBalance());
                UserActivityLogger.logWithdrawal(username, amount, true);
            } else {
                response.put("status", "error");
                response.put("message", "余额不足");
                UserActivityLogger.logWithdrawal(username, amount, false);
            }
        } else {
            response.put("status", "error");
            response.put("message", "用户不存在");
        }

        return response.toString();
    }

    private String handleLogout(JsonNode request) {
        String username = request.path("username").asText();

        ObjectNode response = mapper.createObjectNode();

        if (onlineUsers.remove(username) != null) {
            response.put("status", "success");
            response.put("message", "注销成功");
            logger.info("用户注销: " + username);
            UserActivityLogger.logLogout(username);
        } else {
            response.put("status", "error");
            response.put("message", "用户未登录");
        }

        return response.toString();
    }

    private boolean isUserSessionValid(String username) {
        Socket userSocket = onlineUsers.get(username);
        return userSocket != null && userSocket.equals(clientSocket);
    }
}