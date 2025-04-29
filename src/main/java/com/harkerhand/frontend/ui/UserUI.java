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

public class UserUI {
    private final BufferedReader in;
    private final PrintWriter out;
    private final ObjectMapper mapper;

    public UserUI(BufferedReader in, PrintWriter out, ObjectMapper mapper) {
        this.in = in;
        this.out = out;
        this.mapper = mapper;
    }

    /**
     * 用户菜单
     */
    public void userMenu(Console console, String username) throws IOException {
        while (true) {
            ConsoleHelper.clearScreen();
            System.out.println("\n" + ConsoleHelper.colorText("=== 用户菜单 (" + username + ") ===", AnsiColor.BLUE));
            System.out.println("1. 查询余额");
            System.out.println("2. 存款");
            System.out.println("3. 取款");
            System.out.println("4. 修改密码");
            System.out.println("5. 登出");
            System.out.print(ConsoleHelper.colorText("请选择: ", AnsiColor.YELLOW));

            String choice = console.readLine();
            ObjectNode request = mapper.createObjectNode();
            request.put("username", username);

            switch (choice) {
                case "1":
                    ConsoleHelper.clearScreen();
                    System.out.println(ConsoleHelper.colorText("=== 查询余额 ===", AnsiColor.BLUE));
                    request.put("action", "query");
                    sendRequestAndPrintResponse(request, console);
                    break;
                case "2":
                    ConsoleHelper.clearScreen();
                    System.out.println(ConsoleHelper.colorText("=== 存款 ===", AnsiColor.BLUE));
                    System.out.print("存款金额: ");
                    try {
                        double amount = Double.parseDouble(console.readLine());
                        request.put("action", "deposit");
                        request.put("amount", amount);
                        sendRequestAndPrintResponse(request, console);
                    } catch (NumberFormatException e) {
                        System.out.println(ConsoleHelper.colorText("错误: 请输入有效金额", AnsiColor.RED));
                        ConsoleHelper.waitForKeyPress(console);
                    }
                    break;
                case "3":
                    ConsoleHelper.clearScreen();
                    System.out.println(ConsoleHelper.colorText("=== 取款 ===", AnsiColor.BLUE));
                    System.out.print("取款金额: ");
                    try {
                        double amount = Double.parseDouble(console.readLine());
                        request.put("action", "withdraw");
                        request.put("amount", amount);
                        sendRequestAndPrintResponse(request, console);
                    } catch (NumberFormatException e) {
                        System.out.println(ConsoleHelper.colorText("错误: 请输入有效金额", AnsiColor.RED));
                        ConsoleHelper.waitForKeyPress(console);
                    }
                    break;
                case "4":
                    changePassword(console, username);
                    break;
                case "5":
                    request.put("action", "logout");
                    out.println(request.toString());
                    System.out.println(ConsoleHelper.colorText("您已成功登出", AnsiColor.GREEN));
                    ConsoleHelper.waitForKeyPress(console);
                    return;
                default:
                    System.out.println(ConsoleHelper.colorText("无效选择，请重试", AnsiColor.RED));
                    ConsoleHelper.waitForKeyPress(console);
            }
        }
    }

    /**
     * 处理密码修改
     */
    private void changePassword(Console console, String username) throws IOException {
        ConsoleHelper.clearScreen();
        System.out.println(ConsoleHelper.colorText("=== 修改密码 ===", AnsiColor.BLUE));

        char[] oldPassword = ConsoleHelper.readNonEmptyPassword(console, "请输入原密码: ");
        char[] newPassword = ConsoleHelper.readNonEmptyPassword(console, "请输入新密码: ");
        char[] confirmPassword = ConsoleHelper.readNonEmptyPassword(console, "请再次输入新密码: ");

        if (new String(newPassword).equals(new String(confirmPassword))) {
            ObjectNode request = mapper.createObjectNode();
            request.put("action", "change_password");
            request.put("username", username);
            request.put("oldPassword", new String(oldPassword));
            request.put("newPassword", new String(newPassword));
            sendRequestAndPrintResponse(request, console);
        } else {
            System.out.println(ConsoleHelper.colorText("错误: 两次输入的新密码不一致", AnsiColor.RED));
            ConsoleHelper.waitForKeyPress(console);
        }
    }

    /**
     * 发送请求并处理响应
     */
    private void sendRequestAndPrintResponse(ObjectNode request, Console console) throws IOException {
        out.println(request.toString());
        String response = in.readLine();

        try {
            JsonNode jsonResponse = mapper.readTree(response);
            boolean success = jsonResponse.path("status").asText().equals("success");

            if (success) {
                System.out.println(ConsoleHelper.colorText("操作成功", AnsiColor.GREEN));

                // 根据不同的操作打印不同的信息
                String action = request.path("action").asText();
                if ("query".equals(action)) {
                    double balance = jsonResponse.path("balance").asDouble();
                    System.out.println(
                            ConsoleHelper.colorText("当前余额: " + String.format("%.2f", balance) + " 元", AnsiColor.GREEN));
                } else if ("deposit".equals(action) || "withdraw".equals(action)) {
                    double amount = jsonResponse.path("amount").asDouble();
                    double balance = jsonResponse.path("balance").asDouble();
                    String actionName = "deposit".equals(action) ? "存款" : "取款";
                    System.out.println(
                            ConsoleHelper.colorText(actionName + "金额: " + String.format("%.2f", amount) + " 元",
                                    AnsiColor.GREEN));
                    System.out.println(
                            ConsoleHelper.colorText("当前余额: " + String.format("%.2f", balance) + " 元", AnsiColor.GREEN));
                }

                // 如果有额外消息，显示它
                if (jsonResponse.has("message")) {
                    System.out.println(ConsoleHelper.colorText(jsonResponse.path("message").asText(), AnsiColor.BLUE));
                }
            } else {
                String message = jsonResponse.path("message").asText();
                System.out.println(ConsoleHelper.colorText("操作失败: " + message, AnsiColor.RED));
            }

            ConsoleHelper.waitForKeyPress(console);
        } catch (Exception e) {
            System.out.println(ConsoleHelper.colorText("无法解析响应: " + response, AnsiColor.RED));
            ConsoleHelper.waitForKeyPress(console);
        }
    }
}
