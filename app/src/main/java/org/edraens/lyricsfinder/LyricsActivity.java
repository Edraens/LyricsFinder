package org.edraens.lyricsfinder;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class LyricsActivity extends AppCompatActivity {

    private String[] song = new String[2];
    private TextView textLyrics;
    private ProgressBar progress;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lyrics);

//        Récupération des views
        textLyrics = findViewById(R.id.lyrics_viewLyrics);
        progress = findViewById(R.id.lyrics_progress);

//        Ajout du bouton "back" dans l'ActionBar :
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setHomeButtonEnabled(true);

//        Récupération de l'intent appelé dans MainActivity, de l'artiste et du titre :
        Intent mainIntent = getIntent();
        String artist = mainIntent.getStringExtra("artist");
        String title = mainIntent.getStringExtra("title");
        song[0] = artist;
        song[1] = title;

//        Affichage de l'artiste et du titre dans l'activité :
        setTitle(title + " - " + artist);

//        Modification de la taille des paroles en fonction des paramètres :
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String size = prefs.getString("fontSize", "14");
        textLyrics.setTextSize(TypedValue.COMPLEX_UNIT_SP, Float.parseFloat(size));

//        Récupération des lyrics auprès de l'API lyrics.ovh via une tâche asynchrone :
        Request req = new Request();
        req.execute(song);

    }

    @SuppressWarnings("Duplicates")
    private void appendHistory() {
        Toast.makeText(this, "adding to hist", Toast.LENGTH_SHORT).show();
        JSONArray history_artists;
        try {
            String raw = prefs.getString("history_artists", "");
            if (raw.equals("")) history_artists = new JSONArray();
            else history_artists = new JSONArray(raw);
                history_artists.put(song[0]);
                prefs.edit().putString("history_artists", history_artists.toString()).apply();
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("History error", "Cannot parse history");
        }
        JSONArray history_titles;
        try {
            String raw = prefs.getString("history_titles", "");
            if (raw.equals("")) history_titles = new JSONArray();
            else history_titles = new JSONArray(raw);
            history_titles.put(song[1]);
            prefs.edit().putString("history_titles", history_titles.toString()).apply();
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("History error", "Cannot parse history");
        }
    }

    @SuppressWarnings("Duplicates")
    private class Request extends AsyncTask<String, Void, String> {
        protected String doInBackground(String... song) {
            return request(song[0], song[1]);
        }

        String request(String artist, String title) {
            StringBuilder response = new StringBuilder();

            try {
                HttpURLConnection connection;
                URL url = new
                        URL("https://api.lyrics.ovh/v1/" + URLEncoder.encode(artist, "UTF-8") + "/" + URLEncoder.encode(title, "UTF-8"));
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                InputStream inputStream = connection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String ligne = bufferedReader.readLine();
                while (ligne != null) {
                    response.append(ligne);
                    ligne = bufferedReader.readLine();
                }
                JSONObject lyricsJSON = new JSONObject(response.toString());
                response = new StringBuilder(getLyricsFromJSON(lyricsJSON));
            } catch (FileNotFoundException e) {
                response = new StringBuilder("404");
            } catch (IOException e) {
                response = new StringBuilder("noInternet");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return response.toString();
        }

        private String getLyricsFromJSON(JSONObject jso) throws Exception {
            String response;
            response = jso.getString("lyrics");
            return response;
        }

        protected void onPostExecute(String result) {
            if (result.equals("404")) {
                Toast.makeText(LyricsActivity.this, R.string.lyrics_404, Toast.LENGTH_SHORT).show();
                LyricsActivity.this.finish();
            } else if (result.equals("noInternet")) {
                Toast.makeText(LyricsActivity.this, R.string.internet_issue, Toast.LENGTH_SHORT).show();
                LyricsActivity.this.finish();
            } else {
                progress.setVisibility(View.GONE);
                result = result.replaceAll("\\n\\n\\n+", "\n\n");
                textLyrics.setText(result);

//                Si l'historique est activé, on ajoute le titre :
                if (prefs.getBoolean("historyEnabled", true)) {
//                Si le titre est déjà la dernière entrée dans l'historique, on ajoute pas
                    String rawArtists = prefs.getString("history_artists", "");
                    String rawTitles = prefs.getString("history_titles", "");
                    if (rawArtists.equals("") || rawTitles.equals("")) {
                        appendHistory();
                    } else {
                        try {
//                        Récupération de l'historique
                            JSONArray history_artists = new JSONArray(rawArtists);
                            JSONArray history_titles = new JSONArray(rawTitles);
//                        Si le dernier titre de l'historique n'est pas celui qu'on voit actuellement, alors on l'ajoute. Sinon on fait rien.
                            if (!(history_artists.get(history_artists.length() - 1).equals(song[0]) && history_titles.get(history_titles.length() - 1).equals(song[1]))) {
                                appendHistory();
                            }
                        } catch (JSONException e) {
                            Log.e("JSON", "JSON parse error");
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

    }

}
