package com.harkerhand.frontend.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.harkerhand.frontend.ui.AuthUI;
import com.harkerhand.frontend.ui.UserUI;
import com.harkerhand.frontend.utils.ConsoleHelper;
import com.harkerhand.frontend.utils.ConsoleHelper.AnsiColor;

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
    private AuthUI authUI;
    private UserUI userUI;

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

    private void connect() throws IOException {
        socket = new Socket(host, port);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
        System.out.println(ConsoleHelper.colorText("连接ATM服务器成功", AnsiColor.GREEN));

        // 初始化UI组件
        authUI = new AuthUI(in, out, mapper);
        userUI = new UserUI(in, out, mapper);
    }

    private void mainMenu() throws IOException {
        Console console = System.console();
        if (console == null) {
            throw new IOException("无法获取控制台，请在终端中运行");
        }

        while (true) {
            ConsoleHelper.clearScreen();
            System.out.println("\n" + ConsoleHelper.colorText("=== ATM系统 ===", AnsiColor.BLUE));
            System.out.println("1. 注册");
            System.out.println("2. 登录");
            System.out.println("3. 退出");
            System.out.print(ConsoleHelper.colorText("请选择: ", AnsiColor.YELLOW));

            String choice = console.readLine();

            switch (choice) {
                case "1":
                    authUI.register(console);
                    break;
                case "2":
                    String loggedInUser = authUI.login(console);
                    if (loggedInUser != null) {
                        userUI.userMenu(console, loggedInUser);
                    }
                    break;
                case "3":
                    System.out.println(ConsoleHelper.colorText("感谢使用，再见！", AnsiColor.GREEN));
                    return;
                default:
                    System.out.println(ConsoleHelper.colorText("无效选择，请重试", AnsiColor.RED));
                    ConsoleHelper.waitForKeyPress(console);
            }
        }
    }
}
