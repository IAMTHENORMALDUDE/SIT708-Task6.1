package com.example.llmexample.models;

import java.io.Serializable;

public class Purchase implements Serializable {
    private String id;
    private String userId;
    private String packageType;
    private String paymentMethod;
    private double amount;
    private String createdAt;

    public Purchase(String id, String userId, String packageType, String paymentMethod, double amount, String createdAt) {
        this.id = id;
        this.userId = userId;
        this.packageType = packageType;
        this.paymentMethod = paymentMethod;
        this.amount = amount;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPackageType() {
        return packageType;
    }

    public void setPackageType(String packageType) {
        this.packageType = packageType;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
