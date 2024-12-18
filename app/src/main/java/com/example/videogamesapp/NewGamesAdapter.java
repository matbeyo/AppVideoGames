package com.example.videogamesapp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import android.app.Activity;
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
import com.squareup.picasso.Picasso;

/**
 * Adapter class for displaying new games in a RecyclerView.
 * Handles the data collection and binding for both new games and their liked status.
 */
public class NewGamesAdapter extends RecyclerView.Adapter<NewGamesAdapter.ViewHolder> {
    // List to store new games data
    private List<NewGame> newGames = new ArrayList<>();
    // List to track which games are liked by the user
    private List<Integer> likedGames = new ArrayList<>();
    // Reference to the activity for context
    private Activity activity;

    /**
     * Constructor that initializes the adapter with activity context and liked games
     * @param activity The activity context
     * @param likedGames List of game IDs that the user has liked
     */
    public NewGamesAdapter(Activity activity, List<Integer> likedGames) {
        this.activity = activity;
        this.likedGames = likedGames;
        fetchNewGames();
    }

    /**
     * Updates the games list and notifies the adapter of data changes
     */
    public void setGames(List<NewGame> games) {
        this.newGames = games;
        notifyDataSetChanged();
    }

    /**
     * Updates the liked games list and refreshes the UI
     */
    public void updateLikedGames(List<Integer> likedGames) {
        this.likedGames = likedGames;
        notifyDataSetChanged();
    }

    /**
     * Alternative constructor that only requires activity context
     */
    public NewGamesAdapter(Activity activity) {
        this.activity = activity;
        fetchNewGames();
    }

    /**
     * Fetches new games from the RAWG API in a background thread
     * Makes an HTTP request to get games released in 2023
     */
    private void fetchNewGames() {
        new Thread(() -> {
            try {
                // Construct API URL with key and parameters
                String urlString = "https://api.rawg.io/api/games?key=" + BuildConfig.RAWG_API_KEY + 
                                 "&dates=2023-01-01,2023-12-31&ordering=-released&page_size=20";
                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.connect();

                // Read the response
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder builder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }

                // Parse JSON response and create NewGame objects
                JSONObject response = new JSONObject(builder.toString());
                JSONArray gamesArray = response.getJSONArray("results");
                for (int i = 0; i < gamesArray.length(); i++) {
                    JSONObject game = gamesArray.getJSONObject(i);
                    int gameId = game.getInt("id");
                    String name = game.getString("name");
                    String imageUrl = game.optString("background_image", "");
                    String description = game.optString("description", "");
                    NewGame newGame = new NewGame(gameId, name, imageUrl, description);
                    newGames.add(newGame);
                }

                // Preload images using Picasso
                for (NewGame newGame : newGames) {
                    Picasso.get().load(newGame.getImageUrl()).fetch();
                }

                // Update UI on main thread
                activity.runOnUiThread(() -> notifyDataSetChanged());
            } catch (Exception e) {
                Log.e("NewGamesAdapter", "Error fetching new games", e);
            }
        }).start();
    }

    /**
     * Creates new ViewHolder instances for the RecyclerView
     */
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_new_game, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Updates Firestore database with user's liked/unliked games
     */
    private void updateFirestoreLikedGames(FirebaseFirestore db, String userId, int gameId, boolean add) {
        if (add) {
            db.collection("users").document(userId)
                    .update("likedGames", FieldValue.arrayUnion(gameId))
                    .addOnSuccessListener(aVoid -> Log.d("Firestore", "Game added to favorites"))
                    .addOnFailureListener(e -> Log.e("Firestore", "Error adding game to favorites", e));
        } else {
            db.collection("users").document(userId)
                    .update("likedGames", FieldValue.arrayRemove(gameId))
                    .addOnSuccessListener(aVoid -> Log.d("Firestore", "Game removed from favorites"))
                    .addOnFailureListener(e -> Log.e("Firestore", "Error removing game from favorites", e));
        }
    }

    /**
     * Updates both games list and liked status simultaneously
     */
    public void setGamesAndLikedStatus(List<NewGame> games, List<Integer> likedGamesInt) {
        this.newGames = games;
        this.likedGames = likedGamesInt;
        notifyDataSetChanged();
    }

    /**
     * Adds a game to the user's favorites in Firestore
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
    public void onBindViewHolder(ViewHolder holder, int position) {
        NewGame newGame = newGames.get(position);

        // Set basic game information
        holder.tvGameId.setText(newGame.getGameName());
        holder.tvGameDescription.setText(newGame.getDescription());
        Picasso.get().load(newGame.getImageUrl()).resize(100, 100).into(holder.ivGameImage);

        // Reset the checkbox listener
        holder.btnLike.setOnCheckedChangeListener(null);

        // Set the liked status
        boolean isLiked = likedGames.contains(newGame.getGameId());
        holder.btnLike.setChecked(isLiked);

        // Set up the like button listener
        holder.btnLike.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                addGameToFavorites(newGame.getGameId());
            } else {
                removeGameFromFavorites(newGame.getGameId());
            }
        });

        // Set up click listener for the entire item
        holder.itemView.setOnClickListener(v -> {
            // Launch game details activity with game information
            Intent intent = new Intent(activity, GameDetailsActivity.class);
            intent.putExtra("GAME_ID", newGame.getGameId());
            intent.putExtra("GAME_NAME", newGame.getGameName());
            intent.putExtra("GAME_IMAGE_URL", newGame.getImageUrl());
            intent.putExtra("GAME_DESCRIPTION", newGame.getDescription());
            activity.startActivity(intent);
        });
    }

    /**
     * ViewHolder class for caching view references
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvGameId;
        ImageView ivGameImage;
        TextView tvGameDescription;
        CheckBox btnLike;

        public ViewHolder(View itemView) {
            super(itemView);
            tvGameId = itemView.findViewById(R.id.tvGameId);
            ivGameImage = itemView.findViewById(R.id.ivGameImage);
            tvGameDescription = itemView.findViewById(R.id.tvGameDescription);
            btnLike = itemView.findViewById(R.id.btnLike);

            btnLike.setButtonDrawable(R.drawable.heart_checkbox_selector);
        }
    }

    /**
     * Utility class for managing boolean values
     */
    public class MutableBoolean {
        private boolean value;

        public MutableBoolean(boolean value) {
            this.value = value;
        }

        public boolean getValue() {
            return value;
        }

        public void setValue(boolean value) {
            this.value = value;
        }
    }

    /**
     * Returns the total number of items in the adapter
     */
    @Override
    public int getItemCount() {
        return newGames.size();
    }
}