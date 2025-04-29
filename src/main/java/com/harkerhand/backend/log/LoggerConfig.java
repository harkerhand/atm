package com.harkerhand.backend.log;

import java.io.File;
import java.io.IOException;
import java.util.logging.*;

public class LoggerConfig {
    private static final String LOG_FOLDER = "logs";
    private static final String SYSTEM_LOG_FILE = "system.log";

    public static void setup() throws IOException {
        // 确保日志目录存在
        File logDir = new File(LOG_FOLDER);
        if (!logDir.exists()) {
            logDir.mkdirs();
        }

        // 创建日志格式化器
        SimpleFormatter formatter = new SimpleFormatter();

        // 配置根日志记录器
        Logger rootLogger = Logger.getLogger("");

        // 移除所有已有的处理器
        for (Handler handler : rootLogger.getHandlers()) {
            rootLogger.removeHandler(handler);
        }

        // 添加控制台处理器
        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setFormatter(formatter);
        consoleHandler.setLevel(Level.INFO);
        rootLogger.addHandler(consoleHandler);

        // 添加文件处理器，将所有系统日志都输出到统一的文件
        FileHandler fileHandler = new FileHandler(LOG_FOLDER + File.separator + SYSTEM_LOG_FILE, true);
        fileHandler.setFormatter(formatter);
        fileHandler.setLevel(Level.ALL);
        rootLogger.addHandler(fileHandler);

        // 设置根日志级别
        rootLogger.setLevel(Level.INFO);
    }
}