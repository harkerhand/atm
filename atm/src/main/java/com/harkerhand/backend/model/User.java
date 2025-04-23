package com.harkerhand.backend.model;

import java.io.Serializable;

public class User implements Serializable {
    // 添加序列化版本ID
    private static final long serialVersionUID = 1L;

    private String username;
    private String password;
    private double balance;

    // 如果有不需要序列化的字段，使用transient修饰
    // transient private SomeNonSerializableType someField;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.balance = 0.0;
    }

    public User(String username, String password, double balance) {
        this.username = username;
        this.password = password;
        this.balance = balance;
    }

    public String getUsername() {
        return username;
    }

    public boolean validatePassword(String password) {
        return this.password.equals(password);
    }

    public String getPassword() {
        return password;
    }

    public double getBalance() {
        return balance;
    }

    public void deposit(double amount) {
        if (amount > 0) {
            this.balance += amount;
        }
    }

    public boolean withdraw(double amount) {
        if (amount > 0 && this.balance >= amount) {
            this.balance -= amount;
            return true;
        }
        return false;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }
}