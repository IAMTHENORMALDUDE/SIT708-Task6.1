package com.example.llmexample.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "tasks",
        foreignKeys = @ForeignKey(entity = User.class,
                parentColumns = "username",
                childColumns = "username",
                onDelete = ForeignKey.CASCADE),
        indices = {@Index("username")})
public class Task {
    @PrimaryKey(autoGenerate = true)
    private int id;
    @NonNull
    private String username;
    private String title;
    private String description;
    private String topic;
    private boolean completed;

    public Task(@NonNull String username, String title, String description, String topic) {
        this.username = username;
        this.title = title;
        this.description = description;
        this.topic = topic;
        this.completed = false;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @NonNull
    public String getUsername() {
        return username;
    }

    public void setUsername(@NonNull String username) {
        this.username = username;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}
