package com.example.videogamesapp;

public class NewGame {
    private int gameId;
    private String gameName;
    private String imageUrl;
    private String description;
    public NewGame(int gameId, String gameName, String imageUrl, String description) {
        this.gameId = gameId;
        this.gameName = gameName;
        this.imageUrl = imageUrl;
        this.description = description;
    }



    public String getGameName() {
        return gameName;
    }
    public int getGameId() {
        return gameId;
    }

    public void setGameId(int gameId) {
        this.gameId = gameId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getDescription() {
        return description;
    }
}