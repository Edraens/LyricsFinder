package org.iutrt.lyricsfinder;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private AutoCompleteTextView inputArtist;
    private AutoCompleteTextView inputTitle;
    private Button btnSearch;
    private boolean artistHasText = false;
    private boolean titleHasText = false;

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

//        Création d'un textChangedListener pour les deux champs de texte afin de n'activer le bouton que si du texte a été entré (via la méthode changeButtonState) et de trigger l'autocompletion :
        inputArtist.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 3) {
                    RequestArtist req = new RequestArtist();
                    try {
                        List<String> artists = req.execute(inputArtist.getText().toString()).get(1500, TimeUnit.MILLISECONDS);
                        String[] artistsArray = new String[artists.size()];
                        artistsArray = artists.toArray(artistsArray);
                        inputArtist.setAdapter(new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, artistsArray));
                    } catch (Exception e) {
                        Log.v("Autocomplete", "Nothing found");
                    }
                }
                if (s.length() > 0) artistHasText = true;
                else artistHasText = false;
                changeButtonState();
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
                if (s.length() > 1 && artistHasText) {
                    RequestTitle req = new RequestTitle();
                    try {
                        String[] data = new String[]{inputTitle.getText().toString(), inputArtist.getText().toString()};
                        List<String> titles = req.execute(data).get(1500, TimeUnit.MILLISECONDS);
                        String[] titlesArray = new String[titles.size()];
                        titlesArray = titles.toArray(titlesArray);

                        inputTitle.setAdapter(new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, titlesArray));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (s.length() > 0) titleHasText = true;
                else titleHasText = false;
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
    private static class RequestArtist extends AsyncTask<String, Void, List<String>> {
        protected List<String> doInBackground(String... artist) {
            List<String> response = request(artist[0]);
            return response;
        }

        protected List<String> request(String artist) {
            List<String> response = new ArrayList<String>();

            try {
                HttpURLConnection connection = null;
                URL url = new
                        URL("http://musicbrainz.org/ws/2/artist/?fmt=json&query=" + URLEncoder.encode(artist, "UTF-8"));
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                InputStream inputStream = connection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String rawJson = "";
                String ligne = bufferedReader.readLine();
                while (ligne != null) {
                    rawJson += ligne;
                    ligne = bufferedReader.readLine();
                }
                JSONObject responseJSON = new JSONObject(rawJson);
//                JSONArray arrayArtistsJSON = new JSONArray(responseJSON.getJSONArray("artists"));
                JSONArray arrayArtistsJSON = (JSONArray) responseJSON.get("artists");
                response = getArtistsFromJSON(arrayArtistsJSON);
            } catch (Exception e) {
                response.add("");
            }
            return response;
        }

        private List<String> getArtistsFromJSON(JSONArray array) throws Exception {
            List<String> response = new ArrayList<String>();
            int limit;
            if (array.length() < 6) limit = array.length();
            else limit = 6;

            for (int i = 0; i < limit; i++) {
                String artist = array.getJSONObject(i).getString("name");
                if (!response.contains(artist)) {
                    response.add(artist);
                }
                else Log.i("AVOID DUPLICATE ARTIST", artist);
            }
            return response;
        }
    }

    @SuppressWarnings("Duplicates")
    private static class RequestTitle extends AsyncTask<String, Void, List<String>> {
        protected List<String> doInBackground(String... title) {
            List<String> response = request(title[0], title[1]);
            return response;
        }

        protected List<String> request(String title, String artist) {
            List<String> response = new ArrayList<String>();

            try {
                HttpURLConnection connection = null;
                URL url = new
                        URL("https://api.deezer.com/search?limit=6&q=artist:%22" + URLEncoder.encode(artist, "UTF-8") + "%22+%22" + URLEncoder.encode(title, "UTF-8") + "%22");
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                InputStream inputStream = connection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String rawJson = "";
                String ligne = bufferedReader.readLine();
                while (ligne != null) {
                    rawJson += ligne;
                    ligne = bufferedReader.readLine();
                }
                JSONObject responseJSON = new JSONObject(rawJson);
                JSONArray arrayTitleJSON = (JSONArray) responseJSON.get("data");
                response = getTitlesFromJSON(arrayTitleJSON);
            } catch (Exception e) {
                response.clear();
                response.add("");
            }
            return response;
        }

        private List<String> getTitlesFromJSON(JSONArray array) throws Exception {
            List<String> response = new ArrayList<String>();
            for (int i = 0; i < array.length(); i++) {
                String title = array.getJSONObject(i).getString("title_short");
                if (!response.contains(title)) {
                    response.add(title);
                }
                else Log.i("AVOID DUPLICATE TITLE", title);
            }
            return response;
        }
    }

    protected void btnClick(View v) {
//        Définition de l'intent vers LyricsActivity contenant l'artiste et le titre et démarrage de l'activité :
        Intent lyricsActivity = new Intent(this, LyricsActivity.class);
        lyricsActivity.putExtra("artist", inputArtist.getText().toString());
        lyricsActivity.putExtra("title", inputTitle.getText().toString());
        startActivity(lyricsActivity);
    }
}
