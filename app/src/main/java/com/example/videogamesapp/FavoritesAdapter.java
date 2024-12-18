package com.example.videogamesapp;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Adapter class for managing and displaying favorite games in a RecyclerView.
 * Handles the display of game information and like/unlike functionality.
 */
public class FavoritesAdapter extends RecyclerView.Adapter<FavoritesAdapter.ViewHolder> {
    // List to store the user's liked games
    private List<NewGame> likedGames;
    // Reference to the activity context
    private Activity activity;

    /**
     * Updates the list of liked games and refreshes the view
     * @param likedGames New list of games to display
     */
    public void setLikedGames(List<NewGame> likedGames) {
        this.likedGames = likedGames;
        notifyDataSetChanged();
    }

    /**
     * Constructor for the FavoritesAdapter
     * @param activity The activity context
     * @param likedGames Initial list of liked games
     */
    public FavoritesAdapter(Activity activity, List<NewGame> likedGames) {
        this.activity = activity;
        this.likedGames = likedGames;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Inflate the layout for each game item
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_new_game, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // Get the current game from the list
        NewGame likedGame = likedGames.get(position);
        
        // Set the game details in the view
        holder.tvGameId.setText(likedGame.getGameName());
        holder.tvGameDescription.setText(likedGame.getDescription());
        // Load and resize the game image using Picasso
        Picasso.get().load(likedGame.getImageUrl()).resize(100, 100).into(holder.ivGameImage);

        // Set the like button to checked since this is a favorites view
        holder.btnLike.setChecked(true);

        // Set up the like button behavior
        holder.btnLike.setOnCheckedChangeListener(null);
        holder.btnLike.setOnCheckedChangeListener((buttonView, isChecked) -> {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

            // If the user unlikes a game, remove it from their favorites in Firestore
            if (!isChecked) {
                db.collection("users").document(userId)
                        .update("likedGames", FieldValue.arrayRemove(likedGame.getGameId()))
                        .addOnSuccessListener(aVoid -> Log.d("FavoritesActivity", "Game removed from favorites"))
                        .addOnFailureListener(e -> Log.e("FavoritesActivity", "Error removing game from favorites", e));
            }
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
            // Initialize views
            tvGameId = itemView.findViewById(R.id.tvGameId);
            ivGameImage = itemView.findViewById(R.id.ivGameImage);
            tvGameDescription = itemView.findViewById(R.id.tvGameDescription);
            btnLike = itemView.findViewById(R.id.btnLike);
        }
    }

    @Override
    public int getItemCount() {
        return likedGames.size();
    }
}