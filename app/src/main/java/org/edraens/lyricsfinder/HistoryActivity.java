package org.edraens.lyricsfinder;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.*;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

public class HistoryActivity extends AppCompatActivity {

    private ArrayList<String> listHistoryArrayArtists;
    private ArrayList<String> listHistoryArrayTitles;
    private boolean historyIsEmpty;

    @Override
    @SuppressWarnings("Duplicates")

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

//        Ajout du bouton "back" dans l'ActionBar :
        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        setTitle(R.string.history);

//        Récupération des views :
        TextView textEmptyHistory = findViewById(R.id.history_text_empty);
        ListView listHistory = findViewById(R.id.history_list_history);

//        Peuplement de l'array avec le JSON de l'historique ou affichage de "historique vide"
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (!prefs.contains("history_artists")) {
//            Si l'historique est vide on affiche un message le notifiant et on cache la ListView
            listHistory.setVisibility(View.GONE);
            textEmptyHistory.setVisibility(View.VISIBLE);
            historyIsEmpty = true;
        } else {
//            Récupération de l'historique des artistes à partir des SharedPreferences et peuplement d'un Array depuis le JSONArray récupéré
            try {
                JSONArray historyJSONArtists = new JSONArray(prefs.getString("history_artists", ""));
                listHistoryArrayArtists = new ArrayList<>();
                for (int i = 0; i < historyJSONArtists.length(); i++) {
                    listHistoryArrayArtists.add(historyJSONArtists.getString(i));
                }
            } catch (JSONException e) {
                Log.e("History error", "unable to parse json for artists ");
            }

//            Récupération de l'historique des titres (de même)
            try {
                JSONArray historyJSONTitles = new JSONArray(prefs.getString("history_titles", ""));
                listHistoryArrayTitles = new ArrayList<>();
                for (int i = 0; i < historyJSONTitles.length(); i++) {
                    listHistoryArrayTitles.add(historyJSONTitles.getString(i));
                }
            } catch (JSONException e) {
                Log.e("History error", "unable to parse json for titles");
                e.printStackTrace();
            }

//            Peuplement de la ListView avec les artistes+titres recherchés dans l'ordre inverse
            ArrayAdapter adapterHistory = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, listHistoryArrayArtists) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);
                    TextView text1 = view.findViewById(android.R.id.text1);

//                    Le texte affiché sera sous la forme "Artiste - Titre", et ce dans l'ordre décroissant des index des tableaux : le dernier titre recherché sera tout en haut de la liste affichée
                    text1.setText(listHistoryArrayArtists.get(listHistoryArrayArtists.size()-position-1) + " - " + listHistoryArrayTitles.get(listHistoryArrayTitles.size()-position-1));
                    return view;
                }
            };

//            Application de l'adapter créé plus haut à la ListView
            listHistory.setAdapter(adapterHistory);

//            Ajout d'un onClickListener pour rendre la ListView clickable
            listHistory.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                    Récupération de l'artiste et du titre associés à l'élément de l'historique (ne pas oublier que la ListView est affichée dans l'ordre chronologique inverse)
                    String artist = listHistoryArrayArtists.get(listHistoryArrayArtists.size()-position-1);
                    String title = listHistoryArrayTitles.get(listHistoryArrayTitles.size()-position-1);
//                    Création de l'intent et envoi vers LyricsActivity
                    Intent lyricsActivity = new Intent(HistoryActivity.this, LyricsActivity.class);
                    lyricsActivity.putExtra("artist", artist);
                    lyricsActivity.putExtra("title", title);
                    finish();
                    startActivity(lyricsActivity);
                }
            });

            historyIsEmpty = false;
        }
    }

//    Affichage du bouton de suppression si l'historique n'est pas vide
    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        MenuItem btnClearHistory = menu.findItem(R.id.menuhistory_clear);
        if (!historyIsEmpty) btnClearHistory.setVisible(true);
        return true;
    }

//    Création du menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_history, menu);
        return true;
    }

//    Gestion des clics sur un item du menu
    @SuppressWarnings("Duplicates")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuhistory_clear:
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                prefs.edit().remove("history_artists").apply();
                prefs.edit().remove("history_titles").apply();
                finish();
                Toast.makeText(this, R.string.cleared_history_notification, Toast.LENGTH_SHORT).show();
                return (true);
            case android.R.id.home:
                finish();
                return true;
        }
        return true;
    }

}
