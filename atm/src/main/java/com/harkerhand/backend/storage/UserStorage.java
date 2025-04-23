package com.harkerhand.backend.storage;

import com.harkerhand.backend.model.User;
import java.io.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UserStorage {
    private static final String USER_FILE = "users.dat";
    private static final ConcurrentHashMap<String, User> users = new ConcurrentHashMap<>();
    private static final Logger logger = Logger.getLogger("UserStorage");

    public static void loadUsers() {
        File file = new File(USER_FILE);
        if (!file.exists()) {
            logger.info("用户文件不存在，将创建新文件");
            return;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            @SuppressWarnings("unchecked")
            Map<String, User> loadedUsers = (Map<String, User>) ois.readObject();
            users.putAll(loadedUsers);
            logger.info("已加载 " + users.size() + " 个用户");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "加载用户数据时出错", e);
        }
    }

    public static void saveUsers() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(USER_FILE))) {
            oos.writeObject(users);
            logger.info("已保存 " + users.size() + " 个用户");
        } catch (IOException e) {
            logger.log(Level.SEVERE, "保存用户数据时出错", e);
        }
    }

    public static boolean userExists(String username) {
        return users.containsKey(username);
    }

    public static void addUser(String username, String password) {
        User newUser = new User(username, password);
        users.put(username, newUser);
        saveUsers();
    }

    public static boolean validateUser(String username, String password) {
        User user = users.get(username);
        return user != null && user.validatePassword(password);
    }

    public static User getUser(String username) {
        return users.get(username);
    }

    public static void updateUser(User user) {
        if (user != null) {
            users.put(user.getUsername(), user);
            saveUsers();
        }
    }
}
