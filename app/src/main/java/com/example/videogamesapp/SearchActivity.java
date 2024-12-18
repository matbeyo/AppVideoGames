package com.example.videogamesapp;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Activity that handles game searching functionality with optional genre filtering.
 * Uses the RAWG Video Games Database API to fetch search results.
 */
public class SearchActivity extends AppCompatActivity {
    // UI Components
    private RecyclerView searchResultsRecyclerView;
    private SearchResultsAdapter searchResultsAdapter;
    private Spinner genreSpinner;
    private EditText searchBar;

    // Tracks the currently selected genre ID for filtering (-1 means no filter)
    private int selectedGenre = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // Set activity title
        setTitle("Search");

        // Initialize UI components
        EditText searchBar = findViewById(R.id.searchBar);
        Button btnFilter = findViewById(R.id.btnFilter);
        Button btnSearch = findViewById(R.id.btnSearch);
        searchResultsRecyclerView = findViewById(R.id.searchResultsRecyclerView);
        
        // Set up RecyclerView with vertical scrolling layout
        searchResultsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Set up button click listeners
        btnFilter.setOnClickListener(v -> showGenreFilterDialog());

        // Search button click handler
        btnSearch.setOnClickListener(v -> {
            String query = searchBar.getText().toString();
            if (selectedGenre == -1) {
                // Search without genre filter
                new FetchSearchResultsTask().execute(query, "");
            } else {
                // Search with genre filter
                new FetchSearchResultsTask().execute(query, String.valueOf(selectedGenre));
            }
        });
    }

    /**
     * Shows a dialog for selecting game genres to filter search results.
     * Uses an AlertDialog with a single choice list of genres.
     */
    private void showGenreFilterDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose a genre");

        // Define available genres and their corresponding RAWG API IDs
        String[] genres = {"Action", "Adventure", "RPG", "Simulation", "Puzzle", "Arcade", "Platformer", "Racing", "Sports"};
        int[] genreIds = {4, 3, 5, 14, 7, 11, 83, 1, 15};

        // Set up single choice selection
        builder.setSingleChoiceItems(genres, selectedGenre, (dialog, which) -> selectedGenre = genreIds[which]);

        // Add dialog buttons
        builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * AsyncTask to fetch search results from the RAWG API in the background.
     * Prevents blocking the UI thread during network operations.
     */
    private class FetchSearchResultsTask extends AsyncTask<String, Void, ArrayList<NewGame>> {
        @Override
        protected ArrayList<NewGame> doInBackground(String... params) {
            String query = params[0];
            String genreId = params.length > 1 ? params[1] : "";
            ArrayList<NewGame> searchResults = new ArrayList<>();

            try {
                // Build API URL with search parameters and optional genre filter
                String urlString = "https://api.rawg.io/api/games?key=" + BuildConfig.RAWG_API_KEY + "&search=" + query;
                if (!genreId.isEmpty()) {
                    urlString += "&genres=" + genreId;
                }

                // Set up and execute HTTP request
                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                // Read response
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder builder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }

                // Parse JSON response
                JSONObject response = new JSONObject(builder.toString());
                JSONArray gamesArray = response.getJSONArray("results");

                // Convert JSON data to NewGame objects
                for (int i = 0; i < gamesArray.length(); i++) {
                    JSONObject game = gamesArray.getJSONObject(i);
                    int gameId = game.getInt("id");
                    String name = game.getString("name");
                    String imageUrl = game.optString("background_image", "");
                    String description = game.optString("description", "Description not available.");
                    searchResults.add(new NewGame(gameId, name, imageUrl, description));
                }
            } catch (Exception e) {
                Log.e("FetchSearchResultsTask", "Error fetching search results", e);
            }

            return searchResults;
        }

        @Override
        protected void onPostExecute(ArrayList<NewGame> searchResults) {
            super.onPostExecute(searchResults);

            if (searchResults.isEmpty()) {
                // Show message if no results found
                Toast.makeText(SearchActivity.this, "No results found", Toast.LENGTH_LONG).show();
            } else {
                // Initialize or update RecyclerView adapter with search results
                if (searchResultsAdapter == null) {
                    searchResultsAdapter = new SearchResultsAdapter(SearchActivity.this, searchResults, new ArrayList<>());
                    searchResultsRecyclerView.setAdapter(searchResultsAdapter);
                } else {
                    searchResultsAdapter.updateSearchResultsAndLikedGames(searchResults, new ArrayList<>());
                }
            }
        }
    }
}