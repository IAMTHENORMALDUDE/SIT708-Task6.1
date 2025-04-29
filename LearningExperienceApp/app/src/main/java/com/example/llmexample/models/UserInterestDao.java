package com.example.llmexample.models;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface UserInterestDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(UserInterest userInterest);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<UserInterest> userInterests);

    @Delete
    void delete(UserInterest userInterest);

    @Query("DELETE FROM user_interests WHERE username = :username")
    void deleteAllUserInterests(String username);

    @Query("SELECT interest FROM user_interests WHERE username = :username")
    List<String> getUserInterests(String username);

    @Query("SELECT COUNT(*) FROM user_interests WHERE username = :username")
    int countUserInterests(String username);
}
