package com.example.videogamesapp;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.recyclerview.widget.RecyclerView;
import java.util.Arrays;
import java.util.List;

public class GenresAdapter extends RecyclerView.Adapter<GenresAdapter.ViewHolder> {

    private List<Genre> genres = Arrays.asList(
        new Genre(4, "Action"),
        new Genre(51, "Indie"),
        new Genre(3, "Adventure"),
        new Genre(5, "RPG"),
        new Genre(14, "Simulation"),
        new Genre(7, "Puzzle"),
        new Genre(11, "Arcade"),
        new Genre(83, "Platformer"),
        new Genre(1, "Racing"),
        new Genre(15, "Sports")
    );

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_genre, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Genre genre = genres.get(position);
        holder.genreButton.setText(genre.getName());
        holder.genreButton.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), GenreActivity.class);
            intent.putExtra("genreId", genre.getId());
            intent.putExtra("genreName", genre.getName());
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return genres.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        Button genreButton;

        public ViewHolder(View itemView) {
            super(itemView);
            genreButton = itemView.findViewById(R.id.genreButton);
        }
    }

    public static class Genre {
        private int id;
        private String name;

        public Genre(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }
}