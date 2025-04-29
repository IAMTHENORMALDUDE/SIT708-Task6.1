package com.example.llmexample;

import android.app.Application;

import com.example.llmexample.models.AppDatabase;

public class LearningApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        
        // Initialize database
        AppDatabase.getInstance(this);
    }
}
