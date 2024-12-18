package com.example.videogamesapp;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.firebase.firestore.EventListener;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Activity that displays the user's favorite games.
 * Handles fetching and displaying games that the user has marked as favorites.
 */
public class FavoritesActivity extends AppCompatActivity {
    // UI Components
    RecyclerView favoritesRecyclerView;
    FavoritesAdapter favoritesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);
        setTitle("Favorites");
        
        // Initialize RecyclerView
        favoritesRecyclerView = findViewById(R.id.favoritesRecyclerView);
        favoritesRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Get references to Firebase services
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Set up real-time listener for favorite games changes
        db.collection("users").document(userId)
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot snapshot,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w("FavoritesActivity", "Listen failed.", e);
                            return;
                        }

                        if (snapshot != null && snapshot.exists()) {
                            // Get the list of liked game IDs and fetch their details
                            List<Long> likedGameIds = (List<Long>) snapshot.get("likedGames");
                            fetchLikedGames(likedGameIds);
                        } else {
                            Log.d("FavoritesActivity", "Current data: null");
                        }
                    }
                });
    }

    /**
     * Fetches detailed information for each liked game from the RAWG API.
     * Uses a thread pool to fetch multiple games concurrently.
     * 
     * @param likedGameIds List of game IDs that the user has liked
     */
    private void fetchLikedGames(List<Long> likedGameIds) {
        // Handle case when there are no liked games
        if (likedGameIds == null || likedGameIds.isEmpty()) {
            if (favoritesAdapter == null) {
                favoritesAdapter = new FavoritesAdapter(FavoritesActivity.this, new ArrayList<>());
                favoritesRecyclerView.setAdapter(favoritesAdapter);
            } else {
                favoritesAdapter.setLikedGames(new ArrayList<>());
                favoritesAdapter.notifyDataSetChanged();
            }
            return;
        }

        final List<NewGame> likedGames = new ArrayList<>();
        // Create thread pool with maximum of 5 threads to avoid overwhelming the API
        ExecutorService executor = Executors.newFixedThreadPool(Math.min(likedGameIds.size(), 5));
        // Use CountDownLatch to track when all API calls are complete
        CountDownLatch latch = new CountDownLatch(likedGameIds.size());

        // Fetch details for each game ID in parallel
        for (Long gameId : likedGameIds) {
            executor.execute(() -> {
                try {
                    // Make API call to RAWG
                    URL url = new URL("https://api.rawg.io/api/games/" + gameId + "?key=" + BuildConfig.RAWG_API_KEY);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    
                    // Read API response
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder builder = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        builder.append(line);
                    }
                    JSONObject response = new JSONObject(builder.toString());

                    // Extract game details from response
                    String name = response.getString("name");
                    String imageUrl = response.optString("background_image", "");
                    String description = response.optString("description", "");

                    // Thread-safe addition to the liked games list
                    synchronized (likedGames) {
                        likedGames.add(new NewGame(gameId.intValue(), name, imageUrl, description));
                    }
                } catch (Exception e) {
                    Log.e("FavoritesActivity", "Error fetching game details for ID " + gameId + ": " + e.getMessage(), e);
                } finally {
                    latch.countDown();
                }
            });
        }

        // Shut down the executor after submitting all tasks
        executor.shutdown();
        
        // Wait for all API calls to complete and update UI
        new Thread(() -> {
            try {
                latch.await();
                runOnUiThread(() -> {
                    favoritesAdapter = new FavoritesAdapter(FavoritesActivity.this, likedGames);
                    favoritesRecyclerView.setAdapter(favoritesAdapter);
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                Log.e("FavoritesActivity", "Interrupted while waiting for game details fetch to complete", e);
            }
        }).start();
    }
}