package com.harkerhand.frontend.utils;

import java.util.function.Predicate;

public class InputValidator {
    /**
     * 验证输入字符串是否为空
     * 
     * @param input 输入字符串
     * @return 如果为空或只包含空白字符返回true，否则返回false
     */
    public static boolean isEmpty(String input) {
        return input == null || input.trim().isEmpty();
    }

    /**
     * 验证密码字符数组是否为空
     * 
     * @param password 密码字符数组
     * @return 如果为空或长度为0返回true，否则返回false
     */
    public static boolean isEmpty(char[] password) {
        return password == null || password.length == 0;
    }

    /**
     * 通用验证方法，使用提供的谓词测试输入
     * 
     * @param <T>          输入类型
     * @param input        要验证的输入
     * @param validator    验证谓词
     * @param errorMessage 错误消息
     * @return 如果验证成功返回输入，否则抛出异常
     * @throws IllegalArgumentException 如果输入无效
     */
    public static <T> T validate(T input, Predicate<T> validator, String errorMessage) {
        if (!validator.test(input)) {
            throw new IllegalArgumentException(errorMessage);
        }
        return input;
    }
}
