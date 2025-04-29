package com.example.llmexample.models;

import java.util.List;

public class QuizQuestion {
    private String question;
    private List<String> options;
    private String correctAnswer;
    private String userAnswer; // To track user's selected answer

    public QuizQuestion(String question, List<String> options, String correctAnswer) {
        this.question = question;
        this.options = options;
        this.correctAnswer = correctAnswer;
        this.userAnswer = null; // Initially no answer selected
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public List<String> getOptions() {
        return options;
    }

    public void setOptions(List<String> options) {
        this.options = options;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
    }
    
    public String getUserAnswer() {
        return userAnswer;
    }

    public void setUserAnswer(String userAnswer) {
        this.userAnswer = userAnswer;
    }
    
    public boolean isCorrect() {
        return userAnswer != null && userAnswer.equals(correctAnswer);
    }
}
