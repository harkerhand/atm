package com.harkerhand.backend.server;

import com.google.gson.*;
import com.harkerhand.backend.model.User;
import com.harkerhand.backend.security.PasswordUtils;
import com.harkerhand.backend.storage.UserStorage;

public class UserService {
    public static JsonObject register(JsonObject req) {
        String username = req.get("username").getAsString();
        if (UserStorage.users.containsKey(username)) {
            return response("error", "用户已存在");
        }
        String salt = PasswordUtils.generateSalt();
        String hashed = PasswordUtils.hashPassword(req.get("password").getAsString(), salt);
        User user = new User();
        user.username = username;
        user.salt = salt;
        user.hashedPassword = hashed;
        user.balance = 0;
        UserStorage.users.put(username, user);
        UserStorage.saveUsers();
        return response("success", "注册成功");
    }

    public static JsonObject login(JsonObject req) {
        User user = UserStorage.users.get(req.get("username").getAsString());
        if (user == null)
            return response("error", "用户不存在");
        String hash = PasswordUtils.hashPassword(req.get("password").getAsString(), user.salt);
        if (!hash.equals(user.hashedPassword))
            return response("error", "密码错误");
        return response("success", "登录成功");
    }

    public static JsonObject query(JsonObject req) {
        User user = UserStorage.users.get(req.get("username").getAsString());
        JsonObject res = response("success", "查询成功");
        res.addProperty("balance", user.balance);
        return res;
    }

    public static JsonObject withdraw(JsonObject req) {
        User user = UserStorage.users.get(req.get("username").getAsString());
        double amount = req.get("amount").getAsDouble();
        if (user.balance < amount)
            return response("error", "余额不足");
        user.balance -= amount;
        UserStorage.saveUsers();
        return response("success", "取款成功");
    }

    public static JsonObject deposit(JsonObject req) {
        User user = UserStorage.users.get(req.get("username").getAsString());
        double amount = req.get("amount").getAsDouble();
        user.balance += amount;
        UserStorage.saveUsers();
        return response("success", "存款成功");
    }

    private static JsonObject response(String status, String msg) {
        JsonObject obj = new JsonObject();
        obj.addProperty("status", status);
        obj.addProperty("message", msg);
        return obj;
    }
}