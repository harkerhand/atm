package com.harkerhand.backend.service;

import java.util.logging.Logger;
import com.harkerhand.backend.storage.UserStorage;

/**
 * 利息计算服务类
 * 负责定期为所有用户账户添加利息
 */
public class InterestService implements Runnable {
    private static final Logger logger = Logger.getLogger("InterestService");
    private static final double INTEREST_RATE = 0.05; // 5%利息率

    @Override
    public void run() {
        try {
            logger.info("开始计算利息...");
            int updatedAccounts = UserStorage.applyInterestToAllAccounts(INTEREST_RATE);
            logger.info("完成利息计算，更新了" + updatedAccounts + "个账户");
        } catch (Exception e) {
            logger.severe("利息计算过程中发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
