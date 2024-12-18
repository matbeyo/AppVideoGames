package com.example.videogamesapp;
public class FeaturedGame {
    private int gameId;
    private String gameName;
    private String imageUrl;

    public FeaturedGame(int gameId, String gameName, String imageUrl) {
        this.gameId = gameId;
        this.gameName = gameName;
        this.imageUrl = imageUrl;
    }



    public String getGameName() {
        return gameName;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }


    public FeaturedGame(int gameId, String imageUrl) {
        this.gameId = gameId;
        this.imageUrl = imageUrl;
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
}
