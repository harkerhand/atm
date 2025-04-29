package com.harkerhand.frontend.utils;

import java.io.Console;

public class ConsoleHelper {

    // ANSI颜色支持
    public enum AnsiColor {
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

    /**
     * 给文本添加颜色
     * 
     * @param text  文本
     * @param color 颜色
     * @return 带颜色的文本
     */
    public static String colorText(String text, AnsiColor color) {
        return color.code + text + AnsiColor.RESET.code;
    }

    /**
     * 清屏方法
     */
    public static void clearScreen() {
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

    /**
     * 等待用户按键继续
     */
    public static void waitForKeyPress(Console console) {
        System.out.println(colorText("\n按回车键继续...", AnsiColor.YELLOW));
        console.readLine();
    }

    /**
     * 安全地读取非空字符串输入
     * 
     * @param console 控制台实例
     * @param prompt  提示信息
     * @return 用户输入的非空字符串
     */
    public static String readNonEmptyString(Console console, String prompt) {
        String input;
        do {
            System.out.print(prompt);
            input = console.readLine();
            if (InputValidator.isEmpty(input)) {
                System.out.println(colorText("错误: 输入不能为空", AnsiColor.RED));
            }
        } while (InputValidator.isEmpty(input));
        return input;
    }

    /**
     * 安全地读取非空密码输入
     * 
     * @param console 控制台实例
     * @param prompt  提示信息
     * @return 用户输入的密码字符数组
     */
    public static char[] readNonEmptyPassword(Console console, String prompt) {
        char[] password;
        do {
            System.out.print(prompt);
            password = console.readPassword();
            if (InputValidator.isEmpty(password)) {
                System.out.println(colorText("错误: 密码不能为空", AnsiColor.RED));
            }
        } while (InputValidator.isEmpty(password));
        return password;
    }
}
