package com.example.llmexample.models;

import java.io.Serializable;

public class QuizHistory implements Serializable {
    private String id;
    private String userId;
    private String topic;
    private int score;
    private int totalQuestions;
    private String createdAt;

    public QuizHistory(String id, String userId, String topic, int score, int totalQuestions, String createdAt) {
        this.id = id;
        this.userId = userId;
        this.topic = topic;
        this.score = score;
        this.totalQuestions = totalQuestions;
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

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getTotalQuestions() {
        return totalQuestions;
    }

    public void setTotalQuestions(int totalQuestions) {
        this.totalQuestions = totalQuestions;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public double getPercentage() {
        if (totalQuestions == 0) return 0;
        return (double) score / totalQuestions * 100;
    }

    public String getFormattedPercentage() {
        return String.format("%.1f%%", getPercentage());
    }

    public boolean isPassed() {
        return getPercentage() >= 70;
    }
}
