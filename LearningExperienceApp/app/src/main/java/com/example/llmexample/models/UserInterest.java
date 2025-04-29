package com.example.llmexample.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

@Entity(tableName = "user_interests",
        primaryKeys = {"username", "interest"},
        foreignKeys = @ForeignKey(entity = User.class,
                parentColumns = "username",
                childColumns = "username",
                onDelete = ForeignKey.CASCADE),
        indices = {@Index("username")})
public class UserInterest {
    @NonNull
    private String username;
    @NonNull
    private String interest;

    public UserInterest(@NonNull String username, @NonNull String interest) {
        this.username = username;
        this.interest = interest;
    }

    @NonNull
    public String getUsername() {
        return username;
    }

    public void setUsername(@NonNull String username) {
        this.username = username;
    }

    @NonNull
    public String getInterest() {
        return interest;
    }

    public void setInterest(@NonNull String interest) {
        this.interest = interest;
    }
}
