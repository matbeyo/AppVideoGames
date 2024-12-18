package com.example.videogamesapp;

import android.content.Intent;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import android.util.Log;
import android.widget.Button;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


// Main activity that serves as the home screen of the application
public class MainActivity extends AppCompatActivity {
// Firebase authentication instance
    private FirebaseAuth auth;
    private FirebaseUser user;
// RecyclerViews for different game lists
    private RecyclerView featuredGamesRecyclerView, genresRecyclerView, newGamesRecyclerView;
    private Button logOffButton, favoritesButton, searchButton;

    private NewGamesAdapter newGamesAdapter;
    private FeaturedGamesAdapter featuredGamesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        if (user == null) {
            navigateToLogin();
        } else {
            initializeUserData();
        }

        setupRecyclerViews();
        setupButtons();
        fetchLikedGamesAndUpdateAdapters();
    }

    private void initializeUserData() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = user.getUid();
        db.collection("users").document(userId).get().addOnCompleteListener(task -> {
            if (!task.isSuccessful() || task.getResult() == null || !task.getResult().exists()) {
                Map<String, Object> userData = new HashMap<>();
                userData.put("likedGames", new ArrayList<>());
                db.collection("users").document(userId)
                        .set(userData)
                        .addOnSuccessListener(aVoid -> Log.d("MainActivity", "User document initialized"))
                        .addOnFailureListener(e -> Log.w("MainActivity", "Error initializing user document", e));
            }
        });
    }

    private void setupRecyclerViews() {
        featuredGamesRecyclerView = findViewById(R.id.featuredGamesRecyclerView);
        genresRecyclerView = findViewById(R.id.genresRecyclerView);
        newGamesRecyclerView = findViewById(R.id.newGamesRecyclerView);


        featuredGamesRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        genresRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        newGamesRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        GenresAdapter genresAdapter = new GenresAdapter();
        genresRecyclerView.setAdapter(genresAdapter);

        featuredGamesAdapter = new FeaturedGamesAdapter(MainActivity.this, new ArrayList<>());
        featuredGamesRecyclerView.setAdapter(featuredGamesAdapter);


        newGamesAdapter = new NewGamesAdapter(MainActivity.this, new ArrayList<>());
        newGamesRecyclerView.setAdapter(newGamesAdapter);

        fetchLikedGamesAndUpdateAdapters();
    }



    private void setupButtons() {
        logOffButton = findViewById(R.id.logOffButton);
        favoritesButton = findViewById(R.id.favoritesButton);
        searchButton = findViewById(R.id.searchButton);

        logOffButton.setOnClickListener(view -> {
            FirebaseAuth.getInstance().signOut();
            navigateToLogin();
        });

        favoritesButton.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, FavoritesActivity.class)));
        searchButton.setOnClickListener(view -> {

        });
        searchButton.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, SearchActivity.class);
            startActivity(intent);
        });
    }

    private void navigateToLogin() {
        startActivity(new Intent(getApplicationContext(), Login.class));
        finish();
    }

    private void fetchLikedGamesAndUpdateAdapters() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("users").document(userId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    List<Long> likedGamesLong = (List<Long>) document.get("likedGames");
                    List<Integer> likedGamesInt = new ArrayList<>();
                    if (likedGamesLong != null) {
                        for (Long id : likedGamesLong) {
                            likedGamesInt.add(id.intValue());
                        }
                    }

                    newGamesAdapter = new NewGamesAdapter(MainActivity.this, likedGamesInt);
                    newGamesRecyclerView.setAdapter(newGamesAdapter);

                    featuredGamesAdapter = new FeaturedGamesAdapter(MainActivity.this, likedGamesInt);
                    featuredGamesRecyclerView.setAdapter(featuredGamesAdapter);
                } else {
                    Log.d("MainActivity", "No such document");
                }
            } else {
                Log.d("MainActivity", "get failed with ", task.getException());
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchLikedGamesAndUpdateAdapters();
    }
}
