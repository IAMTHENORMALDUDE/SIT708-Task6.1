package com.example.llmexample.models;

import java.io.Serializable;

public class UserProfile implements Serializable {
    private String id;
    private String username;
    private int totalQuestions;
    private int correctAnswers;
    private int incorrectAnswers;

    public UserProfile(String id, String username, int totalQuestions, int correctAnswers, int incorrectAnswers) {
        this.id = id;
        this.username = username;
        this.totalQuestions = totalQuestions;
        this.correctAnswers = correctAnswers;
        this.incorrectAnswers = incorrectAnswers;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getTotalQuestions() {
        return totalQuestions;
    }

    public void setTotalQuestions(int totalQuestions) {
        this.totalQuestions = totalQuestions;
    }

    public int getCorrectAnswers() {
        return correctAnswers;
    }

    public void setCorrectAnswers(int correctAnswers) {
        this.correctAnswers = correctAnswers;
    }

    public int getIncorrectAnswers() {
        return incorrectAnswers;
    }

    public void setIncorrectAnswers(int incorrectAnswers) {
        this.incorrectAnswers = incorrectAnswers;
    }

    public double getCorrectPercentage() {
        if (totalQuestions == 0) return 0;
        return (double) correctAnswers / totalQuestions * 100;
    }

    public String getFormattedCorrectPercentage() {
        return String.format("%.1f%%", getCorrectPercentage());
    }
}
