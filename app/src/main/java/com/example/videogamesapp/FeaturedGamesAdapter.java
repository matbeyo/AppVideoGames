package com.example.videogamesapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.recyclerview.widget.RecyclerView;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

/**
 * Adapter class for handling the display and interaction of featured games in a RecyclerView.
 * This adapter manages game data fetching, caching, and user interactions like favoriting games.
 */
public class FeaturedGamesAdapter extends RecyclerView.Adapter<FeaturedGamesAdapter.ViewHolder> {
    // List to store featured game data
    private List<FeaturedGame> featuredGames = new ArrayList<>();
    private Activity activity;
    // List to track games liked by the user
    private List<Integer> likedGames;

    // Constants for caching
    private static final String CACHE_KEY = "featuredGames";
    private static final String GAMES_CACHE = "gamesCache";

    /**
     * Constructor initializes the adapter with necessary context and user's liked games
     * @param activity Activity context for UI operations
     * @param likedGames List of game IDs that the user has liked
     */
    public FeaturedGamesAdapter(Activity activity, List<Integer> likedGames) {
        this.activity = activity;
        this.likedGames = likedGames;
        loadFeaturedGamesFromCache();
        fetchFeaturedGames();
    }

    /**
     * Loads previously cached featured games from SharedPreferences
     * This provides immediate content while fresh data is being fetched
     */
    private void loadFeaturedGamesFromCache() {
        String json = activity.getSharedPreferences(GAMES_CACHE, Context.MODE_PRIVATE).getString(CACHE_KEY, null);
        if (json != null) {
            try {
                JSONArray gamesArray = new JSONArray(json);
                for (int i = 0; i < gamesArray.length(); i++) {
                    JSONObject game = gamesArray.getJSONObject(i);
                    int gameId = game.getInt("game_id");
                    String imageUrl = game.optString("image", "");
                    featuredGames.add(new FeaturedGame(gameId, imageUrl));
                }
            } catch (Exception e) {
                Log.e("FeaturedGamesAdapter", "Error loading games from cache", e);
            }
        }
    }

    /**
     * Fetches featured games from the RAWG API
     * Runs in a background thread to avoid blocking the UI
     */
    private void fetchFeaturedGames() {
        new Thread(() -> {
            try {
                String urlString = "https://api.rawg.io/api/games?key=" + BuildConfig.RAWG_API_KEY + "&page_size=20";
                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.connect();

                // Read the API response
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder builder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }

                // Parse JSON response and create FeaturedGame objects
                JSONObject response = new JSONObject(builder.toString());
                JSONArray gamesArray = response.getJSONArray("results");
                for (int i = 0; i < gamesArray.length(); i++) {
                    JSONObject game = gamesArray.getJSONObject(i);
                    int gameId = game.getInt("id");
                    String name = game.getString("name");
                    String imageUrl = game.optString("background_image", "");
                    FeaturedGame featuredGame = new FeaturedGame(gameId, name, imageUrl);
                    featuredGames.add(featuredGame);
                }

                // Pre-fetch images for smoother scrolling
                for (FeaturedGame featuredGame : featuredGames) {
                    Picasso.get().load(featuredGame.getImageUrl()).fetch();
                }

                // Update UI on the main thread
                activity.runOnUiThread(() -> notifyDataSetChanged());
            } catch (Exception e) {
                Log.e("FeaturedGamesAdapter", "Error fetching games from RAWG", e);
            }
        }).start();
    }

    /**
     * Creates new ViewHolder instances for the RecyclerView
     */
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_featured_game, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Adds a game to the user's favorites in Firestore
     * @param gameId ID of the game to be added to favorites
     */
    private void addGameToFavorites(int gameId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db.collection("users").document(userId)
                .update("likedGames", FieldValue.arrayUnion(gameId))
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "Game added to favorites"))
                .addOnFailureListener(e -> Log.e("Firestore", "Error adding game to favorites", e));
    }

    /**
     * Removes a game from the user's favorites in Firestore
     * @param gameId ID of the game to be removed from favorites
     */
    private void removeGameFromFavorites(int gameId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db.collection("users").document(userId)
                .update("likedGames", FieldValue.arrayRemove(gameId))
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "Game removed from favorites"))
                .addOnFailureListener(e -> Log.e("Firestore", "Error removing game from favorites", e));
    }

    /**
     * Binds game data to the ViewHolder and sets up click listeners
     */
    @Override
    public void onBindViewHolder(FeaturedGamesAdapter.ViewHolder holder, int position) {
        FeaturedGame featuredGame = featuredGames.get(position);
        holder.tvGameId.setText(featuredGame.getGameName());
        Picasso.get().load(featuredGame.getImageUrl()).resize(100, 100).into(holder.ivGameImage);

        // Reset the CheckBox listener to prevent unwanted triggers
        holder.btnLike.setOnCheckedChangeListener(null);

        // Set the initial state of the like button
        boolean isLiked = likedGames.contains(featuredGame.getGameId());
        holder.btnLike.setChecked(isLiked);

        // Set up the like button listener
        holder.btnLike.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                addGameToFavorites(featuredGame.getGameId());
            } else {
                removeGameFromFavorites(featuredGame.getGameId());
            }
        });

        // Set up click listener to open game details
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(activity, GameDetailsActivity.class);
            intent.putExtra("GAME_ID", featuredGame.getGameId());
            intent.putExtra("GAME_NAME", featuredGame.getGameName());
            intent.putExtra("GAME_IMAGE_URL", featuredGame.getImageUrl());
            intent.putExtra("GAME_DESCRIPTION", "");
            activity.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return featuredGames.size();
    }

    /**
     * ViewHolder class for caching view references
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvGameId;
        ImageView ivGameImage;
        CheckBox btnLike;

        public ViewHolder(View itemView) {
            super(itemView);
            tvGameId = itemView.findViewById(R.id.tvGameId);
            ivGameImage = itemView.findViewById(R.id.ivGameImage);
            btnLike = itemView.findViewById(R.id.btnLike);

            btnLike.setButtonDrawable(R.drawable.heart_checkbox_selector);
        }
    }
}