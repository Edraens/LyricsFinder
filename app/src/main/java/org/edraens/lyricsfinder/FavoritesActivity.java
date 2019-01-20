package org.edraens.lyricsfinder;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

public class FavoritesActivity extends AppCompatActivity {

    private ArrayList<String> favArrayArtists;
    private ArrayList<String> favArrayTitles;
    private ArrayList<String> favArrayLyrics;

    @Override
    @SuppressWarnings("Duplicates")

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

//        Ajout du bouton "back" dans l'ActionBar :
        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        setTitle(R.string.favorites);

//        Récupération des views :
        TextView textEmptyFavs = findViewById(R.id.favorites_text_empty);
        ListView listFavs = findViewById(R.id.favorites_list);

//        Peuplement de l'array avec le JSON de l'historique ou affichage de "historique vide"
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (prefs.getString("fav_artists", "[]").equals("[]")) {
            listFavs.setVisibility(View.GONE);
            textEmptyFavs.setVisibility(View.VISIBLE);
        } else {

//            Récupération des favoris : artistes
            try {
                JSONArray favJSONArtists = new JSONArray(prefs.getString("fav_artists", ""));
                favArrayArtists = new ArrayList<>();
                for (int i = 0; i < favJSONArtists.length(); i++) {
                    favArrayArtists.add(favJSONArtists.getString(i));
                }
            } catch (JSONException e) {
                Log.e("History error", "unable to parse json for artists ");
            }

//            Récupération des favoris : titres
            try {
                JSONArray favJSONTitles = new JSONArray(prefs.getString("fav_titles", ""));
                favArrayTitles = new ArrayList<>();
                for (int i = 0; i < favJSONTitles.length(); i++) {
                    favArrayTitles.add(favJSONTitles.getString(i));
                }
            } catch (JSONException e) {
                Log.e("History error", "unable to parse json for artists ");
            }

//            Récupération des favoris : paroles
            try {
                JSONArray favJSONLyrics = new JSONArray(prefs.getString("fav_lyrics", ""));
                favArrayLyrics = new ArrayList<>();
                for (int i = 0; i < favJSONLyrics.length(); i++) {
                    favArrayLyrics.add(favJSONLyrics.getString(i));
                }
            } catch (JSONException e) {
                Log.e("History error", "unable to parse json for artists ");
            }

//            Peuplement de la ListView avec les artistes+titres recherchés dans l'ordre inverse
            ArrayAdapter adapterHistory = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, favArrayArtists) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);
                    TextView text1 = view.findViewById(android.R.id.text1);

                    text1.setText(favArrayArtists.get(position) + " - " + favArrayTitles.get(position));
                    return view;
                }
            };

            listFavs.setAdapter(adapterHistory);

//            Ajout d'un onClickListener pour rendre la ListView clickable
            listFavs.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                    Récupération de l'artiste et du titre associés à l'élément de l'historique
                    String artist = favArrayArtists.get(position);
                    String title = favArrayTitles.get(position);
                    String lyrics = favArrayLyrics.get(position);

//                    Création de l'intent et envoi vers LyricsActivity
                    Intent lyricsActivity = new Intent(FavoritesActivity.this, LyricsActivity.class);
                    lyricsActivity.putExtra("artist", artist);
                    lyricsActivity.putExtra("title", title);
                    lyricsActivity.putExtra("lyrics", lyrics);
                    finish();
                    startActivity(lyricsActivity);
                }
            });
        }
    }
}
