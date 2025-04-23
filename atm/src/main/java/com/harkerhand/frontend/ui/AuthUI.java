package com.harkerhand.frontend.ui;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.harkerhand.frontend.utils.ConsoleHelper;
import com.harkerhand.frontend.utils.ConsoleHelper.AnsiColor;

import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.PrintWriter;

public class AuthUI {
    private final BufferedReader in;
    private final PrintWriter out;
    private final ObjectMapper mapper;

    public AuthUI(BufferedReader in, PrintWriter out, ObjectMapper mapper) {
        this.in = in;
        this.out = out;
        this.mapper = mapper;
    }

    /**
     * 用户注册界面
     */
    public void register(Console console) throws IOException {
        ConsoleHelper.clearScreen();
        System.out.println(ConsoleHelper.colorText("=== 用户注册 ===", AnsiColor.BLUE));

        String username = ConsoleHelper.readNonEmptyString(console, "用户名: ");
        char[] password = ConsoleHelper.readNonEmptyPassword(console, "密码: ");

        ObjectNode request = mapper.createObjectNode();
        request.put("action", "register");
        request.put("username", username);
        request.put("password", new String(password));

        out.println(request.toString());
        String response = in.readLine();

        try {
            JsonNode jsonResponse = mapper.readTree(response);
            boolean success = jsonResponse.path("status").asText().equals("success");
            String message = jsonResponse.path("message").asText();

            if (success) {
                System.out.println(ConsoleHelper.colorText("注册成功: " + message, AnsiColor.GREEN));
            } else {
                System.out.println(ConsoleHelper.colorText("注册失败: " + message, AnsiColor.RED));
            }
            ConsoleHelper.waitForKeyPress(console);
        } catch (Exception e) {
            System.out.println(ConsoleHelper.colorText("无法解析响应: " + response, AnsiColor.RED));
            ConsoleHelper.waitForKeyPress(console);
        }
    }

    /**
     * 用户登录界面
     * 
     * @return 登录成功的用户名，如果登录失败返回null
     */
    public String login(Console console) throws IOException {
        ConsoleHelper.clearScreen();
        System.out.println(ConsoleHelper.colorText("=== 用户登录 ===", AnsiColor.BLUE));

        String username = ConsoleHelper.readNonEmptyString(console, "用户名: ");
        char[] password = ConsoleHelper.readNonEmptyPassword(console, "密码: ");

        ObjectNode request = mapper.createObjectNode();
        request.put("action", "login");
        request.put("username", username);
        request.put("password", new String(password));

        out.println(request.toString());
        String response = in.readLine();

        try {
            JsonNode jsonResponse = mapper.readTree(response);
            boolean success = jsonResponse.path("status").asText().equals("success");
            String message = jsonResponse.path("message").asText();

            if (success) {
                System.out.println(ConsoleHelper.colorText("登录成功: " + message, AnsiColor.GREEN));
                return username;
            } else {
                System.out.println(ConsoleHelper.colorText("登录失败: " + message, AnsiColor.RED));
                ConsoleHelper.waitForKeyPress(console);
                return null;
            }
        } catch (Exception e) {
            System.out.println(ConsoleHelper.colorText("无法解析响应: " + response, AnsiColor.RED));
            ConsoleHelper.waitForKeyPress(console);
            return null;
        }
    }
}
