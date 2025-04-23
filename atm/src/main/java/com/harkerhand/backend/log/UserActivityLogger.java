package com.harkerhand.backend.log;

import java.io.File;
import java.io.IOException;
import java.util.logging.*;

/**
 * 用户活动日志记录器
 * 负责记录所有用户的操作活动
 */
public class UserActivityLogger {
    private static final Logger logger = Logger.getLogger("UserActivity");
    private static FileHandler fileHandler;
    private static final String LOG_FOLDER = "logs";
    private static final String USER_ACTIVITY_LOG_FILE = "user_activity.log";

    /**
     * 初始化用户活动日志系统
     */
    public static void initialize() {
        try {
            // 确保日志目录存在
            File logDir = new File(LOG_FOLDER);
            if (!logDir.exists()) {
                logDir.mkdirs();
            }

            // 创建专门的用户活动日志处理器
            fileHandler = new FileHandler(LOG_FOLDER + File.separator + USER_ACTIVITY_LOG_FILE, true);
            SimpleFormatter formatter = new SimpleFormatter();
            fileHandler.setFormatter(formatter);

            // 确保移除之前的处理器
            for (Handler handler : logger.getHandlers()) {
                logger.removeHandler(handler);
            }

            logger.addHandler(fileHandler);
            logger.setUseParentHandlers(false); // 不使用父日志记录器的处理器
            logger.setLevel(Level.INFO);

            logger.info("User activity logging initialized");
        } catch (IOException e) {
            System.err.println("Failed to initialize user activity logger: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 记录用户登录活动
     * 
     * @param username  用户名
     * @param ipAddress IP地址
     * @param success   是否成功
     */
    public static void logLogin(String username, String ipAddress, boolean success) {
        String status = success ? "successful" : "failed";
        logger.info(String.format("LOGIN: User '%s' from %s - %s", username, ipAddress, status));
    }

    /**
     * 记录用户查询余额
     * 
     * @param username 用户名
     */
    public static void logBalanceCheck(String username) {
        logger.info(String.format("BALANCE CHECK: User '%s' checked account balance", username));
    }

    /**
     * 记录用户存款
     * 
     * @param username 用户名
     * @param amount   金额
     */
    public static void logDeposit(String username, double amount) {
        logger.info(String.format("DEPOSIT: User '%s' deposited %.2f", username, amount));
    }

    /**
     * 记录用户取款
     * 
     * @param username 用户名
     * @param amount   金额
     * @param success  是否成功
     */
    public static void logWithdrawal(String username, double amount, boolean success) {
        String status = success ? "successful" : "failed";
        logger.info(String.format("WITHDRAWAL: User '%s' attempted to withdraw %.2f - %s",
                username, amount, status));
    }

    /**
     * 记录用户密码修改
     * 
     * @param username 用户名
     * @param success  是否成功
     */
    public static void logPasswordChange(String username, boolean success) {
        String status = success ? "successful" : "failed";
        logger.info(String.format("PASSWORD CHANGE: User '%s' - %s", username, status));
    }

    /**
     * 记录用户登出
     * 
     * @param username 用户名
     */
    public static void logLogout(String username) {
        logger.info(String.format("LOGOUT: User '%s' logged out", username));
    }

    /**
     * 记录一般用户操作
     * 
     * @param username 用户名
     * @param action   操作描述
     */
    public static void logUserAction(String username, String action) {
        logger.info(String.format("ACTION: User '%s' - %s", username, action));
    }

    /**
     * 记录利息添加事件
     *
     * @param username    用户名
     * @param action      动作类型
     * @param description 描述信息
     */
    public static void logInterestAddition(String username, String action, String description) {
        logger.info(String.format("INTEREST: User '%s' - %s - %s", username, action, description));
    }

    /**
     * 关闭日志系统
     */
    public static void shutdown() {
        if (fileHandler != null) {
            fileHandler.close();
            logger.info("User activity logging system shutdown");
        }
    }
}
