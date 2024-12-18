package com.example.videogamesapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class GameDetailsActivity extends AppCompatActivity {

    private ImageView gameImage;
    private TextView gameTitle;
    private TextView gameDescription;
    private CheckBox favoriteToggle;
    private FirebaseFirestore db;
    private int gameId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_details);


        db = FirebaseFirestore.getInstance();


        gameImage = findViewById(R.id.gameImage);
        gameTitle = findViewById(R.id.gameTitle);
        gameDescription = findViewById(R.id.gameDescription);
        favoriteToggle = findViewById(R.id.favoriteCheckbox);


        Intent intent = getIntent();
        if (intent != null) {
            gameId = intent.getIntExtra("GAME_ID", -1);
            String gameName = intent.getStringExtra("GAME_NAME");
            String imageUrl = intent.getStringExtra("GAME_IMAGE_URL");
            String description = intent.getStringExtra("GAME_DESCRIPTION");


            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(gameName);
            }

            gameTitle.setText(gameName);
            gameDescription.setText(description);
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Picasso.get().load(imageUrl).into(gameImage);
            } else {

            }


            checkIfGameIsFavorite(gameId);
        }

        favoriteToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                addGameToFavorites(gameId);
            } else {
                removeGameFromFavorites(gameId);
            }
        });
    }


    private void checkIfGameIsFavorite(int gameId) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(userId).get().addOnSuccessListener(documentSnapshot -> {

            List<Long> likedGamesLong = (List<Long>) documentSnapshot.get("likedGames");

            List<Integer> likedGamesInt = new ArrayList<>();
            if (likedGamesLong != null) {
                for (Long id : likedGamesLong) {
                    likedGamesInt.add(id.intValue());
                }
            }

            favoriteToggle.setChecked(likedGamesInt.contains(gameId));
        }).addOnFailureListener(e -> Log.e("GameDetailsActivity", "Error fetching favorite games", e));
    }


    private void addGameToFavorites(int gameId) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db.collection("users").document(userId).update("likedGames", FieldValue.arrayUnion(gameId))
                .addOnSuccessListener(aVoid -> Toast.makeText(GameDetailsActivity.this, "Added to favorites", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(GameDetailsActivity.this, "Failed to add to favorites", Toast.LENGTH_SHORT).show());
    }

    private void removeGameFromFavorites(int gameId) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db.collection("users").document(userId).update("likedGames", FieldValue.arrayRemove(gameId))
                .addOnSuccessListener(aVoid -> Toast.makeText(GameDetailsActivity.this, "Removed from favorites", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(GameDetailsActivity.this, "Failed to remove from favorites", Toast.LENGTH_SHORT).show());
    }
}
