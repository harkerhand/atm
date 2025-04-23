package com.harkerhand.backend.model;

import java.io.Serializable;
import com.harkerhand.backend.utils.PasswordUtils;

public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    private String username;
    private String password;
    private String salt;
    private double balance;

    public User(String username, String password) {
        this.username = username;
        this.salt = PasswordUtils.generateSalt();
        this.password = PasswordUtils.hashPassword(password, salt);
        this.balance = 0.0;
    }

    public User(String username, String password, double balance) {
        this.username = username;
        this.salt = PasswordUtils.generateSalt();
        this.password = PasswordUtils.hashPassword(password, salt);
        this.balance = balance;
    }

    public String getUsername() {
        return username;
    }

    public boolean validatePassword(String password) {
        return this.password.equals(PasswordUtils.hashPassword(password, salt));
    }

    public String getSalt() {
        return salt;
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

    public void setPassword(String newPassword) {
        this.salt = PasswordUtils.generateSalt();
        this.password = PasswordUtils.hashPassword(newPassword, salt);
    }
}