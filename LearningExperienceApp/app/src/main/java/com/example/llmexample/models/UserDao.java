package com.example.llmexample.models;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(User user);

    @Update
    void update(User user);

    @Delete
    void delete(User user);

    @Query("SELECT * FROM users WHERE username = :username")
    User getUserByUsername(String username);

    @Query("SELECT * FROM users WHERE username = :username AND password = :password")
    User login(String username, String password);

    @Query("SELECT * FROM users")
    List<User> getAllUsers();
}
