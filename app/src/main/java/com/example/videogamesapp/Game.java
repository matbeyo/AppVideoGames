package com.example.videogamesapp;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Game {
    @PrimaryKey
    public int gameId;
    public String gameName;
    public String imageUrl;
    public String description;

    public Game(int gameId, String gameName, String imageUrl, String description) {
        this.gameId = gameId;
        this.gameName = gameName;
        this.imageUrl = imageUrl;
        this.description = description;
    }

}