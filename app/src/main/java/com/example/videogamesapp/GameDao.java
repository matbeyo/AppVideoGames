package com.example.videogamesapp;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface GameDao {
    @Query("SELECT * FROM game WHERE gameId IN (:gameIds)")
    List<Game> loadAllByIds(int[] gameIds);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(Game... games);

    @Query("DELETE FROM game")
    void deleteAll();
}