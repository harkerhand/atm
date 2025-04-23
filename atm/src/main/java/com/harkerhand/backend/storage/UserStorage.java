package com.harkerhand.backend.storage;

import com.google.gson.*;
import com.harkerhand.backend.model.User;

import java.io.*;
import java.util.*;

public class UserStorage {
    private static final String FILE_PATH = "users.json";
    public static Map<String, User> users = new HashMap<>();

    public static void loadUsers() {
        try (Reader reader = new FileReader(FILE_PATH)) {
            User[] userArray = new Gson().fromJson(reader, User[].class);
            if (userArray != null) {
                for (User u : userArray) {
                    users.put(u.username, u);
                }
            }
        } catch (IOException ignored) {
        }
    }

    public static void saveUsers() {
        try (Writer writer = new FileWriter(FILE_PATH)) {
            new GsonBuilder().setPrettyPrinting().create().toJson(users.values(), writer);
        } catch (IOException e) {
            System.err.println("保存失败: " + e.getMessage());
        }
    }
}
