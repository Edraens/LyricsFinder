package org.edraens.lyricsfinder;

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

public class HistoryActivity extends AppCompatActivity {

    private ArrayList<String> listHistoryArrayArtists;
    private ArrayList<String> listHistoryArrayTitles;

    @Override
    @SuppressWarnings("Duplicates")

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

//        Ajout du bouton "back" dans l'ActionBar :
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setHomeButtonEnabled(true);
        setTitle(R.string.history);

//        Récupération des views :
        TextView textEmptyHistory = findViewById(R.id.history_text_empty);
        ListView listHistory = findViewById(R.id.history_list_history);

//        Peuplement de l'array avec le JSON de l'historique ou affichage de "historique vide"
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (!prefs.contains("history_artists")) {
            listHistory.setVisibility(View.GONE);
            textEmptyHistory.setVisibility(View.VISIBLE);
        } else {
//            Récupération de l'historique des artistes
            try {
                JSONArray historyJSONArtists = new JSONArray(prefs.getString("history_artists", ""));
                listHistoryArrayArtists = new ArrayList<>();
                for (int i = 0; i < historyJSONArtists.length(); i++) {
                    listHistoryArrayArtists.add(historyJSONArtists.getString(i));
                }
            } catch (JSONException e) {
                Log.e("History error", "unable to parse json for artists ");
            }

//            Récupération de l'historique des titres
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

                    text1.setText(listHistoryArrayArtists.get(listHistoryArrayArtists.size()-position-1) + " - " + listHistoryArrayTitles.get(listHistoryArrayTitles.size()-position-1));
//                text2.setText(listHistoryArray.get(position)[1]);
                    return view;
                }
            };

            listHistory.setAdapter(adapterHistory);
        }
    }
}
