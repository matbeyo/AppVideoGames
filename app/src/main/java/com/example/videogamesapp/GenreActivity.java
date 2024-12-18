package com.example.videogamesapp;

import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

// Activity that displays games based on selected genre
public class GenreActivity extends AppCompatActivity {

    // List to store games of the selected genre
    private List<NewGame> games = new ArrayList<>();
    
    // RecyclerView to display the list of games
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_genre);

        // Get the selected genre ID and name from the intent
        int genreId = getIntent().getIntExtra("genreId", 0);
        String genreName = getIntent().getStringExtra("genreName");

        // Set the activity title to the genre name
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(genreName);
        }

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Fetch liked games from Firebase and initialize adapter
        fetchLikedGamesAndInitializeAdapter(genreId);
    }

    // Method to fetch games for the selected genre
    private void fetchGames(int genreId, NewGamesAdapter adapter, List<Integer> likedGamesInt) {
        new Thread(() -> {
            try {
                games.clear();

                // Build URL for API request with genre filter
                String urlString = "https://api.rawg.io/api/games?key=MY_KEY&dates=2023-01-01,2023-12-31&ordering=-released&page_size=20&genres=" + genreId;
                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.connect();

                // Read API response
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder builder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }

                // Parse JSON response
                JSONObject response = new JSONObject(builder.toString());
                JSONArray gamesArray = response.getJSONArray("results");
                
                // Create NewGame objects from JSON data
                for (int i = 0; i < gamesArray.length(); i++) {
                    JSONObject game = gamesArray.getJSONObject(i);
                    int gameId = game.getInt("id");
                    String name = game.getString("name");
                    String imageUrl = game.optString("background_image", "");
                    String description = game.optString("description", "");
                    NewGame newGame = new NewGame(gameId, name, imageUrl, description);
                    games.add(newGame);
                }

                // Update adapter on UI thread
                runOnUiThread(() -> {
                    adapter.setGamesAndLikedStatus(games, likedGamesInt);
                });
            } catch (Exception e) {
                Log.e("GenreActivity", "Error fetching games", e);
            }
        }).start();
    }

    // Method to fetch user's liked games from Firebase and initialize the adapter
    private void fetchLikedGamesAndInitializeAdapter(int genreId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("users").document(userId)
                .get()
                .addOnCompleteListener(task -> {
                    List<Integer> likedGamesInt = new ArrayList<>();
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists() && document.get("likedGames") != null) {
                            // Convert Long values to Integer for liked games IDs
                            List<Long> likedGamesLong = (List<Long>) document.get("likedGames");
                            for (Long id : likedGamesLong) {
                                likedGamesInt.add(id.intValue());
                            }
                        }
                    }

                    // Initialize adapter with liked games
                    NewGamesAdapter adapter = new NewGamesAdapter(GenreActivity.this, likedGamesInt);
                    recyclerView.setAdapter(adapter);

                    // Fetch games for the selected genre
                    fetchGames(genreId, adapter, likedGamesInt);
                });
    }
}