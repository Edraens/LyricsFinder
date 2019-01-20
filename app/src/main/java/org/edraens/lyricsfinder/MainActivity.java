package org.edraens.lyricsfinder;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private AutoCompleteTextView inputArtist;
    private AutoCompleteTextView inputTitle;
    private Button btnSearch;
    private boolean artistHasText = false;
    private boolean titleHasText = false;
    private ArrayAdapter<String> artistSuggestionsAdapter;
    private ArrayAdapter<String> titleSuggestionsAdapter;
    private ArrayList<String> artistSuggestionsArray = new ArrayList<>();
    private ArrayList<String> titleSuggestionsArray = new ArrayList<>();

    @SuppressWarnings("Duplicates")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        Récupération des views :
        inputArtist = findViewById(R.id.main_inputArtist);
        inputTitle = findViewById(R.id.main_inputTitle);
        btnSearch = findViewById(R.id.main_btnSearch);

//        Désactivation du bouton de recherche et mise en place du focus sur le champ Artiste :
        btnSearch.setEnabled(false);
        inputArtist.requestFocus();

//        Création des ArrayAdapter pour les suggestions :
        artistSuggestionsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, artistSuggestionsArray);
        inputArtist.setAdapter(artistSuggestionsAdapter);

        titleSuggestionsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, titleSuggestionsArray);
        inputTitle.setAdapter(titleSuggestionsAdapter);

//        Modification de la sensibilité des AutoCompleteTextView :
        inputArtist.setThreshold(0);
        inputTitle.setThreshold(0);

//        Création d'un textChangedListener pour les deux champs de texte afin de n'activer le bouton que si du texte a été entré (via la méthode changeButtonState) et de trigger l'autocompletion :
        inputArtist.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 2) {
                    RequestArtist req = new RequestArtist();
                    req.execute(inputArtist.getText().toString());
                }
                artistHasText = s.length() > 0;
                changeButtonState();
                titleSuggestionsAdapter.clear();
                titleSuggestionsAdapter.notifyDataSetChanged();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        inputTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() >= 1 && artistHasText) {
                    RequestTitle req = new RequestTitle();
                    req.execute(inputTitle.getText().toString(), inputArtist.getText().toString());
                }
                titleHasText = s.length() > 0;
                changeButtonState();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void changeButtonState() {
//        Vérifie que les deux champs ont du texte et active/désactive le bouton en fonction :
        if (artistHasText && titleHasText) btnSearch.setEnabled(true);
        else btnSearch.setEnabled(false);
    }

    @SuppressWarnings("Duplicates")
    private class RequestArtist extends AsyncTask<String, Void, List<String>> {
        protected List<String> doInBackground(String... artist) {
            return request(artist[0]);
        }

        List<String> request(String artist) {
            List<String> response = new ArrayList<>();

            try {
                HttpURLConnection connection;
                URL url = new
                        URL("http://musicbrainz.org/ws/2/artist/?fmt=json&query=" + URLEncoder.encode(artist, "UTF-8"));
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                InputStream inputStream = connection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                StringBuilder rawJson = new StringBuilder();
                String ligne = bufferedReader.readLine();
                while (ligne != null) {
                    rawJson.append(ligne);
                    ligne = bufferedReader.readLine();
                }
                JSONObject responseJSON = new JSONObject(rawJson.toString());
//                JSONArray arrayArtistsJSON = new JSONArray(responseJSON.getJSONArray("artists"));
                JSONArray arrayArtistsJSON = (JSONArray) responseJSON.get("artists");
                response = getArtistsFromJSON(arrayArtistsJSON);
            } catch (Exception e) {
                response.add("");
            }
            return response;
        }

        private List<String> getArtistsFromJSON(JSONArray array) throws Exception {
            List<String> response = new ArrayList<>();
            int limit;
            if (array.length() < 12) limit = array.length();
            else limit = 12;

            for (int i = 0; i < limit; i++) {
                String artist = array.getJSONObject(i).getString("name");
                if (!response.contains(artist)) {
                    response.add(artist);
                }
            }
            return response;
        }

        protected void onPostExecute(List<String> result) {
            artistSuggestionsAdapter.clear();
            artistSuggestionsAdapter.addAll(result);
            artistSuggestionsAdapter.notifyDataSetChanged();
        }
    }

    @SuppressWarnings("Duplicates")
    private class RequestTitle extends AsyncTask<String, Void, List<String>> {
        protected List<String> doInBackground(String... title) {
            return request(title[0], title[1]);
        }

        List<String> request(String title, String artist) {
            List<String> response = new ArrayList<>();

            try {
                HttpURLConnection connection;
                URL url = new
                        URL("https://api.deezer.com/search?limit=12&q=artist:%22" + URLEncoder.encode(artist, "UTF-8") + "%22+%22" + URLEncoder.encode(title, "UTF-8") + "%22");
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                InputStream inputStream = connection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                StringBuilder rawJson = new StringBuilder();
                String ligne = bufferedReader.readLine();
                while (ligne != null) {
                    rawJson.append(ligne);
                    ligne = bufferedReader.readLine();
                }
                JSONObject responseJSON = new JSONObject(rawJson.toString());
                JSONArray arrayTitleJSON = (JSONArray) responseJSON.get("data");
                response = getTitlesFromJSON(arrayTitleJSON);
            } catch (Exception e) {
                response.clear();
                response.add("");
            }
            return response;
        }

        private List<String> getTitlesFromJSON(JSONArray array) throws Exception {
            List<String> response = new ArrayList<>();
            for (int i = 0; i < array.length(); i++) {
                String title = array.getJSONObject(i).getString("title_short");
                if (!response.contains(title)) {
                    response.add(title);
                }
            }
            return response;
        }

        protected void onPostExecute(List<String> result) {
            titleSuggestionsAdapter.clear();
            titleSuggestionsAdapter.addAll(result);
            titleSuggestionsAdapter.notifyDataSetChanged();

        }

    }

    protected void btnClick(View v) {
//        Définition de l'intent vers LyricsActivity contenant l'artiste et le titre et démarrage de l'activité :
        Intent lyricsActivity = new Intent(this, LyricsActivity.class);
        lyricsActivity.putExtra("artist", inputArtist.getText().toString());
        lyricsActivity.putExtra("title", inputTitle.getText().toString());
        startActivity(lyricsActivity);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_history:
                Intent historyActivity = new Intent(this, HistoryActivity.class);
                startActivity(historyActivity);
                return (true);
            case R.id.menu_favorites:
                Intent favoritesActivity = new Intent(this, FavoritesActivity.class);
                startActivity(favoritesActivity);
                return (true);
            case R.id.menu_settings:
                Intent settingsActivity = new Intent(this, SettingsActivity.class);
                startActivity(settingsActivity);
                return (true);
        }
        return true;
    }
}
