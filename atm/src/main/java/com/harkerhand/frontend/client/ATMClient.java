package com.harkerhand.frontend.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.*;
import java.net.*;
import java.util.concurrent.Callable;

@Command(name = "atm", mixinStandardHelpOptions = true, version = "ATM 1.0", description = "ATM 客户端应用")
public class ATMClient implements Callable<Integer> {

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private ObjectMapper mapper = new ObjectMapper();

    @Option(names = { "-h", "--host" }, description = "服务器地址", defaultValue = "localhost")
    private String host;

    @Option(names = { "-p", "--port" }, description = "服务器端口", defaultValue = "8888")
    private int port;

    public static void main(String[] args) {
        int exitCode = new CommandLine(new ATMClient()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() throws Exception {
        try {
            connect();
            mainMenu();
            return 0;
        } catch (Exception e) {
            System.err.println("错误: " + e.getMessage());
            return 1;
        } finally {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        }
    }

    /**
     * 清屏方法
     */
    private void clearScreen() {
        try {
            String operatingSystem = System.getProperty("os.name");

            if (operatingSystem.contains("Windows")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                System.out.print("\033[H\033[2J");
                System.out.flush();
            }
        } catch (Exception e) {
            // 如果清屏失败，打印多个换行符作为替代方案
            System.out.println("\n\n\n\n\n\n\n\n\n\n");
        }
    }

    private void connect() throws IOException {
        socket = new Socket(host, port);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
        System.out.println(colorText("连接ATM服务器成功", AnsiColor.GREEN));
    }

    private void mainMenu() throws IOException {
        Console console = System.console();
        if (console == null) {
            throw new IOException("无法获取控制台，请在终端中运行");
        }

        while (true) {
            clearScreen();
            System.out.println("\n" + colorText("=== ATM系统 ===", AnsiColor.BLUE));
            System.out.println("1. 注册");
            System.out.println("2. 登录");
            System.out.println("3. 退出");
            System.out.print(colorText("请选择: ", AnsiColor.YELLOW));

            String choice = console.readLine();

            switch (choice) {
                case "1":
                    register(console);
                    break;
                case "2":
                    login(console);
                    break;
                case "3":
                    System.out.println(colorText("感谢使用，再见！", AnsiColor.GREEN));
                    return;
                default:
                    System.out.println(colorText("无效选择，请重试", AnsiColor.RED));
                    waitForKeyPress(console);
            }
        }
    }

    private void register(Console console) throws IOException {
        clearScreen();
        System.out.println(colorText("=== 用户注册 ===", AnsiColor.BLUE));
        System.out.print("用户名: ");
        String username = console.readLine();
        System.out.print("密码: ");
        char[] password = console.readPassword();

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
                System.out.println(colorText("注册成功: " + message, AnsiColor.GREEN));
            } else {
                System.out.println(colorText("注册失败: " + message, AnsiColor.RED));
            }
            waitForKeyPress(console);
        } catch (Exception e) {
            System.out.println(colorText("无法解析响应: " + response, AnsiColor.RED));
            waitForKeyPress(console);
        }
    }

    private void login(Console console) throws IOException {
        clearScreen();
        System.out.println(colorText("=== 用户登录 ===", AnsiColor.BLUE));
        System.out.print("用户名: ");
        String username = console.readLine();
        System.out.print("密码: ");
        char[] password = console.readPassword();

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
                System.out.println(colorText("登录成功: " + message, AnsiColor.GREEN));
                userMenu(console, username);
            } else {
                System.out.println(colorText("登录失败: " + message, AnsiColor.RED));
                waitForKeyPress(console);
            }
        } catch (Exception e) {
            System.out.println(colorText("无法解析响应: " + response, AnsiColor.RED));
            waitForKeyPress(console);
        }
    }

    private void userMenu(Console console, String username) throws IOException {
        while (true) {
            clearScreen();
            System.out.println("\n" + colorText("=== 用户菜单 (" + username + ") ===", AnsiColor.BLUE));
            System.out.println("1. 查询余额");
            System.out.println("2. 存款");
            System.out.println("3. 取款");
            System.out.println("4. 修改密码");
            System.out.println("5. 登出");
            System.out.print(colorText("请选择: ", AnsiColor.YELLOW));

            String choice = console.readLine();
            ObjectNode request = mapper.createObjectNode();
            request.put("username", username);

            switch (choice) {
                case "1":
                    clearScreen();
                    System.out.println(colorText("=== 查询余额 ===", AnsiColor.BLUE));
                    request.put("action", "query");
                    sendRequestAndPrintResponse(request, console);
                    break;
                case "2":
                    clearScreen();
                    System.out.println(colorText("=== 存款 ===", AnsiColor.BLUE));
                    System.out.print("存款金额: ");
                    try {
                        double amount = Double.parseDouble(console.readLine());
                        request.put("action", "deposit");
                        request.put("amount", amount);
                        sendRequestAndPrintResponse(request, console);
                    } catch (NumberFormatException e) {
                        System.out.println(colorText("错误: 请输入有效金额", AnsiColor.RED));
                        waitForKeyPress(console);
                    }
                    break;
                case "3":
                    clearScreen();
                    System.out.println(colorText("=== 取款 ===", AnsiColor.BLUE));
                    System.out.print("取款金额: ");
                    try {
                        double amount = Double.parseDouble(console.readLine());
                        request.put("action", "withdraw");
                        request.put("amount", amount);
                        sendRequestAndPrintResponse(request, console);
                    } catch (NumberFormatException e) {
                        System.out.println(colorText("错误: 请输入有效金额", AnsiColor.RED));
                        waitForKeyPress(console);
                    }
                    break;
                case "4":
                    clearScreen();
                    System.out.println(colorText("=== 修改密码 ===", AnsiColor.BLUE));
                    System.out.print("请输入原密码: ");
                    char[] oldPassword = console.readPassword();
                    System.out.print("请输入新密码: ");
                    char[] newPassword = console.readPassword();
                    System.out.print("请再次输入新密码: ");
                    char[] confirmPassword = console.readPassword();

                    if (new String(newPassword).equals(new String(confirmPassword))) {
                        request.put("action", "change_password");
                        request.put("oldPassword", new String(oldPassword));
                        request.put("newPassword", new String(newPassword));
                        sendRequestAndPrintResponse(request, console);
                    } else {
                        System.out.println(colorText("错误: 两次输入的新密码不一致", AnsiColor.RED));
                        waitForKeyPress(console);
                    }
                    break;
                case "5":
                    request.put("action", "logout");
                    out.println(request.toString());
                    System.out.println(colorText("您已成功登出", AnsiColor.GREEN));
                    waitForKeyPress(console);
                    return;
                default:
                    System.out.println(colorText("无效选择，请重试", AnsiColor.RED));
                    waitForKeyPress(console);
            }
        }
    }

    private void sendRequestAndPrintResponse(ObjectNode request, Console console) throws IOException {
        out.println(request.toString());
        String response = in.readLine();

        try {
            JsonNode jsonResponse = mapper.readTree(response);
            boolean success = jsonResponse.path("status").asText().equals("success");

            if (success) {
                System.out.println(colorText("操作成功", AnsiColor.GREEN));

                // 根据不同的操作打印不同的信息
                String action = request.path("action").asText();
                if ("query".equals(action)) {
                    double balance = jsonResponse.path("balance").asDouble();
                    System.out.println(colorText("当前余额: " + String.format("%.2f", balance) + " 元", AnsiColor.GREEN));
                } else if ("deposit".equals(action) || "withdraw".equals(action)) {
                    double amount = jsonResponse.path("amount").asDouble();
                    double balance = jsonResponse.path("balance").asDouble();
                    String actionName = "deposit".equals(action) ? "存款" : "取款";
                    System.out.println(
                            colorText(actionName + "金额: " + String.format("%.2f", amount) + " 元", AnsiColor.GREEN));
                    System.out.println(colorText("当前余额: " + String.format("%.2f", balance) + " 元", AnsiColor.GREEN));
                }

                // 如果有额外消息，显示它
                if (jsonResponse.has("message")) {
                    System.out.println(colorText(jsonResponse.path("message").asText(), AnsiColor.BLUE));
                }
            } else {
                String message = jsonResponse.path("message").asText();
                System.out.println(colorText("操作失败: " + message, AnsiColor.RED));
            }

            waitForKeyPress(console);
        } catch (Exception e) {
            System.out.println(colorText("无法解析响应: " + response, AnsiColor.RED));
            waitForKeyPress(console);
        }
    }

    /**
     * 等待用户按键继续
     */
    private void waitForKeyPress(Console console) {
        System.out.println(colorText("\n按回车键继续...", AnsiColor.YELLOW));
        console.readLine();
    }

    // ANSI颜色支持
    private enum AnsiColor {
        RED("\u001B[31m"),
        GREEN("\u001B[32m"),
        YELLOW("\u001B[33m"),
        BLUE("\u001B[34m"),
        RESET("\u001B[0m");

        private final String code;

        AnsiColor(String code) {
            this.code = code;
        }
    }

    private String colorText(String text, AnsiColor color) {
        return color.code + text + AnsiColor.RESET.code;
    }
}
