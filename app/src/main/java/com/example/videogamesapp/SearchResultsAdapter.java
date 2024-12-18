package com.example.videogamesapp;
import com.google.firebase.firestore.FieldValue;
import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.List;

public class SearchResultsAdapter extends RecyclerView.Adapter<SearchResultsAdapter.ViewHolder> {
    private List<NewGame> searchResults;
    private Activity activity;
    private List<Integer> likedGames;

    public SearchResultsAdapter(Activity activity, List<NewGame> searchResults, List<Integer> likedGames) {
        this.activity = activity;
        this.searchResults = searchResults;
        this.likedGames = likedGames;
    }

    public void updateSearchResultsAndLikedGames(List<NewGame> searchResults, List<Integer> likedGames) {
        this.searchResults = searchResults;
        this.likedGames = likedGames;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_new_game, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        NewGame game = searchResults.get(position);
        holder.gameName.setText(game.getGameName());
        holder.gameDescription.setText(game.getDescription());
        Picasso.get().load(game.getImageUrl()).into(holder.gameImage);


        boolean isLiked = likedGames.contains(game.getGameId());
        holder.likeCheckBox.setChecked(isLiked);

        holder.likeCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                addGameToFavorites(game.getGameId());
            } else {
                removeGameFromFavorites(game.getGameId());
            }
        });


        holder.itemView.setOnClickListener(v -> {

            Intent intent = new Intent(activity, GameDetailsActivity.class);

            intent.putExtra("GAME_ID", game.getGameId());
            intent.putExtra("GAME_NAME", game.getGameName());
            intent.putExtra("GAME_IMAGE_URL", game.getImageUrl());
            intent.putExtra("GAME_DESCRIPTION", game.getDescription());

            activity.startActivity(intent);
        });
    }


    private void addGameToFavorites(int gameId) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore.getInstance().collection("users").document(userId)
                .update("likedGames", FieldValue.arrayUnion(gameId));
    }

    private void removeGameFromFavorites(int gameId) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore.getInstance().collection("users").document(userId)
                .update("likedGames", FieldValue.arrayRemove(gameId));
    }

    @Override
    public int getItemCount() {
        return searchResults.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView gameName;
        TextView gameDescription;
        ImageView gameImage;
        CheckBox likeCheckBox;

        ViewHolder(View itemView) {
            super(itemView);
            gameName = itemView.findViewById(R.id.tvGameId);
            gameDescription = itemView.findViewById(R.id.tvGameDescription);
            gameImage = itemView.findViewById(R.id.ivGameImage);
            likeCheckBox = itemView.findViewById(R.id.btnLike);
        }
    }
}
