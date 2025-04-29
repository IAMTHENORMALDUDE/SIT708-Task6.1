package com.example.llmexample.models;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface TaskDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Task task);

    @Update
    void update(Task task);

    @Delete
    void delete(Task task);

    @Query("SELECT * FROM tasks WHERE id = :id")
    Task getTaskById(int id);

    @Query("SELECT * FROM tasks WHERE username = :username")
    List<Task> getTasksByUsername(String username);

    @Query("SELECT * FROM tasks WHERE username = :username AND completed = 0")
    List<Task> getIncompleteTasksByUsername(String username);

    @Query("SELECT * FROM tasks WHERE username = :username AND completed = 1")
    List<Task> getCompletedTasksByUsername(String username);
}
