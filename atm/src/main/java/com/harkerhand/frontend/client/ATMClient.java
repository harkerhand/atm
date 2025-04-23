package com.harkerhand.frontend.client;

import com.google.gson.*;
import java.io.*;
import java.net.*;
import java.util.*;

public class ATMClient {
    public static void main(String[] args) throws Exception {
        Socket socket = new Socket("localhost", 8888);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        Scanner scanner = new Scanner(System.in);

        System.out.println("连接ATM成功");
        while (true) {
            System.out.println("1. 注册\n2. 登录\n3. 退出");
            String choice = scanner.nextLine();
            if (choice.equals("1")) {
                System.out.print("用户名: ");
                String username = scanner.nextLine();
                System.out.print("密码: ");
                String password = scanner.nextLine();
                JsonObject req = new JsonObject();
                req.addProperty("action", "register");
                req.addProperty("username", username);
                req.addProperty("password", password);
                out.println(req.toString());
                System.out.println(in.readLine());
            } else if (choice.equals("2")) {
                System.out.print("用户名: ");
                String username = scanner.nextLine();
                System.out.print("密码: ");
                String password = scanner.nextLine();
                JsonObject req = new JsonObject();
                req.addProperty("action", "login");
                req.addProperty("username", username);
                req.addProperty("password", password);
                out.println(req.toString());
                String resp = in.readLine();
                System.out.println(resp);
                if (resp.contains("success")) {
                    runUserSession(scanner, out, in, username);
                }
            } else
                break;
        }
        socket.close();
    }

    static void runUserSession(Scanner scanner, PrintWriter out, BufferedReader in, String username)
            throws IOException {
        while (true) {
            System.out.println("1. 查询\n2. 存钱\n3. 取钱\n4. 登出");
            String opt = scanner.nextLine();
            JsonObject req = new JsonObject();
            req.addProperty("username", username);
            switch (opt) {
                case "1":
                    req.addProperty("action", "query");
                    break;
                case "2":
                    req.addProperty("action", "deposit");
                    System.out.print("金额: ");
                    req.addProperty("amount", Double.parseDouble(scanner.nextLine()));
                    break;
                case "3":
                    req.addProperty("action", "withdraw");
                    System.out.print("金额: ");
                    req.addProperty("amount", Double.parseDouble(scanner.nextLine()));
                    break;
                case "4":
                    req.addProperty("action", "logout");
                    out.println(req.toString());
                    System.out.println("已登出");
                    return;
                default:
                    continue;
            }
            out.println(req.toString());
            System.out.println(in.readLine());
        }
    }
}
